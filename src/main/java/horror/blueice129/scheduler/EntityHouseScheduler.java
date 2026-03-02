package horror.blueice129.scheduler;

import horror.blueice129.HorrorMod129;
import horror.blueice129.data.HorrorModPersistentState;
import horror.blueice129.feature.house.EntityHouse;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.entity.player.PlayerEntity;


public class EntityHouseScheduler {

    private static final int MAXIMUM_FLATNESS_SCORE = 140; 
    private static final String FINALISED_BASE_LOCATION_ID = "finalisedBaseLocation";
    private static final String FLATNESS_CHECK_TIMER = "flatnessCheckTimer";
    private static final int TICKS_BETWEEN_CHECKS = (20 * 60); // Check every 60 seconds
    private static final int MIN_DISTANCE_FROM_PLAYER_BASE = 16 * 20; // 20 chunks (320 blocks)

    private static final Random random = Random.create();

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(EntityHouseScheduler::onServerTick);

        // Register server world loading event to initialize timer if needed
        ServerWorldEvents.LOAD.register((server, world) -> {
            if (world.isClient())
                return;
            if (world.getRegistryKey() == World.OVERWORLD) {
                HorrorModPersistentState state = HorrorModPersistentState.getServerState(server);

                // If the timer is not set, initialize it
                if (!state.hasTimer(FLATNESS_CHECK_TIMER)) {
                    state.setTimer(FLATNESS_CHECK_TIMER, TICKS_BETWEEN_CHECKS);
                    HorrorMod129.LOGGER.info(
                            "EntityHouseScheduler initialized with flatness check timer: " + state.getTimer(FLATNESS_CHECK_TIMER) + " ticks");
                }
            }
        });
        HorrorMod129.LOGGER.info("Registered EntityHouseScheduler");
    }

    private static void onServerTick(net.minecraft.server.MinecraftServer server) {
        // Skip if server is empty (pause timers)
        if (server.getPlayerManager().getPlayerList().isEmpty()) {
            return;
        }
        
        HorrorModPersistentState state = HorrorModPersistentState.getServerState(server);
        int timer = state.hasTimer(FLATNESS_CHECK_TIMER) ? state.getTimer(FLATNESS_CHECK_TIMER) : -1;
        if (timer <= 0) {
            // get random player from player list thats in the overworld
            PlayerEntity player = server.getPlayerManager().getPlayerList().get(random.nextInt(server.getPlayerManager().getPlayerList().size()));
            // if player is not in overworld, return
            if (player.getWorld().getRegistryKey() != World.OVERWORLD) {
                return;
            }

            state.setTimer(FLATNESS_CHECK_TIMER, TICKS_BETWEEN_CHECKS);
            int flatnessRating = EntityHouse.findSuitableHouseStartLocation(player, server.getOverworld(), MAXIMUM_FLATNESS_SCORE);
        }
    }
}