package com.primalartifice.mirror;

import com.leclowndu93150.thaumaturge.content.device.mirror.BlockEntityMirror;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;

/** Mirrored Amulet: use it on a magic mirror to bind it there; when you die, your drops go through that mirror. */
public class MirroredAmuletItem extends MirromirousGearItem {
    public MirroredAmuletItem(Properties properties) {
        super(properties, 3);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof BlockEntityMirror)) {
            return super.useOn(context);
        }
        if (!context.getLevel().isClientSide() && context.getPlayer() != null) {
            MirrorLinks.toggle(context.getItemInHand(), context.getLevel(), context.getClickedPos(), context.getPlayer());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        MirrorLinks.describe(stack.get(TTDataComponents.MIRROR_LINK.get()), tooltip);
    }
}
