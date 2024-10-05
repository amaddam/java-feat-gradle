package com.self.swing.example;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/9/5
 * @description :
 */
public class SnakeData {
    //其中的图片都需要25*25的大小
    private static final short cellSize = 25;
    //头图片
    private static final URL HEADER = SnakeData.class.getResource("/snake/header_image.png");
    public static final ImageIcon HEADER_ICON = new ImageIcon(HEADER
                    // Toolkit.getDefaultToolkit().getImage(HEADER).getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH)
    );

    //蛇头上
    private static final URL SNAKE_HEAD_UP = SnakeData.class.getResource("/snake/snake_header_up.png");
    private static final ImageIcon SNAKE_HEAD_UP_ICON = new ImageIcon(
                    Toolkit.getDefaultToolkit().getImage(SNAKE_HEAD_UP).getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH)
    );
    //蛇头下
    private static final URL SNAKE_HEAD_DOWN = SnakeData.class.getResource("/snake/snake_header_down.png");
    private static final ImageIcon SNAKE_HEAD_DOWN_ICON = new ImageIcon(
                    Toolkit.getDefaultToolkit().getImage(SNAKE_HEAD_DOWN).getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH)
    );
    //蛇头左
    private static final URL SNAKE_HEAD_LEFT = SnakeData.class.getResource("/snake/snake_header_left.png");
    private static final ImageIcon SNAKE_HEAD_LEFT_ICON = new ImageIcon(
                    Toolkit.getDefaultToolkit().getImage(SNAKE_HEAD_LEFT).getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH)
    );
    //蛇头右
    private static final URL SNAKE_HEAD_RIGHT = SnakeData.class.getResource("/snake/snake_header_right.png");
    public static final ImageIcon SNAKE_HEAD_RIGHT_ICON = new ImageIcon(
                    Toolkit.getDefaultToolkit().getImage(SNAKE_HEAD_RIGHT).getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH)
    );
    //蛇身
    private static final URL SNAKE_BODY = SnakeData.class.getResource("/snake/snake_body.png");
    public static final ImageIcon SNAKE_BODY_ICON = new ImageIcon(
                    Toolkit.getDefaultToolkit().getImage(SNAKE_BODY).getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH)
    );
    //食物
    private static final URL FOOD = SnakeData.class.getResource("/snake/snake_food.png");
    public static final ImageIcon FOOD_ICON = new ImageIcon(
                    Toolkit.getDefaultToolkit().getImage(FOOD).getScaledInstance(cellSize, cellSize, Image.SCALE_SMOOTH)
    );
}
