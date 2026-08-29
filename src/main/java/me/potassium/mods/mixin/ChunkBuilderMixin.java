package me.potassium.mods.mixin;

import net.minecraft.client.render.chunk.ChunkBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.potassium.mods.PotassiumMod;

/**
 * ChunkBuilderMixin - 区块构建器混入
 *
 * 设计思路来源：
 * - Fabric Mixin 官方文档
 * - Minecraft 原版 ChunkBuilder 公开 API
 *
 * 注入点：
 * - schedule() 方法：使用 Potassium 的并行构建器
 * - rebuild() 方法：优化构建流程
 */
@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {

    /**
     * 注入区块重建调度
     */
    @Inject(method = "schedule", at = @At("HEAD"), cancellable = true)
    private void onScheduleRebuild(CallbackInfo ci) {
        // TODO: 使用 Potassium 的并行调度器替换原版
        // 当前先不取消原版逻辑，待核心功能完成后启用
        PotassiumMod.LOGGER.debug("Chunk rebuild scheduled");
    }
}
