package com.primalartifice.mirror;

import com.leclowndu93150.thaumaturge.content.essentia.jar.AbstractBlockEntityJar;
import com.leclowndu93150.thaumaturge.content.essentia.jar.AbstractBlockJar;
import com.mojang.serialization.MapCodec;
import com.primalartifice.PAComponents;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Mirrored Jar. Use a Mirrored Jar item on a placed one to tie them: the placed jar gets a link if it had none, and
 * the item copies it, so the jar it becomes shares the same essentia wherever it is placed.
 */
public class MirroredJarBlock extends AbstractBlockJar {
    public static final MapCodec<MirroredJarBlock> CODEC = simpleCodec(MirroredJarBlock::new);

    public MirroredJarBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<MirroredJarBlock> codec() {
        return CODEC;
    }

    @Override
    protected BlockEntityType<? extends AbstractBlockEntityJar> jarType() {
        return PAMirror.MIRRORED_JAR_BE.get();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MirroredJarBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, PAMirror.MIRRORED_JAR_BE.get(), MirroredJarBlockEntity::tick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(PAMirror.MIRRORED_JAR_ITEM.get()) || player.isShiftKeyDown()) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MirroredJarBlockEntity jar) {
            UUID link = jar.link();
            if (link == null) {
                link = UUID.randomUUID();
                jar.setLink(link);
            }
            stack.set(PAComponents.JAR_LINK.get(), link);
            player.sendOverlayMessage(Component.translatable("primalartifice.mirrored_jar.linked"));
        }
        return InteractionResult.SUCCESS;
    }
}
