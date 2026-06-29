## Spring AI 核心功能

> 本项目基于 Spring AI 1.1.x，使用阿里通义千问（Qwen）作为大语言模型。

### 1. 基础聊天 (ChatClient)

Spring AI 提供了统一的 `ChatClient` API，支持多种大语言模型（OpenAI、Qwen、GPT 等）。

**快速开始：**

```java
@RestController
public class AiChatController {

    private final ChatClient chatClient;

    public AiChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/ai/chat")
    public String chat(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
```

**配置示例 (application.yml)：**

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      chat:
        options:
          model: qwen-plus          # 纯文本模型
          temperature: 0.7          # 默认温度（0=确定性，1=创造力）
      embedding:
        options:
          model: text-embedding-v3
      vision:
        model: qwen3.7-plus         # 多模态视觉模型
    mcp:
      server:
        name: whatsmars-mcp-server
        version: 1.0.0
        type: SYNC
```

**测试：**
```bash
curl "http://localhost:8080/ai/chat?message=讲个笑话"
```

---

### 2. 流式响应 (Streaming)

使用 Reactor 的 `Flux` 实现流式输出，提升用户体验。

```java
@GetMapping("/ai/chat/stream")
public ResponseEntity<Flux<String>> streamChat(@RequestParam String message) {
    Flux<String> stream = chatClient.prompt()
            .user(message)
            .stream()
            .content();
    return ResponseEntity.ok()
            .contentType(MediaType.valueOf("text/event-stream;charset=UTF-8"))
            .header("Cache-Control", "no-cache")
            .body(stream);
}
```

**测试：**
```bash
curl "http://localhost:8080/ai/chat/stream?message=介绍一下武汉"
```

---

### 3. 高级对话特性

#### System Message 角色设定 + Temperature 控制

```java
@PostMapping("/system-message")
public String chatWithSystemMessage(@RequestParam String message) {
    return chatClient.prompt()
        .system("你是一个资深的 Java 架构师，擅长设计高并发、高可用的分布式系统。回答要专业、深入。")
        .options(OpenAiChatOptions.builder().temperature(0.4).build()) // 低温度=更准确
        .user(message)
        .call()
        .content();
}
```

#### 创意性对话（高温度）

```java
@PostMapping("/creative")
public String creativeChat(@RequestParam String message) {
    return chatClient.prompt()
        .system("你是一个富有创造力的作家，擅长写故事和诗歌。")
        .options(OpenAiChatOptions.builder().temperature(0.9).build()) // 高温度=更有创造力
        .user(message)
        .call()
        .content();
}
```

#### Few-shot Prompting（少样本提示）

```java
@PostMapping("/few-shot")
public String fewShotPrompting(@RequestParam String message) {
    return chatClient.prompt()
        .system("""
                你是一个代码翻译助手，请将用户的自然语言转换为 Java 代码。
                示例 1:
                用户: 创建一个字符串变量 name，值为 "Hello"
                AI: String name = "Hello";
                """)
        .user(message)
        .call()
        .content();
}
```

#### 多轮对话历史（服务端维护上下文）

```java
// 缓存最近的对话历史
private final List<UserMessage> userMessages = new ArrayList<>();
private final List<AssistantMessage> assistantMessages = new ArrayList<>();

@PostMapping("/conversation")
public String conversation(@RequestParam String message) {
    List<Message> messages = new ArrayList<>();
    messages.addAll(userMessages);
    messages.addAll(assistantMessages);

    String response = chatClient.prompt()
        .messages(messages)
        .user(message)
        .call()
        .content();

    // 超过 10 轮时清空，否则追加
    if (userMessages.size() > 10) {
        userMessages.remove(0);
        assistantMessages.remove(0);
    } else {
        userMessages.add(UserMessage.builder().text(message).build());
        assistantMessages.add(AssistantMessage.builder().content(response).build());
    }
    return response;
}
```

**测试：**
```bash
# System Message（专业回答）
curl -X POST "http://localhost:8080/ai/advanced/system-message?message=如何设计一个秒杀系统？"

# 创意性对话
curl -X POST "http://localhost:8080/ai/advanced/creative?message=写一首关于春天的诗"

# 多轮对话
curl -X POST "http://localhost:8080/ai/advanced/conversation?message=今天天气怎么样？"
```

---

### 4. 结构化输出 (Structured Output)

让 AI 返回格式化的 JSON 字符串。

#### 4.1 信息提取

```java
@PostMapping("/extract-user")
public String extractUserInfo(@RequestParam String text) {
    return chatClient.prompt()
            .system("""
                    你是一个信息提取助手。请从用户的描述中提取个人信息，并以 JSON 格式返回。
                    要求：
                    - name: 姓名（字符串）
                    - age: 年龄（整数）
                    - email: 邮箱（字符串）
                    - hobbies: 爱好（字符串数组）
                    - occupation: 职业（字符串）
                    """)
            .user(text)
            .call()
            .content();
}
```

#### 4.2 评论摘要

```java
@PostMapping("/review-summary")
public String reviewSummary(@RequestParam String review) {
    return chatClient.prompt()
            .system("""
                    你是一个产品评论分析助手。请分析用户评论并提取关键信息。
                    返回格式：
                    - sentiment: 情感倾向（positive/negative/neutral）
                    - rating: 评分（1-5分）
                    - pros: 优点列表
                    - cons: 缺点列表
                    - summary: 一句话总结
                    """)
            .user(review)
            .call()
            .content();
}
```

**测试：**
```bash
curl -X POST "http://localhost:8080/ai/structured/extract-user" \
  -d "text=我叫张三，今年25岁，是一名软件工程师，喜欢编程和打篮球，邮箱是zhangsan@example.com"
```

---

### 5. 工具调用 (Tool Calling)

通过 `@Tool` 注解让 AI 自动调用 Java 方法获取实时数据。

**架构分层：** Controller → Service → Tools

#### 5.1 定义工具类

```java
@Component
public class WeatherTools {

    @Tool(description = "获取指定城市的当前天气信息")
    public String getWeather(@ToolParam(description = "城市名称") String city) {
        return switch (city) {
            case "北京" -> "晴天，温度 25°C，空气质量良好";
            case "上海" -> "多云，温度 28°C，湿度 65%";
            default -> "未知城市";
        };
    }

    @Tool(description = "获取指定城市未来几天的天气预报")
    public String getWeatherForecast(@ToolParam(description = "城市名称") String city,
                                     @ToolParam(description = "预报天数，1-7") int days) {
        // 返回天气预报...
    }
}
```

**可用工具类列表：**

| 工具类 | 功能 | 示例方法 |
|--------|------|---------|
| `WeatherTools` | 天气查询与预报 | `getWeather`, `getWeatherForecast` |
| `TimeTools` | 时间/日期查询 | `getCurrentTime`, `getCurrentDate`, `daysUntil` |
| `SearchTools` | 技术知识搜索 | `searchByKeyword`, `searchByCategory` |
| `CalculatorTools` | 数学计算 | `add`, `subtract`, `multiply`, `divide` |
| `ConversionTools` | URL 编解码、Base64、字符串统计 | `urlEncode`, `base64Encode`, `stringLength` |
| `SystemTools` | 大小写转换、字符串反转 | `toUpperCase`, `toLowerCase`, `reverseString` |
| `UserTools` | 用户信息查询 | `getUserInfo` |

#### 5.2 Service 层封装

```java
@Service
public class ToolCallingService {

    private final ChatClient chatClient;
    private final WeatherTools weatherTools;
    private final TimeTools timeTools;
    private final UserTools userTools;

    public String getWeather(String message) {
        return chatClient.prompt()
                .user(message)
                .tools(weatherTools)
                .call()
                .content();
    }

    public String getTime(String message) {
        return chatClient.prompt()
                .user(message)
                .tools(timeTools)
                .call()
                .content();
    }

    public String smartAssistant(String message) {
        return chatClient.prompt()
                .system("你是一个智能助手，可以根据用户的问题自动调用合适的工具来获取信息。")
                .user(message)
                .tools(weatherTools, timeTools, userTools)
                .call()
                .content();
    }
}
```

#### 5.3 Controller 层

```java
@RestController
@RequestMapping("/ai/tool")
public class ToolCallingController {

    private final ToolCallingService toolCallingService;

    @GetMapping("/weather")
    public String getWeather(@RequestParam String message) {
        return toolCallingService.getWeather(message);
    }

    @GetMapping("/time")
    public String getTime(@RequestParam String message) {
        return toolCallingService.getTime(message);
    }

    @GetMapping("/ask")
    public String smartAssistant(@RequestParam String message) {
        return toolCallingService.smartAssistant(message);
    }
}
```

**测试：**
```bash
# 天气查询
curl "http://localhost:8080/ai/tool/weather?message=北京今天的天气怎么样？"

# 时间查询
curl "http://localhost:8080/ai/tool/time?message=现在几点了？"
curl "http://localhost:8080/ai/tool/time?message=距离国庆节还有多少天？"

# 智能助手（自动选择工具）
curl "http://localhost:8080/ai/tool/ask?message=帮我查一下上海的天气"
```

---

### 6. ReAct Agent

ReAct (Reasoning + Acting) Agent 模式，Agent 会根据任务自主决定调用哪些工具。

```java
@RestController
@RequestMapping("/ai/react-agent")
public class ReactAgentController {

    private final ChatClient chatClient;
    private final WeatherTools weatherTools;
    private final SearchTools searchTools;
    private final TimeTools timeTools;
    private final CalculatorTools calculatorTools;

    @GetMapping("/chat")
    public String agentChat(@RequestParam String message) {
        return chatClient.prompt()
                .system("""
                        你是一个智能助手，可以使用各种工具来帮助用户解决问题。
                        你可以使用的工具包括：天气查询、时间查询、知识搜索、数学计算。
                        回答要求：根据问题需要，主动调用合适的工具获取信息。
                        """)
                .user(message)
                .tools(weatherTools, searchTools, timeTools, calculatorTools)
                .call()
                .content();
    }
}
```

**测试：**
```bash
# 天气查询
curl "http://localhost:8080/ai/react-agent/chat?message=北京今天的天气怎么样？"

# 知识搜索
curl "http://localhost:8080/ai/react-agent/chat?message=什么是Apache%20Dubbo？"

# 时间计算
curl "http://localhost:8080/ai/react-agent/chat?message=现在几点了？距离国庆节还有多少天？"

# 数学计算
curl "http://localhost:8080/ai/react-agent/chat?message=299打8折再减50是多少？"

# 复杂任务
curl "http://localhost:8080/ai/react-agent/complex-task?message=我想去杭州旅游，帮我查天气和介绍著名景点"

# Agent 决策演示（一次展示多个场景）
curl "http://localhost:8080/ai/react-agent/demo"
```

---

### 7. RAG (检索增强生成)

结合向量数据库实现知识库问答，解决大模型知识时效性问题。

#### 7.1 内存向量存储（开发测试用）

```java
@RestController
@RequestMapping("/ai/rag")
public class RagController {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public RagController(ChatClient.Builder builder, EmbeddingModel embeddingModel) {
        this.chatClient = builder.build();
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        initializeKnowledgeBase();
    }

    @PostMapping("/document")
    public String addDocument(@RequestParam String content) {
        Document document = new Document(content);
        vectorStore.add(List.of(document));
        return "文档添加成功，长度: " + content.length();
    }

    @GetMapping("/ask")
    public String askQuestion(@RequestParam String message) {
        List<Document> relevantDocs = vectorStore.similaritySearch(message);
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        return chatClient.prompt()
                .system("基于以下上下文回答问题：\n" + context)
                .user(message)
                .call()
                .content();
    }
}
```

**测试：**
```bash
# 添加文档
curl -X POST "http://localhost:8080/ai/rag/document" \
  -d "content=Nacos 是一个易于构建 AI Agent 应用的动态服务发现、配置管理平台"

# 问答
curl "http://localhost:8080/ai/rag/ask?message=国内最流行的RPC框架是哪一款"
```

#### 7.2 Redis 向量存储（生产环境推荐）

需要先启动 Redis Stack：
```bash
docker run -d --name redis-stack -p 6379:6379 redis/redis-stack:latest
```

**配置 (application.yml)：**

```yaml
spring:
  ai:
    vectorstore:
      redis:
        index-name: spring-ai-vector-index
        dimensions: 1024
        initialize-schema: true
        distance-type: COSINE
  redis:
    host: localhost
    port: 6379
```

**注意：** Spring AI 使用 Redis 作为 VectorStore 时，必须使用 **Redis Stack**（包含 RediSearch 模块），普通 Redis 不支持向量搜索。

---

### 8. 多模态视觉理解 (Vision)

支持图片分析、OCR 文字识别、图表解读等功能。采用 Controller → Service 分层架构。

> 请将 `spring.ai.vision.model` 配置为支持多模态的模型，如 `qwen3.7-plus`

#### 8.1 多模态 ChatClient 配置

```java
@Configuration
public class AiConfig {

    /**
     * 多模态视觉 ChatClient
     * 使用支持视觉识别的模型，VisionService 直接注入即可
     */
    @Bean
    public ChatClient visionChatClient(ChatClient.Builder builder,
                                       @Value("${spring.ai.vision.model:qwen3.7-plus}") String visionModel) {
        return builder
                .defaultOptions(OpenAiChatOptions.builder().model(visionModel).build())
                .build();
    }
}
```

#### 8.2 VisionService（业务逻辑层）

```java
@Service
public class VisionService {

    private final ChatClient visionChatClient;

    public String analyzeImageByUrl(String imageUrl, String prompt) {
        Resource imageResource = new UrlResource(imageUrl);
        return visionChatClient.prompt()
                .user(userSpec -> userSpec
                        .text(prompt)
                        .media(MediaType.IMAGE_JPEG, imageResource))
                .call()
                .content();
    }

    public String analyzeUploadedImage(MultipartFile file, String prompt) {
        Path tempFile = Files.createTempFile("upload-", "-" + file.getOriginalFilename());
        file.transferTo(tempFile);
        Resource imageResource = new UrlResource(tempFile.toUri());
        String description = visionChatClient.prompt()
                .user(userSpec -> userSpec
                        .text(prompt)
                        .media(MediaType.IMAGE_JPEG, imageResource))
                .call()
                .content();
        Files.deleteIfExists(tempFile);
        return description;
    }

    public String ocrTextRecognition(String imageUrl) { /* ... */ }
    public String analyzeChart(String imageUrl) { /* ... */ }
    public String codeFromImage(String imageUrl) { /* ... */ }
    public String compareImages(String imageUrl1, String imageUrl2) { /* ... */ }
}
```

#### 8.3 VisionController（接口层）

```java
@RestController
@RequestMapping("/ai/vision")
public class VisionController {

    private final VisionService visionService;

    @PostMapping("/analyze-url")
    public String analyzeImageByUrl(@RequestParam String imageUrl,
                                    @RequestParam(defaultValue = "请详细描述这张图片的内容") String prompt) {
        return visionService.analyzeImageByUrl(imageUrl, prompt);
    }

    @PostMapping("/analyze-upload")
    public String analyzeUploadedFile(@RequestParam("file") MultipartFile file,
                                      @RequestParam(defaultValue = "请详细描述这张图片的内容") String prompt) {
        return visionService.analyzeUploadedImage(file, prompt);
    }

    @PostMapping("/ocr")
    public String ocrTextRecognition(@RequestParam String imageUrl) { /* ... */ }

    @PostMapping("/chart-analysis")
    public String analyzeChart(@RequestParam String imageUrl) { /* ... */ }

    @PostMapping("/code-from-image")
    public String codeFromImage(@RequestParam String imageUrl) { /* ... */ }

    @PostMapping("/compare")
    public String compareImages(@RequestParam String imageUrl1,
                                @RequestParam String imageUrl2) { /* ... */ }
}
```

**测试：**
```bash
# 1/6 URL 图片分析（澎湃新闻：神舟十号海报）
curl -X POST "http://localhost:8080/ai/vision/analyze-url" \
  -d "imageUrl=https://imagecloud.thepaper.cn/thepaper/image/333/857/150.jpg"

# 2/6 图片上传分析（项目根目录下的架构图）
curl -X POST "http://localhost:8080/ai/vision/analyze-upload" \
  -F "file=@arch.png"

# 3/6 OCR 文字识别（澎湃新闻：北京申奥成功）
curl -X POST "http://localhost:8080/ai/vision/ocr" \
  -d "imageUrl=https://imagecloud.thepaper.cn/thepaper/image/333/857/151.jpg"

# 4/6 图表分析（今日头条：武汉市历年生产总值）
curl -X POST "http://localhost:8080/ai/vision/chart-analysis" \
  -d "imageUrl=https://p3-search.byteimg.com/obj/pgc-image/94e63ee2f0f840b0813e3746d2a9590b"

# 5/6 代码截图转代码（今日头条：Java代码图片）
curl -X POST "http://localhost:8080/ai/vision/code-from-image" \
  -d "imageUrl=https://p3-search.byteimg.com/obj/labis/624fb344cca59ed91d6ada99b45f41ca"

# 6/6 多图片对比分析（今日头条：鞠婧祎 vs 陈都灵）
curl -X POST "http://localhost:8080/ai/vision/compare" \
  -d "imageUrl1=https://p3-search.byteimg.com/obj/labis/9c78113c22823e91536fb63f8f599e13" \
  -d "imageUrl2=https://p3-search.byteimg.com/obj/labis/a7dd04c539c4515b6018e9a39a32be36"
```

---

### 9. MCP Server

通过 MCP（Model Context Protocol）协议将工具暴露给外部 MCP Client 调用。

#### 9.1 添加依赖

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-starter-mcp-server-webmvc</artifactId>
</dependency>
```

#### 9.2 注册工具到 MCP Server

使用 `MethodToolCallbackProvider` 将所有 `@Tool` 标注的方法统一注册：

```java
@Configuration
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider mcpToolProvider(
            WeatherTools weatherTools,
            TimeTools timeTools,
            SearchTools searchTools,
            SystemTools systemTools,
            ConversionTools conversionTools,
            CalculatorTools calculatorTools,
            UserTools userTools) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(weatherTools, timeTools, searchTools, systemTools,
                        conversionTools, calculatorTools, userTools)
                .build();
    }
}
```

#### 9.3 MCP 配置

```yaml
spring:
  ai:
    mcp:
      server:
        name: whatsmars-mcp-server    # MCP 服务名称
        version: 1.0.0
        type: SYNC                    # SYNC 或 ASYNC
```

**说明：** 工具类（`tool` 包下）同时用于内部 Tool Calling 和 MCP 对外暴露，无需维护两份代码。

---

### 10. AI 缓存 (Cache)

使用缓存优化 AI 响应性能，降低 API 调用成本。

```java
@RestController
@RequestMapping("/ai/cache")
public class CacheController {

    private final CachedChatService cachedChatService;

    @GetMapping("/chat")
    public String cachedChat(@RequestParam String message) {
        return cachedChatService.chatWithCache(message);
    }
}
```

**接口列表：**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/ai/cache/chat` | 带缓存的聊天 |
| POST | `/ai/cache/chat-with-system` | 带系统提示词的缓存聊天 |
| POST | `/ai/cache/rag-chat` | RAG 问答（带缓存） |
| POST | `/ai/cache/contextual-chat` | 多轮对话缓存 |
| DELETE | `/ai/cache/evict` | 清除指定问题缓存 |
| DELETE | `/ai/cache/clear-all` | 清除所有缓存 |
| GET | `/ai/cache/benchmark` | 缓存性能对比测试 |

**测试：**
```bash
# 第一次调用（请求 AI API）
curl "http://localhost:8080/ai/cache/chat?message=什么是Spring%20Boot？"

# 第二次调用（直接返回缓存）
curl "http://localhost:8080/ai/cache/chat?message=什么是Spring%20Boot？"

# 性能对比测试
curl "http://localhost:8080/ai/cache/benchmark?message=什么是Spring%20Boot？"
```

---

### 项目结构

```
whatsmars-ai-spring-ai/
├── config/
│   ├── AiConfig.java              # ChatClient Bean 配置（visionChatClient）
│   └── McpServerConfig.java       # MCP Server 工具注册
├── controller/
│   ├── AiChatController.java      # 基础聊天 + 流式响应
│   ├── AdvancedChatController.java # 高级对话（System Message、Temperature）
│   ├── StructuredOutputController.java # 结构化输出
│   ├── ToolCallingController.java # 工具调用（/ai/tool）
│   ├── ReactAgentController.java  # ReAct Agent
│   ├── RagController.java         # RAG（内存向量存储）
│   ├── RedisRagController.java    # RAG（Redis 向量存储）
│   ├── VisionController.java      # 多模态视觉
│   ├── CacheController.java       # AI 缓存
│   └── QwenStreamController.java  # 千问流式
├── service/
│   ├── ToolCallingService.java    # 工具调用服务
│   └── VisionService.java         # 视觉处理服务
├── tool/
│   ├── WeatherTools.java          # 天气工具
│   ├── TimeTools.java             # 时间工具
│   ├── SearchTools.java           # 知识搜索工具
│   ├── CalculatorTools.java       # 数学计算工具
│   ├── ConversionTools.java       # 数据转换工具
│   ├── SystemTools.java           # 系统工具
│   └── UserTools.java             # 用户信息工具
├── cache/
│   ├── CachedChatService.java     # 缓存聊天服务
│   └── AiCacheConfig.java         # 缓存配置
└── vo/                            # 响应 VO（保留但不再使用）
```

---

### 接口说明

所有 AI 接口统一返回 `String` 类型（纯文本响应），请求参数统一使用 `message` 命名。
