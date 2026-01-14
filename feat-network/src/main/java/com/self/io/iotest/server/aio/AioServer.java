
package com.self.io.iotest.server.aio;

import com.self.io.iotest.common.NetUtil;
import com.self.io.iotest.common.SimConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.*;

/**
 * AIO：AsynchronousServerSocketChannel / AsynchronousSocketChannel + CompletionHandler。
 *
 * 注意：
 * - CompletionHandler 回调线程不应该做耗时工作（sleep/业务/DB），否则会阻塞回调线程；
 * - 因此这里显式使用业务线程池 workers 来承接“读/写耗时模拟”，再异步写回。
 */
public class AioServer {

    static final class Conn {
        final AsynchronousSocketChannel ch;
        final ByteBuffer in = ByteBuffer.allocateDirect(8 * 1024);
        final ByteArrayOutputStream lineBytes = new ByteArrayOutputStream(256);

        Conn(AsynchronousSocketChannel ch) { this.ch = ch; }
    }

    private final ExecutorService workers;
    private final AsynchronousChannelGroup ioGroup;

    public AioServer(int ioThreads, int workerThreads) throws IOException {
        this.workers = new ThreadPoolExecutor(
                workerThreads, workerThreads, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                r -> Thread.ofPlatform().name("aio-worker").unstarted(r)
        );
        this.ioGroup = AsynchronousChannelGroup.withFixedThreadPool(
                ioThreads,
                r -> Thread.ofPlatform().name("aio-io").unstarted(r)
        );
    }

    public void shutdown() {
        workers.shutdownNow();
        try { ioGroup.shutdownNow(); } catch (IOException ignored) {}
    }

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 8080;
        int ioThreads = args.length > 1 ? Integer.parseInt(args[1]) : 16;
        int workerThreads = args.length > 2 ? Integer.parseInt(args[2]) : 64;

        AioServer app = new AioServer(ioThreads, workerThreads);
        Runtime.getRuntime().addShutdownHook(new Thread(app::shutdown));

        try (AsynchronousServerSocketChannel server =
                     AsynchronousServerSocketChannel.open(app.ioGroup).bind(new InetSocketAddress(port))) {

            System.out.println("[AIO] server listening on " + port + ", ioThreads=" + ioThreads + ", workerThreads=" + workerThreads);

            server.accept(null, new CompletionHandler<>() {
                @Override
                public void completed(AsynchronousSocketChannel ch, Object att) {
                    server.accept(null, this); // 继续 accept

                    try {
                        System.out.println("[AIO] accepted: " + ch.getRemoteAddress());
                    } catch (IOException ignored) {}

                    Conn conn = new Conn(ch);
                    app.readLoop(conn);
                }

                @Override
                public void failed(Throwable exc, Object att) {
                    server.accept(null, this);
                }
            });

            Thread.currentThread().join(); // 挂起主线程
        }
    }

    private void readLoop(Conn conn) {
        conn.in.clear();
        conn.ch.read(conn.in, conn, new CompletionHandler<>() {
            @Override
            public void completed(Integer n, Conn c) {
                if (n == -1) {
                    close(c);
                    return;
                }

                c.in.flip();
                while (c.in.hasRemaining()) {
                    byte b = c.in.get();
                    if (b == (byte) '\n') {
                        byte[] lineRaw = c.lineBytes.toByteArray();
                        c.lineBytes.reset();

                        String line = new String(lineRaw, SimConfig.CHARSET).replace("\r", "");
                        workers.execute(() -> processAndWrite(c, line));
                    } else {
                        c.lineBytes.write(b);
                    }
                }

                readLoop(c); // 继续读
            }

            @Override
            public void failed(Throwable exc, Conn c) {
                close(c);
            }
        });
    }

    private void processAndWrite(Conn conn, String line) {
        NetUtil.sleepSilently(SimConfig.READ_DELAY_MS);
        String resp = "echo:" + line + "\n";
        NetUtil.sleepSilently(SimConfig.WRITE_DELAY_MS);

        ByteBuffer out = ByteBuffer.wrap(resp.getBytes(SimConfig.CHARSET));
        writeAll(conn, out);
    }

    private void writeAll(Conn conn, ByteBuffer out) {
        conn.ch.write(out, out, new CompletionHandler<>() {
            @Override
            public void completed(Integer n, ByteBuffer buf) {
                if (n == -1) {
                    close(conn);
                    return;
                }
                if (buf.hasRemaining()) {
                    conn.ch.write(buf, buf, this); // partial write：继续写完
                }
            }

            @Override
            public void failed(Throwable exc, ByteBuffer buf) {
                close(conn);
            }
        });
    }

    private void close(Conn conn) {
        try {
            System.out.println("[AIO] closed: " + NetUtil.safeAddr(conn.ch.getRemoteAddress()));
        } catch (IOException ignored) {}
        NetUtil.closeQuietly(conn.ch);
    }
}
