package com.primalartifice.mirror;

import com.leclowndu93150.thaumaturge.content.device.BlockHungryChest;
import com.leclowndu93150.thaumaturge.content.device.mirror.BlockMirror;
import com.leclowndu93150.thaumaturge.content.device.mirror.ItemBlockMirror;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.primalartifice.PARegistry;
import com.primalartifice.PrimalArtifice;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/**
 * The Mirromirous arts and the Starving Chests. Flawless mirrors and starving chests are Thaumaturge's own mirror and
 * hungry chest blocks under new ids (so its block entities, renderers and hand mirror work on them); what makes them
 * better comes from two block data maps read by small mixins.
 */
public final class PAMirror {
    /** Instability a mirror holds before it releases one flux, and the ticks between each point of it decaying. */
    public record MirrorStability(int fluxThreshold, int decayInterval) {
        public static final Codec<MirrorStability> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.intRange(1, 1 << 20).fieldOf("flux_threshold").forGetter(MirrorStability::fluxThreshold),
                Codec.intRange(1, 1 << 20).fieldOf("decay_interval").forGetter(MirrorStability::decayInterval)).apply(i, MirrorStability::new));
    }

    /** How far round a hungry chest it pulls dropped items in (it still eats only what lands on it). */
    public record StarvingReach(int radius) {
        public static final Codec<StarvingReach> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.intRange(1, 16).fieldOf("radius").forGetter(StarvingReach::radius)).apply(i, StarvingReach::new));
    }

    public static final DataMapType<Block, MirrorStability> MIRROR_STABILITY = DataMapType.builder(
            PrimalArtifice.id("mirror_stability"), Registries.BLOCK, MirrorStability.CODEC).build();
    public static final DataMapType<Block, StarvingReach> STARVING_REACH = DataMapType.builder(
            PrimalArtifice.id("starving_reach"), Registries.BLOCK, StarvingReach.CODEC).build();

    private static final UnaryOperator<Block.Properties> MIRROR = p -> p.mapColor(MapColor.METAL).strength(0.1f, 10.0f)
            .sound(SoundType.STONE).noOcclusion();
    private static final UnaryOperator<Block.Properties> CHEST = p -> p.mapColor(MapColor.WOOD).strength(2.5f).sound(SoundType.WOOD);

    public static final DeferredBlock<BlockMirror> FLAWLESS_MIRROR = PARegistry.BLOCKS.registerBlock("flawless_mirror",
            p -> new BlockMirror(p, false), MIRROR);
    public static final DeferredBlock<BlockMirror> FLAWLESS_MIRROR_ESSENTIA = PARegistry.BLOCKS.registerBlock("flawless_mirror_essentia",
            p -> new BlockMirror(p, true), MIRROR);
    public static final DeferredItem<ItemBlockMirror> FLAWLESS_MIRROR_ITEM = PARegistry.item("flawless_mirror",
            p -> new ItemBlockMirror(FLAWLESS_MIRROR.get(), p.useBlockDescriptionPrefix().rarity(Rarity.EPIC)));
    public static final DeferredItem<ItemBlockMirror> FLAWLESS_MIRROR_ESSENTIA_ITEM = PARegistry.item("flawless_mirror_essentia",
            p -> new ItemBlockMirror(FLAWLESS_MIRROR_ESSENTIA.get(), p.useBlockDescriptionPrefix().rarity(Rarity.EPIC)));

    public static final List<DeferredBlock<BlockHungryChest>> STARVING_CHESTS = List.of(
            PARegistry.block("starving_chest_small", BlockHungryChest::new, CHEST, Rarity.UNCOMMON),
            PARegistry.block("starving_chest_medium", BlockHungryChest::new, CHEST, Rarity.UNCOMMON),
            PARegistry.block("starving_chest_large", BlockHungryChest::new, CHEST, Rarity.RARE),
            PARegistry.block("starving_chest_huge", BlockHungryChest::new, CHEST, Rarity.RARE));

    public static final DeferredBlock<MirroredJarBlock> MIRRORED_JAR = PARegistry.BLOCKS.registerBlock("mirrored_jar", MirroredJarBlock::new,
            p -> p.mapColor(MapColor.NONE).strength(0.3f).sound(SoundType.GLASS).noOcclusion());
    public static final DeferredItem<MirroredJarItem> MIRRORED_JAR_ITEM = PARegistry.item("mirrored_jar",
            p -> new MirroredJarItem(MIRRORED_JAR.get(), p.useBlockDescriptionPrefix().rarity(Rarity.RARE)));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MirroredJarBlockEntity>> MIRRORED_JAR_BE =
            PARegistry.BLOCK_ENTITIES.register("mirrored_jar", () -> new BlockEntityType<>(MirroredJarBlockEntity::new, MIRRORED_JAR.get()));

    public static final DeferredItem<MirromirousGearItem> SUSPENSION_BELT = PARegistry.item("suspension_belt",
            p -> new MirromirousGearItem(p.stacksTo(1).rarity(Rarity.RARE), 5));
    public static final DeferredItem<MirromirousGearItem> MIRROMIROUS_HEADBAND = PARegistry.item("mirromirous_headband",
            p -> new MirromirousGearItem(p.stacksTo(1).rarity(Rarity.RARE), 3));
    public static final DeferredItem<MirroredAmuletItem> MIRRORED_AMULET = PARegistry.item("mirrored_amulet",
            p -> new MirroredAmuletItem(p.stacksTo(1).rarity(Rarity.RARE)));

    private PAMirror() {
    }

    /** Loads this class (so its entries join the registers) and hooks its events. */
    public static void init(IEventBus bus) {
        bus.addListener(PAMirror::addBlocks);
        bus.addListener(PAMirror::registerCapabilities);
        bus.addListener((RegisterDataMapTypesEvent e) -> {
            e.register(MIRROR_STABILITY);
            e.register(STARVING_REACH);
        });
        MirrorEvents.register();
    }

    private static void addBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(TTBlockEntities.MIRROR.get(), FLAWLESS_MIRROR.get());
        event.modify(TTBlockEntities.MIRROR_ESSENTIA.get(), FLAWLESS_MIRROR_ESSENTIA.get());
        event.modify(TTBlockEntities.HUNGRY_CHEST.get(), STARVING_CHESTS.stream().map(b -> (Block) b.get()).toArray(Block[]::new));
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, MIRRORED_JAR_BE.get(), (be, side) -> be);
        event.registerBlockEntity(EssentiaCapabilities.STORAGE, MIRRORED_JAR_BE.get(), (be, side) -> be.storage(side));
    }
}
