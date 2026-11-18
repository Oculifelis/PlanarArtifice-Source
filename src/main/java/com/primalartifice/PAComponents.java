package com.primalartifice;

import com.mojang.serialization.Codec;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Item state is kept in data components, never NBT. */
public final class PAComponents {
    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, PrimalArtifice.MODID);

    /** Cuts left from the last vis charge an Alchemical Glass Cutter spent. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CUTTER_ENERGY = register("cutter_energy",
            b -> b.persistent(Codec.intRange(0, 64)).networkSynchronized(ByteBufCodecs.VAR_INT));

    /** The shared pool a Mirrored Jar (placed or as an item) belongs to. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<java.util.UUID>> JAR_LINK = register("jar_link",
            b -> b.persistent(net.minecraft.core.UUIDUtil.CODEC).networkSynchronized(net.minecraft.core.UUIDUtil.STREAM_CODEC));

    private PAComponents() {
    }

    private static <T> DeferredHolder<DataComponentType<?>, DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return COMPONENTS.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }

    static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
