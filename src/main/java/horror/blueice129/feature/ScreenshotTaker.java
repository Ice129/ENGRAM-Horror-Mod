package horror.blueice129.feature;

import horror.blueice129.HorrorMod129;
import horror.blueice129.network.ModNetworking;
import horror.blueice129.utils.LineOfSightUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import java.util.HashMap;
import java.util.Map;

public class ScreenshotTaker {

    private static final Map<BatEntity, Integer> pendingDiscards = new HashMap<>();
    private static final int BAT_DISCARD_DELAY_TICKS = 10;

    private static final int MIN_DISTANCE = 10;
    private static final int MAX_DISTANCE = 20;
    private static final int MAX_ATTEMPTS = 100;
    private static final int VERTICAL_RANGE = 10;
    private static final Random RANDOM = Random.create();

    public static void takeScreenshotOfPlayer(ServerPlayerEntity player) {
        HorrorMod129.LOGGER.info("ScreenshotTaker: Starting screenshot attempt for player " + player.getName().getString());
        ServerWorld world = player.getServerWorld();
        BlockPos playerPos = player.getBlockPos();
        HorrorMod129.LOGGER.info("ScreenshotTaker: Player position: " + playerPos);

        BlockPos cameraPos = findSuitableCameraPosition(world, player, playerPos);
        if (cameraPos == null) {
            HorrorMod129.LOGGER.warn("ScreenshotTaker: Failed to find suitable camera position after " + MAX_ATTEMPTS + " attempts");
            return;
        }

        HorrorMod129.LOGGER.info("ScreenshotTaker: Found camera position: " + cameraPos);

        BatEntity bat = spawnCameraBat(world, cameraPos, player);
        if (bat == null) {
            HorrorMod129.LOGGER.warn("ScreenshotTaker: Failed to spawn camera bat");
            return;
        }

        HorrorMod129.LOGGER.info("ScreenshotTaker: Spawned bat with ID " + bat.getId() + " at " + cameraPos);

        ModNetworking.sendEntityScreenshot(player, bat.getId());
        HorrorMod129.LOGGER.info("ScreenshotTaker: Sent screenshot packet to player, bat will be discarded in " + BAT_DISCARD_DELAY_TICKS + " ticks");

        pendingDiscards.put(bat, BAT_DISCARD_DELAY_TICKS);
    }

    public static void tick() {
        pendingDiscards.entrySet().removeIf(entry -> {
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                entry.getKey().discard();
                HorrorMod129.LOGGER.info("ScreenshotTaker: Discarded bat entity " + entry.getKey().getId());
                return true;
            }
            entry.setValue(remaining);
            return false;
        });
    }

    private static BlockPos findSuitableCameraPosition(ServerWorld world, ServerPlayerEntity player, BlockPos playerPos) {
        HorrorMod129.LOGGER.info("ScreenshotTaker: Searching for camera position...");
        int positionsChecked = 0;
        int airCheckFailures = 0;
        int losFailures = 0;
        
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            int distance = MIN_DISTANCE + RANDOM.nextInt(MAX_DISTANCE - MIN_DISTANCE);
            double angle = RANDOM.nextDouble() * 2 * Math.PI;
            int offsetX = (int) (Math.cos(angle) * distance);
            int offsetZ = (int) (Math.sin(angle) * distance);

            int x = playerPos.getX() + offsetX;
            int z = playerPos.getZ() + offsetZ;

            for (int yOffset = -VERTICAL_RANGE; yOffset <= VERTICAL_RANGE; yOffset++) {
                int y = playerPos.getY() + yOffset;
                BlockPos candidatePos = new BlockPos(x, y, z);
                positionsChecked++;

                if (!world.isAir(candidatePos) || !world.isAir(candidatePos.up())) {
                    airCheckFailures++;
                    continue;
                }

                if (hasLineOfSightFromBatToPlayer(world, candidatePos, player)) {
                    HorrorMod129.LOGGER.info("ScreenshotTaker: Found position after checking " + positionsChecked + " positions (air check failures: " + airCheckFailures + ", LOS failures: " + losFailures + ")");
                    return candidatePos;
                }
                losFailures++;
            }
        }
        
        HorrorMod129.LOGGER.warn("ScreenshotTaker: Position search failed - checked " + positionsChecked + " positions (air check failures: " + airCheckFailures + ", LOS failures: " + losFailures + ")");
        return null;
    }

    private static BatEntity spawnCameraBat(ServerWorld world, BlockPos pos, ServerPlayerEntity player) {
        HorrorMod129.LOGGER.info("ScreenshotTaker: Creating bat entity");
        BatEntity bat = EntityType.BAT.create(world);
        if (bat == null) {
            HorrorMod129.LOGGER.error("ScreenshotTaker: EntityType.BAT.create() returned null");
            return null;
        }

        HorrorMod129.LOGGER.info("ScreenshotTaker: Setting bat position and properties");
        bat.refreshPositionAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0f, 0.0f);
        bat.setInvisible(true);
        bat.setInvulnerable(true);
        bat.setNoGravity(true);
        bat.setSilent(true);
        bat.setAiDisabled(true);

        HorrorMod129.LOGGER.info("ScreenshotTaker: Attempting to spawn bat in world");
        if (!world.spawnEntity(bat)) {
            HorrorMod129.LOGGER.error("ScreenshotTaker: world.spawnEntity() returned false");
            return null;
        }

        HorrorMod129.LOGGER.info("ScreenshotTaker: Bat spawned, setting look direction");
        Vec3d playerEyePos = player.getEyePos();
        bat.getLookControl().lookAt(playerEyePos.x, playerEyePos.y, playerEyePos.z);

        return bat;
    }

    private static boolean hasLineOfSightFromBatToPlayer(ServerWorld world, BlockPos batPos, ServerPlayerEntity player) {
        Vec3d batEyePos = new Vec3d(batPos.getX() + 0.5, batPos.getY() + 0.5, batPos.getZ() + 0.5);
        // return LineOfSightUtils.hasLineOfSightBetweenPoints(batEyePos, player.getEyePos(), world, player);
        return false;
    }
}
