package com.self.io.bio.read.multithread;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BIOThreadPoolServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        // 持续监听
        while (true) {
            System.out.println("1. Server is running, waiting for connections...");
            Socket socket = serverSocket.accept();
            System.out.println("2. Accepted connection from: " + socket.getRemoteSocketAddress());
            //这里使用了多线程来处理每个连接, 每次有新的客户端连接进来时, 都会创建一个新的线程来处理该连接
            // 但是依然会有问题, 请求过多时, 线程创建和销毁的开销会很大, 可能会导致服务器崩溃
            // 有两个方式来解决这个问题:
            // 1. 使用线程池来管理线程的创建和销毁, 降低线程的创建和销毁的开销
            // 2. 使用NIO来处理IO操作, NIO是非阻塞式IO, 可以同时处理多个连接, 不需要为每个连接创建一个线程
            new Thread(() -> {
                try (socket; InputStream inputStream = socket.getInputStream()) {
                    int length = -1;
                    byte[] bytes = new byte[1024];
                    System.out.println("3. Ready to read data...");
                    while ((length = inputStream.read(bytes)) != -1) {
                        System.out.println("4. Reading data Success...");
                        String message = new String(bytes, 0, length);
                        System.out.println("Received message: " + message);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }, Thread.currentThread().getName()).start();
        }
    }
}
