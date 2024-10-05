package com.self.swing;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/11
 * @description :
 */
public class ImageIconDemo extends JFrame {
    public static void main(String[] args) {
        new ImageIconDemo();
    }

    public ImageIconDemo() {


        JLabel jLabel = new JLabel("ImageIconDemo");
        //使用比较大分辨率的图片可能会不显示? 
        URL resource = ImageIconDemo.class.getResource("/image.png");
        //打印URL内的路径
        System.out.println(resource);

        ImageIcon imageIcon = new ImageIcon(resource);
        jLabel.setIcon(imageIcon);
        jLabel.setHorizontalAlignment(SwingConstants.CENTER);

        Container contentPane = this.getContentPane();
        contentPane.add(jLabel);

        this.setBounds(100, 100, 700, 500);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

}
