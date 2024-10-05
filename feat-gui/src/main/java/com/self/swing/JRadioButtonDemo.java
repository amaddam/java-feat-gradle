package com.self.swing;

import javax.swing.*;
import java.awt.*;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/12
 * @description :
 */
public class JRadioButtonDemo extends JFrame {
    public static void main(String[] args) {
        new JRadioButtonDemo();
    }

    public JRadioButtonDemo() {

        //获取容器
        Container contentPane = this.getContentPane();
        //新建一个JRadioButton
        JRadioButton jRadioButton1 = new JRadioButton("JRadioButton1");
        JRadioButton jRadioButton2 = new JRadioButton("JRadioButton2");
        JRadioButton jRadioButton3 = new JRadioButton("JRadioButton3");

        //新建一个ButtonGroup
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(jRadioButton1);
        buttonGroup.add(jRadioButton2);
        buttonGroup.add(jRadioButton3);

        contentPane.add(jRadioButton1, BorderLayout.NORTH);
        contentPane.add(jRadioButton2, BorderLayout.CENTER);
        contentPane.add(jRadioButton3, BorderLayout.SOUTH);

        this.setContentPane(contentPane);

        this.setBounds(100, 100, 300, 350);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }
}
