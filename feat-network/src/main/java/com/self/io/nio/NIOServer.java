package com.self.io.nio;

import java.io.FileDescriptor;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

public class NIOServer {

    // 专门用来保存所有已连接的SocketChannel
    static ArrayList<SocketChannel> socketList = new ArrayList<>();
    // 专门用来读写数据的缓冲区
    static ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(NIOServer.class.getName() + " start.");
        // 创建ServerSocketChannel, 绑定监听端口
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(8080));
            // 配置为非阻塞模式, 默认为阻塞模式, 配置为非阻塞模式需要配合Selector使用(事件等待机制)
            serverSocketChannel.configureBlocking(false);
            System.out.println("Server is running, waiting for connections...");
            // 持续监听
            while (true) {
                // 这里依然会有问题, 虽然使用了非阻塞式IO, 在没有数据可读的时候不会阻塞, 但是依然有问题
                // 1. 需要不断轮询所有已连接的SocketChannel, CPU占用会很高
                // 2. 不能区分哪个连接有数据可读, 只能轮询所有连接
                // 解决方案: 使用Selector(事件等待机制), 可以监听多个通道的事件, 只有当有事件发生时才会被触发, 大大降低了CPU的占用

                // 如果有已连接的SocketChannel, 则遍历读取数据
                for (SocketChannel socketChannel : socketList) {
                    // 非阻塞式读取数据
                    int length = socketChannel.read(byteBuffer);
                    if (length > 0) {
                        System.out.println("Reading data Success...");
                        // 切换读写模式
                        byteBuffer.flip();
                        byte[] bytes = new byte[length];
                        byteBuffer.get(bytes, 0, length);
                        String message = new String(bytes);
                        System.out.println("Received message: " + message);
                        System.out.println("Total connections: " + socketList.size());
                        byteBuffer.clear();
                    }
                    // 如果length == 0, 说明当前没有数据可读, 继续处理下一个连接
                }

                // 非阻塞式接受连接
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    socketChannel.configureBlocking(false);
                    socketList.add(socketChannel);
                    System.out.println("Accepted connection from: " + socketChannel.getRemoteAddress());
                    System.out.println("Total connections: " + socketList.size());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
