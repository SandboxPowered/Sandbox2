package org.sandboxpowered.fabric.mixin;

import net.minecraft.Bootstrap;
import org.sandboxpowered.fabric.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class MixinBootstrap {
    @Shadow
    private static volatile boolean initialized;

    @Inject(method = "initialize", at = @At("HEAD"))
    private static void sandbox_initialize(CallbackInfo ci) {
        if (!initialized) {
            Main.INSTANCE.startSandboxInternals();
        }
    }
}
