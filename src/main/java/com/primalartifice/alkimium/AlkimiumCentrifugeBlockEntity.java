package com.primalartifice.alkimium;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Works like Thaumaturge's centrifuge (draws one compound from the tube below every 5 ticks, spins it for 39 ticks,
 * suction 128 when idle and 64 while busy, stopped by redstone) but splits the compound into all of its components,
 * which leave through the top one at a time.
 */
public class AlkimiumCentrifugeBlockEntity extends AbstractSyncedBlockEntity implements IEssentiaTransport {
    private static final Codec<ResourceKey<IAspect>> KEY = ResourceKey.codec(IAspect.REGISTRY_KEY);
    private static final int PROCESS_TICKS = 39;
    private static final int DRAW_INTERVAL = 5;

    private ResourceKey<IAspect> input;
    private final List<ResourceKey<IAspect>> output = new ArrayList<>();
    private int count;
    private int process;

    public AlkimiumCentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(PAAlkimium.CENTRIFUGE_BE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AlkimiumCentrifugeBlockEntity be) {
        if (level.hasNeighborSignal(pos)) {
            return;
        }
        if (be.output.isEmpty() && be.input == null && ++be.count % DRAW_INTERVAL == 0) {
            be.draw();
        }
        if (be.process > 0) {
            be.process--;
        }
        if (be.output.isEmpty() && be.input != null && be.process == 0) {
            be.split();
        }
    }

    private void draw() {
        IEssentiaTransport below = EssentiaFlowHandler.transport(level, worldPosition.below(), Direction.UP);
        if (below == null || !below.canOutputTo(Direction.UP) || below.getEssentiaAmount(Direction.UP) <= 0
                || below.getSuctionAmount(Direction.UP) >= getSuctionAmount(Direction.DOWN)
                || getSuctionAmount(Direction.DOWN) < below.getMinimumSuction()) {
            return;
        }
        Holder<IAspect> type = below.getEssentiaType(Direction.UP);
        if (type != null && !type.value().isPrimal() && below.takeEssentia(type, 1, Direction.UP) == 1) {
            start(type);
        }
    }

    private void start(Holder<IAspect> type) {
        input = type.unwrapKey().orElse(null);
        process = PROCESS_TICKS;
        setChangedAndSync();
    }

    private void split() {
        Optional<Holder.Reference<IAspect>> held = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(input);
        input = null;
        held.ifPresent(h -> h.value().components().forEach(c -> c.unwrapKey().ifPresent(output::add)));
        setChangedAndSync();
    }

    private Holder<IAspect> head() {
        return output.isEmpty() ? null
                : level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(output.getFirst()).orElse(null);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == Direction.UP || face == Direction.DOWN;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public void setSuction(Holder<IAspect> aspect, int amount) {
    }

    @Override
    public Holder<IAspect> getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        if (face != Direction.DOWN || level == null || level.hasNeighborSignal(worldPosition)) {
            return 0;
        }
        return input == null && output.isEmpty() ? 128 : 64;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canOutputTo(face) || amount < 1 || output.isEmpty() || !aspect.is(output.getFirst())) {
            return 0;
        }
        output.removeFirst();
        setChangedAndSync();
        return 1;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (input != null || !output.isEmpty() || amount < 1 || aspect.value().isPrimal()) {
            return 0;
        }
        start(aspect);
        return 1;
    }

    @Override
    public Holder<IAspect> getEssentiaType(Direction face) {
        return face == Direction.UP ? head() : null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return face == Direction.UP && !output.isEmpty() ? 1 : 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        out.storeNullable("input", KEY, input);
        out.store("output", KEY.listOf(), List.copyOf(output));
        out.putInt("process", process);
    }

    @Override
    protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        input = in.read("input", KEY).orElse(null);
        output.clear();
        in.read("output", KEY.listOf()).ifPresent(output::addAll);
        process = in.getIntOr("process", 0);
    }
}
