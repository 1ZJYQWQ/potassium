package me.potassium.mods.common.collections;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MemoryArena - 内存池
 *
 * 设计思路来源：
 * - 《Object Pool Pattern》- 游戏引擎设计通用模式
 * - 《Java Concurrency in Practice》第5章 - 对象池
 * - 《Effective Java》第6章 - 避免创建不必要的对象
 *
 * 核心原理：
 * 1. 预分配对象：启动时创建一定数量的对象
 * 2. 复用对象：用完后归还池而非丢弃
 * 3. 减少 GC：避免频繁的对象分配和回收
 *
 * 性能优势：
 * - 减少 GC 暂停时间
 * - 降低内存分配开销
 * - 提高缓存命中率
 */
public class MemoryArena {

    // 默认池大小
    private static final int DEFAULT_POOL_SIZE = 256;

    // 顶点缓冲区池
    // 使用 ConcurrentLinkedQueue 实现线程安全
    // 参考：《Java Concurrency in Practice》第5章
    private final ConcurrentLinkedQueue<long[]> vertexBufferPool;

    // 索引缓冲区池
    private final ConcurrentLinkedQueue<int[]> indexBufferPool;

    // 默认缓冲区大小
    // 预估一个区块段的最大顶点数
    // 16x16x16 = 4096 个方块，每个方块最多 6 面
    // 最多约 24000 个顶点
    private final int vertexBufferSize;
    private final int indexBufferSize;

    // 统计信息
    private final AtomicInteger vertexBuffersCreated = new AtomicInteger(0);
    private final AtomicInteger indexBuffersCreated = new AtomicInteger(0);

    /**
     * 构造函数
     *
     * @param poolSize 池大小
     * @param vertexBufferSize 顶点缓冲区大小
     * @param indexBufferSize 索引缓冲区大小
     */
    public MemoryArena(int poolSize, int vertexBufferSize, int indexBufferSize) {
        this.vertexBufferSize = vertexBufferSize;
        this.indexBufferSize = indexBufferSize;

        this.vertexBufferPool = new ConcurrentLinkedQueue<>();
        this.indexBufferPool = new ConcurrentLinkedQueue<>();

        // 预分配缓冲区
        for (int i = 0; i < poolSize; i++) {
            vertexBufferPool.offer(new long[vertexBufferSize]);
            vertexBuffersCreated.incrementAndGet();

            indexBufferPool.offer(new int[indexBufferSize]);
            indexBuffersCreated.incrementAndGet();
        }
    }

    /**
     * 默认构造函数
     */
    public MemoryArena() {
        this(DEFAULT_POOL_SIZE, 120000, 360000);
    }

    /**
     * 获取顶点缓冲区
     *
     * @return long[] 顶点缓冲区
     *
     * 如果池中有可用缓冲区，从池中取出
     * 否则创建新的缓冲区
     */
    public long[] acquireVertexBuffer() {
        long[] buffer = vertexBufferPool.poll();
        if (buffer != null) {
            return buffer;
        }

        // 池中无可用缓冲区，创建新的
        vertexBuffersCreated.incrementAndGet();
        return new long[vertexBufferSize];
    }

    /**
     * 归还顶点缓冲区
     *
     * @param buffer 顶点缓冲区
     */
    public void releaseVertexBuffer(long[] buffer) {
        // 可选：清空数据
        // Arrays.fill(buffer, 0);

        vertexBufferPool.offer(buffer);
    }

    /**
     * 获取索引缓冲区
     *
     * @return int[] 索引缓冲区
     */
    public int[] acquireIndexBuffer() {
        int[] buffer = indexBufferPool.poll();
        if (buffer != null) {
            return buffer;
        }

        indexBuffersCreated.incrementAndGet();
        return new int[indexBufferSize];
    }

    /**
     * 归还索引缓冲区
     *
     * @param buffer 索引缓冲区
     */
    public void releaseIndexBuffer(int[] buffer) {
        indexBufferPool.offer(buffer);
    }

    /**
     * 获取统计信息
     */
    public PoolStats getStats() {
        return new PoolStats(
            vertexBufferPool.size(),
            indexBufferPool.size(),
            vertexBuffersCreated.get(),
            indexBuffersCreated.get()
        );
    }

    /**
     * 清空池
     */
    public void clear() {
        vertexBufferPool.clear();
        indexBufferPool.clear();
    }

    /**
     * 池统计信息
     */
    public static class PoolStats {
        public final int availableVertexBuffers;
        public final int availableIndexBuffers;
        public final int totalVertexBuffersCreated;
        public final int totalIndexBuffersCreated;

        public PoolStats(int availableVB, int availableIB, int totalVB, int totalIB) {
            this.availableVertexBuffers = availableVB;
            this.availableIndexBuffers = availableIB;
            this.totalVertexBuffersCreated = totalVB;
            this.totalIndexBuffersCreated = totalIB;
        }
    }
}
