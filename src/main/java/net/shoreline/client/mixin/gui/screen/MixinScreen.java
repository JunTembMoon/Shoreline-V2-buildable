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
    private static final int SCREEN_OVERLAY = 0xC9050608;
    private static final int SCREEN_PANEL = 0x96111418;
    private static final int SCREEN_PANEL_SOFT = 0x6413161C;
    private static final int SCREEN_BORDER = 0x24FFFFFF;
    private static final int SCREEN_ACCENT = 0xD0BA1328;

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
        context.fill(0, 0, width, Math.round(height * 0.12f), 0x220D1014);
        context.fill(0, Math.round(height * 0.82f), width, height, 0x36090A0D);

        if ((Object) this instanceof OptionsScreen)
        {
            int panelWidth = Math.min(470, width - 72);
            int panelX = (width - panelWidth) / 2;
            int panelY = Math.max(28, height / 2 - 166);
            int panelHeight = Math.min(332, height - panelY - 40);
            context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, SCREEN_PANEL);
            context.fill(panelX, panelY, panelX + panelWidth, panelY + 1, SCREEN_BORDER);
            context.fill(panelX, panelY, panelX + panelWidth, panelY + 4, SCREEN_ACCENT);
            context.fill(panelX + 16, panelY + 20, panelX + panelWidth - 16, panelY + panelHeight - 18, SCREEN_PANEL_SOFT);
            context.fill(panelX + 16, panelY + 48, panelX + panelWidth - 16, panelY + panelHeight - 62, 0x18000000);
        }
        else
        {
            int panelWidth = Math.min(860, width - 76);
            int panelX = (width - panelWidth) / 2;
            int panelY = 34;
            int panelBottom = height - 96;
            int footerTop = height - 84;
            context.fill(panelX, panelY, panelX + panelWidth, panelBottom, SCREEN_PANEL);
            context.fill(panelX, panelY, panelX + panelWidth, panelY + 1, SCREEN_BORDER);
            context.fill(panelX, panelY, panelX + panelWidth, panelY + 4, SCREEN_ACCENT);
            context.fill(panelX + 14, panelY + 18, panelX + panelWidth - 14, panelBottom - 16, SCREEN_PANEL_SOFT);
            context.fill(panelX, footerTop, panelX + panelWidth, height - 18, 0x7A101319);
            context.fill(panelX, footerTop, panelX + panelWidth, footerTop + 1, SCREEN_BORDER);
        }
        ci.cancel();
    }

    @Unique
    private boolean isStyledMenuScreen()
    {
        return (Object) this instanceof SelectWorldScreen
                || (Object) this instanceof MultiplayerScreen
                || (Object) this instanceof OptionsScreen;
    }
}
