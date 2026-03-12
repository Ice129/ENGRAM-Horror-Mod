package horror.blueice129.feature;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public class LilyDamage {

    private static final int H_RADIUS = 3;
    private static final int V_RADIUS = 1;
    private static final int DURABILITY_DRAIN_PER_FLOWER = 2;

    private static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    public static void applyToPlayer(ServerWorld world, ServerPlayerEntity player) {
        int flowers = countNearbyFlowers(world, player.getBlockPos());
        if (flowers == 0) return;

        int drain = flowers * DURABILITY_DRAIN_PER_FLOWER;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack armor = player.getEquippedStack(slot);
            if (!armor.isEmpty() && armor.isDamageable()) {
                armor.damage(drain, player, p -> p.sendEquipmentBreakStatus(slot));
            }
        }
    }

    private static int countNearbyFlowers(ServerWorld world, BlockPos center) {
        int count = 0;
        for (int x = -H_RADIUS; x <= H_RADIUS; x++) {
            for (int z = -H_RADIUS; z <= H_RADIUS; z++) {
                for (int y = -V_RADIUS; y <= V_RADIUS; y++) {
                    if (world.getBlockState(center.add(x, y, z)).isOf(Blocks.LILY_OF_THE_VALLEY)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
}
