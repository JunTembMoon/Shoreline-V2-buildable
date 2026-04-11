package net.shoreline.client.mixin;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Mouse.class)
public interface AccessorMouse
{
    @Invoker("onMouseButton")
    void callOnMouseButton(long window, int button, int action, int mods);
}