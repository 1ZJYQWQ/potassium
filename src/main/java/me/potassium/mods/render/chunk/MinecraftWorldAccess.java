package me.potassium.mods.render.chunk;

import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

/**
 * MinecraftWorldAccess - Minecraft 世界数据访问器
 *
 * 设计思路来源：
 * - Minecraft 原版公开 API
 * - Fabric API 文档
 * - 游戏引擎数据访问模式
 *
 * 功能：
 * - 从 Minecraft 世界读取数据
 * - 创建 WorldSnapshot 快照
 */
public class MinecraftWorldAccess {

    /**
     * 从 Minecraft 世界创建快照
     *
     * @param world Minecraft 世界对象
     * @param chunkX 区块 X 坐标
     * @param chunkY 区块 Y 坐标（区块段）
     * @param chunkZ 区块 Z 坐标
     * @return 世界快照
     */
    public static WorldSnapshot createSnapshot(World world, int chunkX, int chunkY, int chunkZ) {
        WorldSnapshot snapshot = new WorldSnapshot(chunkX, chunkY, chunkZ);

        // 获取区块（使用 BlockPos）
        BlockPos chunkPos = new BlockPos(chunkX << 4, 0, chunkZ << 4);
        WorldChunk chunk = world.getWorldChunk(chunkPos);

        if (chunk == null) {
            // 区块未加载
            return snapshot;
        }

        // 读取方块状态和光照
        for (int localY = 0; localY < 16; localY++) {
            for (int localZ = 0; localZ < 16; localZ++) {
                for (int localX = 0; localX < 16; localX++) {
                    // 计算世界坐标
                    int worldX = (chunkX << 4) + localX;
                    int worldY = (chunkY << 4) + localY;
                    int worldZ = (chunkZ << 4) + localZ;

                    // 获取方块状态（使用 BlockPos）
                    BlockPos pos = new BlockPos(worldX, worldY, worldZ);
                    BlockState blockState = chunk.getBlockState(pos);
                    int stateId = BlockStateIdMapper.getId(blockState);

                    snapshot.setBlockState(localX, localY, localZ, stateId);

                    // 获取光照（简化实现）
                    // 注意：Minecraft 1.21 的光照 API 可能不同
                    // 使用简化值，实际需要查询光照系统
                    int blockLight = 0;  // 简化
                    int skyLight = 15;   // 简化：假设天空光为最大值

                    // 打包光照：blockLight | (skyLight << 4)
                    int lightValue = blockLight | (skyLight << 4);
                    snapshot.setLightData(localX, localY, localZ, lightValue);
                }
            }
        }

        return snapshot;
    }

    /**
     * 区块是否为空
     */
    public static boolean isChunkEmpty(World world, int chunkX, int chunkY, int chunkZ) {
        BlockPos chunkPos = new BlockPos(chunkX << 4, 0, chunkZ << 4);
        WorldChunk chunk = world.getWorldChunk(chunkPos);
        if (chunk == null) {
            return true;
        }

        // 检查区块段是否为空
        // 简化实现：检查是否有非空气方块
        for (int y = 0; y < 16; y++) {
            for (int z = 0; z < 16; z++) {
                for (int x = 0; x < 16; x++) {
                    int worldY = (chunkY << 4) + y;
                    int worldX = (chunkX << 4) + x;
                    int worldZ = (chunkZ << 4) + z;

                    BlockPos pos = new BlockPos(worldX, worldY, worldZ);
                    BlockState state = chunk.getBlockState(pos);
                    if (!state.isAir()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }
}
