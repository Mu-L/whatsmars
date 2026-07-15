## AI 集成的"三轨策略"
> 如需体验 Spring AI 2.0，请访问 https://github.com/javahongxi/spring-cloud-samples

### 模块概览

| 模块 | 框架 | 说明 |
|------|------|------|
| whatsmars-ai-spring | Spring AI 1.1.x | Spring AI 核心功能演示（Chat/Tool/RAG/MCP/Memory 等） |
| whatsmars-ai-alibaba | Spring AI Alibaba 1.x | Spring AI Alibaba Agent Graph 框架演示（ReactAgent） |
| whatsmars-ai-langchain4j | LangChain4j | LangChain4j 对比实验 |

### Spring AI 核心功能

> 本项目基于 Spring AI 1.1.x，支持多模型提供商：阿里通义千问（Qwen）+ DeepSeek。

#### 配置示例 (application.yml)

```yaml
server:
  port: 8888
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://dashscope.aliyuncs.com/compatible-mode
      chat:
        options:
          model: qwen-plus
          temperature: 0.7
    deepseek:
      api-key: ${DEEPSEEK_API_KEY}
      base-url: https://api.deepseek.com
      chat:
        options:
          model: deepseek-v4-pro
    mcp:
      server:
        name: whatsmars-mcp-server
        version: 1.0.0
        type: SYNC
```

#### 1. 基础聊天

```java
@RestController
public class AiChatController {
    private final ChatClient chatClient;

    public AiChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/ai/chat")
    public String chat(@RequestParam String message) {
        return chatClient.prompt().user(message).call().content();
    }

    @GetMapping("/ai/chat/stream")
    public ResponseEntity<Flux<String>> streamChat(@RequestParam String message) {
        Flux<String> stream = chatClient.prompt().user(message).stream().content();
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/event-stream;charset=UTF-8"))
                .body(stream);
    }
}
```

```bash
curl "http://localhost:8888/ai/chat?message=你好"
curl "http://localhost:8888/ai/chat/stream?message=介绍一下武汉"
```

---

#### 2. 高级对话特性

```java
// System Message + Temperature 控制
@PostMapping("/system-message")
public String chatWithSystemMessage(@RequestParam String message) {
    return chatClient.prompt()
        .system("你是一个资深的 Java 架构师。")
        .options(OpenAiChatOptions.builder().temperature(0.4).build())
        .user(message).call().content();
}

// 创意性对话（高温度）
@PostMapping("/creative")
public String creativeChat(@RequestParam String message) {
    return chatClient.prompt()
        .system("你是一个富有创造力的作家。")
        .options(OpenAiChatOptions.builder().temperature(0.9).build())
        .user(message).call().content();
}
```

```bash
curl "http://localhost:8888/ai/advanced/system-message?message=如何设计秒杀系统？"
curl "http://localhost:8888/ai/advanced/creative?message=写一首春天的诗"
```

---

#### 3. 结构化输出

```java
@PostMapping("/extract-user")
public String extractUserInfo(@RequestParam String text) {
    return chatClient.prompt()
            .system("""
                    你是一个信息提取助手。请从用户描述中提取个人信息，以 JSON 格式返回。
                    要求：name(姓名)、age(年龄)、email(邮箱)、hobbies(爱好数组)、occupation(职业)
                    """)
            .user(text).call().content();
}
```

```bash
curl -X POST "http://localhost:8888/ai/structured/extract-user" \
  -d "text=我叫张三，今年25岁，软件工程师，喜欢编程和篮球"
```

---

#### 4. 工具调用 (Tool Calling)

通过 `@Tool` 注解让 AI 自动调用 Java 方法。

```java
@Component
public class WeatherTools {
    @Tool(description = "获取指定城市的当前天气信息")
    public String getWeather(@ToolParam(description = "城市名称") String city) {
        return city + " 晴天，25°C";
    }
}

@Service
public class ToolCallingService {
    public String getWeather(String message) {
        return chatClient.prompt().user(message).tools(weatherTools).call().content();
    }
}
```

**可用工具类：** `WeatherTools`（天气）、`TimeTools`（时间）、`SearchTools`（搜索）、`CalculatorTools`（计算）等

```bash
curl "http://localhost:8888/ai/tool/weather?message=北京今天天气怎么样？"
```

---

#### 5. ReAct Agent

Agent 自主决定调用哪些工具，支持多步推理。

```java
@GetMapping("/chat")
public String agentChat(@RequestParam String message) {
    return chatClient.prompt()
            .system("你是一个智能助手，可以使用工具帮助用户解决问题。")
            .user(message)
            .tools(weatherTools, searchTools, timeTools, calculatorTools)
            .call().content();
}
```

```bash
curl "http://localhost:8888/ai/react-agent/chat?message=北京天气怎么样？适合出门吗？"
```

---

#### 6. 多模态视觉理解

支持图片分析、OCR、图表解读。

```java
@Bean
public ChatClient visionChatClient(ChatClient.Builder builder,
                                   @Value("${spring.ai.vision.model:qwen3.7-plus}") String visionModel) {
    return builder.defaultOptions(OpenAiChatOptions.builder().model(visionModel).build()).build();
}

@PostMapping("/analyze-url")
public String analyzeImageByUrl(@RequestParam String imageUrl) {
    Resource imageResource = new UrlResource(imageUrl);
    return visionChatClient.prompt()
            .user(u -> u.text("描述这张图片").media(MediaType.IMAGE_JPEG, imageResource))
            .call().content();
}
```

```bash
curl -X POST "http://localhost:8888/ai/vision/analyze-url" \
  -d "imageUrl=https://example.com/image.jpg"
```

---

#### 7. MCP Server

通过 MCP 协议将工具暴露给外部 Client。

```java
@Configuration
public class McpServerConfig {
    @Bean
    public ToolCallbackProvider mcpToolProvider(WeatherTools weatherTools, TimeTools timeTools, ...) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(weatherTools, timeTools, ...).build();
    }
}
```

工具类同时用于内部 Tool Calling 和 MCP 对外暴露，无需维护两份代码。

---

#### 8. AI 缓存

使用缓存优化 AI 响应性能。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/ai/cache/chat` | 带缓存的聊天 |
| GET | `/ai/cache/benchmark` | 缓存性能对比测试 |
| DELETE | `/ai/cache/clear-all` | 清除所有缓存 |

---

#### 9. DeepSeek 模型集成

多模型提供商共存，通过不同 `ChatClient` Bean 实现模型切换。

```java
@Bean
public ChatClient deepSeekChatClient(ChatClient.Builder builder, DeepSeekChatModel model) {
    return builder.defaultOptions(OpenAiChatOptions.builder().model(model.getDefaultOptions().getModel()).build()).build();
}

@RestController
@RequestMapping("/deepseek")
public class DeepSeekController {
    @RequestMapping("/chat")
    public String chat(@RequestParam String message) {
        return deepSeekChatClient.prompt().user(message).call().content();
    }

    @RequestMapping("/chat/stream")
    public ResponseEntity<Flux<String>> chatStream(@RequestParam String message) {
        return ResponseEntity.ok().contentType(MediaType.valueOf("text/event-stream"))
                .body(deepSeekChatClient.prompt().user(message).stream().content());
    }

    @RequestMapping("/agent/chat")
    public String agentChat(@RequestParam String message) {
        return deepSeekChatClient.prompt()
                .system("你是一个智能助手，可以使用工具。")
                .user(message).tools(weatherTools, timeTools, searchTools).call().content();
    }
}
```

```bash
curl "http://localhost:8888/deepseek/chat?message=你好"
curl "http://localhost:8888/deepseek/chat/stream?message=武汉简介"
curl "http://localhost:8888/deepseek/agent/chat?message=北京天气怎么样？"
```

---

#### 10. ChatMemory 多轮对话记忆

基于 `spring-ai-starter-model-chat-memory-repository-jdbc`，对话历史持久化到 PostgreSQL，支持会话隔离。需前置 PostgreSQL（同 RAG 模块）。

| 接口                                   | 说明       |
|--------------------------------------|----------|
| `POST /ai/memory/chat`               | 带记忆的多轮对话 |
| `DELETE /ai/memory/{conversationId}` | 清除会话记忆   |

```shell
# 第 1 轮：告诉 AI 你的名字
curl -X POST http://localhost:8888/ai/memory/chat \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"session-001","message":"你好，我叫小明"}'

# 第 2 轮：追问，AI 会记住上下文
curl -X POST http://localhost:8888/ai/memory/chat \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"session-001","message":"我叫什么名字？"}'

# 不同会话完全隔离
curl -X POST http://localhost:8888/ai/memory/chat \
  -H "Content-Type: application/json" \
  -d '{"conversationId":"session-002","message":"我叫什么名字？"}'

# 清除会话记忆
curl -X DELETE http://localhost:8888/ai/memory/session-001
```

---

#### 11. PromptTemplate 提示词模板

使用 Spring AI 的 `PromptTemplate` 进行 `{variable}` 占位符替换，演示三种模板场景。

| 接口                        | 说明          |
|---------------------------|-------------|
| `POST /ai/prompt/product` | 产品描述生成      |
| `POST /ai/prompt/code`    | 代码解释        |
| `POST /ai/prompt/custom`  | 自定义模板（通用入口） |

```shell
# 产品描述生成
curl -X POST http://localhost:8888/ai/prompt/product \
  -H "Content-Type: application/json" \
  -d '{"product":"Spring AI 实战手册","category":"技术书籍","tone":"专业且幽默"}'

# 代码解释
curl -X POST http://localhost:8888/ai/prompt/code \
  -H "Content-Type: application/json" \
  -d '{"code":"public record Point(int x, int y) {}","language":"Java","level":"初学者"}'

# 自定义模板
curl -X POST http://localhost:8888/ai/prompt/custom \
  -H "Content-Type: application/json" \
  -d '{"template":"请用{language}写一个{function}的示例代码","variables":{"language":"Python","function":"快速排序"}}'
```

---

#### 12. RAG（检索增强生成）

前置条件：PostgreSQL + pgvector
```shell
brew install postgresql
brew install pgvector
# 初始化数据库（创建用户、数据库、启用 pgvector 扩展、建表）
psql -U postgres -f whatsmars-ai/init_ai_demo.sql
```


| 接口                         | 说明            |
|----------------------------|---------------|
| `POST /ai/rag/ingest`      | 摄入文档到向量数据库    |
| `GET /ai/rag/query`        | 基于知识库的 RAG 问答 |
| `DELETE /ai/rag/documents` | 删除指定来源的文档     |

```shell
# 摄入文档
curl -X POST http://localhost:8888/ai/rag/ingest \
  -H "Content-Type: application/json" \
  -d '{"content":"Spring AI is a framework for building AI-native applications...","source":"spring-ai-docs"}'

# RAG 查询（topK 控制检索文档数量，默认 3）
curl --get --data-urlencode "question=What is Spring AI?" "http://localhost:8888/ai/rag/query?topK=3"

# 删除指定来源文档
curl -X DELETE "http://localhost:8888/ai/rag/documents?source=spring-ai-docs"
```

> 完整 RAG 流程：文档摄入 → TokenTextSplitter 自动分块 → PgVector 向量化存储 → 相似性检索 → 上下文增强 Prompt → LLM 生成。当知识库无相关文档时自动降级为纯 LLM 回答。

---

### Spring AI Alibaba Agent

> 基于 Spring AI Alibaba 1.x 的 Agent Graph 框架，演示如何构建有状态的 ReAct 智能体。

#### 核心特性

- **ReactAgent**：基于 Graph 有状态工作流的 ReAct 推理循环
- **MemorySaver**：内置记忆管理，支持多轮会话上下文保持
- **线程隔离**：通过 `threadId` 实现多会话完全隔离
- **工具集成**：复用 Spring AI `@Tool` 注解注册工具

#### Agent 配置

```java
@Configuration
public class AgentConfig {

    @Bean
    public ReactAgent chatbotReactAgent(ChatModel chatModel,
                                        WhatsMarsTools whatsMarsTools,
                                        MemorySaver memorySaver) {
        ToolCallbackProvider toolCallbackProvider = MethodToolCallbackProvider.builder()
                .toolObjects(whatsMarsTools)
                .build();
        ToolCallback[] tools = toolCallbackProvider.getToolCallbacks();
        return ReactAgent.builder()
                .name("WhatsMarsAgent")
                .model(chatModel)
                .instruction("You are a helpful assistant named WhatsMars AI.")
                .enableLogging(true)
                .saver(memorySaver)
                .tools(tools)
                .build();
    }
}
```

#### Agent 对话接口

```java
@RestController
@RequestMapping("/api/agent")
public class MyAgentController {

    private final ReactAgent reactAgent;

    // 简单对话
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return reactAgent.call(message).getText();
    }

    // 带线程的多轮对话（会话隔离）
    @GetMapping("/chat/thread")
    public String chatWithThread(@RequestParam String message,
                                 @RequestParam(defaultValue = "default") String threadId) {
        RunnableConfig config = RunnableConfig.builder().threadId(threadId).build();
        return reactAgent.call(message, config).getText();
    }
}
```

程序启动完成后会输出：

```
Application is ready!
Chat with your agent: http://localhost:8888/chatui/index.html
```

直接在浏览器打开上述地址即可进行测试。

> 与 Spring AI 原生 ReAct Agent 的区别：Spring AI Alibaba 的 ReactAgent 基于 Graph 有状态工作流，
> 支持条件路由、中断恢复、多智能体编排等高级特性，更适合构建复杂 Agent 应用。

---

### LangChain4j 功能

> 基于 LangChain4j + Spring Boot Starter，使用阿里通义千问。

#### 配置示例

```yaml
langchain4j:
  open-ai:
    chat-model:
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      api-key: ${OPENAI_API_KEY}
      model-name: qwen-plus
```

#### 1. 基础聊天 (@AiService)

```java
@AiService
public interface SimpleAssistant {
    @SystemMessage("你是一个专业的 Java 技术专家。")
    String chat(String userMessage);
}

@RestController
public class SimpleController {
    @Autowired
    private SimpleAssistant assistant;

    @GetMapping("/ai/chat")
    public String chat(@RequestParam String message) {
        return assistant.chat(message);
    }
}
```

#### 2. 流式响应 (TokenStream + SSE)

```java
@AiService
public interface StreamingAssistant {
    TokenStream chat(String userMessage);
}

@GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public SseEmitter streamChat(@RequestParam String message) {
    SseEmitter emitter = new SseEmitter(0L);
    executor.execute(() -> {
        assistant.chat(message)
                .onPartialResponse(token -> emitter.send(SseEmitter.event().data(token)))
                .onCompleteResponse(response -> emitter.complete())
                .onError(emitter::completeWithError)
                .start();
    });
    return emitter;
}
```

#### 3. 工具调用 (@Tool)

```java
@Service
public class ToolService {
    @Tool("获取当前日期")
    public String getCurrentDate() {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    @Tool("查询城市天气")
    public String getWeather(String city) {
        return city + " 晴天，25°C";
    }
}
```

```bash
curl "http://localhost:8888/ai/function/chat?message=现在几点了？"
curl "http://localhost:8888/ai/function/chat?message=北京天气怎么样？"
```

---
