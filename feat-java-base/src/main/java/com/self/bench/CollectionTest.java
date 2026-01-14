package com.self.bench;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollectionTest {
    static int[] numArray = {1,2,3,4,5,6,7,8,9,10};
    static List<Integer> numList = new ArrayList<>(Arrays.stream(numArray).boxed().toList());
    static Set<Integer> numSet = new HashSet<>(Arrays.stream(numArray).boxed().toList());

    static String[] strArray = {"a","b","c","d","e","f","g","h","i","j"};
    static List<String> strList = new ArrayList<>(Arrays.stream(strArray).toList());
    static Set<String> strSet = new HashSet<>(Arrays.stream(strArray).toList());

    // public static void main(String[] args) {
    //     // 测试10个元素的list和set的查找速度差异
    //     // asl
    //     int targetNum = 1;
    //     long numArrayStart = System.nanoTime();
    //     int numArrayResult = numArraySearch(targetNum);
    //     long numArrayEnd = System.nanoTime();
    //     long numListStart = System.nanoTime();
    //     Integer numListResult = numListSearch(targetNum);
    //     long numListEnd = System.nanoTime();
    //     long numSetStart = System.nanoTime();
    //     Integer numSetResult = numSetSearch(targetNum);
    //     long numSetEnd = System.nanoTime();
    //     System.out.println("Num Array Search Result: " + numArrayResult + ", Time: " + (numArrayEnd - numArrayStart) + " ns");
    //     System.out.println("Num List Search Result: " + numListResult + ", Time: " + (numListEnd - numListStart) + " ns");
    //     System.out.println("Num Set Search Result: " + numSetResult + ", Time: " + (numSetEnd - numSetStart) + " ns");
    //     StringBuilder numDiff = new StringBuilder(calculateDifference((int)(numArrayEnd - numArrayStart), (int)(numListEnd - numListStart), (int)(numSetEnd - numSetStart)));
    //     System.out.println("Num Search Time Difference: " + numDiff);
    //
    //     // sla
    //     String targetStr = "a";
    //     long strArrayStart = System.nanoTime();
    //     String strArrayResult = strArraySearch(targetStr);
    //     long strArrayEnd = System.nanoTime();
    //     long strListStart = System.nanoTime();
    //     String strListResult = strListSearch(targetStr);
    //     long strListEnd = System.nanoTime();
    //     long strSetStart = System.nanoTime();
    //     String strSetResult = strSetSearch(targetStr);
    //     long strSetEnd = System.nanoTime();
    //     System.out.println("Str Array Search Result: " + strArrayResult + ", Time: " + (strArrayEnd - strArrayStart) + " ns");
    //     System.out.println("Str List Search Result: " + strListResult + ", Time: " + (strListEnd - strListStart) + " ns");
    //     System.out.println("Str Set Search Result: " + strSetResult + ", Time: " + (strSetEnd - strSetStart) + " ns");
    //     StringBuilder strDiff = new StringBuilder(calculateDifference((int)(strArrayEnd - strArrayStart), (int)(strListEnd - strListStart), (int)(strSetEnd - strSetStart)));
    //     System.out.println("Str Search Time Difference: " + strDiff);
    // }
    public static void main(String[] args) {
        int target = 1;

        // ==========================================
        // 1. 预热阶段 (Warmup) - 让 JVM 编译代码
        // ==========================================
        System.out.println("正在预热 JVM (跑 50,000 次)...");
        for (int i = 0; i < 500_000; i++) {
            numArraySearch(target);
            numListSearch(target);
            numSetSearch(target);
        }
        System.out.println("预热完成！开始正式测量...");
        System.out.println("--------------------------------");

        // ==========================================
        // 2. 测量阶段 (Measurement) - 跑多次取平均值
        // ==========================================
        int iterations = 10_000; // 测量 1万次

        // --- 测 Array ---
        long start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            numArraySearch(target);
        }
        long end = System.nanoTime();
        System.out.println("Array Avg Time: " + (end - start) / iterations + " ns");

        // --- 测 List ---
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            numListSearch(target);
        }
        end = System.nanoTime();
        System.out.println("List  Avg Time: " + (end - start) / iterations + " ns");

        // --- 测 Set ---
        start = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            numSetSearch(target);
        }
        end = System.nanoTime();
        System.out.println("Set   Avg Time: " + (end - start) / iterations + " ns");
    }

    public static int numArraySearch(int target) {
        for (int num : numArray) {
            if (num == target) {
                return num;
            }
        }
        return -1;
    }

    public static Integer numListSearch(int target) {
        for (Integer num : numList) {
            if (num == target) {
                return num;
            }
        }
        return -1;
    }

    public static Integer numSetSearch(int target) {
        if (numSet.contains(target)) {
            return target;
        }
        return -1;
    }

    public static String strArraySearch(String target) {
        for (String str : strArray) {
            if (str.equals(target)) {
                return str;
            }
        }
        return null;
    }

    public static String strListSearch(String target) {
        for (String str : strList) {
            if (str.equals(target)) {
                return str;
            }
        }
        return null;
    }

    public static String strSetSearch(String target) {
        if (strSet.contains(target)) {
            return target;
        }
        return null;
    }

    // 计算差异
    public static String calculateDifference(int arrayTime, int listTime, int setTime) {
        String[] names = {"arrayTime", "listTime", "setTime"};
        int[] times = {arrayTime, listTime, setTime};
        Integer[] idx = new Integer[] {0, 1, 2};

        Arrays.sort(idx, Comparator.comparingInt(i -> times[i]));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < idx.length; i++) {
            int j = idx[i];
            sb.append(names[j]).append("(").append(times[j]).append("ns)");
            if (i < idx.length - 1) {
                int next = idx[i + 1];
                if (times[j] == times[next]) {
                    sb.append(" = ");
                } else {
                    sb.append(" < ");
                }
            }
        }
        return sb.toString();
    }


}
