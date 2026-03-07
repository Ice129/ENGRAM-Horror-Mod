package horror.blueice129.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.entity.Entity;

@Environment(EnvType.CLIENT)
public class ScreenshotFromEntity {

    private static Entity pendingTarget = null;
    private static Entity originalCamera = null;
    private static boolean awaitingCapture = false;

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(ScreenshotFromEntity::onTickEnd);
        WorldRenderEvents.END.register(context -> onWorldRenderEnd());
    }

    public static void scheduleScreenshot(Entity target) {
        if (target == null || pendingTarget != null || awaitingCapture) return;
        pendingTarget = target;
    }

    private static void onTickEnd(MinecraftClient client) {
        if (pendingTarget == null || client.player == null) return;

        originalCamera = client.cameraEntity;
        client.setCameraEntity(pendingTarget);
        pendingTarget = null;
        awaitingCapture = true;
    }

    private static void onWorldRenderEnd() {
        if (!awaitingCapture) return;

        MinecraftClient client = MinecraftClient.getInstance();
        ScreenshotRecorder.saveScreenshot(client.runDirectory, client.getFramebuffer(), msg -> {});

        Entity restore = originalCamera != null ? originalCamera : client.player;
        if (restore != null) {
            client.setCameraEntity(restore);
        }
        originalCamera = null;
        awaitingCapture = false;
    }
}

