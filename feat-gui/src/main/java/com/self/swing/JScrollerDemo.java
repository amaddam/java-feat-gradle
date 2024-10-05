package com.self.swing;

import javax.swing.*;
import java.awt.*;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/12
 * @description :
 */
public class JScrollerDemo extends JFrame {
    public static void main(String[] args) {
        new JScrollerDemo();
    }

    public JScrollerDemo() {

        //获取容器
        Container contentPane = this.getContentPane();

        //新建一个JTextArea
        JTextArea jTextArea = new JTextArea(20, 50);
        //设置文本
        jTextArea.setText("JScrollerDemo\n" +
                "JScrollPane是一个可以滚动的面板，" +
                "当面板中的组件超出面板的大小时，JScrollPane会自动出现滚动条，" +
                "这样就可以通过滚动条来查看面板中的所有内容。" +
                "JScrollPane是一个轻量级组件，它可以包含任何其他组件，" +
                "并且可以通过setViewportView()方法来设置JScrollPane的视口，" +
                "视口就是JScrollPane中的显示区域，只有视口的大小是固定的，" +
                "当视口中的组件超出视口的大小时，JScrollPane会自动出现滚动条。");
        //新建一个JScrollPane
        JScrollPane jScrollPane = new JScrollPane(jTextArea);
        contentPane.add(jScrollPane);

        this.setBounds(100, 100, 300, 350);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }
}
