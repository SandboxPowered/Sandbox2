package org.sandboxpowered.fabric.mixin;

import net.minecraft.Bootstrap;
import net.minecraft.server.MinecraftServer;
import org.sandboxpowered.fabric.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void sandbox_server_initialize(CallbackInfo ci) {
            Main.INSTANCE.startSandboxServer((MinecraftServer) (Object) this);
    }
}
