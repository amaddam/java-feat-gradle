package com.self.io.bio.accept;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class BIOServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        while (true) {
            System.out.println("Server is running, waiting for connections...");
            Socket socket = serverSocket.accept();
            System.out.println("Accepted connection from: " + socket.getRemoteSocketAddress());
        }
    }
}
