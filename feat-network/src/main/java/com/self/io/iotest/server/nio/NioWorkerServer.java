
package com.self.io.iotest.server.nio;

import com.self.io.iotest.common.NetUtil;
import com.self.io.iotest.common.SimConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.concurrent.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * NIO + Worker：Reactor 线程只负责 IO/解析/派发；耗时处理（READ/WRITE 的 sleep）放到 worker 线程池。
 */
public class NioWorkerServer {

    private static final int IN_BUF_SIZE = 8 * 1024;

    static final class Conn {
        final SocketChannel ch;
        final SelectionKey key;
        final ByteBuffer in = ByteBuffer.allocateDirect(IN_BUF_SIZE);

        final ByteArrayOutputStream lineBytes = new ByteArrayOutputStream(256);
        final ConcurrentLinkedQueue<ByteBuffer> outQ = new ConcurrentLinkedQueue<>();

        Conn(SocketChannel ch, SelectionKey key) {
            this.ch = ch;
            this.key = key;
        }
    }

    private final Selector selector;
    private final ExecutorService workers;

    public NioWorkerServer(Selector selector, int workerThreads) {
        this.selector = selector;
        this.workers = new ThreadPoolExecutor(
                workerThreads, workerThreads,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                r -> Thread.ofPlatform().name("nio-worker").unstarted(r)
        );
    }

    public void shutdown() {
        workers.shutdownNow();
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        int workerThreads = args.length > 1 ? Integer.parseInt(args[1]) : 64;

        try (Selector selector = Selector.open();
             ServerSocketChannel server = ServerSocketChannel.open()) {

            server.configureBlocking(false);
            server.bind(new InetSocketAddress(port));
            server.register(selector, SelectionKey.OP_ACCEPT);

            NioWorkerServer app = new NioWorkerServer(selector, workerThreads);
            Runtime.getRuntime().addShutdownHook(new Thread(app::shutdown));

            System.out.println("[NIO+WORKER] server listening on " + port + ", workerThreads=" + workerThreads);

            while (true) {
                selector.select();

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    if (!key.isValid()) continue;

                    try {
                        if (key.isAcceptable()) {
                            app.onAccept(key);
                        }
                        if (key.isReadable()) {
                            app.onRead(key);
                        }
                        if (key.isWritable()) {
                            app.onWrite(key);
                        }
                    } catch (IOException e) {
                        app.closeKey(key);
                    }
                }
            }
        }
    }

    private void onAccept(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();

        for (;;) {
            SocketChannel ch = server.accept();
            if (ch == null) break;

            ch.configureBlocking(false);
            ch.socket().setTcpNoDelay(true);

            SelectionKey clientKey = ch.register(selector, SelectionKey.OP_READ);
            Conn conn = new Conn(ch, clientKey);
            clientKey.attach(conn);

            System.out.println("[NIO+WORKER] accepted: " + ch.getRemoteAddress());
        }
    }

    private void onRead(SelectionKey key) throws IOException {
        Conn conn = (Conn) key.attachment();
        SocketChannel ch = conn.ch;

        int n = ch.read(conn.in);
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
                workers.execute(() -> processLine(conn, line));
            } else {
                conn.lineBytes.write(b);
            }
        }
        conn.in.clear();
    }

    private void processLine(Conn conn, String line) {
        NetUtil.sleepSilently(SimConfig.READ_DELAY_MS);
        String resp = "echo:" + line + "\n";
        NetUtil.sleepSilently(SimConfig.WRITE_DELAY_MS);

        conn.outQ.add(ByteBuffer.wrap(resp.getBytes(SimConfig.CHARSET)));

        try {
            if (conn.key.isValid()) {
                conn.key.interestOps(conn.key.interestOps() | SelectionKey.OP_WRITE);
                selector.wakeup();
            }
        } catch (CancelledKeyException ignored) {
        }
    }

    private void onWrite(SelectionKey key) throws IOException {
        Conn conn = (Conn) key.attachment();
        SocketChannel ch = conn.ch;

        while (true) {
            ByteBuffer buf = conn.outQ.peek();
            if (buf == null) break;

            ch.write(buf);
            if (buf.hasRemaining()) break;
            conn.outQ.poll();
        }

        if (conn.outQ.isEmpty()) {
            key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
        }
    }

    private void closeKey(SelectionKey key) {
        try {
            Object att = key.attachment();
            if (att instanceof Conn c) {
                System.out.println("[NIO+WORKER] closed: " + NetUtil.safeAddr(c.ch.getRemoteAddress()));
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
