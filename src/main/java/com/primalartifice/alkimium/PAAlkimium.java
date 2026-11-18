package com.primalartifice.alkimium;

import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelter;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.primalartifice.PARegistry;
import com.primalartifice.PrimalArtifice;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
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
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Alkimium devices. The smelteries are Thaumaturge smelters (its block entity, GUI and alembic logic) with their own
 * speed and efficiency in the {@code thaumaturge:smelter_stats} data map. The pump and vent subclass Thaumaturge's,
 * so the smelter counts them as usual, and {@link AlkimiumSmelting} adds their extra effect.
 */
public final class PAAlkimium {
    private static final UnaryOperator<Block.Properties> SMELTER = p -> p.mapColor(MapColor.COLOR_LIGHT_GREEN)
            .strength(3.0f, 17.0f).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()
            .lightLevel(s -> s.getValue(BlockSmelter.LIT) ? 13 : 0);
    private static final UnaryOperator<Block.Properties> FITTING = p -> p.mapColor(MapColor.COLOR_LIGHT_GREEN)
            .strength(2.0f, 10.0f).sound(SoundType.METAL).noOcclusion();

    public static final DeferredBlock<BlockSmelter> SMELTERY = PARegistry.block("alkimium_smeltery", BlockSmelter::new, SMELTER, Rarity.UNCOMMON);
    public static final DeferredBlock<BlockSmelter> SMELTERY_THAUMIUM = PARegistry.block("alkimium_smeltery_thaumium", BlockSmelter::new, SMELTER, Rarity.RARE);
    public static final DeferredBlock<BlockSmelter> SMELTERY_VOID = PARegistry.block("alkimium_smeltery_void", BlockSmelter::new, SMELTER, Rarity.EPIC);
    public static final DeferredBlock<AlkimiumSmelterAuxBlock> SMELTER_AUX = PARegistry.block("alkimium_smelter_aux",
            AlkimiumSmelterAuxBlock::new, FITTING, Rarity.UNCOMMON);
    public static final DeferredBlock<AlkimiumSmelterVentBlock> SMELTER_VENT = PARegistry.block("alkimium_smelter_vent",
            AlkimiumSmelterVentBlock::new, FITTING, Rarity.UNCOMMON);
    public static final DeferredBlock<AlkimiumCentrifugeBlock> CENTRIFUGE = PARegistry.block("alkimium_centrifuge",
            AlkimiumCentrifugeBlock::new, FITTING, Rarity.UNCOMMON);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AlkimiumCentrifugeBlockEntity>> CENTRIFUGE_BE =
            PARegistry.BLOCK_ENTITIES.register("alkimium_centrifuge", () -> new BlockEntityType<>(AlkimiumCentrifugeBlockEntity::new, CENTRIFUGE.get()));

    /** Helmet from the original's Alkimium armour material: 3 armour, 2 toughness, 275 durability, enchantability 20. */
    public static final ResourceKey<EquipmentAsset> GOGGLES_ASSET =
            ResourceKey.create(EquipmentAssets.ROOT_ID, PrimalArtifice.id("alkimium_goggles"));
    public static final DeferredItem<AlkimiumGogglesItem> GOGGLES = PARegistry.item("alkimium_goggles", p -> new AlkimiumGogglesItem(p
            .stacksTo(1).durability(275).enchantable(20).rarity(Rarity.RARE)
            .component(net.minecraft.core.component.DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD)
                    .setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER).setAsset(GOGGLES_ASSET).build())
            .component(TTDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE)
            .repairable(com.primalartifice.PATags.ALKIMIUM_INGOTS)
            .attributes(ItemAttributeModifiers.builder()
                    .add(Attributes.ARMOR, new AttributeModifier(PrimalArtifice.id("alkimium_goggles"), 3.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)
                    .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(PrimalArtifice.id("alkimium_goggles"), 2.0, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)
                    .build())));

    public static final DeferredRegister<GolemMaterial> GOLEM_MATERIALS = DeferredRegister.create(GolemMaterial.REGISTRY_KEY, PrimalArtifice.MODID);
    /** Original stats: 13 health, 12 armour, 4 damage; light, fireproof, blastproof and fragile. */
    public static final DeferredHolder<GolemMaterial, GolemMaterial> GOLEM_ALKIMIUM = GOLEM_MATERIALS.register("alkimium",
            () -> new GolemMaterial(List.of(PrimalArtifice.id("golem_mat_alkimium")), PrimalArtifice.id("textures/block/alkimium_block.png"),
                    0x4CD482, 13, 12, 4, () -> new ItemStack(PARegistry.ALKIMIUM_PLATE.get()), () -> new ItemStack(TTItems.MECHANISM_SIMPLE.get()),
                    List.of(TTGolemTraits.LIGHT, TTGolemTraits.FIREPROOF, TTGolemTraits.BLASTPROOF, TTGolemTraits.FRAGILE)));

    private PAAlkimium() {
    }

    /** Loads this class (so its entries join the registers) and hooks its events. */
    public static void init(IEventBus bus) {
        GOLEM_MATERIALS.register(bus);
        bus.addListener(PAAlkimium::addSmelterBlocks);
        bus.addListener(PAAlkimium::registerCapabilities);
    }

    private static void addSmelterBlocks(BlockEntityTypeAddBlocksEvent event) {
        event.modify(TTBlockEntities.SMELTER.get(), SMELTERY.get(), SMELTERY_THAUMIUM.get(), SMELTERY_VOID.get());
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(EssentiaCapabilities.TRANSPORT, CENTRIFUGE_BE.get(), (be, side) -> be);
    }
}
