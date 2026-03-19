package com.lmvictor20.glypher.mixin;

import com.lmvictor20.glypher.screen.preview.GlypherPreviewHost;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HandledScreen.class)
abstract class HandledScreenMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void glypher$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof GlypherPreviewHost host) {
            cir.setReturnValue(host.glypher$getController().mouseClicked(mouseX, mouseY, button));
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void glypher$mouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof GlypherPreviewHost host) {
            cir.setReturnValue(host.glypher$getController().mouseReleased(mouseX, mouseY, button));
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void glypher$mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof GlypherPreviewHost host) {
            cir.setReturnValue(host.glypher$getController().mouseDragged(mouseX, mouseY, button, deltaX, deltaY));
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void glypher$mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof GlypherPreviewHost host) {
            cir.setReturnValue(host.glypher$getController().mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount));
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void glypher$keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof GlypherPreviewHost host) {
            cir.setReturnValue(host.glypher$getController().keyPressed(keyCode, scanCode));
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void glypher$render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if ((Object) this instanceof GlypherPreviewHost host) {
            host.glypher$getController().renderOverlay(context);
        }
    }
}
