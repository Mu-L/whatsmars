## Spring AI 核心功能

### 1. 基础聊天 (ChatClient)

Spring AI 提供了统一的 `ChatClient` API，支持多种大语言模型（OpenAI、Qwen、GPT 等）。

**快速开始：**

```java
@RestController
public class HelloController {
    
    private final ChatClient chatClient;
    
    public HelloController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }
    
    @GetMapping("/chat")
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
curl "http://localhost:8080/chat?message=讲个笑话"
```

---

### 2. 流式响应 (Streaming)

使用 Reactor 的 `Flux` 实现流式输出，提升用户体验。

```java
@GetMapping("/stream-chat")
public Flux<String> streamChat(@RequestParam String input) {
    return chatClient.prompt()
            .user(input)
            .stream()
            .content();
}
```

**测试：**
```bash
curl "http://localhost:8080/ai/stream-chat?input=介绍一下武汉"
```

---

### 3. 高级对话特性

#### System Message 角色设定

```java
String response = chatClient.prompt()
    .system("你是一个资深的 Java 架构师，擅长设计高并发、高可用的分布式系统。")
    .user("如何设计一个秒杀系统？")
    .call()
    .content();
```

#### Few-shot Prompting（少样本提示）

```java
String response = chatClient.prompt()
    .system("你是一个翻译助手")
    .messages(
        Message.builder().role(Role.USER).text("Hello").build(),
        Message.builder().role(Role.ASSISTANT).text("你好").build(),
        Message.builder().role(Role.USER).text("Good morning").build()
    )
    .call()
    .content();
```

#### 多轮对话历史

```java
List<Message> history = new ArrayList<>();
history.add(Message.builder().role(Role.USER).text("你好").build());
history.add(Message.builder().role(Role.ASSISTANT).text("你好！有什么可以帮助你的？").build());

String response = chatClient.prompt()
    .messages(history)
    .user("今天天气怎么样？")
    .call()
    .content();
```

---

### 4. 结构化输出 (Structured Output)

让 AI 返回格式化的 JSON 数据，并自动转换为 Java 对象。

```java
@PostMapping("/extract-user")
public UserInfo extractUserInfo(@RequestParam String text) {
    return chatClient.prompt()
            .system("从文本中提取用户信息，以 JSON 格式返回")
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
        // 实际项目中可以调用真实天气 API
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
    
    public FunctionCallingController(ChatClient.Builder builder, 
                                     WeatherTools weatherTools) {
        this.chatClient = builder.build();
        this.weatherTools = weatherTools;
    }
    
    @GetMapping("/weather")
    public String getWeather(@RequestParam String question) {
        return chatClient.prompt()
                .user(question)
                .tools(weatherTools)  // 注册工具
                .call()
                .content();
    }
}
```

**测试：**
```bash
curl "http://localhost:8080/ai/fc/weather?question=北京今天的天气怎么样？"
```

AI 会自动识别需要调用 `getWeather("北京")` 获取天气信息。

---

### 6. RAG (检索增强生成)

结合向量数据库实现知识库问答，解决大模型知识时效性问题。

#### 6.1 内存向量存储（开发测试用）

```java
@RestController
@RequestMapping("/ai/rag")
public class RagController {
    
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    
    public RagController(ChatClient.Builder builder, EmbeddingModel embeddingModel) {
        this.chatClient = builder.build();
        this.vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        
        // 初始化知识库
        initializeKnowledgeBase();
    }
    
    private void initializeKnowledgeBase() {
        List<Document> documents = List.of(
            new Document("Spring Boot 是一个用于快速构建基于 Spring 框架的生产级应用程序的框架。"),
            new Document("Apache Dubbo 是一款高性能、轻量级的开源 Java RPC 框架。"),
            new Document("Redis 是一个开源的内存数据结构存储系统，可用作数据库、缓存和消息中间件。")
        );
        vectorStore.add(documents);
    }
    
    @GetMapping("/ask")
    public Map<String, Object> askQuestion(@RequestParam String question) {
        // 步骤 1: 检索相关文档
        List<Document> relevantDocs = vectorStore.similaritySearch(question);
        
        // 步骤 2: 构建上下文
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n\n"));
        
        // 步骤 3: 基于上下文回答问题
        String answer = chatClient.prompt()
                .system("基于以下上下文回答问题：\n" + context)
                .user(question)
                .call()
                .content();
        
        return Map.of("answer", answer, "docCount", relevantDocs.size());
    }
}
```

**测试：**
```bash
# 添加文档
curl -X POST "http://localhost:8080/ai/rag/document" \
  -d "content=Nacos 是一个易于构建 AI Agent 应用的动态服务发现、配置管理平台"

# 问答
curl "http://localhost:8080/ai/rag/ask?question=国内最流行的RPC框架是哪一款"
```

#### 6.2 Redis 向量存储（生产环境推荐）

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
        dimensions: 1024  # text-embedding-v3 默认 1024 维
        initialize-schema: true
        distance-type: COSINE
  
  redis:
    host: localhost
    port: 6379
```

**使用：**

```java
@RestController
@RequestMapping("/ai/rag-redis")
public class RedisRagController {
    
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    
    public RedisRagController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;  // 自动注入 Redis VectorStore
    }
    
    @PostMapping("/document")
    public void addDocument(@RequestParam String content) {
        vectorStore.add(List.of(new Document(content)));
    }
    
    @GetMapping("/ask")
    public String ask(@RequestParam String question) {
        List<Document> docs = vectorStore.similaritySearch(question);
        String context = docs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));
        
        return chatClient.prompt()
                .system("基于上下文回答：" + context)
                .user(question)
                .call()
                .content();
    }
}
```

**注意：** Spring AI 使用 Redis 作为 VectorStore 时，必须使用 **Redis Stack**（包含 RediSearch 模块），普通 Redis 不支持向量搜索。

---

### 7. 多模态视觉理解 (Vision)

支持图片分析、OCR 文字识别、图表解读等功能。

```java
@RestController
@RequestMapping("/ai/vision")
public class VisionController {
    
    private final ChatClient chatClient;
    
    public VisionController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }
    
    /**
     * 分析图片内容
     */
    @PostMapping("/analyze-url")
    public String analyzeImage(@RequestParam String imageUrl) {
        Resource imageResource = new UrlResource(imageUrl);
        
        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("请详细描述这张图片的内容")
                        .media(MediaType.IMAGE_JPEG, imageResource))
                .call()
                .content();
    }
    
    /**
     * OCR 文字识别
     */
    @PostMapping("/ocr")
    public String ocrTextRecognition(@RequestParam String imageUrl) {
        Resource imageResource = new UrlResource(imageUrl);
        
        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("请提取图片中的所有文字，保持原有格式")
                        .media(MediaType.IMAGE_JPEG, imageResource))
                .call()
                .content();
    }
    
    /**
     * 图表分析
     */
    @PostMapping("/chart-analysis")
    public String analyzeChart(@RequestParam String imageUrl) {
        Resource imageResource = new UrlResource(imageUrl);
        
        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("请分析这个图表，包括：\n" +
                              "1. 图表类型\n" +
                              "2. 数据趋势\n" +
                              "3. 关键发现\n" +
                              "4. 结论建议")
                        .media(MediaType.IMAGE_JPEG, imageResource))
                .call()
                .content();
    }
    
    /**
     * 代码截图转代码
     */
    @PostMapping("/code-from-image")
    public String codeFromImage(@RequestParam String imageUrl) {
        Resource imageResource = new UrlResource(imageUrl);
        
        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("请将这张图片中的代码完整提取出来，保持格式和缩进")
                        .media(MediaType.IMAGE_JPEG, imageResource))
                .call()
                .content();
    }
    
    /**
     * 多图片对比分析
     */
    @PostMapping("/compare")
    public String compareImages(@RequestParam String imageUrl1,
                                @RequestParam String imageUrl2) {
        Resource image1 = new UrlResource(imageUrl1);
        Resource image2 = new UrlResource(imageUrl2);
        
        return chatClient.prompt()
                .user(userSpec -> userSpec
                        .text("请对比这两张图片，分析它们的相似点和不同点")
                        .media(MediaType.IMAGE_JPEG, image1)
                        .media(MediaType.IMAGE_JPEG, image2))
                .call()
                .content();
    }
}
```

**测试：**
```bash
# 图片分析
curl -X POST "http://localhost:8080/ai/vision/analyze-url" \
  -d "imageUrl=https://img1.baidu.com/it/u=3224850734,2174446166&fm=253&fmt=auto&app=120&f=JPEG?w=500&h=837"

# OCR 识别
curl -X POST "http://localhost:8080/ai/vision/ocr" \
  -d "imageUrl=https://imagecloud.thepaper.cn/thepaper/image/333/857/151.jpg"

# 图表分析
curl -X POST "http://localhost:8080/ai/vision/chart-analysis" \
  -d "imageUrl=https://img0.baidu.com/it/u=3716881902,3785738263&fm=253&app=138&f=JPEG?w=684&h=912"
```
