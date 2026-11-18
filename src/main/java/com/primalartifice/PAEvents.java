package com.primalartifice;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;

/** Game-bus handlers for the enchantments and worn gear. */
public final class PAEvents {
    public static final ResourceKey<Enchantment> METAPHIZE = ResourceKey.create(Registries.ENCHANTMENT, PrimalArtifice.id("metaphize"));
    public static final ResourceKey<Enchantment> MIRRORED = ResourceKey.create(Registries.ENCHANTMENT, PrimalArtifice.id("mirrored"));

    private PAEvents() {
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, PAEvents::metaphize);
    }

    public static int level(Player player, net.minecraft.world.item.ItemStack stack, ResourceKey<Enchantment> key) {
        return player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
                .map(h -> EnchantmentHelper.getItemEnchantmentLevel(h, stack)).orElse(0);
    }

    /**
     * Metaphize: experience from a mob you kill becomes vis in its chunk instead, 30% of it per level, as long as the
     * chunk's vis and flux together stay under 110% of its base.
     */
    private static void metaphize(LivingExperienceDropEvent event) {
        Player player = event.getAttackingPlayer();
        if (player == null || player.level().isClientSide()) {
            return;
        }
        int lvl = level(player, player.getMainHandItem(), METAPHIZE);
        float vis = event.getDroppedExperience() * lvl * 0.3f;
        if (vis <= 0) {
            return;
        }
        BlockPos pos = event.getEntity().blockPosition();
        var level = player.level();
        if (AuraHelper.getVis(level, pos) + AuraHelper.getFlux(level, pos) < AuraHelper.getAuraBase(level, pos) * 1.1f) {
            AuraHelper.addVis(level, pos, vis);
            event.setCanceled(true);
        }
    }
}
