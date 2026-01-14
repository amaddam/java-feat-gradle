package com.self.io.iotest;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Pure JDK IO-model demo.
 *
 * Protocol: UTF-8 text lines, each line ends with '\n'.
 * Simulated cost per request:
 *   - READ_COST_MS = 100
 *   - WRITE_COST_MS = 500
 */
public final class IOModelsLite {

    static final int READ_COST_MS = 100;
    static final int WRITE_COST_MS = 500;

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            usage();
            return;
        }
        String mode = args[0].toLowerCase(Locale.ROOT);
        switch (mode) {
            case "bio" -> runBio(parsePort(args, 1, 8080));
            case "bio-pool" -> runBioPool(parsePort(args, 1, 8080), parseInt(args, 2, 64));
            case "nio" -> runNio(parsePort(args, 1, 8080), false);
            case "nio-worker" -> runNio(parsePort(args, 1, 8080), true);
            case "aio" -> runAio(parsePort(args, 1, 8080), parseInt(args, 2, 16), parseInt(args, 3, 64));
            case "client" -> runClient(
                    parseStr(args, 1, "127.0.0.1"),
                    parsePort(args, 2, 8080),
                    parseInt(args, 3, 50),
                    parseInt(args, 4, 10)
            );
            default -> {
                usage();
                throw new IllegalArgumentException("unknown mode: " + mode);
            }
        }
    }

    static void usage() {
        System.out.println("Usage:");
        System.out.println("  java IoModelsLite bio <port>");
        System.out.println("  java IoModelsLite bio-pool <port> <poolSize>");
        System.out.println("  java IoModelsLite nio <port>");
        System.out.println("  java IoModelsLite nio-worker <port>");
        System.out.println("  java IoModelsLite aio <port> <ioThreads> <bizThreads>");
        System.out.println("  java IoModelsLite client <host> <port> <connections> <requestsPerConn>");
        System.out.println();
        System.out.println("Protocol: client sends text lines ending with \\n, server replies echo:<line>\\n");
        System.out.println("Simulated cost: read=100ms, write=500ms per request");
    }

    static int parsePort(String[] args, int idx, int dft) {
        return parseInt(args, idx, dft);
    }

    static int parseInt(String[] args, int idx, int dft) {
        if (args.length <= idx) return dft;
        return Integer.parseInt(args[idx]);
    }

    static String parseStr(String[] args, int idx, String dft) {
        if (args.length <= idx) return dft;
        return args[idx];
    }

    static void sleepMs(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // -------------------- BIO: thread-per-connection --------------------

    static void runBio(int port) throws IOException {
        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress("0.0.0.0", port));
            System.out.println("[bio] listening on " + port);
            while (true) {
                Socket s = server.accept();
                Thread t = new Thread(() -> handleBioConn(s), "bio-conn-" + s.getPort());
                t.start();
            }
        }
    }

    static void handleBioConn(Socket s) {
        try (s;
             BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8))) {

            System.out.println("[bio] accepted " + s.getRemoteSocketAddress() + " thread=" + Thread.currentThread().getName());
            String line;
            while ((line = in.readLine()) != null) {
                // simulate "read" cost (e.g. parse/deserialize)
                sleepMs(READ_COST_MS);

                String resp = "echo:" + line;

                // simulate "write" cost (e.g. encode/persist/downstream write)
                sleepMs(WRITE_COST_MS);

                out.write(resp);
                out.write("\n");
                out.flush();
            }
        } catch (IOException ignored) {
        }
    }

    // -------------------- BIO + fixed pool --------------------

    static void runBioPool(int port, int poolSize) throws IOException {
        ExecutorService pool = Executors.newFixedThreadPool(poolSize, r -> {
            Thread t = new Thread(r);
            t.setDaemon(false);
            t.setName("bio-worker-" + t.getId());
            return t;
        });

        try (ServerSocket server = new ServerSocket()) {
            server.bind(new InetSocketAddress("0.0.0.0", port));
            System.out.println("[bio-pool] listening on " + port + " poolSize=" + poolSize);
            while (true) {
                Socket s = server.accept();
                pool.submit(() -> handleBioConn(s));
            }
        } finally {
            pool.shutdownNow();
        }
    }

    // -------------------- NIO (Selector): synchronous non-blocking --------------------

    static final class NioConn {
        final SocketChannel ch;
        final ByteBuffer in = ByteBuffer.allocateDirect(8 * 1024);
        final Deque<ByteBuffer> outQ = new ArrayDeque<>();

        NioConn(SocketChannel ch) {
            this.ch = ch;
        }
    }

    static void runNio(int port, boolean offloadBiz) throws IOException {
        ExecutorService bizPool = offloadBiz
                ? Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()), r -> {
                    Thread t = new Thread(r);
                    t.setDaemon(false);
                    t.setName("nio-biz-" + t.getId());
                    return t;
                })
                : null;

        ConcurrentLinkedQueue<Runnable> pendingOnLoop = new ConcurrentLinkedQueue<>();

        try (Selector selector = Selector.open();
             ServerSocketChannel server = ServerSocketChannel.open()) {

            server.configureBlocking(false);
            server.bind(new InetSocketAddress("0.0.0.0", port));
            server.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("[nio" + (offloadBiz ? "-worker" : "") + "] listening on " + port);

            Thread.currentThread().setName("nio-loop");

            while (true) {
                selector.select();

                // run pending tasks from worker threads
                Runnable task;
                while ((task = pendingOnLoop.poll()) != null) {
                    task.run();
                }

                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    try {
                        if (!key.isValid()) continue;
                        if (key.isAcceptable()) onNioAccept(server, selector);
                        if (key.isReadable()) onNioRead(key, offloadBiz, bizPool, pendingOnLoop, selector);
                        if (key.isWritable()) onNioWrite(key);
                    } catch (IOException e) {
                        closeKey(key);
                    }
                }
            }
        } finally {
            if (bizPool != null) bizPool.shutdownNow();
        }
    }

    static void onNioAccept(ServerSocketChannel server, Selector selector) throws IOException {
        for (;;) {
            SocketChannel ch = server.accept();
            if (ch == null) break;

            ch.configureBlocking(false);
            ch.socket().setTcpNoDelay(true);
            SelectionKey key = ch.register(selector, SelectionKey.OP_READ);
            key.attach(new NioConn(ch));

            System.out.println("[nio] accepted " + ch.getRemoteAddress() + " thread=" + Thread.currentThread().getName());
        }
    }

    static void onNioRead(
            SelectionKey key,
            boolean offloadBiz,
            ExecutorService bizPool,
            ConcurrentLinkedQueue<Runnable> pendingOnLoop,
            Selector selector
    ) throws IOException {
        NioConn c = (NioConn) key.attachment();
        int n = c.ch.read(c.in); // non-blocking: may be 0
        if (n == -1) {
            closeKey(key);
            return;
        }
        if (n == 0) return;

        c.in.flip();

        while (true) {
            int lineEnd = findNewline(c.in);
            if (lineEnd < 0) break;

            int oldLimit = c.in.limit();
            c.in.limit(lineEnd); // slice [pos, lineEnd)
            ByteBuffer lineBuf = c.in.slice();
            c.in.limit(oldLimit);

            // consume line + '\n'
            c.in.position(lineEnd + 1);

            byte[] lineBytes = new byte[lineBuf.remaining()];
            lineBuf.get(lineBytes);
            String line = new String(lineBytes, StandardCharsets.UTF_8);

            if (!offloadBiz) {
                // WARNING: sleeping here blocks the single event-loop thread.
                sleepMs(READ_COST_MS);
                String resp = "echo:" + line + "\n";
                sleepMs(WRITE_COST_MS);
                enqueueWrite(key, c, resp.getBytes(StandardCharsets.UTF_8));
            } else {
                if (bizPool == null) throw new IllegalStateException("bizPool null");
                bizPool.submit(() -> {
                    sleepMs(READ_COST_MS);
                    String resp = "echo:" + line + "\n";
                    sleepMs(WRITE_COST_MS);
                    byte[] out = resp.getBytes(StandardCharsets.UTF_8);

                    // marshal back to selector thread
                    pendingOnLoop.add(() -> enqueueWrite(key, c, out));
                    selector.wakeup();
                });
            }
        }

        c.in.compact();
    }

    static void onNioWrite(SelectionKey key) throws IOException {
        NioConn c = (NioConn) key.attachment();

        while (!c.outQ.isEmpty()) {
            ByteBuffer buf = c.outQ.peekFirst();
            c.ch.write(buf); // non-blocking: partial writes possible
            if (buf.hasRemaining()) {
                // still not done, keep OP_WRITE
                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                return;
            }
            c.outQ.pollFirst();
        }

        // nothing to write, turn off OP_WRITE
        key.interestOps(key.interestOps() & ~SelectionKey.OP_WRITE);
    }

    static void enqueueWrite(SelectionKey key, NioConn c, byte[] bytes) {
        ByteBuffer buf = ByteBuffer.allocateDirect(bytes.length);
        buf.put(bytes);
        buf.flip();
        c.outQ.addLast(buf);
        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE | SelectionKey.OP_READ);
    }

    static int findNewline(ByteBuffer buf) {
        for (int i = buf.position(); i < buf.limit(); i++) {
            if (buf.get(i) == (byte) '\n') return i;
        }
        return -1;
    }

    static void closeKey(SelectionKey key) {
        try { key.channel().close(); } catch (IOException ignored) {}
        key.cancel();
    }

    // -------------------- AIO: AsynchronousSocketChannel --------------------

    static final class AioConn {
        final AsynchronousSocketChannel ch;
        final ByteBuffer in = ByteBuffer.allocateDirect(8 * 1024);

        final ConcurrentLinkedQueue<ByteBuffer> outQ = new ConcurrentLinkedQueue<>();
        final AtomicBoolean writing = new AtomicBoolean(false);

        AioConn(AsynchronousSocketChannel ch) {
            this.ch = ch;
        }
    }

    static void runAio(int port, int ioThreads, int bizThreads) throws IOException {
        ExecutorService ioPool = Executors.newFixedThreadPool(ioThreads, r -> {
            Thread t = new Thread(r);
            t.setDaemon(false);
            t.setName("aio-io-" + t.getId());
            return t;
        });
        ExecutorService bizPool = Executors.newFixedThreadPool(bizThreads, r -> {
            Thread t = new Thread(r);
            t.setDaemon(false);
            t.setName("aio-biz-" + t.getId());
            return t;
        });

        AsynchronousChannelGroup group = AsynchronousChannelGroup.withThreadPool(ioPool);
        AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(group)
                .bind(new InetSocketAddress("0.0.0.0", port));

        System.out.println("[aio] listening on " + port + " ioThreads=" + ioThreads + " bizThreads=" + bizThreads);

        server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel ch, Void att) {
                server.accept(null, this); // accept next

                try {
                    System.out.println("[aio] accepted " + ch.getRemoteAddress() + " thread=" + Thread.currentThread().getName());
                } catch (IOException ignored) {}

                AioConn c = new AioConn(ch);
                startAioRead(c, bizPool);
            }

            @Override
            public void failed(Throwable exc, Void att) {
                exc.printStackTrace();
            }
        });

        // keep running
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try { server.close(); } catch (IOException ignored) {}
            try { group.shutdownNow(); } catch (IOException ignored) {}
            ioPool.shutdownNow();
            bizPool.shutdownNow();
        }
    }

    static void startAioRead(AioConn c, ExecutorService bizPool) {
        c.in.clear();
        c.ch.read(c.in, c, new CompletionHandler<Integer, AioConn>() {
            @Override
            public void completed(Integer n, AioConn conn) {
                if (n == -1) {
                    closeAio(conn);
                    return;
                }

                conn.in.flip();

                while (true) {
                    int lineEnd = findNewline(conn.in);
                    if (lineEnd < 0) break;

                    int oldLimit = conn.in.limit();
                    conn.in.limit(lineEnd);
                    ByteBuffer lineBuf = conn.in.slice();
                    conn.in.limit(oldLimit);

                    conn.in.position(lineEnd + 1);

                    byte[] lineBytes = new byte[lineBuf.remaining()];
                    lineBuf.get(lineBytes);
                    String line = new String(lineBytes, StandardCharsets.UTF_8);

                    // IMPORTANT: do not sleep in aio-io threads; offload business.
                    bizPool.submit(() -> {
                        sleepMs(READ_COST_MS);
                        String resp = "echo:" + line + "\n";
                        sleepMs(WRITE_COST_MS);
                        enqueueAioWrite(conn, resp.getBytes(StandardCharsets.UTF_8));
                    });
                }

                conn.in.compact();
                // continue reading
                conn.ch.read(conn.in, conn, this);
            }

            @Override
            public void failed(Throwable exc, AioConn conn) {
                closeAio(conn);
            }
        });
    }

    static void enqueueAioWrite(AioConn c, byte[] bytes) {
        ByteBuffer buf = ByteBuffer.allocateDirect(bytes.length);
        buf.put(bytes);
        buf.flip();
        c.outQ.add(buf);
        tryStartAioWrite(c);
    }

    static void tryStartAioWrite(AioConn c) {
        if (!c.writing.compareAndSet(false, true)) return;
        doAioWriteNext(c);
    }

    static void doAioWriteNext(AioConn c) {
        ByteBuffer buf = c.outQ.poll();
        if (buf == null) {
            c.writing.set(false);
            return;
        }

        c.ch.write(buf, buf, new CompletionHandler<Integer, ByteBuffer>() {
            @Override
            public void completed(Integer n, ByteBuffer attachment) {
                if (n == -1) {
                    closeAio(c);
                    return;
                }
                if (attachment.hasRemaining()) {
                    c.ch.write(attachment, attachment, this);
                    return;
                }
                doAioWriteNext(c);
            }

            @Override
            public void failed(Throwable exc, ByteBuffer attachment) {
                closeAio(c);
            }
        });
    }

    static void closeAio(AioConn c) {
        try { c.ch.close(); } catch (IOException ignored) {}
    }

    // -------------------- Client (for all servers) --------------------

    static void runClient(String host, int port, int connections, int requestsPerConn) throws Exception {
        System.out.println("[client] host=" + host + " port=" + port + " connections=" + connections + " req/conn=" + requestsPerConn);

        ExecutorService pool = Executors.newFixedThreadPool(Math.min(connections, 200), r -> {
            Thread t = new Thread(r);
            t.setDaemon(false);
            t.setName("client-" + t.getId());
            return t;
        });

        AtomicLong ok = new AtomicLong();
        long start = System.nanoTime();

        List<Future<?>> futures = new ArrayList<>(connections);
        for (int i = 0; i < connections; i++) {
            final int cid = i;
            futures.add(pool.submit(() -> {
                try (Socket s = new Socket()) {
                    s.connect(new InetSocketAddress(host, port), 3000);
                    BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
                    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));

                    for (int j = 0; j < requestsPerConn; j++) {
                        String msg = "c" + cid + "-" + j;
                        out.write(msg);
                        out.write("\n");
                        out.flush();

                        String resp = in.readLine();
                        if (resp != null && resp.endsWith(msg)) {
                            ok.incrementAndGet();
                        }
                    }
                } catch (IOException ignored) {
                }
            }));
        }

        for (Future<?> f : futures) f.get();
        long end = System.nanoTime();

        pool.shutdownNow();

        long total = (long) connections * requestsPerConn;
        double sec = (end - start) / 1_000_000_000.0;
        System.out.println("[client] ok=" + ok.get() + "/" + total + " time=" + String.format(Locale.ROOT, "%.3fs", sec)
                + " rps=" + String.format(Locale.ROOT, "%.1f", total / sec));
    }
}
