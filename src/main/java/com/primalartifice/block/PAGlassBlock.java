package com.primalartifice.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A Glassworking glass. Drops itself only with silk touch or the glass cutter (loot tables). */
public class PAGlassBlock extends TransparentBlock {
    protected final GlassKind kind;

    public PAGlassBlock(GlassKind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public GlassKind kind() {
        return kind;
    }

    /** Whether this entity walks (or flies) straight through. */
    protected boolean lets(BlockGetter level, BlockPos pos, Entity entity) {
        return kind.lets(entity);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext entity && entity.getEntity() != null && lets(level, pos, entity.getEntity())) {
            return Shapes.empty();
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return kind == GlassKind.LIVING && type == PathComputationType.LAND || super.isPathfindable(state, type);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return kind == GlassKind.REDSTONE;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return kind == GlassKind.REDSTONE ? 15 : 0;
    }
}
