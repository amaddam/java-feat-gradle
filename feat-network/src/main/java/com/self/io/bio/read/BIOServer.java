package com.self.io.bio.read;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class BIOServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            System.out.println("1. Server is running, waiting for connections...");
            Socket socket = serverSocket.accept();
            System.out.println("2. Accepted connection from: " + socket.getRemoteSocketAddress());
            // 因为这里是阻塞式IO, 所以在没有数据可读的时候会阻塞在read()方法上, 直到当前socket有数据可读为止
            // 而且单线程情况下, 只能处理一个客户端连接
            InputStream inputStream = socket.getInputStream();
            int length = -1;
            byte[] bytes = new byte[1024];
            System.out.println("3. Ready to read data...");
            while ((length = inputStream.read(bytes)) != -1) {
                System.out.println("4. Reading data Success...");
                String message = new String(bytes, 0, length);
                System.out.println("Received message: " + message);
            }
            inputStream.close();
            socket.close();
        }
    }
}
