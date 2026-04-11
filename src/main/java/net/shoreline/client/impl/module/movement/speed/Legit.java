package net.shoreline.client.impl.module.movement.speed;

import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.module.movement.SpeedModule;

public class Legit extends BaseSpeedFeature<SpeedModule>
{
    public Legit()
    {
        super("Legit");
    }

    @Override
    public Vec3d onMoveUpdate(SpeedModule module, Vec3d currentMove)
    {
        return currentMove;
    }
}