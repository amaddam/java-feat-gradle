package com.self;

public class Base {
    void method()
    {
        System.out.println("Base method");
    }

    static void staticMethod()
    {
        System.out.println("Base static method");
    }

    public class Sub extends Base {
        @Override
        void method() {
            System.out.println("Sub method");
        }

        static void staticMethod() {
            System.out.println("Sub static method");
        }
    }

    public static void main(String[] args) {
        Base base = new Base().new Sub();
        base.method(); // 输出 "Sub method"
        Base.staticMethod(); // 输出 "Base static method"
        // static 方法是与类绑定的, 而不是与实例绑定的
    }


}
