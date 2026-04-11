package net.shoreline.client.impl.module.movement.speed;

import net.minecraft.util.math.Vec2f;
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
        if (!mc.player.input.hasForwardMovement() || mc.player.isSneaking())
        {
            return currentMove;
        }

        double moveY = currentMove.y;
        if (mc.player.isOnGround() && !mc.player.input.playerInput.jump())
        {
            float jump = 0.42f + getJumpModifier();
            moveY = jump;
            module.setMotionY(jump);
        }

        double horizontalSpeed = Math.max(getBaseSpeed(), Math.hypot(currentMove.x, currentMove.z));
        Vec2f motion = module.strafe((float) horizontalSpeed);
        return new Vec3d(motion.x, moveY, motion.y);
    }
}