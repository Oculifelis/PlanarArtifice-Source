package com.primalartifice.alkimium;

import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelterAux;

/**
 * Alkimium Auxiliary Slurry Pump. Thaumaturge's smelter treats it as its own pump (one extra essentia per cycle into
 * the alembics above it); {@link AlkimiumSmelting#pumpExtra} adds the original's two bonus operations on top.
 */
public class AlkimiumSmelterAuxBlock extends BlockSmelterAux {
    /** Extra essentia this pump moves each smelter cycle, beyond Thaumaturge's one. */
    public static final int BONUS_OPERATIONS = 2;

    public AlkimiumSmelterAuxBlock(Properties properties) {
        super(properties);
    }
}
