package com.primalartifice.alkimium;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Alkimium Centrifuge: compound essentia in from below, every one of its components out of the top. */
public class AlkimiumCentrifugeBlock extends BaseEntityBlock {
    public static final MapCodec<AlkimiumCentrifugeBlock> CODEC = simpleCodec(AlkimiumCentrifugeBlock::new);
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    public AlkimiumCentrifugeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlkimiumCentrifugeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, PAAlkimium.CENTRIFUGE_BE.get(), AlkimiumCentrifugeBlockEntity::serverTick);
    }
}
