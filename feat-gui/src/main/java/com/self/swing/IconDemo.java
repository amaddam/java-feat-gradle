package com.self.swing;

import javax.swing.*;
import java.awt.*;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/10
 * @description :
 */
public class IconDemo extends JFrame implements Icon {
    private int width;
    private int height;

    public static void main(String[] args) {
        new IconDemo(15, 15).init();
    }

    public void init() {
        IconDemo iconDemo = new IconDemo(15, 15);

        //图标放在标签上, 也可以放在按钮上
        JLabel jLabel = new JLabel("IconDemo", iconDemo, SwingConstants.CENTER);
        Container contentPane = this.getContentPane();
        contentPane.add(jLabel);
        this.setSize(700, 500);
        this.setVisible(true);
    }
    public IconDemo(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        g.fillOval(x, y, width, height);
    }

    @Override
    public int getIconWidth() {
        return this.width;
    }

    @Override
    public int getIconHeight() {
        return this.height;
    }
}
