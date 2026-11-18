package com.primalartifice.mirror;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.primalartifice.PrimalArtifice;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/** The essentia every set of linked Mirrored Jars shares, kept with the overworld saved data. */
public final class MirroredJarPool extends SavedData {
    public record Contents(Optional<ResourceKey<IAspect>> aspect, int amount) {
        public static final Contents EMPTY = new Contents(Optional.empty(), 0);
        static final Codec<Contents> CODEC = RecordCodecBuilder.create(i -> i.group(
                ResourceKey.codec(IAspect.REGISTRY_KEY).optionalFieldOf("aspect").forGetter(Contents::aspect),
                Codec.INT.fieldOf("amount").forGetter(Contents::amount)).apply(i, Contents::new));

        public static Contents of(ResourceKey<IAspect> aspect, int amount) {
            return amount <= 0 || aspect == null ? EMPTY : new Contents(Optional.of(aspect), amount);
        }
    }

    private static final Codec<MirroredJarPool> CODEC = Codec.unboundedMap(UUIDUtil.STRING_CODEC, Contents.CODEC)
            .xmap(MirroredJarPool::new, p -> p.pools);
    public static final SavedDataType<MirroredJarPool> TYPE = new SavedDataType<>(PrimalArtifice.id("mirrored_jars"), MirroredJarPool::new, CODEC);

    private final Map<UUID, Contents> pools;

    private MirroredJarPool() {
        this(Map.of());
    }

    private MirroredJarPool(Map<UUID, Contents> pools) {
        this.pools = new HashMap<>(pools);
    }

    public static MirroredJarPool get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }

    public Contents get(UUID link) {
        return pools.get(link);
    }

    public void put(UUID link, Contents contents) {
        if (!contents.equals(pools.put(link, contents))) {
            setDirty();
        }
    }
}
