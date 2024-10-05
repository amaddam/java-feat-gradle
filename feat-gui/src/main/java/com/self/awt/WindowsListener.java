package com.self.awt;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/3
 * @description :
 */
public class WindowsListener {
    public static void main(String[] args) {
        new WindowFrame("Window Frame");
    }

    static class WindowFrame extends Frame {
        public WindowFrame(String title) {
            super(title);
            setBounds(300, 300, 400, 300);
            setVisible(true);
            addWindowListener(new WindowAdapter(){
                //窗口打开
                @Override
                public void windowOpened(WindowEvent e) {
                    System.out.println("Window Opened");
                }
                //窗口关闭
                @Override
                public void windowClosing(WindowEvent e) {
                    System.out.println("Window Closing");
                }
                //窗口已经关闭
                @Override
                public void windowClosed(WindowEvent e) {
                    System.out.println("Window Closed");
                }
                //窗口激活, 意思是窗口被选中
                @Override
                public void windowActivated(WindowEvent e) {
                    WindowFrame source = (WindowFrame)e.getSource();
                    source.setTitle("Activated");
                    System.out.println("Window Activated");
                }
                //窗口取消激活, 意思是窗口没有被选中
                @Override
                public void windowDeactivated(WindowEvent e) {
                    WindowFrame source = (WindowFrame)e.getSource();
                    source.setTitle("Deactivated");
                    System.out.println("Window Deactivated");
                }

                @Override
                public void windowGainedFocus(WindowEvent e) {
                    System.out.println("Window Gained Focus");
                }

                @Override
                public void windowLostFocus(WindowEvent e) {
                    System.out.println("Window Lost Focus");
                }
            });
        }
    }
}
