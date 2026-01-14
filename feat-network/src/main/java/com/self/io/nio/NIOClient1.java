package com.self.io.nio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

public class NIOClient1 {
    /**
     * 在控制台输入内容，发送给服务端, 输入exit结束
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        System.out.println(NIOClient1.class.getName() + " start.");

        Socket socket = new Socket("127.0.0.1", 8080);
        OutputStream outputStream = socket.getOutputStream();
        while (true) {
            Scanner scanner = new Scanner(System.in);
            String scannerString = scanner.next();
            String string = NIOClient1.class.getSimpleName()+ " " + scannerString;
            if (scannerString.equals("exit")) {
                break;
            }
            socket.getOutputStream().write(string.getBytes());
            System.out.println(NIOClient1.class.getName() + " message sent to server.");
        }
        outputStream.close();
        socket.close();
    }
}
