package com.primalartifice.mixin;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntitySmelter;
import com.primalartifice.alkimium.AlkimiumSmelting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hooks the Alkimium pump and vent into Thaumaturge's smelter, which only knows its own by class. */
@Mixin(BlockEntitySmelter.class)
public abstract class BlockEntitySmelterMixin {
    @Shadow
    private int tickCounter;
    @Shadow
    private boolean speedBoost;

    /** Same cycle as the smelter's own hand-over to alembics: every {@code ventInterval} ticks, 20% sooner with a speed boost. */
    @Inject(method = "serverTick", at = @At("TAIL"))
    private void primalartifice$alkimiumPump(ServerLevel server, BlockPos pos, BlockState state, CallbackInfo ci) {
        BlockEntitySmelter self = (BlockEntitySmelter) (Object) this;
        if (self.essentiaStock.isEmpty()) {
            return;
        }
        int speed = self.ventInterval();
        if (speedBoost) {
            speed = Math.max(1, (int) (speed * 0.8));
        }
        if (speed > 0 && tickCounter % speed == 0) {
            AlkimiumSmelting.pumpExtra(self);
        }
    }

    /** The flux a smelt pollutes with (points no vent caught) is cut by each Alkimium vent, as a float like on 1.0.2. */
    @Redirect(method = "smeltOne", at = @At(value = "INVOKE",
            target = "Lcom/leclowndu93150/thaumaturge/content/essentia/smeltery/BlockEntitySmelter;pollute(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;I)V"))
    private void primalartifice$alkimiumVent(ServerLevel server, BlockPos pos, int points) {
        if (points > 0) {
            AuraHelper.polluteAura(server, pos, AlkimiumSmelting.ventFlux((BlockEntitySmelter) (Object) this, points), true);
        }
    }
}
