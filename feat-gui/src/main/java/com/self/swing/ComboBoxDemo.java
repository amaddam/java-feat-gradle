package com.self.swing;

import javax.swing.*;
import java.awt.*;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/13
 * @description :
 */
public class ComboBoxDemo extends JFrame {
    public static void main(String[] args) {
        new ComboBoxDemo();
    }

    public ComboBoxDemo() {

        //获取容器
        Container contentPane = this.getContentPane();

        //新建一个JComboBox
        JComboBox<String> jComboBox = new JComboBox<>();
        jComboBox.addItem("JComboBox1");
        jComboBox.addItem("JComboBox2");
        jComboBox.addItem("JComboBox3");

        contentPane.add(jComboBox);

        this.setBounds(100, 100, 300, 350);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }
}
