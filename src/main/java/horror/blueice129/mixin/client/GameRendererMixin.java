package horror.blueice129.mixin.client;

import horror.blueice129.utils.ScreenshotFromEntity;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void horrorMod129$captureEntityScreenshot(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        ScreenshotFromEntity.captureOffscreen((GameRenderer) (Object) this, tickDelta, startTime);
    }
}