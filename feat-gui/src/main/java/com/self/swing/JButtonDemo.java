package com.self.swing;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/12
 * @description :
 */
public class JButtonDemo extends JFrame {
    public static void main(String[] args) {
        new JButtonDemo();
    }

    public JButtonDemo() {

        //获取容器
        Container contentPane = this.getContentPane();

        //新建一个JButton
        JButton jButton = new JButton("JButtonDemo");
        //给按钮添加一个图标
        URL resource = JButtonDemo.class.getResource("/image.png");
        ImageIcon defaultIcon = new ImageIcon(resource);
        jButton.setIcon(defaultIcon);
        contentPane.add(jButton);

        this.setBounds(100, 100, 300, 350);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

}
