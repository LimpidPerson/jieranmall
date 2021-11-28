package com.ygnn.gulimall.search.thread;

import java.util.concurrent.*;

/**
 * @author FangKun
 */
public class ThreadTest {

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        System.out.println("main --> start");
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            System.out.println("当前线程: " + Thread.currentThread().getId());
        }, executor);
        System.out.println("main --> end" + future);
    }

//    public static void main(String[] args) {
//        System.out.println("start....");
//        executor.execute(() -> System.out.println("创建线程池..."));
//        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
//                5,
//                200,
//                10,
//                TimeUnit.SECONDS,
//                new LinkedBlockingDeque<>(100000),
//                Executors.defaultThreadFactory(),
//                new ThreadPoolExecutor.AbortPolicy());
//        System.out.println("end.....");
//    }

}
