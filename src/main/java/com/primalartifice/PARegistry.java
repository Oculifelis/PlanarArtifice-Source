package com.primalartifice;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.content.wands.ItemWandCap;
import com.primalartifice.block.EtherealGlassBlock;
import com.primalartifice.block.EtherealGlassBlockEntity;
import com.primalartifice.block.GlassKind;
import com.primalartifice.block.PAGlassBlock;
import com.primalartifice.item.AuraMeterItem;
import com.primalartifice.item.GlassCutterItem;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class PARegistry {
    private static final String MODID = PrimalArtifice.MODID;

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<WandCap> CAPS = DeferredRegister.create(WandCap.REGISTRY_KEY, MODID);

    private static final List<Supplier<? extends Item>> TAB_ITEMS = new ArrayList<>();

    // ---- Materials ----
    public static final DeferredItem<Item> ALKIMIUM_INGOT = item("alkimium_ingot", p -> new Item(p.rarity(Rarity.RARE)));
    public static final DeferredItem<Item> ALKIMIUM_NUGGET = item("alkimium_nugget", p -> new Item(p.rarity(Rarity.RARE)));
    public static final DeferredItem<Item> ALKIMIUM_PLATE = item("alkimium_plate", p -> new Item(p.rarity(Rarity.RARE)));
    public static final DeferredItem<Item> BISMUTH_INGOT = item("bismuth_ingot", p -> new Item(p.rarity(Rarity.RARE)));
    public static final DeferredItem<Item> BISMUTH_NUGGET = item("bismuth_nugget", p -> new Item(p.rarity(Rarity.RARE)));
    public static final DeferredItem<Item> BISMUTH_PLATE = item("bismuth_plate", p -> new Item(p.rarity(Rarity.RARE)));
    public static final DeferredBlock<Block> ALKIMIUM_BLOCK = block("alkimium_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_GREEN).strength(5.0f, 15.0f).sound(SoundType.METAL).requiresCorrectToolForDrops(), Rarity.RARE);
    public static final DeferredBlock<Block> BISMUTH_BLOCK = block("bismuth_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_PINK).strength(5.0f, 15.0f).sound(SoundType.METAL).requiresCorrectToolForDrops(), Rarity.RARE);
    public static final DeferredBlock<Block> ALKIMIC_CONSTRUCT = block("alkimic_construct", Block::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_GREEN).strength(5.0f, 15.0f).sound(SoundType.METAL).requiresCorrectToolForDrops()
                    .noOcclusion(), Rarity.RARE);

    // ---- Glassworking ----
    public static final Map<GlassKind, DeferredBlock<? extends Block>> GLASS = new EnumMap<>(GlassKind.class);

    static {
        for (GlassKind kind : GlassKind.values()) {
            if (kind == GlassKind.ETHEREAL) {
                GLASS.put(kind, block(kind.id(), EtherealGlassBlock::new, kind::properties, kind.rarity()));
            } else {
                GLASS.put(kind, block(kind.id(), p -> new PAGlassBlock(kind, p), kind::properties, kind.rarity()));
            }
        }
    }

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EtherealGlassBlockEntity>> ETHEREAL_GLASS_BE =
            BLOCK_ENTITIES.register("glass_ethereal", () -> new BlockEntityType<>(EtherealGlassBlockEntity::new, GLASS.get(GlassKind.ETHEREAL).get()));

    public static final DeferredItem<GlassCutterItem> GLASS_CUTTER = item("glass_cutter",
            p -> new GlassCutterItem(p.durability(100).repairable(net.neoforged.neoforge.common.Tags.Items.GEMS_QUARTZ), false));
    public static final DeferredItem<GlassCutterItem> GLASS_CUTTER_VIS = item("glass_cutter_vis",
            p -> new GlassCutterItem(p.stacksTo(1).rarity(Rarity.RARE)
                    .component(com.leclowndu93150.thaumaturge.registry.TTDataComponents.RECHARGEABLE.get(),
                            new com.leclowndu93150.thaumaturge.api.items.ChargeProfile(100,
                                    com.leclowndu93150.thaumaturge.api.items.ChargeDisplay.ON_CHANGE)), true));

    // ---- Bismuth ----
    public static final DeferredItem<AuraMeterItem> AURA_METER = item("aura_meter", p -> new AuraMeterItem(p.stacksTo(1).rarity(Rarity.RARE)));
    /** Diamond-tier sword material from the original (4 / 2081 / 10.0 / 2.5 / 25). */
    public static final ToolMaterial BISMUTH_MATERIAL = new ToolMaterial(net.minecraft.tags.BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            2081, 10.0f, 2.5f, 25, PATags.BISMUTH_INGOTS);
    public static final DeferredItem<Item> BISMUTH_CLAYMORE = item("bismuth_claymore",
            p -> new Item(p.sword(BISMUTH_MATERIAL, 3.0f, -2.4f).rarity(Rarity.RARE)));
    private static final net.minecraft.world.food.FoodProperties FRUIT_FOOD =
            new net.minecraft.world.food.FoodProperties.Builder().nutrition(6).saturationModifier(1.4f).alwaysEdible().build();

    private static net.minecraft.world.item.component.Consumable fruit(boolean enchanted) {
        int[] t = enchanted ? new int[]{600, 2, 7200, 1, 7200, 2400, 3} : new int[]{200, 1, 1200, 0, 1200, 2400, 1};
        return net.minecraft.world.item.component.Consumables.defaultFood()
                .onConsume(new net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect(List.of(
                        new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, t[0], t[1]),
                        new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.RESISTANCE, t[2], t[3]),
                        new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, t[4], 0),
                        new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, t[5], t[6]))))
                .build();
    }

    /** Golden-apple food (6 / 1.4, always edible); a minute of enchanted-apple effects, or 20% more than one. */
    public static final DeferredItem<Item> THAUMATURGES_FRUIT = item("thaumaturges_fruit",
            p -> new Item(p.rarity(Rarity.RARE).food(FRUIT_FOOD, fruit(false))));
    public static final DeferredItem<Item> THAUMATURGES_FRUIT_ENCHANTED = item("thaumaturges_fruit_enchanted",
            p -> new Item(p.rarity(Rarity.EPIC).food(FRUIT_FOOD, fruit(true))
                    .component(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)));

    /** The Bismuth Caster's Gauntlet became a wand cap: 10% off everything and 15% off the primals. */
    public static final DeferredHolder<WandCap, WandCap> CAP_BISMUTH = CAPS.register("bismuth", () -> new WandCap(0.9f,
            List.of(TTAspects.AER, TTAspects.TERRA, TTAspects.IGNIS, TTAspects.AQUA, TTAspects.ORDO, TTAspects.PERDITIO),
            0.85f, 6, PrimalArtifice.id("textures/models/wand_cap_bismuth.png")));
    public static final DeferredItem<Item> WAND_CAP_BISMUTH_INERT = item("wand_cap_bismuth_inert", p -> new Item(p.rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<ItemWandCap> WAND_CAP_BISMUTH = item("wand_cap_bismuth",
            p -> new ItemWandCap(p.rarity(Rarity.RARE), CAP_BISMUTH));
    public static final DeferredItem<com.leclowndu93150.thaumaturge.content.spell.item.FocusItem> FLAWLESS_FOCUS = item("flawless_focus",
            p -> new com.leclowndu93150.thaumaturge.content.spell.item.FocusItem(p.stacksTo(1).rarity(Rarity.EPIC)));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.primalartifice"))
            .icon(() -> new ItemStack(BISMUTH_INGOT.get()))
            .displayItems((params, out) -> TAB_ITEMS.forEach(i -> out.accept(i.get())))
            .build());

    private PARegistry() {
    }

    /** Registers a block and its item, shown in the Primal Artifice tab. */
    public static <B extends Block> DeferredBlock<B> block(String name, Function<Block.Properties, B> factory,
                                                           UnaryOperator<Block.Properties> props, Rarity rarity) {
        DeferredBlock<B> block = BLOCKS.registerBlock(name, factory, props);
        TAB_ITEMS.add(ITEMS.registerSimpleBlockItem(block, p -> p.rarity(rarity)));
        return block;
    }

    /** Registers an item that shows in the Primal Artifice tab. */
    public static <I extends Item> DeferredItem<I> item(String name, Function<Item.Properties, I> factory) {
        DeferredItem<I> item = ITEMS.registerItem(name, factory);
        TAB_ITEMS.add(item);
        return item;
    }

    static void register(IEventBus bus) {
        com.primalartifice.alkimium.PAAlkimium.init(bus);
        com.primalartifice.mirror.PAMirror.init(bus);
        BLOCKS.register(bus);
        ITEMS.register(bus);
        BLOCK_ENTITIES.register(bus);
        TABS.register(bus);
        CAPS.register(bus);
        PAComponents.register(bus);
    }
}
