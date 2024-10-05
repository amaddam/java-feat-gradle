package com.self.swing.example;

import javax.swing.*;
import java.awt.*;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/21
 * @description :
 */
public class SnakeGame {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        //10, 10, 900, 720这些参数分别代表了窗口的左上角的横坐标，纵坐标，宽度和高度
        //宽度和高度是算出来的, 每一个贪吃蛇的格子是25*25的 也就是横向有36个格子, 纵向有28个格子 + 20像素
        //左右侧边栏各占一个格子
        //广告位图片大小为850*50, 距离上面有0个像素, 距离下面的游戏面板是5个像素
        //游戏界面是850*600
        //要算上不知道哪里来的insets ,所以高需要加上30+7, 宽加上7+7
        frame.setBounds(10, 10, 900 + 14, 720+37);
        //窗口大小不可变
        frame.setResizable(false);
        //设置窗口可见
        frame.setVisible(true);
        //设置关闭窗口的时候, 退出程序
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);



        SnakePanel snakePanel = new SnakePanel();
        frame.add(snakePanel);


        //不知道为什么外部页面的insets是top=30, left=7, bottom=7, right=7, 导致内部panel的宽度和高度不够,
        Insets insets = frame.getInsets();
        System.out.println("summary insets = " + insets);
        System.out.println("summary panelWidth = " + frame.getWidth());
        System.out.println("summary panelHeight = " + frame.getHeight());

    }
}
