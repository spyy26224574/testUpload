package com.adai.gkdnavi.utils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolProxy {

    private ThreadPoolExecutor mExecutor;
    private int corePoolSize;
    private int maximumPoolSize;
    private long keepAliveTime;

    public ThreadPoolProxy(int corePoolSize, int maximumPoolSize,
                           long keepAliveTime) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
    }

    public long getTaskCount() {
        return mExecutor.getTaskCount();
    }

    public void execute(Runnable task) {
        if (task == null) {
            return;
        }

        initPool();

        mExecutor.execute(task);
    }

    public Future<?> submit(Runnable task) {
        if (task == null) {
            return null;
        }

        initPool();

        return mExecutor.submit(task);
    }

    public void remove(Runnable task) {
        if (mExecutor != null) {
            mExecutor.getQueue().remove(task);
        }
    }

    public void killPool() {
        if (mExecutor != null) {
            mExecutor.shutdown(); // Disable new tasks from being submitted
            try {
                if (!mExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    mExecutor.shutdownNow(); // Cancel currently executing tasks
                }
            } catch (InterruptedException e) {
                // (Re-)cancel if current thread also interrupted
                mExecutor.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        }
    }

    private void initPool() {
        if (mExecutor == null || mExecutor.isShutdown()) {
            // int corePoolSize = 3;// 核心线程数
            // int maximumPoolSize = 5;// 最大数据量
            // long keepAliveTime = 2000;// 保持存活的时间
            TimeUnit unit = TimeUnit.MILLISECONDS;
            BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>();

            ThreadFactory threadFactory = Executors.defaultThreadFactory();// 线程工厂
            // RejectedExecutionHandler handler = null;// 策略,队列的

            // RejectedExecutionHandler handler = new
            // ThreadPoolExecutor.AbortPolicy();//队列中加不进去时，抛出异常

            // RejectedExecutionHandler handler = new
            // ThreadPoolExecutor.CallerRunsPolicy();//队列中加不进去时，直接在当前线程中执行

            // RejectedExecutionHandler handler = new
            // ThreadPoolExecutor.DiscardOldestPolicy();//队列中加不进去时,移除队列中的第一个，将任务加到队列的中

            RejectedExecutionHandler handler = new ThreadPoolExecutor.DiscardPolicy();// 队列中加不进去时,不处理

            mExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                    keepAliveTime, unit, workQueue, threadFactory, handler);
        }
    }


    public void shutdownNow() {
        if (mExecutor != null) {
            mExecutor.shutdownNow();
        }
    }

    public void shutDown() {
        if (mExecutor != null) {
            mExecutor.shutdown();
        }
    }
}
