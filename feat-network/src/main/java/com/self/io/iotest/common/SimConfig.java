
package com.self.io.iotest.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 统一模拟参数：
 * - 读耗时：READ_DELAY_MS
 * - 写耗时：WRITE_DELAY_MS
 *
 * 注意：这里的“读/写耗时”是用 Thread.sleep 模拟的服务端处理时间，用来观察不同 IO 模型下的调度差异。
 */
public final class SimConfig {
    public static final int READ_DELAY_MS = 100;
    public static final int WRITE_DELAY_MS = 500;

    public static final Charset CHARSET = StandardCharsets.UTF_8;

    private SimConfig() {}
}
