package org.sandboxpowered.fabric.mixin;

import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.obfuscate.DontObfuscate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ClientBrandRetriever.class)
public class MixinClientBrandRetriever {
    /**
     * @author
     */
    @DontObfuscate
    @Overwrite(remap = false)
    public static String getClientModName() {
        return "sandbox";
    }
}
