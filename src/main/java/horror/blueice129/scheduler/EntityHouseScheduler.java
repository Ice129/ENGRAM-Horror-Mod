package horror.blueice129.scheduler;

import horror.blueice129.HorrorMod129;
import horror.blueice129.data.HorrorModPersistentState;
import horror.blueice129.feature.house.EntityHouse;
import horror.blueice129.feature.house.EntityHouse.FlatnessResult;
import horror.blueice129.utils.PlayerBaseLocator;
import horror.blueice129.utils.StructurePlacer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.player.PlayerEntity;

public class EntityHouseScheduler {

    private static final String FLATNESS_CHECK_TIMER = "flatnessCheckTimer";
    // each row stores [x, y, z, flatness] — keeps position and score together to avoid sync issues
    private static final String CANDIDATE_DATA_KEY = "entityHouseCandidates";
    private static final int TICKS_BETWEEN_CHECKS = 20 * 60;
    private static final int SCAN_POINTS_PER_CYCLE = 3;
    private static final int MAX_CANDIDATES = 5;

    private static final Random random = Random.create();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(EntityHouseScheduler::onServerTick);

        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.isClient()) return;
            if (world.getRegistryKey() == World.OVERWORLD) {
                HorrorModPersistentState state = HorrorModPersistentState.getServerState(server);
                if (!state.hasTimer(FLATNESS_CHECK_TIMER)) {
                    state.setTimer(FLATNESS_CHECK_TIMER, TICKS_BETWEEN_CHECKS);
                    HorrorMod129.LOGGER.info("EntityHouseScheduler initialized with timer: {} ticks",
                            state.getTimer(FLATNESS_CHECK_TIMER));
                }
            }
        });
        HorrorMod129.LOGGER.info("Registered EntityHouseScheduler");
    }

    private static void onServerTick(MinecraftServer server) {
        if (server.getPlayerManager().getPlayerList().isEmpty()) return;

        HorrorModPersistentState state = HorrorModPersistentState.getServerState(server);
        int timer = state.decrementTimer(FLATNESS_CHECK_TIMER, 1);
        if (timer > 0) return;

        PlayerEntity player = server.getPlayerManager().getPlayerList()
                .get(random.nextInt(server.getPlayerManager().getPlayerList().size()));

        state.setTimer(FLATNESS_CHECK_TIMER, TICKS_BETWEEN_CHECKS);

        if (player.getWorld().getRegistryKey() != World.OVERWORLD) return;

        runFlatnessCheck(server.getOverworld(), player, state);

        BlockPos playerBasePos = PlayerBaseLocator.getPlayerBaseLocation(server.getOverworld(), player);
    }

    private static void runFlatnessCheck(ServerWorld world, PlayerEntity player, HorrorModPersistentState state) {
        int[][] candidates = state.getInt2DArray(CANDIDATE_DATA_KEY);

        for (int i = 0; i < SCAN_POINTS_PER_CYCLE; i++) {
            BlockPos seed = StructurePlacer.findSurfaceLocation(world, player.getBlockPos(), player, 16 * 15, 16 * 25, true);
            if (seed == null) continue;

            FlatnessResult result = EntityHouse.getBestLocalFlatness(world, seed);
            candidates = insertCandidate(candidates, result);
        }

        state.setInt2DArray(CANDIDATE_DATA_KEY, candidates);
        HorrorMod129.LOGGER.info("EntityHouseScheduler: updated {} house location candidates", candidates.length);
    }

    private static int[][] insertCandidate(int[][] candidates, FlatnessResult result) {
        int[] newEntry = { result.pos.getX(), result.pos.getY(), result.pos.getZ(), result.flatness };

        if (candidates.length < MAX_CANDIDATES) {
            int[][] updated = new int[candidates.length + 1][];
            System.arraycopy(candidates, 0, updated, 0, candidates.length);
            updated[candidates.length] = newEntry;
            return updated;
        }

        // replace the least flat candidate if the new one is better
        int worstIndex = 0;
        for (int i = 1; i < candidates.length; i++) {
            if (candidates[i][3] > candidates[worstIndex][3]) {
                worstIndex = i;
            }
        }

        if (result.flatness < candidates[worstIndex][3]) {
            candidates[worstIndex] = newEntry;
        }

        return candidates;
    }
}
