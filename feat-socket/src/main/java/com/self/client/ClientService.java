package com.self.client;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/7/2
 * @description :
 */
public class ClientService {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JFrame frame;
    private JTextArea messageArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton fileButton;

    public ClientService(String serverAddress, int port) {
        try {
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 启动线程处理服务器消息
            new Thread(new ServerHandler()).start();

            // 创建GUI
            createGUI();
        } catch (IOException e) {
            System.out.println("连接服务器错误：" + e.getMessage());
        }
    }

    private void createGUI() {
        frame = new JFrame("Client");
        messageArea = new JTextArea(20, 50);
        messageArea.setEditable(false);
        inputField = new JTextField(40);
        sendButton = new JButton("发送");
        fileButton = new JButton("发送文件");

        JPanel panel = new JPanel();
        panel.add(inputField);
        panel.add(sendButton);
        panel.add(fileButton);

        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.getContentPane().add(panel, BorderLayout.SOUTH);

        sendButton.addActionListener(e -> sendMessage());
        fileButton.addActionListener(e -> sendFile());

        inputField.addActionListener(e -> sendMessage());

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            out.println(message);
            inputField.setText("");
        }
    }

    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            sendFileToServer(file);
        }
    }

    private void sendFileToServer(File file) {
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = socket.getOutputStream()) {
            out.println("FILE:" + file.getName());

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) > 0) {
                os.write(buffer, 0, bytesRead);
            }
            messageArea.append("文件发送完毕：" + file.getName() + "\n");
        } catch (IOException e) {
            messageArea.append("文件发送错误：" + e.getMessage() + "\n");
        }
    }

    private class ServerHandler implements Runnable {
        @Override
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    messageArea.append("服务器消息：" + message + "\n");
                }
            } catch (IOException e) {
                messageArea.append("读取服务器消息错误：" + e.getMessage() + "\n");
            }
        }
    }

    public static void main(String[] args) {
        new ClientService("localhost", 6666);
    }
}