package com.self;

public class SynchronizedSimpleDemo {
    Object object = new Object();
    public void method1(){
        synchronized (object){
            System.out.println("--- Synchronized Object");
        }
    }

    public synchronized void method2(){
        System.out.println("--- Synchronized Instance Method");
    }

    public static synchronized void method3(){
        System.out.println("--- Synchronized Static Method");
    }

    /**
     * 编译之后进入对应的编译结果文件夹, 使用javap -c SynchronizedSimpleDemo.class 查看字节码, 如果需要查看附加信息, 可以使用-v
     */
    public static void main(String[] args) {
        SynchronizedSimpleDemo demo = new SynchronizedSimpleDemo();
        demo.method1();
        demo.method2();
        SynchronizedSimpleDemo.method3();
    }
}
