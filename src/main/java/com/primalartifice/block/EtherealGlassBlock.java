package com.primalartifice.block;

import com.leclowndu93150.thaumaturge.api.aspect.AspectDataMaps;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCrystalAccess;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Ethereal Glass: use a vis crystal on it to add that crystal's aspect, sneak-use an empty hand to clear it. Mobs whose
 * aspects (Thaumaturge's {@code entity_aspects} data map) include every aspect in the glass walk through it.
 */
public class EtherealGlassBlock extends PAGlassBlock implements EntityBlock {
    public EtherealGlassBlock(Properties properties) {
        super(GlassKind.ETHEREAL, properties);
    }

    @Override
    protected boolean lets(BlockGetter level, BlockPos pos, Entity entity) {
        if (entity == null || !(level.getBlockEntity(pos) instanceof EtherealGlassBlockEntity glass) || glass.aspects().isEmpty()) {
            return false;
        }
        AspectList carried = entity.typeHolder().getData(AspectDataMaps.ENTITY_ASPECTS);
        if (carried == null) {
            return false;
        }
        for (var key : glass.aspects()) {
            if (carried.aspects().stream().noneMatch(h -> h.is(key))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EtherealGlassBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof EtherealGlassBlockEntity glass) || !EssentiaCrystalAccess.isConfigured(stack)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        Holder<IAspect> aspect = EssentiaCrystalAccess.aspect(stack);
        if (!level.isClientSide() && aspect != null && aspect.unwrapKey().isPresent()) {
            if (glass.add(aspect.unwrapKey().get())) {
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8f, 1.2f);
                player.sendOverlayMessage(Component.translatable("primalartifice.glass_ethereal.added", Component.translatable("aspect." + aspect.unwrapKey().get().identifier().toLanguageKey())));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof EtherealGlassBlockEntity glass) {
            if (!level.isClientSide()) {
                glass.clear();
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 0.8f, 0.8f);
                player.sendOverlayMessage(Component.translatable("primalartifice.glass_ethereal.cleared"));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
