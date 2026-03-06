package horror.blueice129.utils;

import horror.blueice129.data.HorrorModPersistentState;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import horror.blueice129.HorrorMod129;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class PlayerBaseLocator {
    public static final String PLAYER_SPAWNPOINT_HISTORY_TRACKER_ID = "playerSpawnpointHistoryTracker";
    // an int[][] where each row is [x, y, z, TimeSpentAsSpawn]
    private static final int BASE_SCORE_THRESHOLD = 20;
    private static final int BASE_SCAN_RADIUS = 15;

    public static BlockPos getPlayerBaseLocation(ServerWorld world, PlayerEntity player) {
        return world.getSpawnPos();
    }

    public static void updateBaseLocationHistory(ServerWorld world, PlayerEntity player) {
        BlockPos spawnPos = world.getSpawnPos();
        if (spawnPos == null) {
            HorrorMod129.LOGGER.warn("World spawn position is null, cannot track player base location.");
            return;
        }

        int totalScore = 0;
        for (int x = -BASE_SCAN_RADIUS; x <= BASE_SCAN_RADIUS; x++) {
            for (int y = -BASE_SCAN_RADIUS; y <= BASE_SCAN_RADIUS; y++) {
                for (int z = -BASE_SCAN_RADIUS; z <= BASE_SCAN_RADIUS; z++) {
                    totalScore += isPlayerBaseIndicatorBlock(world.getBlockState(spawnPos.add(x, y, z)).getBlock());
                }
            }
        }

        if (totalScore < BASE_SCORE_THRESHOLD) return;

        HorrorModPersistentState state = HorrorModPersistentState.getServerState(world.getServer());
        int[][] history = state.getInt2DArray(PLAYER_SPAWNPOINT_HISTORY_TRACKER_ID);
        for (int[] entry : history) {
            if (entry[0] == spawnPos.getX() && entry[1] == spawnPos.getY() && entry[2] == spawnPos.getZ()) {
                entry[3]++;
                return;
            }
        }

        int[][] newHistory = new int[history.length + 1][4];
        System.arraycopy(history, 0, newHistory, 0, history.length);
        newHistory[history.length] = new int[]{ spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), 1 };
        state.setInt2DArray(PLAYER_SPAWNPOINT_HISTORY_TRACKER_ID, newHistory);
    }

    public static int isPlayerBaseIndicatorBlock(Block block) {
        Block[] strongExactIndicators = new Block[] {
                Blocks.CHEST,
                Blocks.TRAPPED_CHEST,
                Blocks.ENDER_CHEST,
                Blocks.FURNACE,
                Blocks.BLAST_FURNACE,
                Blocks.SMOKER,
                Blocks.CRAFTING_TABLE,
                Blocks.ANVIL,
                Blocks.LANTERN,
                Blocks.TORCH,
                Blocks.CAMPFIRE,
        };
        String[] strongFuzzyIndicators = new String[] {
                "door",
                "sign",
                "glass",
                "stripped_log",
                "plate", // pressure plate
                "button",
        };

        String weakFuzzyIndicators[] = new String[] {
                "cobble",
                // "plank",
                "stair",
                // "slab",
                "fence",
                "log"
        };

        for (Block indicator : strongExactIndicators) {
            if (block == indicator) {
                return 3; // strong exact match
            }
        }
        String blockName = block.getTranslationKey().toLowerCase();
        for (String indicator : strongFuzzyIndicators) {
            if (blockName.contains(indicator)) {
                return 2; // strong fuzzy match
            }
        }
        for (String indicator : weakFuzzyIndicators) {
            if (blockName.contains(indicator)) {
                return 1; // weak fuzzy match
            }
        }

        return 0; // no match
    }
}