
package com.self.io.iotest.server.bio;

import com.self.io.iotest.common.NetUtil;
import com.self.io.iotest.common.SimConfig;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * BIO：一连接一线程（阻塞 read/write）。
 *
 * 观察点：
 * - 连接数上来后线程数飙升；
 * - 每个连接都占用一个线程去阻塞等待 read。
 */
public class BioPerConnectionServer {

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;

        try (ServerSocket ss = new ServerSocket(port)) {
            System.out.println("[BIO] per-connection server listening on " + port);

            while (true) {
                Socket s = ss.accept(); // 阻塞
                System.out.println("[BIO] accepted: " + s.getRemoteSocketAddress());

                Thread t = Thread.ofPlatform().name("bio-conn-" + s.getPort()).unstarted(() -> handle(s));
                t.start();
            }
        }
    }

    private static void handle(Socket s) {
        try (s;
             BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream(), SimConfig.CHARSET));
             BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), SimConfig.CHARSET))) {

            String line;
            while ((line = br.readLine()) != null) { // 阻塞直到收到 \n 或对端关闭
                NetUtil.sleepSilently(SimConfig.READ_DELAY_MS);     // 模拟“读耗时”
                String resp = "echo:" + line + "\n";
                NetUtil.sleepSilently(SimConfig.WRITE_DELAY_MS);    // 模拟“写耗时”
                bw.write(resp);
                bw.flush();
            }
        } catch (IOException e) {
            // ignore
        }
    }
}
