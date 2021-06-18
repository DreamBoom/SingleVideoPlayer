package com.seiko.player.helper;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 给PlayView使用的单一线程池
 * @author seiko
 */
public class PlayThreadPoolExecutor implements ExecutorService {

    private static ThreadFactory mTreadFactory = new ThreadFactory() {

        private AtomicInteger atomicInteger = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable runnable) {
            int c = atomicInteger.incrementAndGet();
            return new Thread(runnable, "player_view_thread-" + c);
        }
    };

    private ExecutorService mExecutorService;

    public PlayThreadPoolExecutor() {
        mExecutorService = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(1024),
                mTreadFactory, new ThreadPoolExecutor.AbortPolicy());
    }

    @Override
    public void execute(Runnable runnable) {
        mExecutorService.execute(runnable);
    }

    @Override
    public void shutdown() {
        mExecutorService.shutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        return mExecutorService.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return mExecutorService.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return mExecutorService.isTerminated();
    }

    @Override
    public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
        return mExecutorService.awaitTermination(l, timeUnit);
    }

    @Override
    public <T> Future<T> submit(Callable<T> callable) {
        return mExecutorService.submit(callable);
    }

    @Override
    public <T> Future<T> submit(Runnable runnable, T t) {
        return mExecutorService.submit(runnable, t);
    }

    @Override
    public Future<?> submit(Runnable runnable) {
        return mExecutorService.submit(runnable);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection) throws InterruptedException {
        return mExecutorService.invokeAll(collection);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws InterruptedException {
        return mExecutorService.invokeAll(collection, l, timeUnit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> collection) throws ExecutionException, InterruptedException {
        return mExecutorService.invokeAny(collection);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> collection, long l, TimeUnit timeUnit) throws ExecutionException, InterruptedException, TimeoutException {
        return mExecutorService.invokeAny(collection, l, timeUnit);
    }


}
