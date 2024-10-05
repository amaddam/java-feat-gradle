package com.self.awt;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/7/14
 * @description :
 */
public class CalculatorTest {
    public static void main(String[] args) {
        new AdditionCalculator().loadFrame();
    }

    //加法计算器类
    static class AdditionCalculator extends Frame {
        //属性, 将变量抽取出来让监听器类可以访问
        private TextField firstPlusNumber;
        private TextField secondPlusNumber;
        private TextField resultNumber;

        //方法
        public void loadFrame() {
            //三个文本框
            firstPlusNumber = new TextField(10);
            secondPlusNumber = new TextField(10);
            resultNumber = new TextField(20);

            //一个按钮
            Button equalButton = new Button("=");

            //一个标签
            Label plusLabel = new Label("+");

            //设置布局
            setLayout(new FlowLayout());
            add(firstPlusNumber);
            add(plusLabel);
            add(secondPlusNumber);
            add(equalButton);
            add(resultNumber);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    System.exit(0);
                }
            });
            pack();
            setVisible(true);

            //设置按钮监听
            // equalButton.addActionListener(new AdditionButtonListener(this));//外部类形式
            equalButton.addActionListener(new AdditionButtonListener());//内部类形式
        }

        //监听器类设置为内部类, 可以方便访问外部类的属性
        class AdditionButtonListener implements ActionListener {
            // AdditionCalculator additionCalculator;


            // public AdditionButtonListener(AdditionCalculator additionCalculator) {
            //     this.additionCalculator = additionCalculator;
            // }

            @Override
            public void actionPerformed(ActionEvent e) {
                int firstPlusNumberInteger = Integer.parseInt(firstPlusNumber.getText());
                int secondPlusNumberInteger = Integer.parseInt(secondPlusNumber.getText());
                resultNumber.setText(firstPlusNumberInteger + secondPlusNumberInteger + "");
                //清除输入框
                firstPlusNumber.setText("");
                secondPlusNumber.setText("");
            }
        }
    }

    //    //监听器类, 外部类形式, 需要传入外部类对象
    //     static class AdditionButtonListener implements ActionListener {
    //         AdditionCalculator additionCalculator;
    //
    //
    //         public AdditionButtonListener(AdditionCalculator additionCalculator) {
    //             this.additionCalculator = additionCalculator;
    //         }
    //
    //         @Override
    //         public void actionPerformed(ActionEvent e) {
    //             int firstPlusNumber = Integer.parseInt(additionCalculator.firstPlusNumber.getText());
    //             int secondPlusNumber = Integer.parseInt(additionCalculator.secondPlusNumber.getText());
    //             additionCalculator.resultNumber.setText(firstPlusNumber + secondPlusNumber + "");
    //             //清除输入框
    //             additionCalculator.firstPlusNumber.setText("");
    //             additionCalculator.secondPlusNumber.setText("");
    //         }
    //     }
}
