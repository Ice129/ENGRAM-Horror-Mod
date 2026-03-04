package horror.blueice129.utils;

import horror.blueice129.data.HorrorModPersistentState;

import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import horror.blueice129.HorrorMod129;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class PlayerBaseLocator {
    private static final String PLAYER_SPAWNPOINT_HISTORY_TRACKER_ID = "playerSpawnpointHistoryTracker";
    // an int[][] where each row is [x, y, z,TimeSpentAsSpawn]

    public static BlockPos getPlayerBaseLocation(ServerWorld world, PlayerEntity player) {

        return world.getSpawnPos();
    }

    public static void updateBaseLocationHistory(ServerWorld world, PlayerEntity player) {
        BlockPos spawnPos = world.getSpawnPos();

        // now check for base blocks arround the spawn point
        // cobble, doors, crafting tables, furnaces, chests, planks, stairs, slabs,
        // fences, signs, torches, glass, anvils, double chests,
        // stripped logs, smoker, blast furnace, lanterns, campfires

        HorrorModPersistentState state = HorrorModPersistentState.getServerState(world.getServer());
        if (spawnPos != null) {
            int[][] history = state.getInt2DArray(PLAYER_SPAWNPOINT_HISTORY_TRACKER_ID);
            // Check if the current spawn point is already in the history
            boolean found = false;
            for (int i = 0; i < history.length; i++) {
                int[] entry = history[i];
                if (entry[0] == spawnPos.getX() && entry[1] == spawnPos.getY() && entry[2] == spawnPos.getZ()) {
                    // Increment the time spent as spawn point
                    entry[3]++;
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Add new entry for this spawn point
                int[][] newHistory = new int[history.length + 1][4];
                System.arraycopy(history, 0, newHistory, 0, history.length);
                newHistory[history.length][0] = spawnPos.getX();
                newHistory[history.length][1] = spawnPos.getY();
                newHistory[history.length][2] = spawnPos.getZ();
                newHistory[history.length][3] = 1; // start counting time spent as spawn point
                state.setInt2DArray(PLAYER_SPAWNPOINT_HISTORY_TRACKER_ID, newHistory);
            }
        } else {
            HorrorMod129.LOGGER.warn("World spawn position is null, cannot track player base location.");
        }
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
                "plank",
                "stair",
                "slab",
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