package me.potassium.mods.mixin;

import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.potassium.mods.PotassiumMod;
import me.potassium.mods.render.integration.ChunkBuilderIntegration;

/**
 * ChunkBuilderMixin - 区块构建器混入
 *
 * 设计思路来源：
 * - Fabric Mixin 官方文档
 * - Minecraft 原版 ChunkBuilder 公开 API
 *
 * 注入点：
 * - setWorld() 方法：初始化集成
 * - 重建相关方法：使用 Potassium 的并行构建器
 */
@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {

    /**
     * 注入世界设置
     *
     * 注意：方法签名可能因 Minecraft 版本而异
     * 如果注入失败，需要调整方法描述符
     */
    @Inject(method = "setWorld", at = @At("RETURN"))
    private void onSetWorld(World world, CallbackInfo ci) {
        if (world != null) {
            PotassiumMod.LOGGER.info("Potassium: World set, initializing chunk builder integration");
            // 集成已在主类初始化时完成
        }
    }
}
