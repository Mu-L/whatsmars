## Spring AI 核心功能

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
      base-url: https://dashscope.aliyuncs.com/compatible-mode
      chat:
        options:
          model: qwen3.7-plus
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

#### System Message 角色设定

```java
@PostMapping("/system-message")
public ChatResponse chatWithSystemMessage(@RequestParam String message) {
    String response = chatClient.prompt()
        .system("你是一个资深的 Java 架构师，擅长设计高并发、高可用的分布式系统。")
        .user(message)
        .call()
        .content();
    return new ChatResponse(message, response);
}
```

#### Few-shot Prompting（少样本提示）

```java
@PostMapping("/few-shot")
public ChatResponse fewShotPrompting(@RequestParam String message) {
    String response = chatClient.prompt()
        .system("""
                你是一个代码翻译助手，请将用户的自然语言转换为 Java 代码。
                示例 1:
                用户: 创建一个字符串变量 name，值为 "Hello"
                AI: String name = "Hello";
                """)
        .user(message)
        .call()
        .content();
    return new ChatResponse(message, response);
}
```

#### 多轮对话历史

```java
@PostMapping("/conversation")
public ChatResponse conversation(
        @RequestParam String message,
        @RequestBody(required = false) List<String> history) {
    List<Message> messages = new ArrayList<>();
    messages.add(UserMessage.builder().text("你好").build());
    messages.add(AssistantMessage.builder().content("你好！有什么可以帮助你的？").build());
    if (history != null) {
        for (String h : history) {
            messages.add(UserMessage.builder().text(h).build());
        }
    }
    String response = chatClient.prompt()
        .messages(messages)
        .user(message)
        .call()
        .content();
    return new ChatResponse(message, response);
}
```

**响应格式（ChatResponse）：**

```java
public record ChatResponse(String message, String response) {}
```

**测试：**
```bash
# System Message
curl -X POST "http://localhost:8080/ai/advanced/system-message?message=如何设计一个秒杀系统？"

# 多轮对话
curl -X POST "http://localhost:8080/ai/advanced/conversation?message=今天天气怎么样？" \
  -H "Content-Type: application/json" \
  -d '["你好","你好，有什么可以帮助你的？"]'
```

---

### 4. 结构化输出 (Structured Output)

让 AI 返回格式化的 JSON 数据，并自动转换为 Java 对象。

```java
@PostMapping("/extract-user")
public UserInfo extractUserInfo(@RequestParam String text) {
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
            .entity(UserInfo.class);
}
```

**实体类定义：**

```java
public class UserInfo {
    private String name;
    private int age;
    private String email;
    private List<String> hobbies;
    private String occupation;
    // getters and setters
}
```

**测试：**
```bash
curl -X POST "http://localhost:8080/ai/structured/extract-user" \
  -d "text=我叫张三，今年25岁，是一名软件工程师，喜欢编程和打篮球，邮箱是zhangsan@example.com"
```

---

### 5. 函数调用 (Function Calling)

通过 `@Tool` 注解让 AI 自动调用 Java 方法获取实时数据。

**定义工具类：**

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
}
```

**使用工具：**

```java
@RestController
@RequestMapping("/ai/fc")
public class FunctionCallingController {

    private final ChatClient chatClient;
    private final WeatherTools weatherTools;

    public FunctionCallingController(ChatClient.Builder builder, WeatherTools weatherTools) {
        this.chatClient = builder.build();
        this.weatherTools = weatherTools;
    }

    @GetMapping("/weather")
    public ChatResponse getWeather(@RequestParam String message) {
        String response = chatClient.prompt()
                .user(message)
                .tools(weatherTools)
                .call()
                .content();
        return new ChatResponse(message, response);
    }
}
```

**测试：**
```bash
curl "http://localhost:8080/ai/fc/weather?message=北京今天的天气怎么样？"
```

AI 会自动识别需要调用 `getWeather("北京")` 获取天气信息。

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
    private final CalculatorTools calculatorTools;

    @GetMapping("/chat")
    public AgentResult agentChat(@RequestParam String message) {
        String response = chatClient.prompt()
                .system("你是一个智能助手，可以使用各种工具来帮助用户解决问题。")
                .user(message)
                .tools(weatherTools, searchTools, calculatorTools)
                .call()
                .content();
        return new AgentResult(message, response, "react-agent");
    }
}
```

**响应格式（AgentResult）：**

```java
public record AgentResult(String message, String response, String type) {}
```

**测试：**
```bash
# 天气查询
curl "http://localhost:8080/ai/react-agent/chat?message=北京今天的天气怎么样？"

# 知识搜索
curl "http://localhost:8080/ai/react-agent/chat?message=什么是Apache%20Dubbo？"

# 数学计算
curl "http://localhost:8080/ai/react-agent/chat?message=299打8折再减50是多少？"

# 复杂任务
curl "http://localhost:8080/ai/react-agent/complex-task?message=我想去杭州旅游，帮我查天气和介绍著名景点"
```

---

### 7. RAG (检索增强生成)

结合向量数据库实现知识库问答，解决大模型知识时效性问题。

#### 7.1 内存向量存储（开发测试用）

**配置 (application.yml)：**

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      base-url: https://dashscope.aliyuncs.com/compatible-mode
      chat:
        options:
          model: qwen3.7-plus
      embedding:
        options:
          model: text-embedding-v3
```

**使用：**

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
    public DocumentAddResult addDocument(@RequestParam String content) {
        Document document = new Document(content);
        vectorStore.add(List.of(document));
        return new DocumentAddResult("文档添加成功", content.length());
    }

    @GetMapping("/ask")
    public ChatResponse askQuestion(@RequestParam String message) {
        List<Document> relevantDocs = vectorStore.similaritySearch(message);
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));

        String answer = chatClient.prompt()
                .system("基于以下上下文回答问题：\n" + context)
                .user(message)
                .call()
                .content();
        return new ChatResponse(message, answer);
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

支持图片分析、OCR 文字识别、图表解读等功能。

> 请将 model 改为支持多模态的模型，如 qwen3.7-plus

```java
@RestController
@RequestMapping("/ai/vision")
public class VisionController {

    private final ChatClient chatClient;

    @PostMapping("/analyze-url")
    public VisionResult analyzeImageByUrl(@RequestParam String imageUrl,
                                          @RequestParam(defaultValue = "请详细描述这张图片的内容") String prompt) {
        Resource imageResource = new UrlResource(imageUrl);
        String description = chatClient.prompt()
                .user(userSpec -> userSpec
                        .text(prompt)
                        .media(MediaType.IMAGE_JPEG, imageResource))
                .call()
                .content();
        return new VisionResult(imageUrl, description);
    }

    @PostMapping("/ocr")
    public VisionResult ocrTextRecognition(@RequestParam String imageUrl) {
        Resource imageResource = new UrlResource(imageUrl);
        String text = chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("请提取图片中的所有文字，保持原有格式")
                        .media(MediaType.IMAGE_JPEG, imageResource))
                .call()
                .content();
        return new VisionResult(imageUrl, text);
    }

    @PostMapping("/compare")
    public ImageComparisonResult compareImages(@RequestParam String imageUrl1,
                                               @RequestParam String imageUrl2) {
        Resource image1 = new UrlResource(imageUrl1);
        Resource image2 = new UrlResource(imageUrl2);
        String comparison = chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("请对比这两张图片，分析它们的相似点和不同点")
                        .media(MediaType.IMAGE_JPEG, image1)
                        .media(MediaType.IMAGE_JPEG, image2))
                .call()
                .content();
        return new ImageComparisonResult(List.of(imageUrl1, imageUrl2), comparison);
    }
}
```

**响应格式：**

```java
public record VisionResult(String imageUrl, String response) {}
public record ImageComparisonResult(List<String> imageUrls, String response) {}
```

**测试：**
```bash
# 1/6 URL 图片分析（神舟十号海报）
curl -X POST "http://localhost:8080/ai/vision/analyze-url" \
  -d "imageUrl=https://imagecloud.thepaper.cn/thepaper/image/333/857/150.jpg"

# 2/6 图片上传分析（项目根目录下的架构图）
curl -X POST "http://localhost:8080/ai/vision/analyze-upload" \
  -F "file=@arch.png"

# 3/6 OCR 文字识别
curl -X POST "http://localhost:8080/ai/vision/ocr" \
  -d "imageUrl=https://imagecloud.thepaper.cn/thepaper/image/333/857/151.jpg"

# 4/6 图表分析（QuickChart.io 生成的柱状图）
curl -X POST "http://localhost:8080/ai/vision/chart-analysis" \
  -d "imageUrl=https://quickchart.io/chart?c=%7Btype%3A%27bar%27%2Cdata%3A%7Blabels%3A%5B%27Q1%27%2C%27Q2%27%2C%27Q3%27%2C%27Q4%27%5D%2Cdatasets%3A%5B%7Blabel%3A%27Revenue%27%2Cdata%3A%5B100%2C200%2C150%2C300%5D%7D%5D%7D%7D"

# 5/6 代码截图转代码（CSDN C语言代码图片）
curl -X POST "http://localhost:8080/ai/vision/code-from-image" \
  -d "imageUrl=https://i-blog.csdnimg.cn/blog_migrate/486ded85cb954f0da650e7f9c306900e.png"

# 6/6 多图片对比分析（神舟十号海报 vs 北京申奥号外）
curl -X POST "http://localhost:8080/ai/vision/compare" \
  -d "imageUrl1=https://imagecloud.thepaper.cn/thepaper/image/333/857/150.jpg" \
  -d "imageUrl2=https://imagecloud.thepaper.cn/thepaper/image/333/857/151.jpg"
```

---

### 9. AI 缓存 (Cache)

使用缓存优化 AI 响应性能，降低 API 调用成本。

```java
@RestController
@RequestMapping("/ai/cache")
public class CacheController {

    private final CachedChatService cachedChatService;

    @GetMapping("/chat")
    public ChatResponse cachedChat(@RequestParam String message) {
        String answer = cachedChatService.chatWithCache(message);
        return new ChatResponse(message, answer);
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

### 接口响应规范

所有接口统一使用 `vo` 包下的 record 类作为响应类型：

| VO 类 | 字段 | 使用场景 |
|--------|------|----------|
| `ChatResponse` | message, response | 通用聊天响应 |
| `AgentResult` | message, response, type | Agent 响应 |
| `VisionResult` | imageUrl, response | 图片分析 |
| `ImageComparisonResult` | imageUrls, response | 图片对比 |
| `DocumentAddResult` | message, contentLength | 文档添加 |
| `ClearResult` | message, hint | 清空操作 |
| `DocInfo` | content, source, score | 检索文档信息 |
| `SearchByCategoryResult` | category, query, documents, count | 分类检索 |
| `DemoResult` | weatherExample, searchExample, ... | Agent 演示 |

请求参数统一使用 `message` 命名。
