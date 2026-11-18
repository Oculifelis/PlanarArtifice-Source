package com.primalartifice.alkimium;

import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelterVent;

/**
 * Alkimium Venting Port. Thaumaturge's smelter rolls its usual one-in-three flux filter for it, and
 * {@link AlkimiumSmelting#ventFlux} scales the rest so each Alkimium vent filters half, the original's 0.5 chance.
 */
public class AlkimiumSmelterVentBlock extends BlockSmelterVent {
    /** Flux kept after Thaumaturge's 1/3 filter: 2/3 x 0.75 = 1/2. */
    public static final float EXTRA_KEEP = 0.75f;

    public AlkimiumSmelterVentBlock(Properties properties) {
        super(properties);
    }
}
