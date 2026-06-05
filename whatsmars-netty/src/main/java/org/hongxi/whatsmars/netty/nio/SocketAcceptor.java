package org.hongxi.whatsmars.netty.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;

/**
 * Created by jjenkov on 19-10-2015.
 */
public class SocketAcceptor implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(SocketAcceptor.class);

    private int tcpPort = 0;
    private ServerSocketChannel serverSocket = null;

    private Queue socketQueue = null;

    public SocketAcceptor(int tcpPort, Queue socketQueue)  {
        this.tcpPort     = tcpPort;
        this.socketQueue = socketQueue;
    }

    @Override
    public void run() {
        try{
            this.serverSocket = ServerSocketChannel.open();
            this.serverSocket.bind(new InetSocketAddress(tcpPort));
        } catch(IOException e){
            logger.error("Failed to bind server socket", e);
            return;
        }


        while(true){
            try{
                SocketChannel socketChannel = this.serverSocket.accept();

                logger.info("Socket accepted: {}", socketChannel);

                //todo check if the queue can even accept more sockets.
                this.socketQueue.add(new Socket(socketChannel));

            } catch(IOException e){
                logger.error("Error accepting socket", e);
            }

        }

    }
}
