package me.potassium.mods.render.chunk;

/**
 * WorldSnapshot - 世界数据快照
 *
 * 设计思路来源：
 * - 《Game Engine Architecture》- Jason Gregory
 * - 游戏引擎中常见的数据快照模式
 *
 * 目的：
 * - 避免多线程访问主世界数据
 * - 提供只读视图给工作线程
 * - 确保数据一致性
 */
public class WorldSnapshot {

    // 区块坐标
    public final int chunkX;
    public final int chunkY;
    public final int chunkZ;

    // 方块状态数据
    // 使用 long[] 存储打包的方块状态
    // 每个 long 存储 4 个方块状态（每个 16 位）
    // 每个区块段 16x16x16 = 4096 个方块
    // 4096 / 4 = 1024 个 long
    private final long[] blockStates;

    // 光照数据
    // 使用 int[] 存储光照信息
    // 每个方块的光照：block light (4 bits) + sky light (4 bits) = 8 bits
    // 可以打包到 int[] 中，每个 int 存储 4 个方块的光照
    private final int[] lightData;

    // 区块是否为空
    private final boolean isEmpty;

    /**
     * 构造函数
     */
    public WorldSnapshot(int chunkX, int chunkY, int chunkZ) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;
        this.chunkZ = chunkZ;

        // 初始化数组
        this.blockStates = new long[1024];
        this.lightData = new int[1024];
        this.isEmpty = false;
    }

    /**
     * 获取方块状态
     *
     * @param localX 区块内 X 坐标 (0-15)
     * @param localY 区块内 Y 坐标 (0-15)
     * @param localZ 区块内 Z 坐标 (0-15)
     * @return 方块状态 ID
     */
    public int getBlockState(int localX, int localY, int localZ) {
        // 计算索引
        // 索引 = y << 8 | z << 4 | x
        int index = (localY << 8) | (localZ << 4) | localX;

        // 计算在 long 数组中的位置
        int longIndex = index >> 2;  // index / 4
        int bitOffset = (index & 3) << 4;  // (index % 4) * 16

        // 提取 16 位方块状态
        long packed = blockStates[longIndex];
        return (int) ((packed >> bitOffset) & 0xFFFF);
    }

    /**
     * 设置方块状态
     */
    public void setBlockState(int localX, int localY, int localZ, int stateId) {
        int index = (localY << 8) | (localZ << 4) | localX;
        int longIndex = index >> 2;
        int bitOffset = (index & 3) << 4;

        // 清除旧值并设置新值
        long mask = ~(0xFFFFL << bitOffset);
        blockStates[longIndex] = (blockStates[longIndex] & mask) | ((long) stateId << bitOffset);
    }

    /**
     * 获取光照数据
     *
     * @return 打包的光照值：block light (低 4 位) + sky light (高 4 位)
     */
    public int getLightData(int localX, int localY, int localZ) {
        int index = (localY << 8) | (localZ << 4) | localX;
        int intIndex = index >> 2;
        int bitOffset = (index & 3) << 3;

        int packed = lightData[intIndex];
        return (packed >> bitOffset) & 0xFF;
    }

    /**
     * 设置光照数据
     */
    public void setLightData(int localX, int localY, int localZ, int lightValue) {
        int index = (localY << 8) | (localZ << 4) | localX;
        int intIndex = index >> 2;
        int bitOffset = (index & 3) << 3;

        int mask = ~(0xFF << bitOffset);
        lightData[intIndex] = (lightData[intIndex] & mask) | ((lightValue & 0xFF) << bitOffset);
    }

    /**
     * 区块是否为空
     */
    public boolean isEmpty() {
        return isEmpty;
    }

    /**
     * 获取方块状态的原始数组（用于快速复制）
     */
    public long[] getBlockStatesArray() {
        return blockStates;
    }

    /**
     * 获取光照数据的原始数组
     */
    public int[] getLightDataArray() {
        return lightData;
    }
}
