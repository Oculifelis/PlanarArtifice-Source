package com.primalartifice;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.slf4j.Logger;

/** Planar Artifice (Thaumcraft 6 addon for 1.12.2), reimplemented as a Thaumaturge addon for NeoForge 26.1.2. */
@Mod(PrimalArtifice.MODID)
public final class PrimalArtifice {
    public static final String MODID = "primalartifice";
    /** The mod id before the 2026-11-14 rename; see {@link LegacyIds}. */
    public static final String LEGACY_MODID = "planarartifice";
    public static final Logger LOG = LogUtils.getLogger();

    public PrimalArtifice(IEventBus modBus, Dist dist) {
        LegacyIds.register(modBus);
        PARegistry.register(modBus);
        PAEvents.register();
        modBus.addListener(PrimalArtifice::addPacks);
        if (dist == Dist.CLIENT) {
            com.primalartifice.client.PAClient.init(modBus);
        }
    }

    /** The Spatio, Tempus and Tinctura aspects: a built-in data pack, off unless a world turns it on. */
    private static void addPacks(AddPackFindersEvent event) {
        event.addPackFinders(id("datapacks/extra_aspects"), PackType.SERVER_DATA,
                Component.translatable("pack.primalartifice.extra_aspects.title"), PackSource.FEATURE, false, Pack.Position.TOP);
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }

    /** The same path under the old mod id. */
    public static Identifier legacyId(String path) {
        return Identifier.fromNamespaceAndPath(LEGACY_MODID, path);
    }
}
