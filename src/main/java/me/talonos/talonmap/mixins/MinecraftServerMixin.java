package me.talonos.talonmap.mixins;

import me.talonos.talonmap.lib.ServerExtension;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements ServerExtension {

    @Shadow private ServerResourceManager serverResourceManager;

    public ServerResourceManager getServerResourceManager() {
        return serverResourceManager;
    }
}
