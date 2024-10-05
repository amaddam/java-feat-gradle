package com.self.swing;

import javax.swing.*;
import java.awt.*;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/8/13
 * @description :
 */
public class JListDemo extends JFrame {
    public static void main(String[] args) {
        new JListDemo();
    }

    public JListDemo() {


        //获取容器
        Container contentPane = this.getContentPane();

        //新建一个JList
        String[] strings = {"JList1", "JList2", "JList3"};
        JList<String> jList = new JList<>();
        jList.setListData(strings);

        contentPane.add(jList);

        this.setBounds(100, 100, 300, 350);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
