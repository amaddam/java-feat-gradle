package com.self.bench;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MapBenchmarkRefined {

    // ⚡️ 数据量加大到 1000 万，让多线程跑开
    private static final int DATA_SIZE = 10_000_000;
    // 使用你的 20 核
    private static final int THREAD_COUNT = 20;
    
    // 全局线程池，排除启动干扰
    private static final ExecutorService GLOBAL_EXECUTOR = Executors.newFixedThreadPool(THREAD_COUNT/2);

    public static void main(String[] args) throws InterruptedException {
        System.out.println("🔥 正在预热 JVM (JIT Warmup)... 请耐心等待...");
        // 预热：跑小一点的数据，让代码编译
        runTest(100_000, false); 
        System.out.println("✅ 预热完成，开始正式压测！");
        System.out.println("=============================================");

        // 正式测试
        runTest(DATA_SIZE, true);

        GLOBAL_EXECUTOR.shutdown();
    }

    private static void runTest(int size, boolean printLog) throws InterruptedException {
        // 计算精准容量，避免中途扩容带来的性能抖动
        // 公式：需要存储的数量 / 负载因子(0.75) + 1
        int capacity = (int) (size / 0.75 + 1);

        if (printLog) printHeader();

        // 1. HashMap 单线程
        if (printLog) {
            Map<String, Integer> map = new HashMap<>(capacity);
            runBenchmark("HashMap", "Single", 1, map, size, false);
        }

        // 2. CHM 单线程
        if (printLog) {
            Map<String, Integer> map = new ConcurrentHashMap<>(capacity);
            runBenchmark("ConcurrentHashMap", "Single", 1, map, size, false);
        }

        // 3. CHM 多线程 (使用全局线程池)
        if (printLog) {
            Map<String, Integer> map = new ConcurrentHashMap<>(capacity);
            runBenchmark("ConcurrentHashMap", "Multi-" + THREAD_COUNT, THREAD_COUNT, map, size, true);
        }
        
        if (printLog) printFooter();
    }

    private static void runBenchmark(String type, String mode, int threads, Map<String, Integer> map, int size, boolean usePool) throws InterruptedException {
        // Write
        long wStart = System.nanoTime();
        if (usePool) {
            Map<String, Integer> finalMap = map;
            runMultiThreadTask(threads, size, (idx) -> finalMap.put("k:" + idx, idx));
        }
        else for (int i = 0; i < size; i++) map.put("k:" + i, i);
        long wTime = (System.nanoTime() - wStart) / 1_000_000;

        // Read
        long rStart = System.nanoTime();
        if (usePool) {
            Map<String, Integer> finalMap1 = map;
            runMultiThreadTask(threads, size, (idx) -> finalMap1.get("k:" + idx));
        }
        else for (int i = 0; i < size; i++) map.get("k:" + i);
        long rTime = (System.nanoTime() - rStart) / 1_000_000;

        // Print immediately
        printRow(type, mode, wTime, rTime, size);
        
        // Help GC
        map = null;
        System.gc();
        Thread.sleep(1000); 
    }

    private static void runMultiThreadTask(int threads, int totalSize, Task task) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(threads);
        int chunkSize = totalSize / threads;

        for (int i = 0; i < threads; i++) {
            int finalI = i;
            GLOBAL_EXECUTOR.submit(() -> {
                try {
                    int start = finalI * chunkSize;
                    int end = (finalI == threads - 1) ? totalSize : start + chunkSize;
                    for (int j = start; j < end; j++) {
                        task.execute(j);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
    }

    @FunctionalInterface
    interface Task {
        void execute(int index);
    }

    // --- 打印辅助 ---
    private static void printHeader() {
        System.out.printf("| %-18s | %-10s | %-18s | %-18s |%n", "Type", "Mode", "✍️ Put (ms/QPS)", "📖 Get (ms/QPS)");
        System.out.println("-----------------------------------------------------------------------------");
    }

    private static void printRow(String type, String mode, long wMs, long rMs, int size) {
        if(wMs == 0) wMs = 1; 
        if(rMs == 0) rMs = 1;
        double wQps = (size * 1000.0 / wMs) / 1_000_000;
        double rQps = (size * 1000.0 / rMs) / 1_000_000;
        
        System.out.printf("| %-18s | %-10s | %6dms (%4.1fM) | %6dms (%4.1fM) |%n", 
            type, mode, wMs, wQps, rMs, rQps);
    }
    
    private static void printFooter() {
        System.out.println("-----------------------------------------------------------------------------");
    }
}