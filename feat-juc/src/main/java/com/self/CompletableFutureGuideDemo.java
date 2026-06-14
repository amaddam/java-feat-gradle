package com.self;

import java.time.Duration;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class CompletableFutureGuideDemo {

    /**
     * 1. 异步任务创建、聚合与 Future 视图包装。
     */
    static class AsyncTaskCreationAggregationAndViewDemo {
        public static void main(String[] args) {
            ExecutorService customExecutor = newNamedFixedThreadPool("creation-demo", 3);
            try {
                // ---------------- runAsync ----------------
                // 作用：开一个只执行动作、没有返回值的异步任务。
                // 依赖：这是起点任务，不依赖上一步；返回CompletableFuture<Void>。
                CompletableFuture.runAsync(() -> printStep("runAsync action"), customExecutor).join();

                // ---------------- supplyAsync ----------------
                // 作用：开一个异步任务，并把计算结果放进CompletableFuture。
                // 依赖：这是起点任务，不依赖上一步；返回CompletableFuture<T>。
                CompletableFuture<String> supplyFuture = CompletableFuture.supplyAsync(() -> {
                    printStep("supplyAsync calculates value");
                    return "hello";
                }, customExecutor);
                System.out.println("supplyAsync result: " + supplyFuture.join());

                // ---------------- default executor vs custom executor ----------------
                // 作用：不传Executor通常走默认异步执行器；传Executor就走指定线程池。
                CompletableFuture.runAsync(() -> printStep("runAsync uses default executor")).join();
                CompletableFuture.runAsync(() -> printStep("runAsync uses custom executor"), customExecutor).join();
                CompletableFuture.supplyAsync(() -> {
                    printStep("supplyAsync uses default executor");
                    return "default";
                }).thenAccept(result -> System.out.println("supplyAsync default result: " + result)).join();
                CompletableFuture.supplyAsync(() -> {
                    printStep("supplyAsync uses custom executor");
                    return "custom";
                }, customExecutor).thenAccept(result -> System.out.println("supplyAsync custom result: " + result)).join();

                // ---------------- completedFuture / completedStage ----------------
                // 作用：直接得到已经成功的结果，适合测试、快速返回。
                CompletionStage<String> completedStage = CompletableFuture.completedStage("stage-done");
                System.out.println("completedFuture result: " + CompletableFuture.completedFuture("done").join());
                System.out.println("completedStage result: " + completedStage.toCompletableFuture().join());

                // ---------------- failedFuture / failedStage ----------------
                // 作用：直接得到已经失败的结果，方便统一走异常链路。
                try {
                    CompletableFuture.failedFuture(new IllegalStateException("failed future")).join();
                } catch (CompletionException e) {
                    printFailureCause("failedFuture", e);
                }
                try {
                    CompletableFuture.failedStage(new IllegalArgumentException("failed stage")).toCompletableFuture().join();
                } catch (CompletionException e) {
                    printFailureCause("failedStage", e);
                }

                // ---------------- allOf ----------------
                // 作用：等一组Future全部完成；它本身不收集结果，结果还要从原Future里取。
                CompletableFuture<String> firstFuture = delayedValue("A", 40, customExecutor);
                CompletableFuture<String> secondFuture = delayedValue("B", 60, customExecutor);
                CompletableFuture<String> thirdFuture = delayedValue("C", 20, customExecutor);
                CompletableFuture.allOf(firstFuture, secondFuture, thirdFuture).join();
                System.out.println("allOf results: " + firstFuture.join() + ", " + secondFuture.join() + ", " + thirdFuture.join());

                // ---------------- anyOf ----------------
                // 作用：一组Future里谁先完成就返回谁；异常完成也算完成。
                CompletableFuture<String> fastFuture = delayedValue("fast", 40, customExecutor);
                CompletableFuture<String> slowFuture = delayedValue("slow", 120, customExecutor);
                System.out.println("anyOf first result: " + CompletableFuture.anyOf(fastFuture, slowFuture).join());

                CompletableFuture<String> failedFastFuture = CompletableFuture.supplyAsync(() -> {
                    printStep("failedFastFuture fails");
                    throw new RuntimeException("fast failure");
                }, customExecutor);
                CompletableFuture<String> slowSuccessFuture = delayedValue("slow-success", 150, customExecutor);
                try {
                    CompletableFuture.anyOf(failedFastFuture, slowSuccessFuture).join();
                } catch (CompletionException e) {
                    printFailureCause("anyOf first completed exceptionally", e);
                    System.out.println("slowSuccessFuture final result: " + slowSuccessFuture.join());
                }

                // ---------------- delayedExecutor ----------------
                // 作用：延迟一小段时间后再提交任务。
                Executor delayedExecutor = CompletableFuture.delayedExecutor(120, TimeUnit.MILLISECONDS, customExecutor);
                CompletableFuture.runAsync(() -> printStep("delayed task runs"), delayedExecutor).join();

                // ---------------- copy / minimalCompletionStage / toCompletableFuture ----------------
                // 作用：给调用方一个受限视图，避免调用方直接改原始Future。
                CompletableFuture<String> originalFuture = new CompletableFuture<>();
                CompletableFuture<String> copiedFuture = originalFuture.copy();
                CompletionStage<String> minimalStage = originalFuture.minimalCompletionStage();
                CompletableFuture<String> convertedFuture = minimalStage.toCompletableFuture();
                originalFuture.complete("value");
                System.out.println("copy result: " + copiedFuture.join());
                System.out.println("minimalCompletionStage result: " + minimalStage.toCompletableFuture().join());
                System.out.println("toCompletableFuture is original future: " + (convertedFuture == originalFuture));

                CompletableFuture<String> pendingOriginalFuture = new CompletableFuture<>();
                CompletableFuture<String> callerOwnedFuture = pendingOriginalFuture.minimalCompletionStage().toCompletableFuture();
                callerOwnedFuture.complete("caller-value");
                System.out.println("callerOwnedFuture result: " + callerOwnedFuture.join());
                System.out.println("pendingOriginalFuture is done: " + pendingOriginalFuture.isDone());

                // ---------------- newIncompleteFuture / defaultExecutor ----------------
                // 作用：主要给子类扩展用；日常业务代码一般不需要重写。
                ExecutorService subclassExecutor = newNamedFixedThreadPool("subclass-demo", 1);
                try {
                    CustomExecutorCompletableFuture<String> baseFuture = new CustomExecutorCompletableFuture<>(subclassExecutor);
                    CompletableFuture<String> nextFuture = baseFuture.thenApplyAsync(value -> {
                        printStep("subclass defaultExecutor handles thenApplyAsync");
                        return value.toUpperCase();
                    });
                    baseFuture.complete("subclass");
                    System.out.println("nextFuture is CustomExecutorCompletableFuture: " + (nextFuture instanceof CustomExecutorCompletableFuture));
                    System.out.println("defaultExecutor result: " + nextFuture.join());
                } finally {
                    shutdownExecutor(subclassExecutor);
                }
            } finally {
                shutdownExecutor(customExecutor);
            }
        }
    }

    private static class CustomExecutorCompletableFuture<T> extends CompletableFuture<T> {
        private final Executor defaultExecutor;

        private CustomExecutorCompletableFuture(Executor defaultExecutor) {
            this.defaultExecutor = defaultExecutor;
        }

        @Override
        public Executor defaultExecutor() {
            return defaultExecutor;
        }

        @Override
        public <U> CompletableFuture<U> newIncompleteFuture() {
            return new CustomExecutorCompletableFuture<>(defaultExecutor);
        }
    }

    /**
     * 2. Future 结果获取、状态判断与完成控制。
     */
    static class FutureResultStateAndControlDemo {
        public static void main(String[] args) {
            ExecutorService customExecutor = newNamedFixedThreadPool("state-demo", 1);
            try {
                // ---------------- get / join ----------------
                // get阻塞等结果，失败时抛ExecutionException；join也阻塞，但失败时抛CompletionException。
                try {
                    System.out.println("get result: " + delayedValue("value", 40, customExecutor).get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    printFailureCause("get", e);
                } catch (ExecutionException e) {
                    printFailureCause("get", e);
                }
                try {
                    System.out.println("join result: " + failedFuture(customExecutor).join());
                } catch (CompletionException e) {
                    printFailureCause("join", e);
                }

                // ---------------- get(timeout, unit) ----------------
                // 作用：最多等一段时间；超时后当前等待失败，不代表原任务一定停止。
                CompletableFuture<String> slowFuture = delayedValue("slow-value", 180, customExecutor);
                try {
                    System.out.println("get with timeout result: " + slowFuture.get(50, TimeUnit.MILLISECONDS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    printFailureCause("get with timeout", e);
                } catch (ExecutionException | TimeoutException e) {
                    printFailureCause("get with timeout", e);
                }
                System.out.println("slowFuture final result: " + slowFuture.join());

                // ---------------- getNow / resultNow / exceptionNow ----------------
                // getNow不等待；resultNow和exceptionNow适合确认状态后立即取成功结果或失败原因。
                CompletableFuture<String> pendingFuture = delayedValue("real-value", 80, customExecutor);
                System.out.println("getNow immediate result: " + pendingFuture.getNow("default-value"));
                System.out.println("getNow final result: " + pendingFuture.join());
                System.out.println("resultNow result: " + CompletableFuture.completedFuture("success-value").resultNow());
                Throwable failure = CompletableFuture.failedFuture(new IllegalStateException("failed-value")).exceptionNow();
                System.out.println("exceptionNow type: " + failure.getClass().getSimpleName());
                System.out.println("exceptionNow message: " + failure.getMessage());

                // ---------------- complete / completeExceptionally / completeAsync ----------------
                // complete手动给成功结果；completeExceptionally手动给失败；completeAsync异步算结果再填入。
                CompletableFuture<String> manualFuture = new CompletableFuture<>();
                System.out.println("complete returned: " + manualFuture.complete("manual-value"));
                System.out.println("complete result: " + manualFuture.join());
                CompletableFuture<String> manualFailedFuture = new CompletableFuture<>();
                manualFailedFuture.completeExceptionally(new IllegalStateException("manual failure"));
                try {
                    manualFailedFuture.join();
                } catch (CompletionException e) {
                    printFailureCause("completeExceptionally", e);
                }
                CompletableFuture<String> completeAsyncDefaultFuture = new CompletableFuture<>();
                completeAsyncDefaultFuture.completeAsync(() -> {
                    printStep("completeAsync uses default executor");
                    return "async-default";
                });
                System.out.println("completeAsync default result: " + completeAsyncDefaultFuture.join());

                CompletableFuture<String> completeAsyncCustomFuture = new CompletableFuture<>();
                completeAsyncCustomFuture.completeAsync(() -> {
                    printStep("completeAsync uses custom executor");
                    return "async-custom";
                }, customExecutor);
                System.out.println("completeAsync custom result: " + completeAsyncCustomFuture.join());

                // complete只改变CompletableFuture结果，不代表底层任务一定停止。
                CompletableFuture<Void> started = new CompletableFuture<>();
                CompletableFuture<String> runningFuture = CompletableFuture.supplyAsync(() -> {
                    started.complete(null);
                    sleep(Duration.ofMillis(120));
                    printStep("runningFuture still reaches the end");
                    return "running-value";
                }, customExecutor);
                started.join();
                runningFuture.complete("manual-running-value");
                System.out.println("runningFuture result: " + runningFuture.join());
                sleep(Duration.ofMillis(150));

                // ---------------- state / debug ----------------
                // 作用：观察完成状态、取消状态、依赖数量和调试字符串。
                CompletableFuture<String> debugFuture = new CompletableFuture<>();
                CompletableFuture<String> dependentFuture = debugFuture.thenApply(value -> value + "-checked");
                System.out.println("dependents before complete: " + debugFuture.getNumberOfDependents());
                System.out.println("debugFuture before complete: " + debugFuture);
                debugFuture.complete("debug");
                System.out.println("dependent result: " + dependentFuture.join());
                System.out.println("debugFuture after complete: " + debugFuture);

                CompletableFuture<String> cancelledFuture = new CompletableFuture<>();
                cancelledFuture.cancel(true);
                System.out.println("isDone: " + cancelledFuture.isDone());
                System.out.println("isCancelled: " + cancelledFuture.isCancelled());
                System.out.println("isCompletedExceptionally: " + cancelledFuture.isCompletedExceptionally());
                System.out.println("state: " + cancelledFuture.state());
                try {
                    cancelledFuture.join();
                } catch (CancellationException e) {
                    printFailureCause("cancel", e);
                }

                // cancel(true)不要误以为一定能中断正在运行的线程。
                CompletableFuture<Void> cancelStarted = new CompletableFuture<>();
                CompletableFuture<String> runningCancelledFuture = CompletableFuture.supplyAsync(() -> {
                    cancelStarted.complete(null);
                    sleep(Duration.ofMillis(120));
                    printStep("runningCancelledFuture still reaches the end");
                    return "after-cancel";
                }, customExecutor);
                cancelStarted.join();
                runningCancelledFuture.cancel(true);
                try {
                    runningCancelledFuture.join();
                } catch (CancellationException e) {
                    printFailureCause("running cancel", e);
                }
                sleep(Duration.ofMillis(150));

                // ---------------- orTimeout / completeOnTimeout ----------------
                // orTimeout让Future超时失败；completeOnTimeout给超时任务一个默认成功结果。
                try {
                    new CompletableFuture<String>().orTimeout(80, TimeUnit.MILLISECONDS).join();
                } catch (CompletionException e) {
                    printFailureCause("orTimeout", e);
                }
                System.out.println("completeOnTimeout result: " +
                        new CompletableFuture<String>().completeOnTimeout("timeout-default", 80, TimeUnit.MILLISECONDS).join());

                // ---------------- obtrudeValue / obtrudeException ----------------
                // 强行改后续get/join看到的结果；这不是正常业务流程里的complete，慎用。
                CompletableFuture<String> forcedFuture = CompletableFuture.completedFuture("old-value");
                forcedFuture.obtrudeValue("forced-value");
                System.out.println("obtrudeValue result: " + forcedFuture.join());
                forcedFuture.obtrudeException(new IllegalStateException("forced failure"));
                try {
                    forcedFuture.join();
                } catch (CompletionException e) {
                    printFailureCause("obtrudeException", e);
                }
            } finally {
                shutdownExecutor(customExecutor);
            }
        }
    }

    /**
     * 3. 单个异步任务成功后的串行处理。
     */
    static class SingleFutureChainDemo {
        public static void main(String[] args) {
            ExecutorService customExecutor = newNamedFixedThreadPool("single-chain-demo", 2);
            try {
                // thenApply / thenAccept / thenRun / thenCompose默认只在上一步正常完成时执行。
                // 如果上一步异常完成，会跳过当前回调，异常继续向后传。

                // ---------------- thenApply ----------------
                // 作用：拿上一步成功结果，转换成一个新结果。
                CompletableFuture<String> thenApplyFuture = CompletableFuture.completedFuture("hello")
                        .thenApply(value -> value + " world");
                System.out.println("thenApply result: " + thenApplyFuture.join());

                // ---------------- thenAccept ----------------
                // 作用：拿上一步成功结果用一下；不返回新值，结果类型是Void。
                CompletableFuture.completedFuture("hello")
                        .thenAccept(value -> printStep("thenAccept consumes: " + value))
                        .join();

                // ---------------- thenRun ----------------
                // 作用：只关心上一步成功；不接收上一步结果，也不返回新值。
                CompletableFuture.completedFuture("hello")
                        .thenRun(() -> printStep("thenRun prints done"))
                        .join();

                // ---------------- thenApply vs thenCompose ----------------
                // thenApply返回Future会产生套娃；thenCompose用来拍平下一步Future。
                CompletableFuture<CompletableFuture<String>> nestedFuture = CompletableFuture.completedFuture("userId=1")
                        .thenApply(value -> CompletableFuture.completedFuture("userName=Tom"));
                System.out.println("thenApply nested result: " + nestedFuture.join().join());

                CompletableFuture<String> composedFuture = CompletableFuture.completedFuture("userId=1")
                        .thenCompose(value -> CompletableFuture.completedFuture("userName=Tom"));
                System.out.println("thenCompose result: " + composedFuture.join());

                // ---------------- Async variants ----------------
                // 作用：Async不传Executor通常走commonPool；传Executor走指定线程池。
                CompletableFuture.completedFuture("hello")
                        .thenApplyAsync(value -> {
                            printStep("thenApplyAsync uses default executor");
                            return value.toUpperCase();
                        })
                        .thenApplyAsync(value -> {
                            printStep("thenApplyAsync uses custom executor");
                            return value + "-CUSTOM";
                        }, customExecutor)
                        .thenAccept(result -> System.out.println("thenApplyAsync result: " + result))
                        .join();

                CompletableFuture.completedFuture("hello")
                        .thenAcceptAsync(value -> printStep("thenAcceptAsync uses default executor"))
                        .thenRunAsync(() -> printStep("thenRunAsync uses custom executor"), customExecutor)
                        .join();

                CompletableFuture.completedFuture("userId=1")
                        .thenComposeAsync(value -> {
                            printStep("thenComposeAsync uses custom executor");
                            return CompletableFuture.completedFuture("userName=Tom");
                        }, customExecutor)
                        .thenAccept(result -> System.out.println("thenComposeAsync result: " + result))
                        .join();
            } finally {
                shutdownExecutor(customExecutor);
            }
        }
    }

    /**
     * 4. 两个异步任务都成功后的合并处理。
     */
    static class BothFutureSuccessDemo {
        public static void main(String[] args) {
            ExecutorService customExecutor = newNamedFixedThreadPool("both-success-demo", 2);
            try {
                // 这组方法要求两个Future都正常完成；任意一个异常完成，正常合并逻辑不会执行。
                CompletableFuture<Integer> firstFuture = delayedInt(10, 50, customExecutor);
                CompletableFuture<Integer> secondFuture = delayedInt(20, 70, customExecutor);

                // ---------------- thenCombine ----------------
                // 作用：两个Future都成功后，把两个结果合成一个新结果。
                System.out.println("thenCombine result: " +
                        firstFuture.thenCombine(secondFuture, Integer::sum).join());

                // ---------------- thenAcceptBoth ----------------
                // 作用：两个Future都成功后，消费两个结果；不返回新值。
                firstFuture.thenAcceptBoth(secondFuture,
                        (first, second) -> printStep("thenAcceptBoth consumes: " + first + ", " + second))
                        .join();

                // ---------------- runAfterBoth ----------------
                // 作用：两个Future都成功后执行动作；不接收两个结果，也不返回新值。
                firstFuture.runAfterBoth(secondFuture, () -> printStep("runAfterBoth prints both finished")).join();

                // ---------------- Async variants ----------------
                // 作用：Async不传Executor通常走commonPool；传Executor走指定线程池。
                CompletableFuture.completedFuture(10)
                        .thenCombineAsync(CompletableFuture.completedFuture(20), (first, second) -> {
                            printStep("thenCombineAsync uses default executor");
                            return first + second;
                        })
                        .thenCombineAsync(CompletableFuture.completedFuture(1), (sum, extra) -> {
                            printStep("thenCombineAsync uses custom executor");
                            return sum + extra;
                        }, customExecutor)
                        .thenAccept(result -> System.out.println("thenCombineAsync result: " + result))
                        .join();

                CompletableFuture.completedFuture(10)
                        .thenAcceptBothAsync(CompletableFuture.completedFuture(20),
                                (first, second) -> printStep("thenAcceptBothAsync uses default executor"))
                        .runAfterBothAsync(CompletableFuture.completedFuture(null),
                                () -> printStep("runAfterBothAsync uses custom executor"), customExecutor)
                        .join();

                // ---------------- failure path ----------------
                // 作用：说明任意一个Future异常完成，thenCombine的正常回调不会执行。
                CompletableFuture<Integer> failedFuture = CompletableFuture.supplyAsync(() -> {
                    throw new RuntimeException("second failure");
                }, customExecutor);
                try {
                    firstFuture.thenCombine(failedFuture, Integer::sum).join();
                } catch (CompletionException e) {
                    printFailureCause("thenCombine with failed future", e);
                }
            } finally {
                shutdownExecutor(customExecutor);
            }
        }
    }

    /**
     * 5. 两个异步任务中任意一个先成功后的抢先处理。
     */
    static class EitherFutureSuccessDemo {
        public static void main(String[] args) {
            ExecutorService customExecutor = newNamedFixedThreadPool("either-success-demo", 2);
            try {
                // applyToEither / acceptEither / runAfterEither关注的是两个Future里谁先正常完成。
                // anyOf是静态方法，谁先完成就返回，异常完成也算完成。
                CompletableFuture<String> fastFuture = delayedValue("fast", 50, customExecutor);
                CompletableFuture<String> slowFuture = delayedValue("slow", 120, customExecutor);

                // ---------------- applyToEither ----------------
                // 作用：谁先正常完成，就拿谁的结果转换成新结果。
                System.out.println("applyToEither result: " +
                        fastFuture.applyToEither(slowFuture, String::toUpperCase).join());

                // ---------------- acceptEither ----------------
                // 作用：谁先正常完成，就消费谁的结果；不返回新值。
                fastFuture.acceptEither(slowFuture, value -> printStep("acceptEither consumes: " + value)).join();

                // ---------------- runAfterEither ----------------
                // 作用：谁先正常完成，就执行动作；不关心结果。
                fastFuture.runAfterEither(slowFuture, () -> printStep("runAfterEither prints one finished")).join();

                // ---------------- Async variants ----------------
                // 作用：Async不传Executor通常走commonPool；传Executor走指定线程池。
                CompletableFuture.completedFuture("fast")
                        .applyToEitherAsync(delayedValue("slow", 40, customExecutor), value -> {
                            printStep("applyToEitherAsync uses default executor");
                            return value.toUpperCase();
                        })
                        .thenAccept(result -> System.out.println("applyToEitherAsync result: " + result))
                        .join();

                CompletableFuture.completedFuture("fast")
                        .acceptEitherAsync(CompletableFuture.completedFuture("slow"),
                                value -> printStep("acceptEitherAsync uses default executor"))
                        .runAfterEitherAsync(CompletableFuture.completedFuture(null),
                                () -> printStep("runAfterEitherAsync uses custom executor"), customExecutor)
                        .join();

                // ---------------- anyOf vs applyToEither ----------------
                // anyOf是谁先完成就返回，异常完成也算完成；applyToEither不是可靠的first-success工具。
                CompletableFuture<String> failedFastFuture = CompletableFuture.supplyAsync(() -> {
                    throw new RuntimeException("fast failure");
                }, customExecutor);
                CompletableFuture<String> slowSuccessFuture = delayedValue("slow-success", 120, customExecutor);
                try {
                    CompletableFuture.anyOf(failedFastFuture, slowSuccessFuture).join();
                } catch (CompletionException e) {
                    printFailureCause("anyOf with fast failure", e);
                }
                try {
                    failedFastFuture.applyToEither(slowSuccessFuture, String::toUpperCase).join();
                } catch (CompletionException e) {
                    printFailureCause("applyToEither after fast failure", e);
                }
                System.out.println("slowSuccessFuture final result: " + slowSuccessFuture.join());
            } finally {
                shutdownExecutor(customExecutor);
            }
        }
    }

    /**
     * 6. 异常兜底、异常恢复与成败统一处理。
     */
    static class ExceptionRecoveryAndCompletionDemo {
        public static void main(String[] args) {
            ExecutorService customExecutor = newNamedFixedThreadPool("exception-demo", 2);
            try {
                // ---------------- exceptionally ----------------
                // 作用：上游异常时，返回一个兜底值；只处理失败路径，并返回新结果。
                System.out.println("exceptionally result: " +
                        failedFuture(customExecutor).exceptionally(e -> "fallback").join());

                // ---------------- exceptionallyCompose ----------------
                // 作用：上游异常时，不直接给值，而是切换到另一个Future。
                System.out.println("exceptionallyCompose result: " +
                        failedFuture(customExecutor).exceptionallyCompose(e -> fallbackFuture(customExecutor)).join());

                // ---------------- exceptionallyAsync variants ----------------
                // 作用：Async不传Executor通常走commonPool；传Executor走指定线程池。
                failedFuture(customExecutor)
                        .exceptionallyAsync(e -> {
                            printStep("exceptionallyAsync uses default executor");
                            return "fallback-default-async";
                        })
                        .thenAccept(result -> System.out.println("exceptionallyAsync default result: " + result))
                        .join();
                failedFuture(customExecutor)
                        .exceptionallyAsync(e -> {
                            printStep("exceptionallyAsync uses custom executor");
                            return "fallback-custom-async";
                        }, customExecutor)
                        .thenAccept(result -> System.out.println("exceptionallyAsync custom result: " + result))
                        .join();

                failedFuture(customExecutor)
                        .exceptionallyComposeAsync(e -> {
                            printStep("exceptionallyComposeAsync uses default executor");
                            return fallbackFuture(customExecutor);
                        })
                        .thenAccept(result -> System.out.println("exceptionallyComposeAsync default result: " + result))
                        .join();
                failedFuture(customExecutor)
                        .exceptionallyComposeAsync(e -> {
                            printStep("exceptionallyComposeAsync uses custom executor");
                            return fallbackFuture(customExecutor);
                        }, customExecutor)
                        .thenAccept(result -> System.out.println("exceptionallyComposeAsync custom result: " + result))
                        .join();

                // ---------------- whenComplete ----------------
                // 作用：成功失败都会执行，适合看一眼结果或异常；一般不改变最终结果。
                CompletableFuture.completedFuture("success")
                        .whenComplete((result, e) -> printStep("whenComplete observes result=" + result))
                        .thenAccept(result -> System.out.println("whenComplete success result: " + result))
                        .join();
                try {
                    failedFuture(customExecutor)
                            .whenComplete((result, e) -> printStep("whenComplete observes exception=" + e))
                            .join();
                } catch (CompletionException e) {
                    printFailureCause("whenComplete failure", e);
                }

                CompletableFuture.completedFuture("success")
                        .whenCompleteAsync((result, e) -> printStep("whenCompleteAsync uses default executor"))
                        .thenAccept(result -> System.out.println("whenCompleteAsync default result: " + result))
                        .join();
                CompletableFuture.completedFuture("success")
                        .whenCompleteAsync((result, e) -> printStep("whenCompleteAsync uses custom executor"), customExecutor)
                        .thenAccept(result -> System.out.println("whenCompleteAsync custom result: " + result))
                        .join();

                // ---------------- handle ----------------
                // 作用：成功失败都会执行，并且可以生成新的最终结果。
                System.out.println("handle success result: " +
                        CompletableFuture.completedFuture("success").handle((result, e) -> result + "-handled").join());
                System.out.println("handle failure result: " +
                        failedFuture(customExecutor).handle((result, e) -> "fallback").join());
                failedFuture(customExecutor)
                        .handleAsync((result, e) -> {
                            printStep("handleAsync uses default executor");
                            return "fallback-default-async";
                        })
                        .thenAccept(result -> System.out.println("handleAsync default result: " + result))
                        .join();
                failedFuture(customExecutor)
                        .handleAsync((result, e) -> {
                            printStep("handleAsync uses custom executor");
                            return "fallback-custom-async";
                        }, customExecutor)
                        .thenAccept(result -> System.out.println("handleAsync custom result: " + result))
                        .join();

                // ---------------- callback throws ----------------
                // 作用：说明回调自己抛异常时，新阶段也会异常。
                try {
                    CompletableFuture.completedFuture("success")
                            .whenComplete((result, e) -> {
                                throw new RuntimeException("whenComplete failure");
                            })
                            .join();
                } catch (CompletionException e) {
                    printFailureCause("whenComplete throws", e);
                }
                try {
                    failedFuture(customExecutor)
                            .handle((result, e) -> {
                                throw new RuntimeException("handle failure");
                            })
                            .join();
                } catch (CompletionException e) {
                    printFailureCause("handle throws", e);
                }
            } finally {
                shutdownExecutor(customExecutor);
            }
        }
    }

    private static ExecutorService newNamedFixedThreadPool(String namePrefix, int nThreads) {
        AtomicInteger threadNumber = new AtomicInteger(1);
        ThreadFactory threadFactory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName(namePrefix + "-" + threadNumber.getAndIncrement());
            return thread;
        };
        return Executors.newFixedThreadPool(nThreads, threadFactory);
    }

    private static void printStep(String stepName) {
        System.out.printf("[%s] %s%n", Thread.currentThread().getName(), stepName);
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread was interrupted", e);
        }
    }

    private static CompletableFuture<String> delayedValue(String value, long millis, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(Duration.ofMillis(millis));
            printStep("returns " + value);
            return value;
        }, executor);
    }

    private static CompletableFuture<Integer> delayedInt(int value, long millis, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            sleep(Duration.ofMillis(millis));
            printStep("returns " + value);
            return value;
        }, executor);
    }

    private static CompletableFuture<String> failedFuture(Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            printStep("failedFuture fails");
            throw new RuntimeException("failed");
        }, executor);
    }

    private static CompletableFuture<String> fallbackFuture(Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            printStep("fallbackFuture returns fallback");
            return "fallback";
        }, executor);
    }

    private static void shutdownExecutor(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(3, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static void printFailureCause(String label, Throwable throwable) {
        Throwable rootCause = throwable;
        while ((rootCause instanceof CompletionException || rootCause instanceof ExecutionException)
                && rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        System.out.printf("[%s] %s failed: %s%n", Thread.currentThread().getName(), label, rootCause);
    }
}
