package me.talonos.talonmap;

import me.talonos.talonmap.lib.ServerExtension;
import me.talonos.talonmap.world.ImageBiomeSource;
import me.talonos.talonmap.world.ImageChunkGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class Talonmap implements ModInitializer {
    private static ServerResourceManager manager = null;

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(ImagesLoader.INSTANCE);
        ServerLifecycleEvents.SERVER_STARTING.register(s -> ((ServerExtension) s).getServerResourceManager());
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((s, resourceManager) -> setResourceManager(resourceManager));
        ServerLifecycleEvents.SERVER_STOPPING.register(s -> setResourceManager(null));

        ImageChunkGenerator.init();
        ImageBiomeSource.init();
    };

    public static Optional<ServerResourceManager> getServerResourceManager() {
        return Optional.ofNullable(manager);
    }

    private static void setResourceManager(ServerResourceManager server) {
        Talonmap.manager = server;
    }
}
