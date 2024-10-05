package com.self.swing;

import javax.swing.*;
import java.awt.*;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/20
 * @description :
 */
public class JTextAreaDemo extends JFrame {
    public static void main(String[] args) {
        new JTextAreaDemo();
    }

    public JTextAreaDemo() {

        //获取容器
        Container contentPane = this.getContentPane();

        //新建一个JTextArea
        JTextArea jTextArea = new JTextArea(20, 50);
        //设置文本
        jTextArea.setText("JTextAreaDemo\n" +
                "JTextArea是一个多行文本框，" +
                "它可以显示多行文本，" +
                "并且可以通过setLineWrap()方法来设置文本是否自动换行，" +
                "默认情况下，JTextArea中的文本是不会自动换行的，" +
                "如果想要文本自动换行，可以通过setLineWrap(true)来设置。" +
                "JTextArea是一个轻量级组件，它可以包含任何其他组件，" +
                "并且可以通过setViewportView()方法来设置JScrollPane的视口，" +
                "视口就是JScrollPane中的显示区域，只有视口的大小是固定的，" +
                "当视口中的组件超出视口的大小时，JScrollPane会自动出现滚动条。");
        jTextArea.setLineWrap(true);
        contentPane.add(jTextArea);

        this.setBounds(100, 100, 300, 350);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }
}
