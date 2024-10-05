package com.self.awt;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/2
 * @description :
 * 整个程序的思路:
 * 1. 主程序入口 (PaintPoint类)：
 * 创建一个MouseFrame对象，设置窗口标题并显示窗口。
 *
 * 2. 窗口类 (MouseFrame类)：
 * 继承自Frame类，设置窗口的基本属性（位置和大小）并显示。
 * 创建一个ArrayList用于存储Point对象，即待绘制的点。
 * 添加鼠标点击事件监听器MyMouseAdapter，用于响应鼠标点击事件。
 *
 * 3. 绘制方法 (paint方法)：
 * 当系统需要绘制窗口内容时调用。
 * 迭代points集合中的每个Point对象，并使用Graphics对象的方法（fillOval）在指定位置绘制圆点。
 *
 * 4. 鼠标事件监听器 (MyMouseAdapter类)：
 * 继承自MouseAdapter，重写mousePressed方法。
 * 在鼠标点击事件发生时获取鼠标点击的坐标，并创建一个新的Point对象。
 * 将这个Point对象添加到MouseFrame类的points集合中。
 * 调用repaint()方法，通知系统重新绘制窗口以更新显示。
 *
 * 5. 重绘 (repaint方法)：
 * 在mousePressed调用repaint()方法会触发系统重新调用paint方法，以便更新窗口的显示内容。
 */
public class PaintPoint {
    public static void main(String[] args) {
        new MouseFrame("画点");
    }

    static class MouseFrame extends Frame {
        //属性, 画点需要的属性
        ArrayList<Point> points = new ArrayList<>();
        public MouseFrame(String title) {
            super(title);
            setBounds(300, 300, 400, 300);
            setVisible(true);
            //监听鼠标点击
            addMouseListener(new MyMouseAdapter());
        }

        @Override
        public void paint(Graphics g) {
            //画画, 监听鼠标点击的位置
            Iterator<Point> iterator = points.iterator();
            while (iterator.hasNext()) {
                Point next = iterator.next();
                //画点, 10*10的点
                g.fillOval(next.x, next.y, 10, 10);
            }
        }

        //添加一个点到集合中
        public void addPoint(Point point) {
            points.add(point);
        }
    }

    static class MyMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            //获取鼠标点击的位置
            int x = e.getX();
            int y = e.getY();
            //将这个点加入到集合中
            MouseFrame frame = (MouseFrame) e.getSource();
            frame.addPoint(new Point(x, y));
            //需要重绘, 因为通过获取鼠标点击的位置创建了一个新的Point对象，并将它添加到了points集合中。
            //但是，这个操作并不会立即反映在屏幕上，因为绘图是在paint方法中执行的，而不是在添加点的方法中。
            //调用repaint()方法。这个方法会告诉系统，当前窗口需要重新绘制，从而导致系统调用paint方法
            frame.repaint();
        }
    }
}
