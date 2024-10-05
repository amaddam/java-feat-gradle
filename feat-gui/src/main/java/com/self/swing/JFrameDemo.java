package com.self.swing;

import javax.swing.*;
import java.awt.*;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/10
 * @description :
 */
public class JFrameDemo {
    public void init() {
        JFrame jFrame = new JFrame("JFrame Demo");

        //这样设置背景色不生效, 需要设置ContentPane
        // jFrame.setBackground(Color.CYAN);
        Container contentPane = jFrame.getContentPane();
        contentPane.setBackground(Color.CYAN);

        //设置文字
        JLabel jLabel = new JLabel("Hello, Swing!", SwingConstants.CENTER);
        //也可以
        // jLabel.setHorizontalAlignment(SwingConstants.CENTER);
        jFrame.add(jLabel);

        //关闭事件
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //WindowConstants.DO_NOTHING_ON_CLOSE: 关闭窗口时不做任何操作
        //WindowConstants.HIDE_ON_CLOSE: 关闭窗口时隐藏窗口
        //WindowConstants.DISPOSE_ON_CLOSE: 当最后一个窗口关闭时退出程序
        //WindowConstants.EXIT_ON_CLOSE: 关闭窗口时退出程序
        jFrame.setBounds(300, 300, 300, 400);
        jFrame.setVisible(true);

    }
    public static void main(String[] args) {
        new JFrameDemo().init();
    }
}
