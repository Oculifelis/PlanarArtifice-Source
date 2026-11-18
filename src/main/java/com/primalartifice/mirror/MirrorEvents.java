package com.primalartifice.mirror;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.content.device.mirror.BlockEntityMirror;
import com.leclowndu93150.thaumaturge.content.research.ResearchGrants;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.primalartifice.PAEvents;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;

/** Game-bus handlers for the Mirrored enchantment and the worn Mirromirous gear. */
public final class MirrorEvents {
    /** Amulet links of players who just died, read at death (before Curios drops the amulet) and used for the drops. */
    private static final Map<UUID, GlobalPos> DYING = new HashMap<>();

    private MirrorEvents() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(MirrorEvents::linkTool);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, MirrorEvents::blockDrops);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGHEST, MirrorEvents::death);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, MirrorEvents::livingDrops);
        NeoForge.EVENT_BUS.addListener(MirrorEvents::headband);
        NeoForge.EVENT_BUS.addListener(MirrorEvents::belt);
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> DYING.clear());
    }

    public static Optional<ItemStack> worn(LivingEntity entity, Item item) {
        return CuriosApi.getCuriosInventory(entity).flatMap(c -> c.findFirstCurio(item)).map(SlotResult::stack);
    }

    private static GlobalPos mirroredLink(Player player, ItemStack tool) {
        return PAEvents.level(player, tool, PAEvents.MIRRORED) > 0 ? tool.get(TTDataComponents.MIRROR_LINK.get()) : null;
    }

    /** Sneak-use a Mirrored tool on a magic mirror to link it there (again to unlink). */
    private static void linkTool(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        ItemStack held = event.getItemStack();
        if (!player.isShiftKeyDown() || PAEvents.level(player, held, PAEvents.MIRRORED) <= 0
                || !(event.getLevel().getBlockEntity(event.getPos()) instanceof BlockEntityMirror)) {
            return;
        }
        if (!event.getLevel().isClientSide()) {
            MirrorLinks.toggle(held, event.getLevel(), event.getPos(), player);
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private static void blockDrops(BlockDropsEvent event) {
        if (!(event.getBreaker() instanceof Player player)) {
            return;
        }
        GlobalPos link = mirroredLink(player, event.getTool());
        if (link != null && MirrorLinks.send(event.getLevel().getServer(), link, event.getDrops().stream().map(ItemEntity::getItem).toList())) {
            event.getDrops().clear();
        }
    }

    private static void death(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            worn(player, PAMirror.MIRRORED_AMULET.get()).map(s -> s.get(TTDataComponents.MIRROR_LINK.get()))
                    .ifPresent(link -> DYING.put(player.getUUID(), link));
        }
    }

    /** A Mirrored weapon sends a kill's drops to its mirror; a Mirrored Amulet sends its dead wearer's. */
    private static void livingDrops(LivingDropsEvent event) {
        List<ItemStack> stacks = event.getDrops().stream().map(ItemEntity::getItem).toList();
        if (event.getEntity() instanceof ServerPlayer dead) {
            GlobalPos link = DYING.remove(dead.getUUID());
            if (link != null && MirrorLinks.send(dead.level().getServer(), link, stacks)) {
                event.setCanceled(true);
            }
            return;
        }
        if (event.isRecentlyHit() && event.getSource().getEntity() instanceof ServerPlayer killer) {
            GlobalPos link = mirroredLink(killer, killer.getMainHandItem());
            if (link != null && MirrorLinks.send(killer.level().getServer(), link, stacks)) {
                event.setCanceled(true);
            }
        }
    }

    /**
     * Mirromirous Headband: like the Curiosity Band it halves experience orbs, but the half it keeps back turns into
     * knowledge twice as often and twice as much (2 theory at 10% per point, else 2 observation at 40% per point).
     */
    private static void headband(PlayerXpEvent.PickupXp event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || worn(player, PAMirror.MIRROMIROUS_HEADBAND.get()).isEmpty()) {
            return;
        }
        ExperienceOrb orb = event.getOrb();
        int kept = orb.getValue() / 2;
        if (kept < 1) {
            return;
        }
        orb.setValue(orb.getValue() - kept);
        float roll = player.getRandom().nextFloat();
        if (roll < 0.1f * kept) {
            ResearchGrants.grantConvertedKnowledge(player, KnowledgeType.THEORY, 2);
        } else if (roll < 0.4f * kept) {
            ResearchGrants.grantConvertedKnowledge(player, KnowledgeType.OBSERVATION, 2);
        }
    }

    /** Belt of Suspension: no fall damage. Its hover and slow sink are driven by the wearer's client. */
    private static void belt(LivingFallEvent event) {
        if (worn(event.getEntity(), PAMirror.SUSPENSION_BELT.get()).isPresent()) {
            event.setCanceled(true);
        }
    }
}
