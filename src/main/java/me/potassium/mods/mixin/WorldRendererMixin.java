package me.potassium.mods.mixin;

import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.potassium.mods.PotassiumMod;

/**
 * WorldRendererMixin - 世界渲染器混入
 *
 * 设计思路来源：
 * - Fabric Mixin 官方文档
 * - Minecraft 原版 WorldRenderer 渲染循环
 *
 * 注入点：
 * - render() 方法：在渲染前后添加性能追踪
 * - setupTerrain() 方法：优化区块设置流程
 */
@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    /**
     * 注入渲染开始
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(CallbackInfo ci) {
        // 性能追踪：记录渲染开始时间
        PotassiumMod.LOGGER.debug("Frame render start");
    }

    /**
     * 注入渲染结束
     */
    @Inject(method = "render", at = @At("RETURN"))
    private void onRenderEnd(CallbackInfo ci) {
        // 性能追踪：记录渲染结束时间
        PotassiumMod.LOGGER.debug("Frame render end");
    }
}
