package com.self.swing.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/21
 * @description : 贪吃蛇游戏的面板
 * 继承JPanel, 重写paintComponent方法, 画出游戏界面
 * 实现KeyListener接口, 监听键盘事件
 * 实现ActionListener接口, 监听定时器事件
 * 流程大概是这样的:
 * 1. 定义数据
 * 2. 画上去
 * 3. 监听事件
 * 3.1 键盘事件
 * 3.2 定时器事件
 */
public class SnakePanel extends JPanel implements KeyListener, ActionListener {
    //常量
    //字体
    private static final Font MICROSOFT_YA_HEI = new Font("Microsoft YaHei", Font.BOLD, 40);
    //蛇的每格大小
    private static final int CELL_SIZE = 25;
    //游戏面板的格子数, 纵向24个, 横向34个
    private static final int ROWS = 24;
    private static final int COLS = 34;
    //食物的生成器
    private static final Random RANDOM = new Random();
    //定义蛇的长度
    private int length;
    //定义蛇的x坐标, 蛇头的x坐标在snakeX[0], 蛇身体的x坐标在snakeX[1], snakeX[2]...
    private final int[] snakeX = new int[600];//25*25, 600个格子完全够用
    //定义蛇的y坐标, 蛇头的y坐标在snakeY[0], 蛇身体的y坐标在snakeY[1], snakeY[2]...
    private final int[] snakeY = new int[500]; //25*25, 500个格子完全够用
    //蛇的方向
    private String direction;//U D L R
    //游戏是否开始
    private boolean isStart = false;
    //定时器, 100ms执行一次
    private final Timer timer = new Timer(100, this);
    //食物的x坐标
    private int foodX;
    //食物的y坐标
    private int foodY;
    //是否失败
    private boolean isFail = false;
    //添加成绩
    private int score = 0;

    public SnakePanel() {
        //初始化
        init();
        //获得焦点和键盘事件, 这个panel需要获得焦点, 才能监听到键盘事件
        this.setFocusable(true);
        //添加键盘监听, 当前类实现了KeyListener接口, 所以可以直接传this
        this.addKeyListener(this);
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
        //初始化方向
        direction = "R";
        //初始化食物的坐标
        foodX = 25 + 25 * RANDOM.nextInt(34);
        foodY = 95 + 25 * RANDOM.nextInt(24);
        //初始化定时器
        timer.start();
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
        //画分数
        g.setColor(Color.BLUE);
        g.setFont(MICROSOFT_YA_HEI.deriveFont(18.0f));
        g.drawString("长度: " + length, 750, 30);
        g.drawString("分数: " + score, 750, 65);


        //画食物, 先画食物, 后画蛇, 这样确保蛇头可以覆盖食物
        SnakeData.FOOD_ICON.paintIcon(this, g, foodX, foodY);
        //把小蛇画上去, 蛇头初始化的时候是向右的, 两截身体
        switch (direction) {
            case "U":
                SnakeData.SNAKE_HEAD_UP_ICON.paintIcon(this, g, snakeX[0], snakeY[0]);
                break;
            case "D":
                SnakeData.SNAKE_HEAD_DOWN_ICON.paintIcon(this, g, snakeX[0], snakeY[0]);
                break;
            case "L":
                SnakeData.SNAKE_HEAD_LEFT_ICON.paintIcon(this, g, snakeX[0], snakeY[0]);
                break;
            case "R":
                SnakeData.SNAKE_HEAD_RIGHT_ICON.paintIcon(this, g, snakeX[0], snakeY[0]);
                break;
        }
        //画蛇的身体
        for (int i = 1; i < length; i++) {
            SnakeData.SNAKE_BODY_ICON.paintIcon(this, g, snakeX[i], snakeY[i]);
        }
        //游戏是否开始
        if (!isStart) {
            //画一个文字, 提示用户按下空格键开始游戏
            String message = "按下空格键开始游戏";
            setTips(g, Color.WHITE, MICROSOFT_YA_HEI, message);
        }
        //游戏失败
        if (isFail) {
            //画一个文字, 提示用户按下空格键重新开始游戏
            String message = "游戏失败, 按下空格键重新开始游戏";
            setTips(g, Color.RED, MICROSOFT_YA_HEI, message);
        }
    }

    //居中显示文字
    private void setTips(Graphics g, Color color, Font font, String message) {
        g.setColor(color);
        g.setFont(font);

        // 获取 FontMetrics 对象
        FontMetrics metrics = g.getFontMetrics();
        // 计算文本的宽度和高度
        int textWidth = metrics.stringWidth(message);
        int textHeight = metrics.getHeight();
        // 计算文本的起始坐标，使其居中
        int x = (this.getWidth() - textWidth) / 2;  // 水平居中
        int y = (this.getHeight() - textHeight) / 2 + textHeight; // 垂直居中

        // 绘制文本
        g.drawString(message, x, y);
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

    //当按键组合产生一个可以打印的字符时，调用 keyTyped() 方法
    @Override
    public void keyTyped(KeyEvent e) {
        int keyCode = e.getKeyCode();
        System.out.println("keyTyped keyCode = " + keyCode);
    }

    //当按下键盘上的任意键时，调用 keyPressed() 方法
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        System.out.println("keyPressed keyCode = " + keyCode);
        if (keyCode == KeyEvent.VK_SPACE) {
            //判断是否失败
            if (isFail) {
                //重新开始游戏
                isFail = false;
                //!重新初始化
                init();
            } else {
                //游戏是否开始
                isStart = !isStart;
            }
            //重新绘制界面
            repaint();
        }

        //键盘控制走向
        if (keyCode == KeyEvent.VK_UP) {
            direction = "U";
        } else if (keyCode == KeyEvent.VK_DOWN) {
            direction = "D";
        } else if (keyCode == KeyEvent.VK_LEFT) {
            direction = "L";
        } else if (keyCode == KeyEvent.VK_RIGHT) {
            direction = "R";
        }
    }

    //当释放键盘上的任意键时，调用 keyReleased() 方法
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        System.out.println("keyReleased keyCode = " + keyCode);
    }

    //事件监听, 监听定时器, 每100ms刷新一次
    @Override
    public void actionPerformed(ActionEvent e) {
        //判断游戏是否开始
        if (isStart && !isFail) {
            //吃食物, 如果蛇头的坐标和食物的坐标重合, 则吃掉食物, 这里加了长度之后, 后面的身体会自动加长
            if (snakeX[0] == foodX && snakeY[0] == foodY) {
                //蛇的长度加1
                length++;
                //分数加10
                score += 10;
                //重新生成食物
                foodX = 25 + 25 * RANDOM.nextInt(34);
                foodY = 95 + 25 * RANDOM.nextInt(24);
            }
            //移动, 有几节身体就移动几次, 后面的身体移动到前面的身体的位置
            for (int i = length - 1; i > 0; i--) {
                snakeX[i] = snakeX[i - 1];
                snakeY[i] = snakeY[i - 1];
            }
            //头部移动
            switch (direction) {
                case "U":
                    snakeY[0] -= CELL_SIZE;
                    //向上移动, 如果蛇的头部超出边界, 则从另一边出来
                    if (snakeY[0] < 95) {
                        snakeY[0] = 95 + 600 - CELL_SIZE;
                    }
                    break;
                case "D":
                    snakeY[0] += CELL_SIZE;
                    //向下移动, 如果蛇的头部超出边界, 则从另一边出来
                    if (snakeY[0] > 95 + 600 - CELL_SIZE) {
                        snakeY[0] = 95;
                    }
                    break;
                case "L":
                    snakeX[0] -= CELL_SIZE;
                    //向左移动, 如果蛇的头部超出边界, 则从另一边出来
                    if (snakeX[0] < 25) {
                        snakeX[0] = 850;
                    }
                    break;
                case "R":
                    snakeX[0] += CELL_SIZE;
                    //向右移动, 如果蛇的头部超出边界, 则从另一边出来
                    if (snakeX[0] > 850) {
                        snakeX[0] = 25;
                    }
                    break;
            }
            //判断是否失败, 如果蛇头和身体重合, 则失败
            for (int i = 1; i < length; i++) {
                if (snakeX[0] == snakeX[i] && snakeY[0] == snakeY[i]) {
                    isFail = true;
                }
            }
            //重新绘制界面
            repaint();
        }
    }
}
