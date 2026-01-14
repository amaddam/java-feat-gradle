package com.self.io.bio.accept;

import java.io.IOException;
import java.net.Socket;

public class BIOClient1 {
    public static void main(String[] args) throws IOException {
        System.out.println(BIOClient1.class.getName() + " start.");
        Socket socket = new Socket("127.0.0.1", 8080);
        System.out.println("Connected to server: " + socket.getRemoteSocketAddress());
    }
}
