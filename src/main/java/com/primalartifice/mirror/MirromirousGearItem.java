package com.primalartifice.mirror;

import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

/**
 * Worn Mirromirous gear (Curios): a vis discount, and an effect handled in {@link MirrorEvents} (and, for the belt's
 * hover, on the client). The slot each goes in is set by its Curios tag.
 */
public class MirromirousGearItem extends Item implements IVisDiscountGear, ICurioItem {
    private final int visDiscount;

    public MirromirousGearItem(Properties properties, int visDiscount) {
        super(properties);
        this.visDiscount = visDiscount;
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return visDiscount;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.primalartifice." + BuiltInRegistries.ITEM.getKey(this).getPath())
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.primalartifice.vis_discount", visDiscount).withStyle(ChatFormatting.DARK_PURPLE));
    }
}
