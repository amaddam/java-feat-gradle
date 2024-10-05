package com.self.recordpojo;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2024/6/20
 * @description :
 */
public record RecordVO(String name, Integer age) {
    //构造函数, 如果record中有校验逻辑, 可以在构造函数中添加校验逻辑, 且默认其实有一个全参构造函数
    public RecordVO(String name, Integer age) {
        if (age < 0) {
            //
            this.age = age++;
            throw new IllegalArgumentException("年龄不能小于0");
        }

        //下面两个赋值的语句也可以省略, 因为record会自动帮我们生成这两个字段
        this.name = name;
        this.age = age;
    }
    public RecordVO(String name) {
        this(name, 0);
    }

    //record中可以添加实例方法
    public void print() {
        System.out.println("name: " + name + ", age: " + age);
    }

    //record中可以添加静态字段
    public static String staticField = "static field";
    public static String staticField2;

    //record中可以添加静态方法
    public static void printStatic() {
        System.out.println("I am static method, this is static field: " + staticField);
    }

    //record中可以添加静态代码块
    static {
        staticField2 = "static field2";
        System.out.println("I am static block, this is static field2: " + staticField2);
    }



    //record中不可以添加实例字段和实例代码块


    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        String json = "{\"name\":\"lrns1\",\"age\":18}";
        try {
            //即使record中没有无参构造函数和getter setter方法, 也可以使用ObjectMapper来反序列化
            RecordVO recordVO = objectMapper.readValue(json, RecordVO.class);
            recordVO.print();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String json2 = "{\"innerField\":\"innerField\"}";
        try {
            // 但是类不会就不会被序列化, 需要添加可见性, 如果不添加可见性, 就需要添加getter方法, 否则会报错, 上面已添加getter方法
            // objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            InnerClass innerClass = objectMapper.readValue(json2, InnerClass.class);
            innerClass.print();

            String innerClassJson = objectMapper.writeValueAsString(innerClass);
            System.out.println(innerClassJson);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //record中可以添加内部类
    static class InnerClass {
        private String innerField;

        public String getInnerField() {
            return innerField;
        }

        public void print() {
            System.out.println("I am inner class and this is inner field: " + innerField);
        }
    }
}
