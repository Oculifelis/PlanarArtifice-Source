package com.primalartifice.compat;

import com.primalartifice.PrimalArtifice;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * JEI: information pages for the items whose use is not a recipe (glass that lets things through, the cutters, the
 * worn gear, the machines). Arcane, crucible and infusion recipes are Thaumaturge's own types, which its plugin shows.
 * Loaded only with JEI.
 */
@JeiPlugin
public final class PAJeiPlugin implements IModPlugin {
    /** Every item with a {@code jei.primalartifice.info.<name>} page; missing items (later phases) are skipped. */
    public static final String[] INFO = {"glass_entity", "glass_non_living", "glass_ethereal", "glass_hardened", "glass_light",
            "glass_redstone", "glass_cutter", "glass_cutter_vis", "aura_meter", "bismuth_claymore",
            "wand_cap_bismuth", "flawless_focus", "alkimium_smeltery", "alkimium_smeltery_thaumium", "alkimium_smeltery_void",
            "alkimium_smelter_aux", "alkimium_smelter_vent", "alkimium_centrifuge", "alkimium_goggles", "starving_chest_small",
            "starving_chest_medium", "starving_chest_large", "starving_chest_huge", "suspension_belt", "mirromirous_headband",
            "mirrored_amulet", "mirrored_jar", "flawless_mirror", "flawless_mirror_essentia"};

    @Override
    public Identifier getPluginUid() {
        return PrimalArtifice.id("jei");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        for (String name : INFO) {
            BuiltInRegistries.ITEM.getOptional(PrimalArtifice.id(name)).ifPresent(item ->
                    registration.addIngredientInfo(item, Component.translatable("jei.primalartifice.info." + name)));
        }
    }
}
