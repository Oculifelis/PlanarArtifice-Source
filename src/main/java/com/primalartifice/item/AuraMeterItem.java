package com.primalartifice.item;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * Aura Meter: while it is anywhere in the inventory or in a Curios charm slot, the chunk's vis and flux gauge shows on
 * the HUD (drawn by {@code client.PAClient}). Sneak to read the numbers.
 */
public class AuraMeterItem extends Item implements ICurioItem {
    public AuraMeterItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.primalartifice.aura_meter").withStyle(ChatFormatting.GRAY));
    }
}
