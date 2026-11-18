package com.primalartifice.mirror;

import com.leclowndu93150.thaumaturge.content.essentia.jar.JarItem;
import com.primalartifice.PAComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/** Sneak-use in the air to clear the item link. */
public class MirroredJarItem extends JarItem {
    public MirroredJarItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown() || !stack.has(PAComponents.JAR_LINK.get())) {
            return super.use(level, player, hand);
        }
        if (!level.isClientSide()) {
            stack.remove(PAComponents.JAR_LINK.get());
            player.sendOverlayMessage(Component.translatable("primalartifice.mirrored_jar.cleared"));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable(stack.has(PAComponents.JAR_LINK.get())
                ? "tooltip.primalartifice.mirrored_jar.linked" : "tooltip.primalartifice.mirrored_jar.unlinked").withStyle(ChatFormatting.GRAY));
    }
}
