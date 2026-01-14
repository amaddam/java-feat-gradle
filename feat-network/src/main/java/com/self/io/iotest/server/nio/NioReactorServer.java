
package com.self.io.iotest.server.nio;

import com.self.io.iotest.common.NetUtil;
import com.self.io.iotest.common.SimConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;

/**
 * NIO：单线程 Reactor（Selector）。
 *
 * 重要说明：
 * - 本实现“故意”把 READ/WRITE 的 sleep 放在 Reactor 线程里，用来展示误用 NIO 时的后果：
 *   某个连接的耗时处理会阻塞整个事件循环，导致所有连接延迟抖动。
 */
public class NioReactorServer {

    private static final int IN_BUF_SIZE = 8 * 1024;

    static final class Conn {
        final SocketChannel ch;
        final ByteBuffer in = ByteBuffer.allocateDirect(IN_BUF_SIZE);

        // 按行（\n）解析：累计原始字节，遇到 \n 才 decode
        final ByteArrayOutputStream lineBytes = new ByteArrayOutputStream(256);

        // 待写队列（处理 partial write）
        final Queue<ByteBuffer> outQ = new ArrayDeque<>();

        Conn(SocketChannel ch) { this.ch = ch; }
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;

        try (Selector selector = Selector.open();
             ServerSocketChannel server = ServerSocketChannel.open()) {

            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));
            server.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("[NIO] reactor server listening on " + port);

            while (true) {
                selector.select(); // 等待就绪事件（可能阻塞）

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (!key.isValid()) continue;

                    try {
                        if (key.isAcceptable()) {
                            onAccept(selector, key);
                        }
                        if (key.isReadable()) {
                            onRead(key);
                        }
                        if (key.isWritable()) {
                            onWrite(key);
                        }
                    } catch (IOException e) {
                        closeKey(key);
                    }
                }
            }
        }
    }

    private static void onAccept(Selector selector, SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();

        for (;;) {
            SocketChannel ch = server.accept(); // non-blocking，可能返回 null
            if (ch == null) break;

            ch.configureBlocking(false);
            ch.socket().setTcpNoDelay(true);

            Conn conn = new Conn(ch);
            ch.register(selector, SelectionKey.OP_READ, conn);

            System.out.println("[NIO] accepted: " + ch.getRemoteAddress());
        }
    }

    private static void onRead(SelectionKey key) throws IOException {
        Conn conn = (Conn) key.attachment();
        SocketChannel ch = conn.ch;

        int n = ch.read(conn.in); // non-blocking：没数据可能返回 0
        if (n == -1) {
            closeKey(key);
            return;
        }
        if (n == 0) return;

        conn.in.flip();
        while (conn.in.hasRemaining()) {
            byte b = conn.in.get();

            if (b == (byte) '\n') {
                byte[] lineRaw = conn.lineBytes.toByteArray();
                conn.lineBytes.reset();

                String line = new String(lineRaw, SimConfig.CHARSET).replace("\r", "");

                NetUtil.sleepSilently(SimConfig.READ_DELAY_MS);      // 模拟“读耗时”
                String resp = "echo:" + line + "\n";
                NetUtil.sleepSilently(SimConfig.WRITE_DELAY_MS);     // 模拟“写耗时”（阻塞 Reactor）

                conn.outQ.add(ByteBuffer.wrap(resp.getBytes(SimConfig.CHARSET)));
                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
            } else {
                conn.lineBytes.write(b);
            }
        }
        conn.in.clear();
    }

    private static void onWrite(SelectionKey key) throws IOException {
        Conn conn = (Conn) key.attachment();
        SocketChannel ch = conn.ch;

        while (true) {
            ByteBuffer buf = conn.outQ.peek();
            if (buf == null) break;

            ch.write(buf); // non-blocking：可能 partial write
            if (buf.hasRemaining()) {
                break;
            } else {
                conn.outQ.poll();
            }
        }

        if (conn.outQ.isEmpty()) {
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
        }
    }

    private static void closeKey(SelectionKey key) {
        try {
            Object att = key.attachment();
            if (att instanceof Conn c) {
                System.out.println("[NIO] closed: " + NetUtil.safeAddr(c.ch.getRemoteAddress()));
                NetUtil.closeQuietly(c.ch);
            } else {
                NetUtil.closeQuietly(key.channel());
            }
        } catch (IOException ignored) {
            NetUtil.closeQuietly(key.channel());
        } finally {
            key.cancel();
        }
    }
}
