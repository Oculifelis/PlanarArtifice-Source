package com.primalartifice.mixin.client;

import com.leclowndu93150.thaumaturge.client.render.blockentity.HungryChestRenderState;
import com.leclowndu93150.thaumaturge.client.render.blockentity.HungryChestRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Thaumaturge draws every hungry chest with one sprite. A Primal Artifice chest (a Starving Chest) is drawn with
 * {@code primalartifice:entity/chest/<block id>} instead.
 */
@Mixin(HungryChestRenderer.class)
public abstract class HungryChestRendererMixin {
    @Redirect(method = "submit(Lcom/leclowndu93150/thaumaturge/client/render/blockentity/HungryChestRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(value = "FIELD", opcode = org.objectweb.asm.Opcodes.GETSTATIC,
                    target = "Lcom/leclowndu93150/thaumaturge/client/render/blockentity/HungryChestRenderer;SPRITE:Lnet/minecraft/client/resources/model/sprite/SpriteId;"))
    private SpriteId primalartifice$sprite(HungryChestRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        ClientLevel level = Minecraft.getInstance().level;
        Identifier id = level == null || state.blockPos == null ? null
                : BuiltInRegistries.BLOCK.getKey(level.getBlockState(state.blockPos).getBlock());
        if (id != null && id.getNamespace().equals("primalartifice")) {
            return Sheets.CHEST_MAPPER.apply(id);
        }
        return primalartifice$thaumaturge();
    }

    private static SpriteId primalartifice$hungry;

    private static SpriteId primalartifice$thaumaturge() {
        if (primalartifice$hungry == null) {
            primalartifice$hungry = Sheets.CHEST_MAPPER.apply(Identifier.fromNamespaceAndPath("thaumaturge", "hungry"));
        }
        return primalartifice$hungry;
    }
}
