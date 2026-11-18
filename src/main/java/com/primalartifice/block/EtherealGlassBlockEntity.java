package com.primalartifice.block;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.primalartifice.PARegistry;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** The aspects set into a pane of Ethereal Glass, synced to clients so collision matches on both sides. */
public class EtherealGlassBlockEntity extends BlockEntity {
    private static final com.mojang.serialization.Codec<List<ResourceKey<IAspect>>> CODEC =
            ResourceKey.codec(IAspect.REGISTRY_KEY).listOf();
    private final List<ResourceKey<IAspect>> aspects = new ArrayList<>();

    public EtherealGlassBlockEntity(BlockPos pos, BlockState state) {
        super(PARegistry.ETHEREAL_GLASS_BE.get(), pos, state);
    }

    public List<ResourceKey<IAspect>> aspects() {
        return aspects;
    }

    public boolean add(ResourceKey<IAspect> aspect) {
        if (aspects.contains(aspect)) {
            return false;
        }
        aspects.add(aspect);
        changed();
        return true;
    }

    public void clear() {
        aspects.clear();
        changed();
    }

    private void changed() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.store("aspects", CODEC, List.copyOf(aspects));
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        aspects.clear();
        in.read("aspects", CODEC).ifPresent(aspects::addAll);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
