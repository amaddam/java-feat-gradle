package com.self.swing;

import javax.swing.*;
import java.awt.*;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/11
 * @description :
 */
public class JPanelDemo extends JFrame {
    public static void main(String[] args) {
        new JPanelDemo();
    }

    public JPanelDemo() {


        Container contentPane = this.getContentPane();
        //设置布局管理器, 2行1列, 水平间距10, 垂直间距10
        contentPane.setLayout(new GridLayout(2,1,10,10));

        JPanel panel1 = new JPanel(new GridLayout(1, 3));
        JPanel panel2 = new JPanel(new GridLayout(1, 2));
        JPanel panel3 = new JPanel(new GridLayout(2, 1));
        JPanel panel4 = new JPanel(new GridLayout(3, 2));

        panel1.add(new JButton("Button 1.1"));
        panel1.add(new JButton("Button 1.2"));
        panel1.add(new JButton("Button 1.3"));
        panel2.add(new JButton("Button 2.1"));
        panel2.add(new JButton("Button 2.2"));
        panel3.add(new JButton("Button 3.1"));
        panel3.add(new JButton("Button 3.2"));
        panel4.add(new JButton("Button 4.1"));
        panel4.add(new JButton("Button 4.2"));
        panel4.add(new JButton("Button 4.3"));
        panel4.add(new JButton("Button 4.4"));
        panel4.add(new JButton("Button 4.5"));
        panel4.add(new JButton("Button 4.6"));

        contentPane.add(panel1);
        contentPane.add(panel2);
        contentPane.add(panel3);
        contentPane.add(panel4);

        this.setBounds(100, 100, 700, 500);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

}
