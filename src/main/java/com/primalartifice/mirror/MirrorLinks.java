package com.primalartifice.mirror;

import com.leclowndu93150.thaumaturge.content.device.mirror.BlockEntityMirror;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.Collection;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Items tied to a magic mirror carry Thaumaturge's {@code mirror_link} component (a dimension and position).
 * Sending only works while the mirror's chunk is loaded; otherwise the items drop as usual.
 */
public final class MirrorLinks {
    private MirrorLinks() {
    }

    /** Links the stack to this mirror, or unlinks it if it was already linked here. */
    public static void toggle(ItemStack stack, Level level, BlockPos pos, Player player) {
        GlobalPos here = GlobalPos.of(level.dimension(), pos);
        boolean unlink = here.equals(stack.get(TTDataComponents.MIRROR_LINK.get()));
        if (unlink) {
            stack.remove(TTDataComponents.MIRROR_LINK.get());
        } else {
            stack.set(TTDataComponents.MIRROR_LINK.get(), here);
        }
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.8f, unlink ? 0.7f : 1.4f);
        player.sendOverlayMessage(Component.translatable(unlink ? "primalartifice.mirror.unlinked" : "primalartifice.mirror.linked"));
    }

    /** The linked mirror, if its chunk is loaded. */
    public static BlockEntityMirror mirror(MinecraftServer server, GlobalPos link) {
        if (server == null || link == null) {
            return null;
        }
        ServerLevel level = server.getLevel(link.dimension());
        if (level == null || !level.isLoaded(link.pos())) {
            return null;
        }
        return level.getBlockEntity(link.pos()) instanceof BlockEntityMirror mirror ? mirror : null;
    }

    /** Sends every stack through the linked mirror; true if they went (the caller then drops nothing). */
    public static boolean send(MinecraftServer server, GlobalPos link, Collection<ItemStack> stacks) {
        BlockEntityMirror mirror = mirror(server, link);
        if (mirror == null) {
            return false;
        }
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                mirror.addStack(stack.copy());
            }
        }
        return true;
    }

    public static void describe(GlobalPos link, Consumer<Component> tooltip) {
        if (link == null) {
            tooltip.accept(Component.translatable("tooltip.primalartifice.mirror_unlinked").withStyle(ChatFormatting.DARK_GRAY));
        } else {
            BlockPos p = link.pos();
            tooltip.accept(Component.translatable("tooltip.primalartifice.mirror_linked", p.getX(), p.getY(), p.getZ(),
                    link.dimension().identifier().toString()).withStyle(ChatFormatting.DARK_AQUA));
        }
    }
}
