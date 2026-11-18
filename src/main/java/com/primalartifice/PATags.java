package com.primalartifice;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class PATags {
    /** Blocks the glass cutters take whole: every glass block and pane, and the port's own glass. */
    public static final TagKey<Block> CUTTABLE_GLASS = TagKey.create(Registries.BLOCK, PrimalArtifice.id("cuttable_glass"));
    public static final TagKey<Item> ALKIMIUM_INGOTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/alkimium"));
    public static final TagKey<Item> BISMUTH_INGOTS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", "ingots/bismuth"));

    private PATags() {
    }
}
