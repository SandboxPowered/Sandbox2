package org.sandboxpowered.fabric.mixin;

import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.registry.DynamicRegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerResourceManager.class)
public class MixinServerResourceManager {
    @Inject(method = "<init>", at = @At(value = "TAIL"))
    public void main(DynamicRegistryManager registryManager, CommandManager.RegistrationEnvironment commandEnvironment, int functionPermissionLevel, CallbackInfo ci) {
        org.sandboxpowered.fabric.Main.INSTANCE.setResourceManager((ServerResourceManager) (Object) this);
    }
}
