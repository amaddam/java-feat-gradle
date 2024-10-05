package com.self.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/10
 * @description :
 */
public class DialogDemo {
    public static void main(String[] args) {
        new DialogFrame("Dialog Frame");
    }
    static class DialogFrame extends JFrame {
        public DialogFrame(String title) {
            super(title);

            //JFrame 放东西, 需要ContentPane
            Container contentPane = this.getContentPane();
            //设置绝对布局
            contentPane.setLayout(null);
            //创建一个按钮, 用于打开对话框
            JButton jButton = new JButton("Open Dialog");
            jButton.setBounds(30, 30, 200, 50);
            //创建监听事件, 点击按钮打开对话框
            jButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new MyDialog();
                }
            });
            //创建一个对话框
            contentPane.add(jButton);
            this.setSize(700, 500);
            this.setVisible(true);
            this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        }
        //弹窗窗口
        class MyDialog extends JDialog {
            public MyDialog() {
                this.setVisible(true);
                this.setBounds(100, 100, 500, 500);
                //弹窗默认EXIT_ON_CLOSE, 不需要设置, 设置EXIT_ON_CLOSE后控制台会报错
                // this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

                Container contentPane = this.getContentPane();
                //设置绝对布局
                contentPane.setLayout(null);
                contentPane.add(new JLabel("This is a dialog"));
            }
        }
    }
}
