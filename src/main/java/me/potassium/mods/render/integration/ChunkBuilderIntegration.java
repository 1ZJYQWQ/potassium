package me.potassium.mods.render.integration;

import me.potassium.mods.PotassiumMod;
import me.potassium.mods.common.executor.AsyncBuilderPool;
import me.potassium.mods.common.executor.BlockBuildTask;
import me.potassium.mods.render.chunk.MinecraftWorldAccess;
import me.potassium.mods.render.chunk.WorldSnapshot;

import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.world.World;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ChunkBuilderIntegration - 区块构建器集成
 *
 * 设计思路来源：
 * - Fabric Mixin 官方文档
 * - Minecraft 原版 ChunkBuilder 公开 API
 * - 生产者-消费者模式
 *
 * 功能：
 * - 替换/增强 Minecraft 的区块构建
 * - 使用 Potassium 的并行构建器
 */
public class ChunkBuilderIntegration {

    // 全局构建器池
    private static AsyncBuilderPool builderPool;

    // 任务映射（跟踪构建任务）
    private static final ConcurrentHashMap<Long, BlockBuildTask> pendingTasks = new ConcurrentHashMap<>();

    /**
     * 初始化集成
     */
    public static void initialize() {
        builderPool = PotassiumMod.getBuilderPool();
        PotassiumMod.LOGGER.info("ChunkBuilder integration initialized");
    }

    /**
     * 提交区块构建任务
     *
     * @param world Minecraft 世界
     * @param chunkX 区块 X 坐标
     * @param chunkY 区块 Y 坐标（区块段）
     * @param chunkZ 区块 Z 坐标
     * @param distanceToCamera 距离相机的距离（用于优先级）
     * @param callback 完成回调
     */
    public static void submitBuildTask(World world, int chunkX, int chunkY, int chunkZ,
                                       double distanceToCamera, BuildCompletionCallback callback) {
        // 创建任务
        double priority = 1.0 / (distanceToCamera + 1.0);
        BlockBuildTask task = new BlockBuildTask(chunkX, chunkY, chunkZ, priority, (x, y, z, time) -> {
            // 任务完成回调
            pendingTasks.remove(packChunkPos(x, y, z));

            if (callback != null) {
                callback.onBuildComplete(x, y, z, time);
            }
        });

        // 创建世界快照
        WorldSnapshot snapshot = MinecraftWorldAccess.createSnapshot(world, chunkX, chunkY, chunkZ);
        task.setWorldSnapshot(snapshot);

        // 提交到线程池
        builderPool.submit(task);

        // 记录待处理任务
        pendingTasks.put(packChunkPos(chunkX, chunkY, chunkZ), task);
    }

    /**
     * 检查区块是否正在构建
     */
    public static boolean isBuilding(int chunkX, int chunkY, int chunkZ) {
        return pendingTasks.containsKey(packChunkPos(chunkX, chunkY, chunkZ));
    }

    /**
     * 取消构建任务
     */
    public static void cancelBuild(int chunkX, int chunkY, int chunkZ) {
        pendingTasks.remove(packChunkPos(chunkX, chunkY, chunkZ));
    }

    /**
     * 获取待处理任务数
     */
    public static int getPendingTaskCount() {
        return pendingTasks.size();
    }

    /**
     * 打包区块坐标
     */
    private static long packChunkPos(int x, int y, int z) {
        return (long) x | ((long) y << 20) | ((long) z << 40);
    }

    /**
     * 构建完成回调接口
     */
    @FunctionalInterface
    public interface BuildCompletionCallback {
        void onBuildComplete(int chunkX, int chunkY, int chunkZ, long buildTimeNanos);
    }
}
