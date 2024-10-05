package com.self.swing.example;

import javax.swing.*;
import java.awt.*;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/21
 * @description :
 */
public class SnakePanel extends JPanel {
    //常量
    //蛇的每格大小
    private static final int CELL_SIZE = 25;
    //定义蛇的长度
    private int length;
    //定义蛇的x坐标
    private int[] snakeX = new int[600];//25*25, 600个格子完全够用
    //定义蛇的y坐标
    private int[] snakeY = new int[500]; //25*25, 500个格子完全够用

    public SnakePanel() {
        //初始化
        init();
    }

    public void init() {
        //初始化蛇的长度
        length = 3;
        //初始化蛇头的坐标
        snakeX[0] = 100;
        snakeY[0] = 95 + 25;
        //初始化第一个身体坐标
        snakeX[1] = 75;
        snakeY[1] = 95 + 25;
        //初始化第二个身体坐标
        snakeX[2] = 50;
        snakeY[2] = 95 + 25;
    }

    //绘制游戏界面, 游戏中的所有东西都是在这个方法中绘制的
    @Override
    protected void paintComponent(Graphics g) {
        int panelWidth = this.getWidth();
        int panelHeight = this.getHeight();
        //起到清屏的作用, 如果不加会闪烁
        super.paintComponent(g);
        //设置画笔颜色
        this.setBackground(Color.WHITE);
        //绘制静态面板
        setAdvertisementsSize(g, panelWidth, panelHeight);
        //把小蛇画上去, 蛇头初始化的时候是向右的, 两截身体
        SnakeData.SNAKE_HEAD_RIGHT_ICON.paintIcon(this, g, snakeX[0], snakeY[0]);
        SnakeData.SNAKE_BODY_ICON.paintIcon(this, g, snakeX[1], snakeY[1]);
        SnakeData.SNAKE_BODY_ICON.paintIcon(this, g, snakeX[2], snakeY[2]);
    }

    private void setAdvertisementsSize(Graphics g, int panelWidth, int panelHeight) {
        // 获取广告图片
        Image headerImage = SnakeData.HEADER_ICON.getImage();  // 假设HEADER_ICON是ImageIcon

        //顶部广告栏, 广告位图片大小为850*50, 距离上面有11个像素, 距离下面有14个像素
        //广告栏的宽度和高度
        int adWidth = 850;
        int adHeight = 90;


        System.out.println("sub insets = " + this.getInsets());
        System.out.println("sub panelWidth = " + panelWidth);
        System.out.println("sub panelHeight = " + panelHeight);

        //计算广告栏的x, y坐标, 使其居中
        int adX = (panelWidth - adWidth) / 2;
        int adY = 0;

        // 将 g 转换为 Graphics2D, 以便使用更多的绘图功能
        Graphics2D g2d = (Graphics2D) g;

        // 绘制缩放后的广告栏图片，使其缩放至 850x50
        g2d.drawImage(headerImage, adX, adY, adWidth, adHeight, this);

        //绘制游戏区域
        //外部有insets, 所以这里的x, y坐标需要加上insets的值,top=30, left=7, bottom=7, right=7
        //24X34
        g.fillRect(25, 120 - 25, 850, 600);
    }

}
