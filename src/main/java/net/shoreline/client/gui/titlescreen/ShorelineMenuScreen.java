package net.shoreline.client.gui.titlescreen;

import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.titlescreen.particle.ParticleManager;
import net.shoreline.client.gui.titlescreen.particle.snow.SnowManager;
import net.shoreline.client.gui.titlescreen.particle.snow.SnowParticle;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.module.client.TitleScreenModule;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
public class ShorelineMenuScreen extends Screen
{
    private static final Identifier WHITE_LOGO = Identifier.of(ShorelineMod.MOD_ID, "logo/white.png");
    private static final Identifier RED_LOGO = Identifier.of(ShorelineMod.MOD_ID, "logo/red.png");
    private static final int BACKGROUND = 0xFF050608;
    private static final int SURFACE = 0x88111418;
    private static final int SURFACE_SOFT = 0x4412141A;
    private static final int ACCENT = 0xFFBA1328;
    private static final int TEXT = 0xFFF3F3F3;
    private final List<MenuButton> buttons;
    private static ParticleManager<SnowParticle> snowManager;
    private final ClickGuiScreen clickGuiScreen = ClickGuiScreen.INSTANCE;
    private boolean renderingGui;

    public ShorelineMenuScreen()
    {
        super(Text.of(ShorelineMod.MOD_NAME + "-MainMenu"));
        buttons = new ArrayList<>();
    }

    @Override
    public void resize(MinecraftClient client, int width, int height)
    {
        super.resize(client, width, height);
        if (snowManager != null)
        {
            snowManager.reset();
        }
    }

    @Override
    protected void init()
    {
        super.init();
        if (snowManager == null)
        {
            snowManager = TitleScreenModule.INSTANCE.getManager();
        }
        else
        {
            snowManager.reset();
        }

        resetButtons();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta)
    {
        int width = client.getWindow().getScaledWidth();
        int height = client.getWindow().getScaledHeight();

        renderBackdrop(context, width, height);
        snowManager.update();
        snowManager.render(context);
        renderHero(context, width, height);
        renderLogoColumn(context, width, height);

        double mX = renderingGui ? -1 : mouseX;
        double mY = renderingGui ? -1 : mouseY;
        for (MenuButton button : buttons)
        {
            button.render(context, mX, mY, delta);
        }

        renderFooter(context, width, height);

        if (renderingGui)
        {
            context.fill(0, 0, width, height, 0x44000000);
            clickGuiScreen.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (renderingGui)
        {
            clickGuiScreen.mouseClicked(mouseX, mouseY, button);
        }
        else
        {
            buttons.forEach(menuButton -> menuButton.mouseClicked(mouseX, mouseY, button));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (renderingGui)
        {
            clickGuiScreen.mouseReleased(mouseX, mouseY, button);
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount)
    {
        if (renderingGui)
        {
            clickGuiScreen.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == ClickGuiModule.INSTANCE.getKeybind().getValue().getKeycode())
        {
            renderingGui = true;
            Window window = client.getWindow();
            ClickGuiModule.INSTANCE.setFadeState(true);
            clickGuiScreen.init(client, window.getScaledWidth(), window.getScaledHeight());
        }
        else if (keyCode == GLFW.GLFW_KEY_ESCAPE)
        {
            renderingGui = false;
            clickGuiScreen.reset(); // idk
        }
        else if (renderingGui)
        {
            clickGuiScreen.keyPressed(keyCode, scanCode, modifiers);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers)
    {
        if (renderingGui)
        {
            clickGuiScreen.charTyped(chr, modifiers);
        }

        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc()
    {
        return false;
    }

    public void resetButtons()
    {
        buttons.clear();
        Window window = client.getWindow();
        float scaledWidth = window.getScaledWidth();
        float scaledHeight = window.getScaledHeight();
        float compact = scaledHeight < 420.0f ? 0.88f : 1.0f;
        float panelX = Math.max(26.0f, scaledWidth * 0.075f);
        float panelWidth = Math.min(320.0f, scaledWidth * 0.36f);
        float buttonX = panelX + 14.0f;
        float buttonWidth = panelWidth - 28.0f;
        float buttonHeight = 34.0f * compact;
        float spacing = 8.0f * compact;

        List<MenuButton> allButtons = new ArrayList<>();
        allButtons.add(new MenuButton(I18n.translate("menu.singleplayer").toUpperCase(Locale.ROOT), "Local worlds and test setups", () -> client.setScreen(new SelectWorldScreen(this)), 0, 0, buttonWidth, buttonHeight));
        allButtons.add(new MenuButton(I18n.translate("menu.multiplayer").toUpperCase(Locale.ROOT), "Servers, queues and practice", () -> client.setScreen(new MultiplayerScreen(this)), 0, 0, buttonWidth, buttonHeight));
        allButtons.add(new MenuButton(I18n.translate("menu.options").toUpperCase(Locale.ROOT).replace(".", ""), "Video, audio and control tuning", () -> client.setScreen(new OptionsScreen(this, client.options)), 0, 0, buttonWidth, buttonHeight));

        if (hasIAS())
        {
            allButtons.add(new MenuButton("ACCOUNTS", "Session and profile switching", () -> client.setScreen(getAccountScreen(this)), 0, 0, buttonWidth, buttonHeight));
        }

        allButtons.add(new MenuButton(I18n.translate("menu.quit").toUpperCase(Locale.ROOT), "Close the client cleanly", client::scheduleStop, 0, 0, buttonWidth, buttonHeight));

        float totalHeight = allButtons.size() * buttonHeight + (allButtons.size() - 1) * spacing;
    float heroBottom = Math.max(24.0f, scaledHeight * 0.13f) + (160.0f * compact);
    float startY = Math.min(scaledHeight - totalHeight - 24.0f, Math.max(heroBottom, scaledHeight * 0.42f));

        float currentY = startY;
        for (MenuButton button : allButtons)
        {
            buttons.add(new MenuButton(button.getName(), button.getDescription(), button.getRunnable(), buttonX, currentY, buttonWidth, buttonHeight));
            currentY += buttonHeight + spacing;
        }
    }

    public static void setSnowManager(SnowManager manager)
    {
        ShorelineMenuScreen.snowManager = manager;
        if (snowManager != null)
        {
            snowManager.reset();
        }
    }

    private void renderBackdrop(DrawContext context, int width, int height)
    {
        int panelX = Math.round(Math.max(26.0f, width * 0.075f));
        int panelY = Math.round(Math.max(24.0f, height * 0.13f));
        int panelWidth = Math.round(Math.min(320.0f, width * 0.36f));
        int panelHeight = Math.round(Math.min(height - 48.0f, height * 0.72f));
        int rightPanelX = Math.round(width * 0.56f);
        int rightPanelY = Math.round(height * 0.12f);
        int rightPanelBottom = Math.round(height * 0.84f);

        context.fill(0, 0, width, height, BACKGROUND);
        context.fill(0, 0, width, height / 2, 0x140C0D10);
        context.fill(width / 2, 0, width, height, 0x22080509);
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, SURFACE);
        context.fill(panelX, panelY, panelX + 4, panelY + panelHeight, ACCENT);
        context.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight, 0x26FFFFFF);
        context.fill(rightPanelX, rightPanelY, width - 30, rightPanelBottom, SURFACE_SOFT);
        context.fill(rightPanelX, rightPanelY, width - 30, rightPanelY + 1, 0x26FFFFFF);
        context.fill(0, height - 42, width, height - 41, 0x15FFFFFF);
        context.fill(Math.round(width * 0.075f), Math.round(height * 0.12f), Math.round(width * 0.075f) + 1, Math.round(height * 0.82f), 0x22FFFFFF);
    }

    private void renderHero(DrawContext context, int width, int height)
    {
        float compact = height < 420 ? 0.88f : 1.0f;
        float panelX = Math.max(26.0f, width * 0.075f) + 14.0f;
        float panelY = Math.max(24.0f, height * 0.13f) + 16.0f;
        float titleScale = (width < 900 ? 2.7f : 3.2f) * compact;

        drawChip(context, "COMBAT CLIENT", panelX, panelY, 0x66110F14, 0xFFBA1328);
        drawScaledText(context, ShorelineMod.MOD_NAME, panelX, panelY + (22.0f * compact), titleScale, TEXT);
    }

    private void renderLogoColumn(DrawContext context, int width, int height)
    {
        float columnX = width * 0.56f;
        float columnY = height * 0.12f;
        float columnWidth = width - columnX - 30.0f;
        float columnHeight = height * 0.72f;
        float logoSize = Math.min(columnWidth, columnHeight) * (height < 420 ? 0.82f : 0.9f);
        float whiteLogoSize = logoSize * 0.88f;
        float bob = (float) Math.sin(System.currentTimeMillis() / 850.0) * 6.0f;
        float logoX = columnX + (columnWidth - logoSize) / 2.0f;
        float logoY = columnY + (columnHeight - logoSize) / 2.0f + bob;
        float whiteLogoX = columnX + (columnWidth - whiteLogoSize) / 2.0f;
        float whiteLogoY = columnY + (columnHeight - whiteLogoSize) / 2.0f + bob;

        context.drawTextWithShadow(client.textRenderer, "MAIN MENU", Math.round(columnX + 14.0f), Math.round(columnY + 12.0f), 0x99FFFFFF);
        drawLogo(context, RED_LOGO, logoX + 24.0f, logoY + 24.0f, logoSize, 0x50FF2436);
        drawLogo(context, WHITE_LOGO, whiteLogoX, whiteLogoY, whiteLogoSize, 0xDDFFFFFF);
        context.drawTextWithShadow(client.textRenderer, ShorelineMod.MOD_NAME.toUpperCase(Locale.ROOT), Math.round(columnX + 14.0f), Math.round(columnY + columnHeight - 22.0f), 0xCCFFFFFF);
    }

    private void renderFooter(DrawContext context, int width, int height)
    {
        if (renderingGui)
        {
            context.drawTextWithShadow(client.textRenderer, "PRESS ESC TO CLOSE OVERLAY", 18, height - 20, 0x88FFFFFF);
        }
    }

    private void drawChip(DrawContext context, String text, float x, float y, int background, int edge)
    {
        int chipWidth = Math.round(getChipWidth(text));
        int left = Math.round(x);
        int top = Math.round(y);
        int bottom = top + 14;

        context.fill(left, top, left + chipWidth, bottom, background);
        context.fill(left, bottom - 1, left + chipWidth, bottom, edge);
        context.drawTextWithShadow(client.textRenderer, text, left + 7, top + 3, 0xFFFFFFFF);
    }

    private float getChipWidth(String text)
    {
        return client.textRenderer.getWidth(text) + 14.0f;
    }

    private void drawScaledText(DrawContext context, String text, float x, float y, float scale, int color)
    {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0.0f);
        context.getMatrices().scale(scale, scale, 1.0f);
        context.drawTextWithShadow(client.textRenderer, text, 0, 0, color);
        context.getMatrices().pop();
    }

    private void drawLogo(DrawContext context, Identifier logo, float x, float y, float size, int color)
    {
        int renderSize = Math.round(size);
        context.drawTexture(RenderLayer::getGuiTextured,
                logo,
                Math.round(x),
                Math.round(y),
                0.0f,
                0.0f,
                renderSize,
                renderSize,
                renderSize,
                renderSize,
                color);
    }

    public boolean hasIAS()
    {
        try
        {
            Class.forName("ru.vidtu.ias.IASMinecraft");
            return true;
        }
        catch (ClassNotFoundException e)
        {
            return false;
        }
    }

    public Screen getAccountScreen(Screen parent)
    {
        try
        {
            String screenName = "ru.vidtu.ias.screen.AccountScreen";
            Class<?> screen = Class.forName(screenName);
            Constructor<?> ctr = screen.getDeclaredConstructor(Screen.class);
            ctr.setAccessible(true);
            return (Screen) ctr.newInstance(parent);
        }
        catch (ClassNotFoundException
               | InstantiationException
               | IllegalAccessException
               | InvocationTargetException
               | NoSuchMethodException e)
        {
            e.printStackTrace();
            return null;
        }
    }
}
