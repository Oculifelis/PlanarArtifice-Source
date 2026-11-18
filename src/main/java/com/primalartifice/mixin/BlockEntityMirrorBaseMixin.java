package com.primalartifice.mixin;

import com.leclowndu93150.thaumaturge.content.device.mirror.BlockEntityMirrorBase;
import com.primalartifice.mirror.PAMirror;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * A mirror block with a {@code primalartifice:mirror_stability} data map entry (the Flawless Mirrors) holds more
 * instability before it leaks flux, and sheds it faster, than Thaumaturge's fixed numbers.
 */
@Mixin(BlockEntityMirrorBase.class)
public abstract class BlockEntityMirrorBaseMixin {
    private PAMirror.MirrorStability primalartifice$stability() {
        return ((BlockEntityMirrorBase) (Object) this).getBlockState().getBlock().builtInRegistryHolder().getData(PAMirror.MIRROR_STABILITY);
    }

    @Redirect(method = "shedExcess", at = @At(value = "INVOKE",
            target = "Lcom/leclowndu93150/thaumaturge/content/device/mirror/BlockEntityMirrorBase;instabilityThreshold()I"))
    private int primalartifice$threshold(BlockEntityMirrorBase mirror) {
        PAMirror.MirrorStability stability = primalartifice$stability();
        return stability != null ? stability.fluxThreshold() : ((BlockEntityMirrorBaseAccessor) mirror).primalartifice$instabilityThreshold();
    }

    @ModifyConstant(method = "decayStress", constant = @Constant(longValue = 100L))
    private long primalartifice$decayInterval(long interval) {
        PAMirror.MirrorStability stability = primalartifice$stability();
        return stability != null ? stability.decayInterval() : interval;
    }
}
