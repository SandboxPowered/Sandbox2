package org.sandboxpowered.fabric.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.world.SaveProperties;
import org.sandboxpowered.fabric.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Redirect(method = "startIntegratedServer(Ljava/lang/String;Lnet/minecraft/util/registry/DynamicRegistryManager$Impl;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function4;ZLnet/minecraft/client/MinecraftClient$WorldLoadAction;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient$IntegratedResourceManager;getSaveProperties()Lnet/minecraft/world/SaveProperties;"))
    public SaveProperties inj(MinecraftClient.IntegratedResourceManager integratedResourceManager) {
        Main.INSTANCE.setResourceManager(integratedResourceManager.getServerResourceManager());
        Main.INSTANCE.startSandboxServer();
        return integratedResourceManager.getSaveProperties();
    }
}
