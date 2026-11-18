package com.primalartifice.alkimium;

import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * Alkimium Goggles of Revealing: a helmet (or Curios head item) with Thaumaturge's revealing upgrade and an 8% vis
 * discount. While sneaking, the HUD also lists the essentia and suction of the block looked at ({@code PAClient}).
 */
public class AlkimiumGogglesItem extends Item implements IVisDiscountGear, ICurioItem {
    public static final int VIS_DISCOUNT = 8;

    public AlkimiumGogglesItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return VIS_DISCOUNT;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.primalartifice.alkimium_goggles").withStyle(ChatFormatting.GRAY));
    }
}
