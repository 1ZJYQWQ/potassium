package me.potassium.mods.render.chunk;

/**
 * BuildOutput - 区块构建结果
 *
 * 设计思路来源：
 * - 《Java Concurrency in Practice》第6章 - 任务执行与结果
 * - 游戏引擎中常见的渲染数据结构
 *
 * 结果包含：
 * - 顶点数据
 * - 索引数据
 * - 元信息
 */
public class BuildOutput {

    // 区块坐标
    public final int sectionX;
    public final int sectionY;
    public final int sectionZ;

    // 顶点数据
    // 使用 long[] 存储打包的顶点数据
    // 每个顶点 5 个 long：
    //   - 位置 (x, y, z) 打包
    //   - UV 坐标打包
    //   - 光照数据打包
    //   - 法线数据打包
    //   - 颜色数据打包
    //
    // 参考：游戏引擎常用的顶点压缩技术
    // 使用 long 而非对象，避免对象分配开销
    private long[] vertexData;

    // 索引数据
    private int[] indexData;

    // 实际使用的顶点数
    private int vertexCount;

    // 实际使用的索引数
    private int indexCount;

    // 构建时间（纳秒）
    private long buildTimeNanos;

    /**
     * 构造函数
     */
    public BuildOutput(int sectionX, int sectionY, int sectionZ) {
        this.sectionX = sectionX;
        this.sectionY = sectionY;
        this.sectionZ = sectionZ;

        // 初始化为空，等待填充
        this.vertexData = null;
        this.indexData = null;
        this.vertexCount = 0;
        this.indexCount = 0;
        this.buildTimeNanos = 0;
    }

    /**
     * 设置顶点数据
     *
     * @param data 顶点数据数组
     * @param count 顶点数量
     */
    public void setVertexData(long[] data, int count) {
        this.vertexData = data;
        this.vertexCount = count;
    }

    /**
     * 设置索引数据
     *
     * @param data 索引数据数组
     * @param count 索引数量
     */
    public void setIndexData(int[] data, int count) {
        this.indexData = data;
        this.indexCount = count;
    }

    /**
     * 设置构建时间
     */
    public void setBuildTime(long nanos) {
        this.buildTimeNanos = nanos;
    }

    /**
     * 获取顶点数据
     */
    public long[] getVertexData() {
        return vertexData;
    }

    /**
     * 获取索引数据
     */
    public int[] getIndexData() {
        return indexData;
    }

    /**
     * 获取顶点数
     */
    public int getVertexCount() {
        return vertexCount;
    }

    /**
     * 获取索引数
     */
    public int getIndexCount() {
        return indexCount;
    }

    /**
     * 获取构建时间
     */
    public long getBuildTimeNanos() {
        return buildTimeNanos;
    }

    /**
     * 清空数据（用于复用）
     */
    public void clear() {
        // 不释放数组，仅重置计数器
        // 数组可以在下次构建时复用
        this.vertexCount = 0;
        this.indexCount = 0;
        this.buildTimeNanos = 0;
    }
}
