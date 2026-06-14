package com.self;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureDemo {
    /**
     * 一个简单的 CompletableFuture 示例, 无返回值, 同时证明默认使用的是 ForkJoinPool.commonPool 线程池
     */
    static class SimpleCompletableFutureWithoutResultDemo {
        public static void main(String[] args) throws ExecutionException, InterruptedException {
            CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
                System.out.println("CompletableFuture Default Thread Name : " + Thread.currentThread().getName());
            });

            System.out.println(voidCompletableFuture.get());
        }
    }

    /**
     * 自定义线程池的 CompletableFuture 示例, 无返回值
     */
    static class CustomThreadCompletableFutureWithoutResultDemo {
        public static void main(String[] args) throws ExecutionException, InterruptedException {
            ExecutorService threadPool = Executors.newFixedThreadPool(3);
            CompletableFuture<Void> voidCompletableFuture = CompletableFuture.runAsync(() -> {
                System.out.println("CompletableFuture Custom Thread Name : " + Thread.currentThread().getName());
            }, threadPool);

            System.out.println(voidCompletableFuture.get());
            threadPool.shutdown();
        }
    }

    /**
     * 一个简单的 CompletableFuture 示例, 有返回值
     */
    static class SimpleCompletableFutureWithResultDemo {
        public static void main(String[] args) throws ExecutionException, InterruptedException {
            ExecutorService threadPool = Executors.newFixedThreadPool(3);
            CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(() -> {
                System.out.println("CompletableFuture Default Thread Name : " + Thread.currentThread().getName());
                return "Hello from CompletableFuture";
            }, threadPool);
            System.out.println(stringCompletableFuture.get());
        }
    }

    /**
     * CompletableFuture的任务拆分示例, 使用 whenComplete 和 exceptionally 方法来处理结果和异常
     */
    static class CompletableFutureTaskSpiltDemo {
        public static void main(String[] args) throws ExecutionException, InterruptedException {
            CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(() -> {
                System.out.println("CompletableFuture Default Thread Name : " + Thread.currentThread().getName());
                return "Hello from CompletableFuture";
            });

            // 使用 whenComplete 方法处理结果或异常, 他的返回值类型仍然是 CompletableFuture<T>,
            // 可以把他理解成专门为异步任务设计的try(...)finally {...}, 可以用whenComplete来完成任务的拆分
            CompletableFuture<String> whenCompleteCompletableFuture = stringCompletableFuture.whenComplete((result, exception) -> {
                if (exception == null) {
                    System.out.println("Result: " + result);
                } else {
                    System.out.println("Exception: " + exception.getMessage());
                }
            });

            // 使用 exceptionally 方法来处理异常, 并返回一个默认值
            CompletableFuture<String> exceptionally = whenCompleteCompletableFuture.exceptionally(exception -> {
                System.out.println("Handling exception: " + exception.getMessage());
                return "Default Value";
            });

            // 无论 whenComplete 还是 exceptionally 都不会改变原来的 CompletableFuture 的结果, 他们只是对结果进行处理,
            // 所以 whenCompleteCompletableFuture 和 exceptionally 的结果都是 "Hello from CompletableFuture"

            System.out.println(whenCompleteCompletableFuture.get());
        }
    }

    /**
     * CompletableFuture的阻塞获取结果的方法, join和get方法的区别示例, join会抛出一个unchecked异常, 而get会抛出一个checked异常
     */
    static class CompletableFutureJoinGetResultDemo {
        public static void main(String[] args) {
            CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(() -> {
                System.out.println("CompletableFuture Default Thread Name : " + Thread.currentThread().getName());
                throw new RuntimeException("Something went wrong");
            });

            // 使用join来获取结果, join和get的区别在于join会抛出一个unchecked异常, 而get会抛出一个checked异常,
            // 所以join不需要处理异常, 但是如果任务执行过程中发生了异常, join会抛出一个CompletionException异常, 这个异常的getCause就是原来的异常
            System.out.println(stringCompletableFuture.join());

            // 而get需要处理异常, 如果任务执行过程中发生了异常, get会抛出一个ExecutionException异常, 这个异常的getCause就是原来的异常
            // System.out.println(stringCompletableFuture.get());
        }
    }

    /**
     * CompletableFuture的立即获取结果的方法, getNow和complete方法示例,
     * getNow方法会立即返回结果, 如果任务还没有完成, 则返回一个默认值, 这个默认值是由我们自己指定的
     * complete方法会立即完成这个 CompletableFuture, 这样立即获取到结果, 如果这个 CompletableFuture 已经完成了, 则 complete 方法会返回 false, 否则返回 true
     *
     */
    static class CompletableFutureGetResultImmediatelyDemo {
        public static void main(String[] args) throws ExecutionException, InterruptedException {
            CompletableFuture<String> stringCompletableFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    for (int i = 0; i < 10; i++) {
                        Thread.sleep(1000);
                        System.out.println("CompletableFuture is running... " + (i + 1) + " seconds");
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return "Hello from CompletableFuture";
            });

            Thread.sleep(1000);
            // 使用getNow方法来获取结果, getNow方法会立即返回结果, 如果任务还没有完成, 则返回一个默认值, 这个默认值是由我们自己指定的
            String getNowMethodReturnedDefaultValue = stringCompletableFuture.getNow("Default Value");
            System.out.println("getNow method result: " + getNowMethodReturnedDefaultValue);

            // 使用complete方法来手动完成一个 CompletableFuture, complete方法会立即完成这个 CompletableFuture, 这样立即获取到结果
            // 如果这个 CompletableFuture 已经完成了, 则 complete 方法会返回 false, 否则返回 true
            boolean complete = stringCompletableFuture.complete("Manually Completed Value");
            // 当返回true, 说明这个任务没有完成, 但是我们手动完成了这个任务, 这样 get 方法就能立即获取到值了
            System.out.println("complete method returned: " + complete + "result: " + stringCompletableFuture.get());

            // 虽然立即获取到了值, 但是其中的异步任务依然在执行
            Thread.sleep(10000);
            // 虽然我们手动完成了这个 CompletableFuture, 但是其中的异步任务依然在执行,
            // 这说明 complete 方法只是改变了 CompletableFuture 的结果, 但是并不会影响异步任务的执行
            System.out.println("get method result: " + stringCompletableFuture.get());
        }
    }

    /**
     * CompletableFuture的thenApply方法和handle方法示例,
     * thenApply方法会在任务完成后对结果进行处理, handle方法会在任务完成后对结果进行处理, 但是 handle 方法还会处理异常
     */
    static class CompletableFutureThenApplyHandleDemo {
        public static void main(String[] args) {
            CompletableFuture<String> normalFuture = CompletableFuture.supplyAsync(() -> {
                System.out.println("CompletableFuture Default Thread Name : " + Thread.currentThread().getName());
                return "Hello from CompletableFuture";
            });

            // 使用thenApply方法来对结果进行处理, thenApply方法会在任务完成后对结果进行处理, 但是如果任务执行过程中发生了异常, thenApply方法不会处理异常, 这个异常会被传递到下一个 thenApply 方法中
            // 使用handle方法来对结果进行处理, handle方法会在任务完成后执行, 并且可以同时拿到结果或异常
            CompletableFuture<String> normalResult = normalFuture
                    .thenApply(result -> {
                        System.out.println("thenApply method received result: " + result);
                        return result + " - Processed by thenApply";
                    })
                    .handle((result, exception) -> {
                        if (exception == null) {
                            System.out.println("handle method received result: " + result);
                            return result + " - Processed by handle";
                        } else {
                            System.out.println("handle method received exception: " + exception.getMessage());
                            return "Default Value from handle";
                        }
                    });

            // join会等待整个链路完成, 避免main方法提前结束导致异步打印结果不稳定
            System.out.println("normal chain result: " + normalResult.join());
            System.out.println("-----------------");

            // thenApply和handle有一个重要的区别: 当上一个任务异常完成时,
            // thenApply不会执行, 异常会继续传递给thenApply返回的新CompletableFuture.
            CompletableFuture<Integer> thenApplyExceptionFuture = CompletableFuture.<Integer>supplyAsync(() -> {
                System.out.println("thenApply previous task executing");
                throw new RuntimeException("oops before thenApply");
            }).thenApply(result -> {
                // 这里不会执行, 因为前一个supplyAsync已经异常完成
                System.out.println("thenApply execute: " + result);
                return result * 2;
            });

            try {
                System.out.println("thenApply exception result: " + thenApplyExceptionFuture.join());
            } catch (CompletionException e) {
                System.out.println("thenApply caught exception: " + e);
                System.out.println("thenApply real cause: " + e.getCause());
            }

            // 而handle无论上一个任务成功还是失败都会执行, 所以这里可以把异常转换成一个默认值.
            CompletableFuture<Integer> handleRecoverFuture = CompletableFuture.<Integer>supplyAsync(() -> {
                throw new RuntimeException("handle前oops");
            }).handle((result, exception) -> {
                System.out.println("handle recover executed: result=" + result + ", exception=" + exception);
                if (exception != null) {
                    return -1;
                }
                return result * 2;
            });

            System.out.println("handle recover result: " + handleRecoverFuture.join());

            // handle只是能接住上一个阶段的异常, 不代表handle内部不会再抛新异常.
            CompletableFuture<Integer> handleThrowExceptionFuture = CompletableFuture.<Integer>supplyAsync(() -> {
                throw new RuntimeException("oops");
            }).handle((result, exception) -> {
                System.out.println("handle throw executed: result=" + result + ", exception=" + exception);
                // result此时是null, 自动拆箱会抛NullPointerException
                return result * 2;
            });

            try {
                System.out.println("handle throw result: " + handleThrowExceptionFuture.join());
            } catch (CompletionException e) {
                System.out.println("handle throw caught exception: " + e);
                System.out.println("handle throw real cause: " + e.getCause());
            }
        }
    }

    /**
     * CompletableFuture 的线程池规则：
     *
     * 1. supplyAsync(..., executor) 只指定当前阶段用 executor，
     *    后面的 thenApply / thenRun 不会自动继承这个线程池。
     *
     * 2. thenApply / thenRun 这种不带 Async 的方法，是“非 Async 就地执行”：
     *    - 上一步未完成时注册回调：通常由完成上一步的线程执行；
     *    - 上一步已完成时注册回调：可能由当前注册线程执行，比如 main。
     *
     * 3. thenApplyAsync / thenRunAsync 不传 Executor，默认走 commonPool。
     *
     * 4. 想稳定使用自己的线程池，就写 xxxAsync(..., executor)。
     */
    static class CompletableFutureThreadPoolDemo {
        public static void main(String[] args) throws InterruptedException {
            ExecutorService threadPool = Executors.newFixedThreadPool(3);

            // 情况一：非 Async，可能由完成上一步的线程继续执行。
            CompletableFuture<String> syncFuture = CompletableFuture.supplyAsync(() -> {
                System.out.println("case1-stage1: " + Thread.currentThread().getName());
                return "Hello";
            }, threadPool).thenApply(result -> {
                System.out.println("case1-stage2: " + Thread.currentThread().getName());
                return result + " World";
            }).thenApply(result -> {
                System.out.println("case1-stage3: " + Thread.currentThread().getName());
                return result + "!";
            });

            System.out.println("case1 result: " + syncFuture.join());

            // 情况二：Async 不传 Executor，不继承 threadPool，默认走 commonPool。
            CompletableFuture<String> asyncFuture = CompletableFuture.supplyAsync(() -> {
                System.out.println("case2-stage1: " + Thread.currentThread().getName());
                return "Hello";
            }, threadPool).thenApplyAsync(result -> {
                System.out.println("case2-stage2: " + Thread.currentThread().getName());
                return result + " World";
            }).thenApplyAsync(result -> {
                System.out.println("case2-stage3: " + Thread.currentThread().getName());
                return result + "!";
            });

            System.out.println("case2 result: " + asyncFuture.join());

            // 情况三：非 Async，如果上一步很快完成，后续可能由 main 现场执行。
            CompletableFuture<Void> maybeMainFuture = CompletableFuture.supplyAsync(() -> {
                System.out.println("case3-stage1: " + Thread.currentThread().getName());
                return "Hello";
            }, threadPool).thenRun(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                }
                System.out.println("case3-stage2: " + Thread.currentThread().getName());
            }).thenRun(() -> {
                System.out.println("case3-stage3: " + Thread.currentThread().getName());
            });

            System.out.println("case3 result: " + maybeMainFuture.join());

            // 情况四：想稳定用自己的线程池，就每个 Async 都显式传 executor。
            CompletableFuture<String> customFuture = CompletableFuture.supplyAsync(() -> {
                System.out.println("case4-stage1: " + Thread.currentThread().getName());
                return "Hello";
            }, threadPool).thenApplyAsync(result -> {
                System.out.println("case4-stage2: " + Thread.currentThread().getName());
                return result + " World";
            }, threadPool).thenApplyAsync(result -> {
                System.out.println("case4-stage3: " + Thread.currentThread().getName());
                return result + "!";
            }, threadPool);

            System.out.println("case4 result: " + customFuture.join());

            threadPool.shutdown();
        }
    }
}