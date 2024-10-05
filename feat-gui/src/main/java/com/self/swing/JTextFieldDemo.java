package com.self.swing;

import javax.swing.*;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/15
 * @description :
 */
public class JTextFieldDemo extends JFrame {
    public static void main(String[] args) {
        new JTextFieldDemo();
    }

    public JTextFieldDemo() {


        //获取容器
        JPanel contentPane = new JPanel();

        //新建一个文本框, 文本框特点是无法换行
        JTextField jTextField1 = new JTextField(20);
        jTextField1.setText("JTextFieldDemo1");
        JTextField jTextField2 = new JTextField();
        jTextField2.setText("JTextFieldDemo2");

        contentPane.add(jTextField1);
        contentPane.add(jTextField2);

        this.setContentPane(contentPane);
        this.setBounds(100, 100, 300, 350);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
