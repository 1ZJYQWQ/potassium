package me.potassium.mods.render.cull;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HierarchicalCuller - 分层遮挡剔除
 *
 * 设计思路来源：
 * - 《Real-Time Rendering》第19章 - 遮挡剔除
 * - 《Game Engine Architecture》- Jason Gregory
 * - 层次化剔除算法（Hierarchical Culling）
 *
 * 核心原理：
 * 1. 分层剔除：先剔除粗粒度 Region，再剔除细粒度 Section
 * 2. 视锥体剔除：排除视锥外的区块
 * 3. 遮挡剔除：排除被其他区块遮挡的区块
 * 4. 并行处理：使用 ForkJoinPool 并行剔除多个区域
 *
 * 性能优势：
 * - 减少不必要的区块构建
 * - 降低 CPU 和 GPU 负载
 * - 提高帧率
 */
public class HierarchicalCuller {

    // Region 大小（16x16 个 Section）
    private static final int REGION_SIZE = 16;

    // 区块数据存储
    // Region Map: regionId -> RegionData
    private final Map<Long, RegionData> regions;

    // Section Map: sectionId -> SectionData
    private final Map<Long, SectionData> sections;

    // 帧计数器
    private final AtomicInteger frameCounter = new AtomicInteger(0);

    // 结果缓存
    private final Map<Long, CullingResult> resultCache;

    /**
     * 构造函数
     */
    public HierarchicalCuller() {
        this.regions = new ConcurrentHashMap<>();
        this.sections = new ConcurrentHashMap<>();
        this.resultCache = new ConcurrentHashMap<>();
    }

    /**
     * 执行遮挡剔除
     *
     * @param view 视图信息
     * @return 剔除结果
     */
    public CullingResult cull(ViewInfo view) {
        int frame = frameCounter.incrementAndGet();

        // 步骤 1: 检查缓存
        long cacheKey = view.getCacheKey();
        CullingResult cached = resultCache.get(cacheKey);
        if (cached != null && cached.frame == frame - 1) {
            // 使用上一帧的结果（相机移动很小时）
            return cached;
        }

        // 步骤 2: 并行剔除
        Set<Long> visibleSections = performParallelCulling(view);

        // 步骤 3: 创建结果
        CullingResult result = new CullingResult(frame, regions.size(), visibleSections);

        // 步骤 4: 缓存结果
        resultCache.put(cacheKey, result);

        // 清理旧缓存
        if (resultCache.size() > 100) {
            resultCache.clear();
        }

        return result;
    }

    /**
     * 并行剔除
     *
     * 使用 ForkJoinPool 并行处理多个 Region
     */
    private Set<Long> performParallelCulling(ViewInfo view) {
        // 创建并行任务
        CullingTask task = new CullingTask(view, new HashSet<>(regions.keySet()));

        // 使用 ForkJoinPool 执行
        ForkJoinPool pool = ForkJoinPool.commonPool();
        return pool.invoke(task);
    }

    /**
     * 添加 Region
     */
    public void addRegion(long regionId, double x, double y, double z, double radius) {
        RegionData region = new RegionData(regionId, x, y, z, radius);
        regions.put(regionId, region);
    }

    /**
     * 添加 Section
     */
    public void addSection(long sectionId, long regionId, int x, int y, int z) {
        SectionData section = new SectionData(sectionId, regionId, x, y, z);
        sections.put(sectionId, section);
    }

    /**
     * 移除 Region
     */
    public void removeRegion(long regionId) {
        regions.remove(regionId);
        // 移除该 Region 下的所有 Section
        sections.entrySet().removeIf(entry -> entry.getValue().regionId == regionId);
    }

    /**
     * 视锥体剔除 - 检查 Region 是否在视锥内
     *
     * 参考：《Real-Time Rendering》第4章 - 视锥体剔除
     */
    private boolean isRegionVisible(ViewInfo view, RegionData region) {
        // 简化实现：基于距离的剔除
        // 实际游戏中需要完整的视锥体检测

        double dx = region.x - view.cameraX;
        double dy = region.y - view.cameraY;
        double dz = region.z - view.cameraZ;

        double distanceSquared = dx * dx + dy * dy + dz * dz;
        double maxDistance = view.maxDistance + region.radius;

        return distanceSquared <= maxDistance * maxDistance;
    }

    /**
     * 视锥体剔除 - 检查 Section 是否在视锥内
     */
    private boolean isSectionVisible(ViewInfo view, SectionData section) {
        // 计算 Section 的世界坐标
        double worldX = section.x * 16.0;
        double worldY = section.y * 16.0;
        double worldZ = section.z * 16.0;

        double dx = worldX - view.cameraX;
        double dy = worldY - view.cameraY;
        double dz = worldZ - view.cameraZ;

        double distanceSquared = dx * dx + dy * dy + dz * dz;
        double maxDistance = view.maxDistance;

        return distanceSquared <= maxDistance * maxDistance;
    }

    /**
     * 遮挡剔除 - 检查 Section 是否被遮挡
     *
     * 简化实现：基于距离的简化遮挡检测
     * 实际游戏中需要完整的遮挡查询
     */
    private boolean isSectionOccluded(ViewInfo view, SectionData section) {
        // TODO: 实现实际的遮挡检测
        // 当前简化版本：假设所有可见的 Section 都没有被遮挡
        return false;
    }

    /**
     * Region 数据
     */
    private static class RegionData {
        final long id;
        final double x, y, z;
        final double radius;

        RegionData(long id, double x, double y, double z, double radius) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
        }
    }

    /**
     * Section 数据
     */
    private static class SectionData {
        final long id;
        final long regionId;
        final int x, y, z;

        SectionData(long id, long regionId, int x, int y, int z) {
            this.id = id;
            this.regionId = regionId;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /**
     * 视图信息
     */
    public static class ViewInfo {
        public final double cameraX, cameraY, cameraZ;
        public final float maxDistance;

        public ViewInfo(double x, double y, double z, float maxDist) {
            this.cameraX = x;
            this.cameraY = y;
            this.cameraZ = z;
            this.maxDistance = maxDist;
        }

        public long getCacheKey() {
            // 简化：使用相机位置生成缓存键
            return (long) cameraX ^ ((long) cameraY << 20) ^ ((long) cameraZ << 40);
        }
    }

    /**
     * 剔除结果
     */
    public static class CullingResult {
        public final int frame;
        public final int regionCount;
        public final Set<Long> visibleSections;

        public CullingResult(int frame, int regionCount, Set<Long> visibleSections) {
            this.frame = frame;
            this.regionCount = regionCount;
            this.visibleSections = visibleSections;
        }
    }

    /**
     * 并行剔除任务
     *
     * 参考：《Java Concurrency in Practice》第8章 - Fork/Join 框架
     */
    private class CullingTask extends RecursiveTask<Set<Long>> {

        private final ViewInfo view;
        private final Set<Long> regionIds;

        CullingTask(ViewInfo view, Set<Long> regionIds) {
            this.view = view;
            this.regionIds = regionIds;
        }

        @Override
        protected Set<Long> compute() {
            // 如果只有一个 Region，直接处理
            if (regionIds.size() <= 1) {
                return processSingleRegion(view, regionIds.iterator().next());
            }

            // 分割任务
            Set<Long> left = new HashSet<>();
            Set<Long> right = new HashSet<>();

            int i = 0;
            for (Long regionId : regionIds) {
                if (i++ % 2 == 0) {
                    left.add(regionId);
                } else {
                    right.add(regionId);
                }
            }

            // 创建子任务
            CullingTask leftTask = new CullingTask(view, left);
            CullingTask rightTask = new CullingTask(view, right);

            // 并行执行
            leftTask.fork();
            Set<Long> rightResult = rightTask.compute();
            Set<Long> leftResult = leftTask.join();

            // 合并结果
            leftResult.addAll(rightResult);
            return leftResult;
        }

        /**
         * 处理单个 Region
         */
        private Set<Long> processSingleRegion(ViewInfo view, long regionId) {
            Set<Long> visible = new HashSet<>();

            RegionData region = regions.get(regionId);
            if (region == null) {
                return visible;
            }

            // 步骤 1: Region 级别的视锥体剔除
            if (!isRegionVisible(view, region)) {
                // 整个 Region 不可见
                return visible;
            }

            // 步骤 2: Section 级别的剔除
            for (Map.Entry<Long, SectionData> entry : sections.entrySet()) {
                SectionData section = entry.getValue();

                if (section.regionId != regionId) {
                    continue;
                }

                // 视锥体剔除
                if (!isSectionVisible(view, section)) {
                    continue;
                }

                // 遮挡剔除
                if (isSectionOccluded(view, section)) {
                    continue;
                }

                // Section 可见
                visible.add(section.id);
            }

            return visible;
        }
    }
}
