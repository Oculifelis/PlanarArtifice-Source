package com.primalartifice.mirror;

import com.leclowndu93150.thaumaturge.content.essentia.jar.AbstractBlockEntityJar;
import com.primalartifice.PAComponents;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A Thaumaturge jar whose contents are one shared pool with every jar of the same link. Each tick a jar whose
 * contents changed since it last looked writes them to the pool; otherwise it takes what the pool holds.
 */
public class MirroredJarBlockEntity extends AbstractBlockEntityJar {
    private UUID link;
    private MirroredJarPool.Contents seen;

    public MirroredJarBlockEntity(BlockPos pos, BlockState state) {
        super(PAMirror.MIRRORED_JAR_BE.get(), pos, state);
    }

    public UUID link() {
        return link;
    }

    public void setLink(UUID id) {
        link = id;
        seen = null;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MirroredJarBlockEntity jar) {
        AbstractBlockEntityJar.serverTick(level, pos, state, jar);
        if (jar.link != null && level.getServer() != null) {
            jar.share(MirroredJarPool.get(level.getServer()));
        }
    }

    private void share(MirroredJarPool pool) {
        MirroredJarPool.Contents mine = MirroredJarPool.Contents.of(aspectKey(), amount());
        MirroredJarPool.Contents shared = pool.get(link);
        if (shared == null || !mine.equals(seen)) {
            pool.put(link, mine);
            seen = mine;
        } else if (!shared.equals(mine)) {
            setStorageContents(shared.aspect().orElse(null), shared.amount());
            setChangedAndSync();
            seen = shared;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        link = input.read("link", UUIDUtil.CODEC).orElse(null);
        seen = null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("link", UUIDUtil.CODEC, link);
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (link != null) {
            components.set(PAComponents.JAR_LINK.get(), link);
        }
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        link = components.get(PAComponents.JAR_LINK.get());
        seen = null;
    }
}
