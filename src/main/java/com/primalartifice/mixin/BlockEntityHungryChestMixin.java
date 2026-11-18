package com.primalartifice.mixin;

import com.leclowndu93150.thaumaturge.content.device.BlockEntityHungryChest;
import com.primalartifice.mirror.StarvingChests;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Hungry chests with a {@code primalartifice:starving_reach} entry (the Starving Chests) also draw items in. */
@Mixin(BlockEntityHungryChest.class)
public abstract class BlockEntityHungryChestMixin {
    @Inject(method = "serverTick", at = @At("HEAD"))
    private static void primalartifice$starve(Level level, BlockPos pos, BlockState state, BlockEntityHungryChest chest, CallbackInfo ci) {
        StarvingChests.pull(level, pos, state, chest);
    }
}
