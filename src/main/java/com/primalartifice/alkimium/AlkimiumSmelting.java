package com.primalartifice.alkimium;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntityAlembic;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockEntitySmelter;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.BlockSmelter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** What the Alkimium pump and vent add to a Thaumaturge smelter; called from the smelter mixin. */
public final class AlkimiumSmelting {
    private AlkimiumSmelting() {
    }

    /** On each pump cycle, every Alkimium pump facing the smelter moves its bonus essentia into its alembics. */
    public static void pumpExtra(BlockEntitySmelter smelter) {
        Level level = smelter.getLevel();
        BlockPos pos = smelter.getBlockPos();
        Direction front = smelter.getBlockState().getValue(BlockSmelter.FACING);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (side == front) {
                continue;
            }
            BlockPos auxPos = pos.relative(side);
            BlockState aux = level.getBlockState(auxPos);
            if (!(aux.getBlock() instanceof AlkimiumSmelterAuxBlock)
                    || aux.getValue(BlockStateProperties.HORIZONTAL_FACING) != side.getOpposite()) {
                continue;
            }
            for (int i = 0; i < AlkimiumSmelterAuxBlock.BONUS_OPERATIONS && !smelter.essentiaStock.isEmpty(); i++) {
                pumpOne(smelter, level, auxPos);
            }
        }
    }

    private static void pumpOne(BlockEntitySmelter smelter, Level level, BlockPos auxPos) {
        for (AspectInstance entry : smelter.essentiaStock.entries()) {
            if (entry.amount() > 0 && intoAlembics(level, auxPos, entry.aspect())) {
                smelter.takeAspect(entry.aspect(), 1);
                return;
            }
        }
    }

    /** The alembic column above a pump: one holding this aspect first, then an empty one. */
    private static boolean intoAlembics(Level level, BlockPos base, Holder<com.leclowndu93150.thaumaturge.api.aspect.IAspect> aspect) {
        for (boolean matching : new boolean[]{true, false}) {
            for (BlockPos at = base.above(); level.getBlockEntity(at) instanceof BlockEntityAlembic alembic; at = at.above()) {
                boolean holds = alembic.amount() > 0 && aspect.is(alembic.aspectKey());
                boolean free = alembic.amount() == 0 && (alembic.aspectFilterKey() == null || aspect.is(alembic.aspectFilterKey()));
                if ((matching ? holds : free) && alembic.fill(aspect, 1) == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Scales the flux a smelt would release by each Alkimium vent facing the smelter. */
    public static float ventFlux(BlockEntitySmelter smelter, float flux) {
        Level level = smelter.getLevel();
        BlockPos pos = smelter.getBlockPos();
        Direction front = smelter.getBlockState().getValue(BlockSmelter.FACING);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState vent = level.getBlockState(pos.relative(side));
            if (side != front && vent.getBlock() instanceof AlkimiumSmelterVentBlock
                    && vent.getValue(BlockStateProperties.HORIZONTAL_FACING) == side.getOpposite()) {
                flux *= AlkimiumSmelterVentBlock.EXTRA_KEEP;
            }
        }
        return flux;
    }
}
