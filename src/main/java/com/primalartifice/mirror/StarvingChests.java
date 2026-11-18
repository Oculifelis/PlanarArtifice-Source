package com.primalartifice.mirror;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Starving Chests: hungry chests that draw dropped items towards themselves from {@code radius} blocks round and above
 * (1 to 4 by size), as long as they have room for them. The hungry chest then eats what lands on it.
 */
public final class StarvingChests {
    private static final double PULL = 0.05;

    private StarvingChests() {
    }

    public static void pull(Level level, BlockPos pos, BlockState state, Container chest) {
        PAMirror.StarvingReach reach = state.getBlock().builtInRegistryHolder().getData(PAMirror.STARVING_REACH);
        if (reach == null) {
            return;
        }
        int r = reach.radius();
        AABB area = new AABB(pos.getX() - r, pos.getY(), pos.getZ() - r, pos.getX() + r + 1, pos.getY() + r + 1, pos.getZ() + r + 1);
        Vec3 centre = Vec3.atCenterOf(pos);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area, e -> e.isAlive() && hasRoom(chest, e.getItem()))) {
            Vec3 to = centre.subtract(item.position());
            double dist = to.length();
            if (dist < 0.01) {
                continue;
            }
            Vec3 motion = item.getDeltaMovement();
            // Stop any drift away from the chest, then pull a little towards it.
            double mx = motion.x * to.x < 0 ? 0 : motion.x;
            double my = motion.y * to.y < 0 ? 0 : motion.y;
            double mz = motion.z * to.z < 0 ? 0 : motion.z;
            item.setDeltaMovement(mx + PULL * to.x / dist, my + PULL * to.y / dist, mz + PULL * to.z / dist);
            item.hurtMarked = true;
        }
    }

    private static boolean hasRoom(Container chest, ItemStack stack) {
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack slot = chest.getItem(i);
            if (slot.isEmpty() || ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }
}
