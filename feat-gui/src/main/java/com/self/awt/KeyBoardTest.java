package com.self.awt;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/10
 * @description :
 */
public class KeyBoardTest {
    public static void main(String[] args) {
        new KeyBoardFrame("KeyBoard Frame");
    }

    static class KeyBoardFrame extends Frame {
        public KeyBoardFrame(String title) {
            super(title);
            setBounds(300, 300, 400, 300);
            setVisible(true);
            this.addKeyListener(new KeyAdapter() {
                //键盘按下
                @Override
                public void keyPressed(KeyEvent e) {
                    int keyCode = e.getKeyCode();
                    //按下ESC退出, 不需要记住数字, 直接使用VK_XXX
                    if (keyCode == KeyEvent.VK_ESCAPE) {
                        System.exit(0);
                    }
                    System.out.println("Key Pressed: " + keyCode);
                }
            });
        }
    }
}
