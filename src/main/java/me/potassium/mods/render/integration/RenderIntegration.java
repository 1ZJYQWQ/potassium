package me.potassium.mods.render.integration;

import me.potassium.mods.PotassiumMod;
import me.potassium.mods.render.cull.HierarchicalCuller;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

/**
 * RenderIntegration - 渲染集成
 *
 * 设计思路来源：
 * - Fabric Mixin 官方文档
 * - Minecraft 原版 WorldRenderer 公开 API
 * - 游戏引擎渲染管线集成模式
 *
 * 功能：
 * - 集成遮挡剔除
 * - 优化渲染流程
 */
public class RenderIntegration {

    // 遮挡剔除器
    private static HierarchicalCuller culler;

    // 上一帧的可见区块
    private static Set<Long> lastVisibleSections;

    /**
     * 初始化集成
     */
    public static void initialize() {
        culler = new HierarchicalCuller();
        PotassiumMod.LOGGER.info("Render integration initialized");
    }

    /**
     * 执行遮挡剔除
     *
     * @param camera 相机
     * @param renderDistance 渲染距离
     * @return 可见的区块 ID 集合
     */
    public static Set<Long> performCulling(Camera camera, float renderDistance) {
        if (culler == null) {
            return null;
        }

        // 获取相机位置
        Vec3d cameraPos = camera.getPos();
        double cameraX = cameraPos.x;
        double cameraY = cameraPos.y;
        double cameraZ = cameraPos.z;

        // 创建视图信息
        HierarchicalCuller.ViewInfo view = new HierarchicalCuller.ViewInfo(
            cameraX, cameraY, cameraZ, renderDistance * 16.0f
        );

        // 执行剔除
        HierarchicalCuller.CullingResult result = culler.cull(view);

        // 缓存结果
        lastVisibleSections = result.visibleSections;

        return result.visibleSections;
    }

    /**
     * 检查区块是否可见
     */
    public static boolean isSectionVisible(long sectionId) {
        if (lastVisibleSections == null) {
            return true; // 默认可见
        }
        return lastVisibleSections.contains(sectionId);
    }

    /**
     * 添加区块到剔除器
     */
    public static void addSection(int sectionX, int sectionY, int sectionZ) {
        if (culler == null) {
            return;
        }

        long sectionId = packSectionPos(sectionX, sectionY, sectionZ);
        long regionId = packRegionPos(sectionX >> 4, sectionY >> 4, sectionZ >> 4);

        // 添加 Region（如果不存在）
        double regionX = (sectionX >> 4) * 256.0;
        double regionY = (sectionY >> 4) * 256.0;
        double regionZ = (sectionZ >> 4) * 256.0;
        culler.addRegion(regionId, regionX, regionY, regionZ, 128.0);

        // 添加 Section
        culler.addSection(sectionId, regionId, sectionX, sectionY, sectionZ);
    }

    /**
     * 移除区块
     */
    public static void removeSection(int sectionX, int sectionY, int sectionZ) {
        if (culler == null) {
            return;
        }

        long sectionId = packSectionPos(sectionX, sectionY, sectionZ);
        // 注意：当前 HierarchicalCuller 没有 removeSection 方法
        // TODO: 添加移除功能
    }

    /**
     * 打包区块段坐标
     */
    private static long packSectionPos(int x, int y, int z) {
        return (long) x | ((long) y << 20) | ((long) z << 40);
    }

    /**
     * 打包区域坐标
     */
    private static long packRegionPos(int x, int y, int z) {
        return (long) x | ((long) y << 20) | ((long) z << 40);
    }
}
