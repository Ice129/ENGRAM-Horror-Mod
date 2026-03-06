package horror.blueice129.feature;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.sound.SoundCategory;

@Environment(EnvType.CLIENT)
public class AmbianceVolumeForcer {

    public static void enforceAmbianceVolume() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.options == null) return;

        var option = client.options.getSoundVolumeOption(SoundCategory.AMBIENT);
        if (option == null) return;

        if (option.getValue() < 1.0) {
            option.setValue(1.0);
            client.options.write();
        }
    }
}
