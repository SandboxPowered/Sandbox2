package org.sandboxpowered.fabric.mixin;

import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;
import org.sandboxpowered.fabric.api.StateManagement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BooleanProperty.class)
public abstract class MixinBooleanProperty extends Property<Boolean> {
    public MixinBooleanProperty(String name, Class<Boolean> type) {
        super(name, type);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void sandbox_init(String name, CallbackInfo ci) {
        StateManagement.INSTANCE.putInternalStateProperty(this);
    }
}
