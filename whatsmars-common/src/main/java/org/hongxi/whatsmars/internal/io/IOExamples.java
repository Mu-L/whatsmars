package org.hongxi.whatsmars.internal.io;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * IO/NIO 核心用法示例：字节流、字符流、装饰器模式、NIO Buffer/Channel、文件操作
 */
class IOExamples {

    public static void main(String[] args) throws Exception {
        byteStreamDemo();
        charStreamDemo();
        decoratorPattern();
        nioBufferChannel();
        fileOperations();
    }

    // ==================== 1. 字节流 ====================

    static void byteStreamDemo() throws Exception {
        System.out.println("===== 字节流 =====");

        // InputStream/OutputStream 是所有字节流的基类
        // 场景：图片、视频、序列化数据等非文本数据

        // 写入字节
        String data = "Hello, IO!";
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            baos.write(data.getBytes(StandardCharsets.UTF_8));

            // 读取字节
            try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
                byte[] buffer = new byte[4]; // 模拟缓冲区
                int len;
                StringBuilder sb = new StringBuilder();
                while ((len = bais.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, len, StandardCharsets.UTF_8));
                }
                System.out.println("读取结果: " + sb);
            }
        }

        // 关键点：
        // 1. 始终使用 try-with-resources 确保流关闭
        // 2. 使用缓冲区（buffer）减少实际IO次数，提升性能
        // 3. read() 返回 -1 表示流结束
        System.out.println();
    }

    // ==================== 2. 字符流 ====================

    static void charStreamDemo() throws Exception {
        System.out.println("===== 字符流 =====");

        // Reader/Writer 处理字符数据，自动处理编码
        // 场景：文本文件读写

        String text = "你好世界\nHello World\nこんにちは";

        // StringWriter 写入内存
        StringWriter sw = new StringWriter();
        try (BufferedWriter bw = new BufferedWriter(sw)) {
            bw.write(text);
            bw.newLine(); // 跨平台换行
            bw.write("第二行内容");
        }
        System.out.println("字符流写出: " + sw);

        // StringReader 从内存读取
        try (BufferedReader br = new BufferedReader(new StringReader(sw.toString()))) {
            String line;
            int lineNum = 0;
            while ((line = br.readLine()) != null) {
                System.out.println("第" + (++lineNum) + "行: " + line);
            }
        }

        // 字节流 vs 字符流选择：
        // - 文本数据 -> Reader/Writer（自动编码转换）
        // - 二进制数据 -> InputStream/OutputStream（原始字节）
        // - 需要转换 -> InputStreamReader / OutputStreamWriter（桥接）
        System.out.println();
    }

    // ==================== 3. 装饰器模式（IO流设计的经典应用）====================

    static void decoratorPattern() throws Exception {
        System.out.println("===== 装饰器模式 =====");

        // Java IO 是装饰器模式的经典实现：
        // 基础流 -> 缓冲流 -> 功能流（层层包装，灵活组合）

        // 示例：给字节流添加缓冲能力 + 数据读写能力
        ByteArrayOutputStream baos = new ByteArrayOutputStream();            // 基础流
        BufferedOutputStream bos = new BufferedOutputStream(baos);           // 加缓冲
        DataOutputStream dos = new DataOutputStream(bos);                    // 加数据类型读写

        dos.writeInt(42);
        dos.writeUTF("装饰器模式");
        dos.writeDouble(3.14);
        dos.flush();

        // 反向解包读取
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        BufferedInputStream bis = new BufferedInputStream(bais);
        DataInputStream dis = new DataInputStream(bis);

        System.out.println("int:    " + dis.readInt());
        System.out.println("utf:    " + dis.readUTF());
        System.out.println("double: " + dis.readDouble());

        // 常见装饰器组合：
        // new BufferedReader(new FileReader("file.txt"))     - 文件+缓冲+按行读
        // new BufferedOutputStream(new FileOutputStream(f))  - 文件+缓冲写出
        // new ObjectOutputStream(new FileOutputStream(f))    - 文件+序列化
        System.out.println();
    }

    // ==================== 4. NIO Buffer/Channel ====================

    static void nioBufferChannel() throws Exception {
        System.out.println("===== NIO Buffer/Channel =====");

        // NIO 核心三件套：Buffer（缓冲区）、Channel（通道）、Selector（多路复用）
        // 与 BIO 的区别：面向块、非阻塞、支持零拷贝

        // ByteBuffer 核心属性：
        // capacity - 容量（固定）
        // position - 当前读写位置
        // limit    - 可读写的上界
        // mark     - 标记（可回退到此处）

        ByteBuffer buffer = ByteBuffer.allocate(32);
        System.out.printf("初始: pos=%d, limit=%d, capacity=%d%n",
            buffer.position(), buffer.limit(), buffer.capacity());

        // 写入数据
        buffer.put("Hello NIO".getBytes(StandardCharsets.UTF_8));
        System.out.printf("写入后: pos=%d, limit=%d%n", buffer.position(), buffer.limit());

        // 切换为读模式（flip: limit=position, position=0）
        buffer.flip();
        System.out.printf("flip后: pos=%d, limit=%d%n", buffer.position(), buffer.limit());

        // 读取数据
        byte[] dst = new byte[buffer.remaining()];
        buffer.get(dst);
        System.out.println("读取内容: " + new String(dst, StandardCharsets.UTF_8));

        // compact()：保留未读数据，准备再次写入
        buffer.compact();

        // 直接内存（DirectByteBuffer）：零拷贝场景使用
        ByteBuffer direct = ByteBuffer.allocateDirect(1024);
        System.out.println("直接内存: isDirect=" + direct.isDirect());
        // 直接内存优势：避免 JVM 堆与操作系统内核空间的复制
        // 适用场景：大文件传输、网络IO（Netty 大量使用 DirectByteBuffer）

        System.out.println();
    }

    // ==================== 5. 文件操作（NIO.2）====================

    static void fileOperations() throws Exception {
        System.out.println("===== 文件操作(NIO.2) =====");

        Path tempFile = Files.createTempFile("io-demo-", ".txt");
        System.out.println("临时文件: " + tempFile);

        // 写入
        Files.writeString(tempFile, "第一行\n第二行\n第三行", StandardCharsets.UTF_8);

        // 读取所有行
        java.util.List<String> lines = Files.readAllLines(tempFile, StandardCharsets.UTF_8);
        System.out.println("行数: " + lines.size());

        // 文件属性
        System.out.println("大小: " + Files.size(tempFile) + " bytes");
        System.out.println("是否可读: " + Files.isReadable(tempFile));
        System.out.println("是否目录: " + Files.isDirectory(tempFile));

        // 遍历目录
        System.out.println("当前目录文件(前5个):");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get("."), "*.java")) {
            int count = 0;
            for (Path entry : stream) {
                if (count++ >= 5) break;
                System.out.println("  " + entry.getFileName());
            }
        } catch (DirectoryIteratorException e) {
            // 当前目录可能没有 .java 文件，忽略
        }

        // 删除
        Files.deleteIfExists(tempFile);
        System.out.println("已删除: " + !Files.exists(tempFile));
        System.out.println();
    }
}

