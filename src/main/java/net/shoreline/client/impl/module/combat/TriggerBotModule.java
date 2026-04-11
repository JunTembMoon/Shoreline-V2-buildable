package net.shoreline.client.impl.module.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.ConfigGroup;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.imixin.IPlayerInteractEntityC2S;
import net.shoreline.client.impl.module.impl.CombatModule;
import net.shoreline.client.impl.module.impl.Priorities;
import net.shoreline.client.mixin.AccessorMouse;
import net.shoreline.client.util.entity.FakePlayerEntity;
import net.shoreline.eventbus.annotation.EventListener;
import org.lwjgl.glfw.GLFW;

public class TriggerBotModule extends CombatModule
{
    Config<Float> rangeConfig = new NumberConfig.Builder<Float>("Range")
            .setDefaultValue(4.0f).setMin(0.5f).setMax(6.0f).setFormat("m")
            .setDescription("The maximum triggerbot attack range").build();
    Config<Boolean> cooldownConfig = new BooleanConfig.Builder("Cooldown")
            .setDescription("Waits for the vanilla attack cooldown before attacking")
            .setDefaultValue(true).build();
    Config<Boolean> multitaskConfig = new BooleanConfig.Builder("Multitask")
            .setDescription("Allows triggering while using items")
            .setDefaultValue(false).build();
    Config<Boolean> playersConfig = targetPlayers;
    Config<Boolean> hostilesConfig = targetHostiles;
    Config<Boolean> passivesConfig = targetPassives;
    Config<Void> targetConfig = new ConfigGroup.Builder("Target")
            .addAll(playersConfig, hostilesConfig, passivesConfig).build();

    Config<Boolean> packetConfig = new BooleanConfig.Builder("PostPacket")
            .setDescription("Sends the attack packet during post tick")
            .setDefaultValue(true).build();
    Config<Boolean> glfwClickConfig = new BooleanConfig.Builder("GLFWClick")
            .setDescription("Simulates a real left click through the GLFW mouse callback")
            .setDefaultValue(true).build();
    Config<Boolean> swingConfig = new BooleanConfig.Builder("Swing")
            .setDescription("Swings the hand when sending the attack packet")
            .setDefaultValue(true).build();
    Config<Void> attackConfig = new ConfigGroup.Builder("Attack")
            .addAll(packetConfig, glfwClickConfig, swingConfig).build();

    private final Timer attackTimer = new NanoTimer();
    private long suppressPacketExpire;

    private boolean suppressGlfwAttackPacket;
    private int suppressEntityId = -1;

    public TriggerBotModule()
    {
        super("TriggerBot", new String[] {"Trigger"}, "Automatically attacks what your crosshair is on", GuiCategory.COMBAT);
    }

    @Override
    public void onDisable()
    {
        suppressGlfwAttackPacket = false;
        suppressEntityId = -1;
        suppressPacketExpire = 0L;
    }

    @EventListener
    public void onWorldDisconnect(WorldEvent.Disconnect event)
    {
        disable();
    }

    @EventListener(priority = Priorities.KILL_AURA)
    public void onTick(TickEvent.Post event)
    {
        if (suppressGlfwAttackPacket && System.currentTimeMillis() > suppressPacketExpire)
        {
            suppressGlfwAttackPacket = false;
            suppressEntityId = -1;
        }

        if (checkNull() || mc.player.isSpectator())
        {
            return;
        }

        if (mc.currentScreen != null)
        {
            return;
        }

        if (mc.player.isUsingItem() && !multitaskConfig.getValue())
        {
            return;
        }

        HitResult crosshairTarget = mc.crosshairTarget;
        if (!(crosshairTarget instanceof EntityHitResult entityHitResult))
        {
            return;
        }

        Entity target = entityHitResult.getEntity();
        if (!isValidTarget(target))
        {
            return;
        }

        if (cooldownConfig.getValue() && mc.player.getAttackCooldownProgress(0.5f) < 1.0f)
        {
            return;
        }

        attackTarget(target);
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (!suppressGlfwAttackPacket)
        {
            return;
        }

        if (event.getPacket() instanceof IPlayerInteractEntityC2S packet
                && packet.getInteractType() == PlayerInteractEntityC2SPacket.InteractType.ATTACK)
        {
            Entity entity = packet.getEntity(mc.world);
            if (entity != null && entity.getId() == suppressEntityId)
            {
                event.cancel();
                suppressGlfwAttackPacket = false;
                suppressEntityId = -1;
                suppressPacketExpire = 0L;
            }
        }
    }

    private boolean isValidTarget(Entity target)
    {
        if (target == null || !target.isAlive() || target == mc.player)
        {
            return false;
        }

        if (Managers.SOCIAL.isFriend(target))
        {
            return false;
        }

        if (!isValid(target))
        {
            return false;
        }

        double distanceSq = mc.player.squaredDistanceTo(target);
        return distanceSq <= rangeConfig.getValue() * rangeConfig.getValue();
    }

    private void attackTarget(Entity target)
    {
        if (packetConfig.getValue())
        {
            boolean sprinting = mc.player.isSprinting();
            if (sprinting)
            {
                sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
            }

            sendAttackPackets(target, swingConfig.getValue());
            mc.player.resetLastAttackedTicks();
            attackTimer.reset();

            if (sprinting)
            {
                sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
            }
        }

        if (glfwClickConfig.getValue())
        {
            suppressGlfwAttackPacket = packetConfig.getValue();
            suppressEntityId = suppressGlfwAttackPacket ? target.getId() : -1;
            suppressPacketExpire = suppressGlfwAttackPacket ? System.currentTimeMillis() + 250L : 0L;
            long window = mc.getWindow().getHandle();
            AccessorMouse mouse = (AccessorMouse) mc.mouse;
            mouse.callOnMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_PRESS, 0);
            mouse.callOnMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT, GLFW.GLFW_RELEASE, 0);
        }
    }

    @Override
    public boolean isValid(Entity entity)
    {
        if (entity instanceof PlayerEntity player && player.isCreative())
        {
            return false;
        }

        return super.isValid(entity) && !(entity instanceof FakePlayerEntity);
    }
}