## AI 集成的"双轨策略"
> 如需体验 Spring AI 2.0，请访问 https://github.com/javahongxi/spring-cloud-samples

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

#### 6. RAG (检索增强生成)

结合向量数据库实现知识库问答。

```java
@RestController
@RequestMapping("/ai/rag")
public class RagController {
    private final VectorStore vectorStore;

    @PostMapping("/document")
    public String addDocument(@RequestParam String content) {
        vectorStore.add(List.of(new Document(content)));
        return "文档添加成功";
    }

    @GetMapping("/ask")
    public String askQuestion(@RequestParam String message) {
        List<Document> docs = vectorStore.similaritySearch(message);
        String context = docs.stream().map(Document::getText).collect(Collectors.joining("\n"));
        return chatClient.prompt()
                .system("基于以下上下文回答问题：\n" + context)
                .user(message).call().content();
    }
}
```

支持 **内存向量存储**（开发）和 **Redis 向量存储**（生产）。

```bash
curl -X POST "http://localhost:8888/ai/rag/document" -d "content=Nacos 是动态服务发现平台"
curl "http://localhost:8888/ai/rag/ask?message=国内流行的 RPC 框架"
```

---

#### 7. 多模态视觉理解

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

#### 8. MCP Server

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

#### 9. AI 缓存

使用缓存优化 AI 响应性能。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/ai/cache/chat` | 带缓存的聊天 |
| GET | `/ai/cache/benchmark` | 缓存性能对比测试 |
| DELETE | `/ai/cache/clear-all` | 清除所有缓存 |

---

#### 10. DeepSeek 模型集成

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

#### 项目结构

```
whatsmars-ai-spring/
├── config/          # AiConfig, McpServerConfig
├── controller/      # AiChatController, AdvancedChatController, DeepSeekController, 
│                    # StructuredOutputController, ToolCallingController, ReactAgentController,
│                    # RagController, RedisRagController, VisionController, CacheController
├── service/         # ToolCallingService, VisionService
├── tool/            # WeatherTools, TimeTools, SearchTools, CalculatorTools, ...
└── cache/           # CachedChatService, AiCacheConfig
```

**接口规范：** 所有接口统一返回 `String`，参数统一使用 `message`。

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

#### 项目结构

```
whatsmars-ai-langchain4j/
├── SimpleAssistant.java             # 基础聊天接口
├── SimpleController.java            # 基础聊天控制器
├── StreamingAssistant.java          # 流式响应接口
├── StreamingController.java         # 流式控制器
├── FunctionCallingAssistant.java    # 工具调用接口
├── FunctionCallingController.java   # 工具调用控制器
└── ToolService.java                 # 工具定义
```
