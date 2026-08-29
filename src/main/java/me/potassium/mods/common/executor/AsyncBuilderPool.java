package me.potassium.mods.common.executor;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * AsyncBuilderPool - 异步区块构建线程池
 *
 * 设计思路来源：
 * - 《Java Concurrency in Practice》第8章 - 线程池配置
 * - 《Work-Stealing Scheduling》- Blumofe & Leiserson 论文 (1999)
 * - Java ForkJoinPool 官方文档
 *
 * 核心原理：
 * 1. Work Stealing: 空闲线程从忙碌线程的队列尾部偷取任务
 * 2. 避免锁竞争: 每个线程有本地队列，仅偷取时才竞争
 * 3. 负载均衡: 自动平衡各线程工作量
 */
public class AsyncBuilderPool {

    private final ForkJoinPool pool;
    private final int workerCount;
    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private volatile boolean running = true;

    /**
     * 构造函数
     * @param threadCount 工作线程数
     *
     * 参考来源：《Java Concurrency in Practice》
     * 对于计算密集型任务，线程数 = N_cpu + 1 可获得最优吞吐量
     * 但我们保留2个核心给主线程和系统，所以使用 N_cpu - 2
     */
    public AsyncBuilderPool(int threadCount) {
        this.workerCount = threadCount;

        // 创建 ForkJoinPool
        // 参考：Java ForkJoinPool 文档
        // 使用自定义线程工厂以便命名线程
        this.pool = new ForkJoinPool(
            threadCount,
            new PotassiumThreadFactory(),
            null,  // 无异常处理器
            true   // 启用公平调度
        );
    }

    /**
     * 提交构建任务
     *
     * @param task 构建任务
     */
    public void submit(BlockBuildTask task) {
        if (!running) {
            throw new IllegalStateException("Pool is shutdown");
        }

        activeTasks.incrementAndGet();
        pool.execute(() -> {
            try {
                task.execute();
            } finally {
                activeTasks.decrementAndGet();
            }
        });
    }

    /**
     * 获取当前活动任务数
     */
    public int getActiveTaskCount() {
        return activeTasks.get();
    }

    /**
     * 获取工作线程数
     */
    public int getWorkerCount() {
        return workerCount;
    }

    /**
     * 获取 ForkJoinPool 实例
     * 用于并行流等高级操作
     */
    public ForkJoinPool getPool() {
        return pool;
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        running = false;
        pool.shutdown();
    }

    /**
     * 线程工厂 - 为工作线程命名
     *
     * 参考：《Java Concurrency in Practice》第7章
     */
    private static class PotassiumThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

        private final AtomicInteger counter = new AtomicInteger(0);

        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            thread.setName("Potassium-Worker-" + counter.getAndIncrement());
            return thread;
        }
    }
}
