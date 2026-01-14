
package com.self.io.iotest.common;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.Channel;

public final class NetUtil {
    private NetUtil() {}

    public static void closeQuietly(Channel ch) {
        if (ch == null) return;
        try { ch.close(); } catch (IOException ignored) {}
    }

    public static void closeQuietly(Closeable c) {
        if (c == null) return;
        try { c.close(); } catch (IOException ignored) {}
    }

    public static String safeAddr(SocketAddress addr) {
        return addr == null ? "unknown" : addr.toString();
    }

    public static void sleepSilently(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
