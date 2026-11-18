package com.primalartifice.block;

import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/**
 * The eight glasses of Glassworking, with the original's properties: hardened glass shrugs off explosions, light glass
 * glows, redstone glass is a power source, and three let some entities through. The original's dark glass is cut:
 * vanilla tinted glass does the same job (blocks all light, stays see-through).
 */
public enum GlassKind {
    CLEAR("glass_clear", Rarity.COMMON, null),
    CRYSTAL("glass_crystal", Rarity.COMMON, null),
    HARDENED("glass_hardened", Rarity.COMMON, null),
    LIGHT("glass_light", Rarity.COMMON, null),
    REDSTONE("glass_redstone", Rarity.COMMON, null),
    /** Living things pass, everything else bumps into it. */
    LIVING("glass_entity", Rarity.RARE, e -> e instanceof LivingEntity),
    /** Items, arrows and other non-living things pass. */
    NON_LIVING("glass_non_living", Rarity.RARE, e -> !(e instanceof LivingEntity)),
    /** Mobs that carry every aspect set into the glass pass (see {@link EtherealGlassBlock}). */
    ETHEREAL("glass_ethereal", Rarity.RARE, null);

    private final String id;
    private final Rarity rarity;
    private final Predicate<Entity> passes;

    GlassKind(String id, Rarity rarity, Predicate<Entity> passes) {
        this.id = id;
        this.rarity = rarity;
        this.passes = passes;
    }

    public String id() {
        return id;
    }

    public Rarity rarity() {
        return rarity;
    }

    public boolean lets(Entity entity) {
        return passes != null && entity != null && passes.test(entity);
    }

    public boolean permeable() {
        return passes != null;
    }

    public Block.Properties properties(Block.Properties p) {
        p = p.mapColor(MapColor.NONE).sound(SoundType.GLASS).noOcclusion()
                .isValidSpawn((s, l, pos, e) -> false).isRedstoneConductor((s, l, pos) -> false)
                .isSuffocating((s, l, pos) -> false).isViewBlocking((s, l, pos) -> false);
        return switch (this) {
            case HARDENED -> p.strength(3.0f, 2000.0f);
            case LIGHT -> p.strength(0.3f).lightLevel(s -> 15);
            default -> p.strength(0.3f);
        };
    }
}
