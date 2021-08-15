package org.sandboxpowered.fabric.mixin.impl;

import net.minecraft.block.Block;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.NotNull;
import org.sandboxpowered.fabric.api.block.PolyBlock;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Block.class)
@Implements(@Interface(iface = PolyBlock.class, prefix = "sbx$", unique = true))
public class MixinBlock {

    @NotNull
    public String sbx$id() {
        return Registry.BLOCK.getId((Block) (Object) this).toString();
    }
}
