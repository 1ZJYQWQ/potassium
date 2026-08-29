package me.potassium.mods.render.chunk;

/**
 * MeshBuilder - 网格构建器
 *
 * 设计思路来源：
 * - 《Game Engine Architecture》- Jason Gregory
 * - 《Real-Time Rendering》- Akenine-Möller
 * - 游戏引擎中常见的网格生成模式
 *
 * 功能：
 * - 遍历方块生成顶点
 * - 剔除隐藏面
 * - 打包顶点数据
 */
public class MeshBuilder {

    // 顶点格式（每个顶点的数据）
    // 使用 long 打包：
    // - 位 0-15: 位置 X (相对区块)
    // - 位 16-31: 位置 Y
    // - 位 32-47: 位置 Z
    // - 位 48-63: 法线/其他数据

    // 顶点缓冲区
    private long[] vertexBuffer;
    private int vertexCount;

    // 索引缓冲区
    private int[] indexBuffer;
    private int indexCount;

    // 面剔除缓存
    // 用于快速判断相邻方块是否遮挡
    private final boolean[] solidCache;

    /**
     * 构造函数
     */
    public MeshBuilder(int maxVertices, int maxIndices) {
        this.vertexBuffer = new long[maxVertices];
        this.indexBuffer = new int[maxIndices];
        this.vertexCount = 0;
        this.indexCount = 0;

        // 16x16x16 = 4096 个方块的遮挡缓存
        this.solidCache = new boolean[4096];
    }

    /**
     * 构建区块网格
     *
     * @param snapshot 世界快照
     * @param output 输出缓冲区
     */
    public void build(WorldSnapshot snapshot, BuildOutput output) {
        // 重置
        vertexCount = 0;
        indexCount = 0;

        // 步骤 1: 构建遮挡缓存
        buildSolidCache(snapshot);

        // 步骤 2: 遍历方块生成网格
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    int blockState = snapshot.getBlockState(x, y, z);

                    // 跳过空气方块
                    if (blockState == 0) {
                        continue;
                    }

                    // 生成方块面
                    generateBlockFaces(snapshot, x, y, z, blockState);
                }
            }
        }

        // 步骤 3: 复制数据到输出
        output.setVertexData(vertexBuffer, vertexCount);
        output.setIndexData(indexBuffer, indexCount);
    }

    /**
     * 构建遮挡缓存
     *
     * 判断每个方块是否是实心的（会遮挡相邻面）
     */
    private void buildSolidCache(WorldSnapshot snapshot) {
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    int index = (y << 8) | (z << 4) | x;
                    int state = snapshot.getBlockState(x, y, z);

                    // 简化判断：非空气即为实心
                    // TODO: 更精确的遮挡判断（透明方块、非完整方块等）
                    solidCache[index] = (state != 0);
                }
            }
        }
    }

    /**
     * 生成方块的面
     *
     * 参考：《Real-Time Rendering》第12章 - 几何处理
     */
    private void generateBlockFaces(WorldSnapshot snapshot, int x, int y, int z, int blockState) {
        // 检查 6 个方向的面
        // 上 (Y+)
        if (shouldRenderFace(x, y + 1, z)) {
            addFace(x, y, z, Direction.UP, blockState, snapshot);
        }

        // 下 (Y-)
        if (shouldRenderFace(x, y - 1, z)) {
            addFace(x, y, z, Direction.DOWN, blockState, snapshot);
        }

        // 北 (Z-)
        if (shouldRenderFace(x, y, z - 1)) {
            addFace(x, y, z, Direction.NORTH, blockState, snapshot);
        }

        // 南 (Z+)
        if (shouldRenderFace(x, y, z + 1)) {
            addFace(x, y, z, Direction.SOUTH, blockState, snapshot);
        }

        // 西 (X-)
        if (shouldRenderFace(x - 1, y, z)) {
            addFace(x, y, z, Direction.WEST, blockState, snapshot);
        }

        // 东 (X+)
        if (shouldRenderFace(x + 1, y, z)) {
            addFace(x, y, z, Direction.EAST, blockState, snapshot);
        }
    }

    /**
     * 判断是否应该渲染该面
     *
     * 如果相邻位置是实心方块，则该面被遮挡，不需要渲染
     */
    private boolean shouldRenderFace(int nx, int ny, int nz) {
        // 边界检查
        if (nx < 0 || nx >= 16 || ny < 0 || ny >= 16 || nz < 0 || nz >= 16) {
            // 区块边界，需要渲染（相邻区块可能没有加载）
            return true;
        }

        int index = (ny << 8) | (nz << 4) | nx;
        return !solidCache[index];
    }

    /**
     * 添加一个面到网格
     */
    private void addFace(int x, int y, int z, Direction dir, int blockState, WorldSnapshot snapshot) {
        // 获取光照
        int light = snapshot.getLightData(x, y, z);

        // 添加 4 个顶点（一个四边形）
        int baseVertex = vertexCount;

        // 根据方向生成顶点
        // 简化实现：每个面 4 个顶点
        for (int i = 0; i < 4; i++) {
            long vertex = packVertex(x, y, z, dir, light, i);
            vertexBuffer[vertexCount++] = vertex;
        }

        // 添加 2 个三角形（6 个索引）
        indexBuffer[indexCount++] = baseVertex + 0;
        indexBuffer[indexCount++] = baseVertex + 1;
        indexBuffer[indexCount++] = baseVertex + 2;

        indexBuffer[indexCount++] = baseVertex + 2;
        indexBuffer[indexCount++] = baseVertex + 3;
        indexBuffer[indexCount++] = baseVertex + 0;
    }

    /**
     * 打包顶点数据
     */
    private long packVertex(int x, int y, int z, Direction dir, int light, int cornerIndex) {
        // 简化实现：打包位置和法线
        // 实际游戏中还需要 UV、颜色等

        long packed = 0;

        // 打包位置（根据方向和角索引调整）
        // TODO: 实现精确的顶点位置计算

        packed |= (long) x;
        packed |= (long) y << 16;
        packed |= (long) z << 32;
        packed |= (long) dir.ordinal() << 48;
        packed |= (long) light << 56;

        return packed;
    }

    /**
     * 方向枚举
     */
    private enum Direction {
        DOWN, UP, NORTH, SOUTH, WEST, EAST
    }

    /**
     * 重置构建器（用于复用）
     */
    public void reset() {
        vertexCount = 0;
        indexCount = 0;
    }
}
