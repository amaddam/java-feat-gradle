
package com.self.io.iotest.server.bio;


import com.self.io.iotest.common.NetUtil;
import com.self.io.iotest.common.SimConfig;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;

/**
 * BIO + 线程池：accept 线程不断接入，但连接处理（阻塞 read/write）交给固定线程池。
 *
 * 观察点：
 * - 线程数可控；
 * - 连接数大于线程数时，任务会排队，部分连接会迟迟得不到处理（端到端延迟增大）。
 */
public class BioThreadPoolServer {

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        int threads = args.length > 1 ? Integer.parseInt(args[1]) : 64;

        ExecutorService pool = new ThreadPoolExecutor(
                threads, threads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                r -> Thread.ofPlatform().name("bio-pool-worker").unstarted(r)
        );

        Runtime.getRuntime().addShutdownHook(new Thread(pool::shutdownNow));

        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("[BIO+POOL] server listening on " + port + ", poolThreads=" + threads);

            while (true) {
                Socket s = ss.accept();
                System.out.println("[BIO+POOL] accepted: " + s.getRemoteSocketAddress());
                pool.execute(() -> handle(s));
            }
        }
    }

    private static void handle(Socket s) {
        try (s;
             BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream(), SimConfig.CHARSET));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), SimConfig.CHARSET))) {

            String line;
            while ((line = br.readLine()) != null) {
                NetUtil.sleepSilently(SimConfig.READ_DELAY_MS);
                String resp = "echo:" + line + "\n";
                NetUtil.sleepSilently(SimConfig.WRITE_DELAY_MS);
                bw.write(resp);
                bw.flush();
            }
        } catch (IOException e) {
            // ignore
        }
    }
}
