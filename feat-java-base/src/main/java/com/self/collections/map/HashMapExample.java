package com.self.collections.map;

import java.util.Objects;

/**
 * @author : lrns1
 * @version : 1.0
 * @date : Created in 2025/4/1
 * @description :
 */
public class HashMapExample {
    public static void main(String[] args) {
        System.out.println("".hashCode());
        System.out.println("a".hashCode());
        System.out.println("123asdkasdjadkas".hashCode());
        System.out.println("1".hashCode());
    }


    static final int tableSizeFor(int cap) {
        int n = -1 >>> Integer.numberOfLeadingZeros(cap - 1);
        return (n < 0) ? 1 : (n >= 1 << 30) ? 1 << 30 : n + 1;
    }

    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }


    class User {
        private String name;
        private Integer age;

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            User user = (User) o;
            return Objects.equals(name, user.name) && Objects.equals(age, user.age);
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(name);
            result = 31 * result + Objects.hashCode(age);
            return result;
        }
    }

}
