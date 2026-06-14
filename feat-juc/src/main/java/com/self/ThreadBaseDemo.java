package com.self;

public class ThreadBaseDemo {
    // 用户线程如果没有结束, 则JVM不会退出
    static class UserThreadDemo {
        public static void main(String[] args) {
            Thread t1 = new Thread(() -> {
                while (true) {
                    System.out.println("Thread is running...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            t1.start();
        }
    }

    // 只要用户线程结束, JVM就会退出, 不管守护线程是否结束
    static class DaemonThreadDemo {
        public static void main(String[] args) {
            Thread t1 = new Thread(() -> {
                while (true) {
                    System.out.println("Daemon Thread is running...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            // 设置为守护线程
            t1.setDaemon(true);
            t1.start();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Main thread is ending...");
        }
    }

    // 虚拟线程本身是守护线程, 所以JVM不会等待虚拟线程结束而退出
    static class VirtualThreadDemo {
        public static void main(String[] args) {
            Thread vt1 = Thread.ofVirtual().unstarted(() -> {
                while (true) {
                    System.out.println("Virtual Thread is running...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            vt1.start();

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Main thread is ending...");
        }
    }
}