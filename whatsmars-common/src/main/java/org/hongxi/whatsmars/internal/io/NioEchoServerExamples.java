package org.hongxi.whatsmars.internal.io;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * NIO 网络通信示例：基于 Selector 的非阻塞 Echo 服务端 + 客户端
 *
 * NIO 模型核心：
 * - 一个线程管理多个 Channel（IO多路复用）
 * - Selector 监听多个 Channel 的事件（Accept/Read/Write/Connect）
 * - 非阻塞模式：没有事件时不阻塞线程
 *
 * 对比 BIO：
 * - BIO: 一个连接一个线程（线程模型受限）
 * - NIO: 一个线程处理多个连接（适合高并发、短连接场景）
 */
class NioEchoServerExamples {

    public static void main(String[] args) throws Exception {
        int port = 18080;

        // 启动服务端（后台线程）
        Thread serverThread = new Thread(() -> {
            try {
                startEchoServer(port);
            } catch (IOException e) {
                System.err.println("服务端异常: " + e.getMessage());
            }
        }, "nio-server");
        serverThread.setDaemon(true);
        serverThread.start();

        // 等待服务端启动
        Thread.sleep(200);

        // 启动多个客户端连接
        System.out.println("===== NIO Echo 通信测试 =====\n");
        for (int i = 1; i <= 3; i++) {
            String message = "Hello NIO, client-" + i;
            String response = sendAndReceive(port, message);
            System.out.println("[客户端-" + i + "] 发送: " + message + ", 收到: " + response);
        }

        System.out.println("\n===== 通信完成 =====");
    }

    // ==================== 服务端实现 ====================

    static void startEchoServer(int port) throws IOException {
        // 1. 创建 ServerSocketChannel（相当于 BIO 的 ServerSocket）
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false); // 非阻塞模式（必须）
        serverChannel.bind(new InetSocketAddress(port));

        // 2. 创建 Selector（事件多路复用器）
        Selector selector = Selector.open();

        // 3. 将 serverChannel 注册到 Selector，监听 ACCEPT 事件
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("[服务端] 启动在端口 " + port + ", 等待连接...");

        // 4. 事件循环（核心）
        while (true) {
            // select() 阻塞等待事件发生，有事件时返回就绪的 key 数量
            // select(timeout) 可设置超时，避免永久阻塞
            int readyCount = selector.select();
            if (readyCount == 0) {
                continue;
            }

            // 获取所有就绪的 SelectionKey
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();

                if (key.isAcceptable()) {
                    // 新连接到达
                    handleAccept(serverChannel, selector, key);

                } else if (key.isReadable()) {
                    // 客户端发送数据
                    handleRead(selector, key);
                }

                // 处理完必须移除 key，否则下次 select 还会处理
                iterator.remove();
            }
        }
    }

    static void handleAccept(ServerSocketChannel serverChannel, Selector selector, SelectionKey key)
        throws IOException {
        // accept() 获取客户端连接
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false); // 客户端 Channel 也设为非阻塞

        // 注册 READ 事件（后续可以读写）
        clientChannel.register(selector, SelectionKey.OP_READ);

        System.out.println("[服务端] 新连接: " + clientChannel.getRemoteAddress());
    }

    static void handleRead(Selector selector, SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        // 分配读取缓冲区
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = clientChannel.read(buffer);

        if (bytesRead > 0) {
            // 切换为读模式
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            String message = new String(data, StandardCharsets.UTF_8);

            System.out.println("[服务端] 收到: " + message);

            // Echo: 将收到的数据原样返回
            String response = "Echo: " + message;
            ByteBuffer writeBuffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
            while (writeBuffer.hasRemaining()) {
                clientChannel.write(writeBuffer);
            }

            System.out.println("[服务端] 响应: " + response);

        } else if (bytesRead == -1) {
            // 客户端关闭连接
            System.out.println("[服务端] 客户端断开: " + clientChannel.getRemoteAddress());
            clientChannel.close();
            key.cancel();
        }
    }

    // ==================== 客户端实现 ====================

    static String sendAndReceive(int port, String message) throws IOException {
        // 创建 SocketChannel 并连接服务端
        SocketChannel clientChannel = SocketChannel.open();
        clientChannel.configureBlocking(true); // 客户端使用阻塞模式简化示例
        clientChannel.connect(new InetSocketAddress("localhost", port));

        // 发送数据
        ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8));
        clientChannel.write(writeBuffer);

        // 接收响应
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        int bytesRead = clientChannel.read(readBuffer);
        readBuffer.flip();
        byte[] data = new byte[readBuffer.remaining()];
        readBuffer.get(data);

        clientChannel.close();
        return new String(data, StandardCharsets.UTF_8);
    }
}
