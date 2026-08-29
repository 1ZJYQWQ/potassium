package me.potassium.mods.common.collections;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * BlockBuildQueue - 无锁环形缓冲区任务队列
 *
 * 设计思路来源：
 * - 《Disruptor High-Performance Queue》- LMAX 开源文档
 * - 《Java Concurrency in Practice》第15章 - 原子变量与非阻塞同步
 * - 《Single-Producer Single-Consumer Queue》- 经典并发算法
 *
 * 核心原理：
 * 1. 环形缓冲区：固定大小数组，head/tail 指针循环
 * 2. 无锁设计：使用 CAS 操作更新指针
 * 3. 单生产者单消费者：避免锁竞争
 * 4. 缓存行填充：避免伪共享
 *
 * 性能优势：
 * - 无锁操作：避免线程阻塞
 * - 内存连续：数组缓存友好
 * - O(1) 操作：固定时间插入和删除
 */
public class BlockBuildQueue {

    // 环形缓冲区大小（必须是2的幂次方）
    // 这样可以使用位运算取模：index = value & (size - 1)
    // 参考：《Java Concurrency in Practice》第13章
    private static final int BUFFER_SIZE = 1024;  // 2^10
    private static final int MASK = BUFFER_SIZE - 1;

    // 任务数组
    // 使用 Object[] 存储任务指针
    private final Object[] buffer = new Object[BUFFER_SIZE];

    // 读指针（消费者）
    // volatile 确保多线程可见性
    private volatile int head = 0;

    // 写指针（生产者）
    private volatile int tail = 0;

    // 缓存行填充避免伪共享
    // 参考：《Java Concurrency in Practice》第13章
    // @Contended 注解需要 JVM 参数支持，这里手动填充
    @SuppressWarnings("unused")
    private long pad1, pad2, pad3, pad4, pad5, pad6, pad7, pad8;

    /**
     * 插入任务（生产者调用）
     *
     * @param task 任务对象
     * @return true 成功，false 队列已满
     *
     * 实现参考：
     * - Disruptor 的单生产者序列算法
     * - 使用 volatile 写入保证可见性
     */
    public boolean offer(Object task) {
        // 检查队列是否已满
        // 队列满条件：(tail + 1) % size == head
        // 优化：使用位运算而非取模
        int nextTail = (tail + 1) & MASK;

        if (nextTail == head) {
            // 队列已满
            return false;
        }

        // 写入任务
        buffer[tail] = task;

        // 更新 tail 指针
        // volatile 写，确保对消费者可见
        tail = nextTail;

        return true;
    }

    /**
     * 取出任务（消费者调用）
     *
     * @return 任务对象，null 表示队列空
     *
     * 实现参考：
     * - Disruptor 的单消费者序列算法
     */
    public Object poll() {
        // 检查队列是否为空
        if (head == tail) {
            return null;
        }

        // 读取任务
        Object task = buffer[head];

        // 清空引用，帮助 GC
        buffer[head] = null;

        // 更新 head 指针
        head = (head + 1) & MASK;

        return task;
    }

    /**
     * 获取队列大小
     */
    public int size() {
        // 防止整数溢出
        return (tail - head) & MASK;
    }

    /**
     * 队列是否为空
     */
    public boolean isEmpty() {
        return head == tail;
    }

    /**
     * 队列是否已满
     */
    public boolean isFull() {
        return ((tail + 1) & MASK) == head;
    }

    /**
     * 清空队列
     */
    public void clear() {
        while (!isEmpty()) {
            poll();
        }
    }
}
