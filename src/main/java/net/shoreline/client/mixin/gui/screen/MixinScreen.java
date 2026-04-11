package net.shoreline.client.mixin.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class MixinScreen
{
    @Unique
    private static final Identifier SHORELINE_LOGO = Identifier.of(ShorelineMod.MOD_ID, "logo/white.png");
    @Unique
    private static final int SCREEN_OVERLAY = 0xC9050608;
    @Unique
    private static final int SCREEN_PANEL = 0x7A111419;
    @Unique
    private static final int SCREEN_PANEL_SOFT = 0x50121419;
    @Unique
    private static final int SCREEN_ACCENT = 0xFFBA1328;

    @Shadow
    protected MinecraftClient client;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    protected abstract <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement);

    @Shadow
    protected abstract void renderPanoramaBackground(DrawContext context, float deltaTicks);

    @Inject(method = "keyPressed", at = @At(value = "HEAD"))
    private void hookKeyPressed(int keyCode,
                                int scanCode,
                                int modifiers,
                                CallbackInfoReturnable<Boolean> cir)
    {
        if ((Object) this instanceof TitleScreen
                || (Object) this instanceof MultiplayerScreen
                || (Object) this instanceof SelectWorldScreen
                || (Object) this instanceof OptionsScreen)
        {
            if (keyCode == ClickGuiModule.INSTANCE.getKeybindMacro().getKeycode())
            {
                ClickGuiModule.INSTANCE.enable();
            }
        }
    }

    @Inject(method = "renderBackground(Lnet/minecraft/client/gui/DrawContext;IIF)V", at = @At("HEAD"), cancellable = true)
    private void hookRenderBackground(DrawContext context,
                                      int mouseX,
                                      int mouseY,
                                      float deltaTicks,
                                      CallbackInfo ci)
    {
        if (!isStyledMenuScreen())
        {
            return;
        }

        if (client.world == null)
        {
            renderPanoramaBackground(context, deltaTicks);
        }

        context.fill(0, 0, width, height, SCREEN_OVERLAY);
        context.fill(0, 0, width, Math.round(height * 0.16f), 0x300B0D10);
        context.fill(0, height - 46, width, height, 0x5A090A0D);
        context.fill(22, 18, 24, height - 18, 0x25FFFFFF);
        context.fill(24, 18, 28, height - 18, SCREEN_ACCENT);

        if ((Object) this instanceof OptionsScreen)
        {
            int panelWidth = Math.min(420, width - 52);
            int panelX = (width - panelWidth) / 2;
            int panelY = 34;
            int panelHeight = height - 72;
            context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, SCREEN_PANEL);
            context.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0x20FFFFFF);
            context.fill(panelX, panelY, panelX + 5, panelY + panelHeight, SCREEN_ACCENT);
            context.fill(panelX + 20, panelY + 42, panelX + panelWidth - 20, panelY + panelHeight - 20, SCREEN_PANEL_SOFT);
        }
        else
        {
            int panelX = 32;
            int panelY = 36;
            int panelWidth = width - 64;
            int panelHeight = height - 94;
            int footerY = height - 52;
            context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, SCREEN_PANEL);
            context.fill(panelX, panelY, panelX + panelWidth, panelY + 1, 0x20FFFFFF);
            context.fill(panelX, panelY, panelX + 5, panelY + panelHeight, SCREEN_ACCENT);
            context.fill(panelX, footerY, panelX + panelWidth, height - 18, 0x68101318);
            context.fill(panelX + 18, panelY + 18, panelX + panelWidth - 18, footerY - 14, SCREEN_PANEL_SOFT);
        }

        drawScreenWatermark(context);
        ci.cancel();
    }

    @Unique
    private boolean isStyledMenuScreen()
    {
        return (Object) this instanceof SelectWorldScreen
                || (Object) this instanceof MultiplayerScreen
                || (Object) this instanceof OptionsScreen;
    }

    @Unique
    private void drawScreenWatermark(DrawContext context)
    {
        int logoSize = Math.min(140, Math.max(72, Math.min(width, height) / 5));
        int logoX = width - logoSize - 26;
        int logoY = 18;

        context.drawTexture(RenderLayer::getGuiTextured,
                SHORELINE_LOGO,
                logoX + 14,
                logoY + 14,
                0.0f,
                0.0f,
                logoSize,
                logoSize,
                logoSize,
                logoSize,
                0x26B81428);
        context.drawTexture(RenderLayer::getGuiTextured,
                SHORELINE_LOGO,
                logoX,
                logoY,
                0.0f,
                0.0f,
                logoSize,
                logoSize,
                logoSize,
                logoSize,
                0x16FFFFFF);
    }
}
