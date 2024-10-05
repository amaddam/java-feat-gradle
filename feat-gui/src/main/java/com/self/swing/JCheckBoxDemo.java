package com.self.swing;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/13
 * @description :
 */
public class JCheckBoxDemo extends JFrame {
    public static void main(String[] args) {
        new JCheckBoxDemo();
    }

    public JCheckBoxDemo() {


        //将图片变为图标
        URL resource = JButtonDemo.class.getResource("/image.png");
        ImageIcon defaultIcon = new ImageIcon(resource);

        //获取容器
        Container contentPane = this.getContentPane();

        //新建一个JCheckBox
        JCheckBox jCheckBox1 = new JCheckBox("JCheckBox1");
        JCheckBox jCheckBox2 = new JCheckBox("JCheckBox2");

        contentPane.add(jCheckBox1, BorderLayout.NORTH);
        contentPane.add(jCheckBox2, BorderLayout.SOUTH);

        this.setContentPane(contentPane);

        this.setBounds(100, 100, 300, 350);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
