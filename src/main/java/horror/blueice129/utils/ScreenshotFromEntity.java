package horror.blueice129.utils;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class ScreenshotFromEntity {

    private static Entity pendingTarget = null;
    private static Framebuffer offscreenFramebuffer = null;
    private static boolean captureInProgress = false;

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(ScreenshotFromEntity::onTickEnd);
    }

    public static void scheduleScreenshot(Entity target) {
        if (target == null || pendingTarget != null || captureInProgress) return;
        pendingTarget = target;
    }

    private static void onTickEnd(MinecraftClient client) {
        if (pendingTarget == null || client.player == null || client.world == null) return;
        if (!pendingTarget.isAlive()) {
            pendingTarget = null;
            return;
        }

        captureInProgress = true;
        try {
            captureOffscreen(client, pendingTarget);
        } finally {
            pendingTarget = null;
            captureInProgress = false;
        }
    }

    private static void ensureOffscreenFramebuffer(MinecraftClient client) {
        int width = client.getWindow().getFramebufferWidth();
        int height = client.getWindow().getFramebufferHeight();

        if (offscreenFramebuffer == null) {
            offscreenFramebuffer = new SimpleFramebuffer(width, height, true, MinecraftClient.IS_SYSTEM_MAC);
            return;
        }

        if (offscreenFramebuffer.textureWidth != width || offscreenFramebuffer.textureHeight != height) {
            offscreenFramebuffer.resize(width, height, MinecraftClient.IS_SYSTEM_MAC);
        }
    }

    private static void captureOffscreen(MinecraftClient client, Entity target) {
        ensureOffscreenFramebuffer(client);

        Framebuffer mainFramebuffer = client.getFramebuffer();
        Entity originalCamera = client.cameraEntity;
        Perspective originalPerspective = client.options.getPerspective();

        try {
            client.options.setPerspective(Perspective.FIRST_PERSON);
            client.setCameraEntity(target);

            offscreenFramebuffer.beginWrite(true);
            float tickDelta = client.getTickDelta();
            client.gameRenderer.renderWorld(tickDelta, client.getRenderTime(), new MatrixStack());
            renderLocalPlayerForCapture(client, tickDelta);
            ScreenshotRecorder.saveScreenshot(client.runDirectory, offscreenFramebuffer, msg -> {});
        } finally {
            offscreenFramebuffer.endWrite();
            mainFramebuffer.beginWrite(true);

            Entity restore = originalCamera != null ? originalCamera : client.player;
            if (restore != null) {
                client.setCameraEntity(restore);
            }
            client.options.setPerspective(originalPerspective);
        }
    }

    private static void renderLocalPlayerForCapture(MinecraftClient client, float tickDelta) {
        if (client.player == null) return;

        Camera camera = client.gameRenderer.getCamera();
        if (camera == null) return;

        Vec3d cameraPos = camera.getPos();
        Vec3d playerPos = client.player.getLerpedPos(tickDelta);

        VertexConsumerProvider.Immediate consumers = client.getBufferBuilders().getEntityVertexConsumers();
        int light = client.getEntityRenderDispatcher().getLight(client.player, tickDelta);

        client.getEntityRenderDispatcher().render(
                client.player,
                playerPos.x - cameraPos.x,
                playerPos.y - cameraPos.y,
                playerPos.z - cameraPos.z,
                client.player.getYaw(tickDelta),
                tickDelta,
                new MatrixStack(),
                consumers,
                light);

        consumers.draw();
    }
}

