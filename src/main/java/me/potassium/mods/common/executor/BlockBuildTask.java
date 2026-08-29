package me.potassium.mods.common.executor;

import me.potassium.mods.render.chunk.WorldSnapshot;
import me.potassium.mods.render.chunk.MeshBuilder;
import me.potassium.mods.render.chunk.BuildOutput;

/**
 * BlockBuildTask - 区块构建任务
 *
 * 设计思路来源：
 * - 《Java Concurrency in Practice》第6章 - 任务执行
 * - Minecraft 原版 ChunkBuilder 设计（公开 API）
 *
 * 任务包含：
 * - 区块坐标 (x, y, z)
 * - 世界数据快照
 * - 优先级
 * - 构建结果
 */
public class BlockBuildTask {

    // 区块坐标
    public final int sectionX;
    public final int sectionY;
    public final int sectionZ;

    // 优先级
    // 计算公式：priority = 1.0 / (distance + 1.0)
    // 参考：游戏引擎中常用的 LOD 优先级计算
    public final double priority;

    // 世界快照（由主线程填充）
    private WorldSnapshot worldSnapshot;

    // 构建结果（输出）
    private BuildOutput buildOutput;

    // 构建器（复用）
    private MeshBuilder meshBuilder;

    // 构建结果回调
    private BuildCallback callback;

    /**
     * 构造函数
     */
    public BlockBuildTask(int sectionX, int sectionY, int sectionZ, double priority, BuildCallback callback) {
        this.sectionX = sectionX;
        this.sectionY = sectionY;
        this.sectionZ = sectionZ;
        this.priority = priority;
        this.callback = callback;

        // 初始化构建器（可复用）
        this.meshBuilder = new MeshBuilder(120000, 360000);
        this.buildOutput = new BuildOutput(sectionX, sectionY, sectionZ);
    }

    /**
     * 设置世界快照
     */
    public void setWorldSnapshot(WorldSnapshot snapshot) {
        this.worldSnapshot = snapshot;
    }

    /**
     * 执行构建任务
     *
     * 这是任务的核心执行方法
     * 在工作线程中调用
     *
     * 实现参考：
     * - 《Game Engine Architecture》- Jason Gregory
     * - 《Real-Time Rendering》- Akenine-Möller
     */
    public void execute() {
        long startTime = System.nanoTime();

        // 检查是否有世界快照
        if (worldSnapshot == null) {
            // 没有数据，跳过构建
            long duration = System.nanoTime() - startTime;
            if (callback != null) {
                callback.onComplete(sectionX, sectionY, sectionZ, duration);
            }
            return;
        }

        try {
            // 步骤 1: 检查区块是否为空
            if (worldSnapshot.isEmpty()) {
                // 空区块，无需构建
                buildOutput.setBuildTime(System.nanoTime() - startTime);
                notifyComplete(startTime);
                return;
            }

            // 步骤 2: 重置构建器
            meshBuilder.reset();

            // 步骤 3: 构建网格
            // 遍历方块生成顶点，剔除隐藏面
            meshBuilder.build(worldSnapshot, buildOutput);

            // 步骤 4: 记录构建时间
            long duration = System.nanoTime() - startTime;
            buildOutput.setBuildTime(duration);

            // 步骤 5: 回调通知完成
            notifyComplete(startTime);

        } catch (Exception e) {
            // 构建失败，记录错误
            // 实际项目中应该使用日志系统
            System.err.println("Block build failed at (" + sectionX + ", " + sectionY + ", " + sectionZ + "): " + e.getMessage());
            e.printStackTrace();

            // 仍然通知完成（带错误）
            notifyComplete(startTime);
        }
    }

    /**
     * 通知完成
     */
    private void notifyComplete(long startTime) {
        long duration = System.nanoTime() - startTime;
        if (callback != null) {
            callback.onComplete(sectionX, sectionY, sectionZ, duration);
        }
    }

    /**
     * 获取构建结果
     */
    public BuildOutput getBuildOutput() {
        return buildOutput;
    }

    /**
     * 构建完成回调接口
     */
    @FunctionalInterface
    public interface BuildCallback {
        void onComplete(int sectionX, int sectionY, int sectionZ, long buildTimeNanos);
    }
}
