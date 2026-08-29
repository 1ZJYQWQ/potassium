package me.potassium.mods;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import me.potassium.mods.common.executor.AsyncBuilderPool;

/**
 * Potassium 模组主入口
 *
 * 设计思路来源：
 * - Fabric MDK 模板 (公开文档)
 * - Fabric API 文档
 */
public class PotassiumMod implements ClientModInitializer {

    public static final String MOD_ID = "potassium";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // 全局工作线程池
    // 设计参考：《Java Concurrency in Practice》第8章 - 线程池
    private static AsyncBuilderPool builderPool;

    @Override
    public void onInitializeClient() {
        LOGGER.info("====================================");
        LOGGER.info("  Potassium 渲染优化模组启动中...");
        LOGGER.info("  基于 SPECIFICATION.md 独立开发");
        LOGGER.info("====================================");

        // 初始化工作线程池
        // 线程数公式参考：《Java Concurrency in Practice》
        // 线程数 = N_cpu - 2 (保留2个核心给主线程和系统)
        int cpuCores = Runtime.getRuntime().availableProcessors();
        int workerThreads = Math.max(1, cpuCores - 2);

        LOGGER.info("检测到 CPU 核心数: {}", cpuCores);
        LOGGER.info("工作线程数: {}", workerThreads);

        builderPool = new AsyncBuilderPool(workerThreads);

        LOGGER.info("Potassium 初始化完成!");
    }

    /**
     * 获取全局构建器池
     */
    public static AsyncBuilderPool getBuilderPool() {
        return builderPool;
    }

    /**
     * 关闭资源
     */
    public static void shutdown() {
        if (builderPool != null) {
            builderPool.shutdown();
            LOGGER.info("Potassium 资源已释放");
        }
    }
}
