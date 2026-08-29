package me.potassium.mods.render.chunk;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * BlockStateIdMapper - 方块状态 ID 映射器
 *
 * 设计思路来源：
 * - Minecraft 原版公开 API
 * - Fabric API 文档
 *
 * 功能：
 * - 将 BlockState 转换为整数 ID
 * - 用于快速比较和数据打包
 */
public class BlockStateIdMapper {

    // 空气方块的 ID
    public static final int AIR_ID = 0;

    /**
     * 获取方块状态的 ID
     *
     * @param state 方块状态
     * @return 整数 ID
     */
    public static int getId(BlockState state) {
        if (state == null || state.isAir()) {
            return AIR_ID;
        }

        // 使用 Minecraft 的注册表获取 ID
        // 注意：这是简化实现，实际游戏中 BlockState 的序列化更复杂
        try {
            // 使用 Registries 访问（Minecraft 1.21）
            return Registries.BLOCK.getRawId(state.getBlock());
        } catch (Exception e) {
            return AIR_ID;
        }
    }

    /**
     * 根据 ID 获取方块状态
     *
     * @param id 整数 ID
     * @return 方块状态
     */
    public static BlockState getState(int id) {
        if (id == AIR_ID) {
            return null;
        }

        try {
            return Registries.BLOCK.get(id).getDefaultState();
        } catch (Exception e) {
            return null;
        }
    }
}
