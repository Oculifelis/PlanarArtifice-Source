package com.primalartifice.mixin;

import com.leclowndu93150.thaumaturge.content.device.mirror.BlockEntityMirrorBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Calls the protected threshold of a mirror that has no Flawless data. */
@Mixin(BlockEntityMirrorBase.class)
public interface BlockEntityMirrorBaseAccessor {
    @Invoker("instabilityThreshold")
    int primalartifice$instabilityThreshold();
}
