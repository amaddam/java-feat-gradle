package com.self.swing;

import javax.swing.*;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/15
 * @description :
 */
public class JPasswordDemo extends JFrame {
    public static void main(String[] args) {
        new JPasswordDemo();
    }

    public JPasswordDemo() {
        //获取容器
        JPanel contentPane = new JPanel();

        //新建一个密码框
        JPasswordField jPasswordField = new JPasswordField(20);
        //设置密码框的初始值
        jPasswordField.setText("JPasswordFieldDemo");
        //设置密码框的回显字符(默认是 '*')
        jPasswordField.setEchoChar('#');

        contentPane.add(jPasswordField);

        this.setContentPane(contentPane);
        this.setBounds(100, 100, 300, 350);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
