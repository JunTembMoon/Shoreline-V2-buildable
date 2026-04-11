package net.shoreline.client.impl.module.combat.crystal;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.shoreline.client.api.LoggingFeature;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.event.render.RenderEntityWorldEvent;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CrystalOptimizer extends LoggingFeature
{
    private static final long DEAD_CRYSTAL_TIMEOUT = 1000L;

    private final ConcurrentMap<Integer, Long> deadCrystals = new ConcurrentHashMap<>();

    public CrystalOptimizer()
    {
        super("Crystal Optimizer");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onRenderEntity(RenderEntityWorldEvent event)
    {
        cleanup();
        if (event.getEntity() instanceof EndCrystalEntity && isDead(event.getEntity()))
        {
            event.cancel();
        }
    }

    @EventListener
    public void onWorldJoin(WorldEvent.Join event)
    {
        clear();
    }

    @EventListener
    public void onWorldDisconnect(WorldEvent.Disconnect event)
    {
        clear();
    }

    public boolean isDead(Entity entity)
    {
        Long deathTime = deadCrystals.get(entity.getId());
        return deathTime != null && System.currentTimeMillis() - deathTime <= DEAD_CRYSTAL_TIMEOUT;
    }

    public void setDead(int id)
    {
        deadCrystals.put(id, System.currentTimeMillis());
    }

    public void clearDead(int id)
    {
        deadCrystals.remove(id);
    }

    public void clear()
    {
        deadCrystals.clear();
    }

    private void cleanup()
    {
        long now = System.currentTimeMillis();
        deadCrystals.entrySet().removeIf(entry -> now - entry.getValue() > DEAD_CRYSTAL_TIMEOUT);
    }
}
