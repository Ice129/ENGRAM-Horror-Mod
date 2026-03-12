package horror.blueice129.scheduler;

import horror.blueice129.HorrorMod129;
import horror.blueice129.data.HorrorModPersistentState;
import horror.blueice129.sounds.ScaryAmbiance;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class AmbianceScheduler {

    private static final int COOLDOWN_TICKS = 20 * 60 * 20; // 20 minutes in ticks
    // private static final int COOLDOWN_TICKS = 3; ////////////////// DEBUG ONLY, 3 TICKS COOLDOWN //////////////////////

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayerEntity player) {
                ServerWorld world = (ServerWorld) player.getWorld();
                MinecraftServer server = world.getServer();
                HorrorModPersistentState state = HorrorModPersistentState.getServerState(server);

                String key = "ambianceCooldown_" + player.getUuidAsString();
                long currentTick = server.getTicks();
                long lastPlay = state.getLongValue(key, -1L);

                if (lastPlay == -1L) {
                    state.setLongValue(key, currentTick);
                } else if (currentTick - lastPlay >= COOLDOWN_TICKS) {
                    if (ScaryAmbiance.attemptPlayScaryAmbiance(world, player)) {
                        state.setLongValue(key, currentTick);
                        HorrorMod129.LOGGER.info("Played scary ambiance for player " + player.getName().getString() + " at tick " + currentTick);
                    }
                }
            }
            return true;
        });

        HorrorMod129.LOGGER.info("Registered AmbianceScheduler");
    }
}
