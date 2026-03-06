package horror.blueice129.sounds;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public class ScaryAmbiance {

    private static final String[] SCARY_SOUNDS = new String[] {
            "minecraft:entity.fox.screech",
            "minecraft:ambient.warped_forest.mood",
            "minecraft:block.conduit.ambient.short",
            "minecraft:ambient.basalt_deltas.mood",
            "minecraft:ambient.crimson_forest.mood",
            "minecraft:ambient.underwater.loop.additions.ultra_rare",
            "minecraft:ambient.cave"
    };

    // play 1 of the sounds at a 10% chance of happeneing every time the player takes damage, with a cooldown of 3 in game day
    public static boolean attemptPlayScaryAmbiance(ServerWorld world, ServerPlayerEntity player) {
        // play the noise, the logic can be done in the scheduler
        if (Random.createLocal().nextFloat() < 0.1) {
            String soundId = SCARY_SOUNDS[Random.createLocal().nextInt(SCARY_SOUNDS.length)];
            SoundEvent soundEvent = SoundEvent.of(Identifier.tryParse(soundId));
            world.playSound(null, player.getBlockPos(), soundEvent, SoundCategory.AMBIENT, 2.0f, 1.0f);
            return true;
        }
        return false;
    }

}
