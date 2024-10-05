package com.self.awt;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/7/5
 * @description :
 */
public class MainForm {
    public static void main(String[] args) {
        Frame frame = new Frame("Main Form");
        frame.setSize(400, 300);
        frame.setLocation(300, 300);
        frame.setBackground(Color.BLUE);
        frame.setVisible(true);
        frame.setLayout(new GridLayout(2,1));

        //4个面板
        Panel panel1 = new Panel(new BorderLayout());
        Panel panel2 = new Panel(new GridLayout(2,1));
        Panel panel3 = new Panel(new BorderLayout());
        Panel panel4 = new Panel(new GridLayout(2,1));

        //上面
        panel1.add(new Button("west-1 button"), BorderLayout.WEST);
        panel1.add(new Button("east-1 button"), BorderLayout.EAST);
        panel2.add(new Button("center north button"));
        panel2.add(new Button("center south button"));
        panel1.add(panel2, BorderLayout.CENTER);

        //下面
        panel3.add(new Button("west-2 button"), BorderLayout.WEST);
        panel3.add(new Button("east-2 button"), BorderLayout.EAST);

        //下面的中间4个按钮
        for (int i = 0; i < 4; i++) {
            panel4.add(new Button("button-" + i));
        }
        panel3.add(panel4, BorderLayout.CENTER);

        frame.add(panel1);
        frame.add(panel3);

        //适配器
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

    }
}
