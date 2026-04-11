package net.shoreline.client.gui.titlescreen;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.api.font.FontRenderer;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.render.animation.Animation;

@AllArgsConstructor
@Getter
public class MenuButton
{
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final FontRenderer RENDERER = FontManager.FONT_RENDERER;
    private final Animation hoverAnimation = new Animation(false, 180.0f, Easing.CUBIC_OUT);
    private final String name;
    private final String description;
    private final Runnable runnable;
    private final float x;
    private final float y;
    private final float width;
    private final float height;

    public void render(DrawContext context, double mouseX, double mouseY, float delta)
    {
        boolean hovered = Mouse.isHovering(mouseX, mouseY, x, y, width, height);
        hoverAnimation.setState(hovered);
        float factor = (float) hoverAnimation.getFactor();

        int left = Math.round(x);
        int top = Math.round(y);
        int right = Math.round(x + width);
        int bottom = Math.round(y + height);

        context.fill(left, top, right, bottom, mix(0x55111217, 0xCC181A21, factor));
        context.fill(left + 1, top + 1, right - 1, bottom - 1, mix(0x6615161B, 0xB0121419, factor));
        context.fill(left, top, left + Math.round(4.0f + factor * 4.0f), bottom, mix(0xCC6A0A16, 0xFFBF1629, factor));
        context.fill(left, bottom - 1, right, bottom, mix(0x22FFFFFF, 0x55FFFFFF, factor));

        RENDERER.drawStringWithShadow(context, name, x + 12.0f, y + 6.0f, mix(0xFFD7D7D7, 0xFFFFFFFF, factor));
        context.drawTextWithShadow(MC.textRenderer, description, left + 12, top + Math.round(height - 12.0f), mix(0x88C4C4C4, 0xFFE7E7E7, factor));

        String marker = ">";
        int markerWidth = MC.textRenderer.getWidth(marker);
        context.drawTextWithShadow(MC.textRenderer, marker, right - markerWidth - 12, top + Math.round((height - 8.0f) / 2.0f), mix(0x55FFFFFF, 0xFFFFFFFF, factor));
    }

    public void mouseClicked(double mouseX, double mouseY, int button)
    {
        if (Mouse.isHovering(mouseX, mouseY, x, y, width, height) && button == 0)
        {
            runnable.run();
        }
    }

    private int mix(int start, int end, float factor)
    {
        int startA = (start >>> 24) & 0xff;
        int startR = (start >>> 16) & 0xff;
        int startG = (start >>> 8) & 0xff;
        int startB = start & 0xff;

        int endA = (end >>> 24) & 0xff;
        int endR = (end >>> 16) & 0xff;
        int endG = (end >>> 8) & 0xff;
        int endB = end & 0xff;

        int alpha = Math.round(startA + (endA - startA) * factor);
        int red = Math.round(startR + (endR - startR) * factor);
        int green = Math.round(startG + (endG - startG) * factor);
        int blue = Math.round(startB + (endB - startB) * factor);
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
}