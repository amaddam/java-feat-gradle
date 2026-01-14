
package com.self.io.iotest.client;

import com.self.io.iotest.common.SimConfig;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 纯 JDK 客户端压测：
 * - connections: 并发连接数
 * - requestsPerConn: 每个连接请求数（按行发送）
 *
 * 示例：
 *   mvn -q exec:java -Dexec.mainClass=com.example.iomodels.client.LoadClient -Dexec.args="127.0.0.1 8080 50 10"
 */
public class LoadClient {

    public static void main(String[] args) throws Exception {
        // if (args.length < 4) {
        //     System.out.println("Usage: <host> <port> <connections> <requestsPerConn>");
        //     return;
        // }

        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 8080;
        int connections = args.length > 2 ? Integer.parseInt(args[2]) : 50;
        int requestsPerConn = args.length > 3 ? Integer.parseInt(args[3]) : 10;

        System.out.println("[CLIENT] starting load test to " + host + ":" + port +
                ", connections=" + connections + ", requestsPerConn=" + requestsPerConn);

        CountDownLatch latch = new CountDownLatch(connections);
        List<Thread> threads = new ArrayList<>(connections);

        Instant start = Instant.now();

        for (int i = 0; i < connections; i++) {
            int idx = i;
            Thread t = Thread.ofPlatform().name("client-" + idx).unstarted(() -> {
                try {
                    runConn(host, port, idx, requestsPerConn);
                } catch (Exception e) {
                    // ignore
                } finally {
                    latch.countDown();
                }
            });
            threads.add(t);
            t.start();
        }

        latch.await();

        Duration cost = Duration.between(start, Instant.now());
        long total = (long) connections * requestsPerConn;
        double seconds = Math.max(0.001, cost.toNanos() / 1_000_000_000.0);
        double rps = total / seconds;

        System.out.printf("[CLIENT] done. connections=%d, requestsPerConn=%d, total=%d, cost=%d ms, throughput=%.2f req/s%n",
                connections, requestsPerConn, total, cost.toMillis(), rps);
    }

    private static void runConn(String host, int port, int idx, int requestsPerConn) throws Exception {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, port), 3000);
            s.setTcpNoDelay(true);

            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream(), SimConfig.CHARSET));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), SimConfig.CHARSET));

            // 兼容：有的服务端会先发欢迎语；如果没有也不影响
            s.setSoTimeout(200);
            try { br.readLine(); } catch (Exception ignored) {}
            s.setSoTimeout(0);

            for (int i = 0; i < requestsPerConn; i++) {
                String req = "c" + idx + "-msg" + i;
                bw.write(req);
                bw.write("\n");
                bw.flush();

                String resp = br.readLine();
                if (resp == null) return;
            }
        }
    }
}
