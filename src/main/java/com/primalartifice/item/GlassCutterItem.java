package com.primalartifice.item;

import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.primalartifice.PAComponents;
import com.primalartifice.PATags;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Glass Cutter: use it on glass to take the block whole, as silk touch would. The plain cutter wears out (100 uses,
 * mended with quartz). The alchemical one runs on vis charge from the Recharge Pedestal, one charge per five cuts.
 */
public class GlassCutterItem extends Item {
    private static final int CUTS_PER_CHARGE = 5;
    private final boolean vis;

    public GlassCutterItem(Properties properties, boolean vis) {
        super(properties);
        this.vis = vis;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (!state.is(PATags.CUTTABLE_GLASS)) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (vis && !hasEnergy(stack)) {
            if (player != null && !context.getLevel().isClientSide()) {
                player.sendOverlayMessage(Component.translatable("primalartifice.glass_cutter.empty"));
            }
            return InteractionResult.FAIL;
        }
        if (context.getLevel() instanceof ServerLevel level) {
            BlockPos pos = context.getClickedPos();
            ItemStack silk = new ItemStack(Items.DIAMOND_PICKAXE);
            silk.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
            var drops = Block.getDrops(state, level, pos, level.getBlockEntity(pos), player, silk);
            SoundType sound = state.getSoundType();
            level.removeBlock(pos, false);
            level.playSound(null, pos, sound.getBreakSound(), SoundSource.PLAYERS, sound.getVolume(), sound.getPitch());
            drops.forEach(drop -> Block.popResource(level, pos, drop));
            if (vis) {
                spend(stack, player);
            } else if (player != null) {
                stack.hurtAndBreak(1, player, context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                        ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static boolean hasEnergy(ItemStack stack) {
        return stack.getOrDefault(PAComponents.CUTTER_ENERGY.get(), 0) > 0 || RechargeAccess.getCharge(stack) > 0;
    }

    /** Each charge is worth five cuts, banked in the cutter's energy. */
    private static void spend(ItemStack stack, Player player) {
        int energy = stack.getOrDefault(PAComponents.CUTTER_ENERGY.get(), 0);
        if (energy <= 0 && player != null && RechargeAccess.consumeCharge(stack, player, 1)) {
            energy = CUTS_PER_CHARGE;
        }
        stack.set(PAComponents.CUTTER_ENERGY.get(), Math.max(0, energy - 1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.primalartifice.glass_cutter").withStyle(ChatFormatting.GRAY));
    }
}
