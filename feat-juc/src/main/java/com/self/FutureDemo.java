package com.self;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class FutureDemo {
    static class MyThread_Runnable implements Runnable {
        public static void main(String[] args) {
            MyThread_Runnable myThread_runnable = new MyThread_Runnable();
            Thread thread = new Thread(myThread_runnable);
            thread.start();
        }
        @Override
        public void run() {
            System.out.println("MyThread is running...");
        }
    }

    // callable 有返回值 且能异步执行的方式, 是使用 FutureTask 来包装 Callable 对象, FutureTask 实现了 Runnable 接口, 使用了适配器模式
    static class MyThread_Callable implements Callable<String> {
        public static void main(String[] args) {
            MyThread_Callable myThread_callable = new MyThread_Callable();
            FutureTask<String> futureTask = new FutureTask<>(myThread_callable);
            Thread thread = new Thread(futureTask);
            thread.start();
            try {
                String result = futureTask.get();
                System.out.println("Result from Callable: " + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public String call() throws Exception {
            System.out.println("MyThread is running...");
            return "Callable Result";
        }
    }
}
