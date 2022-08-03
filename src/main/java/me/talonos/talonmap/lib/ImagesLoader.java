package me.talonos.talonmap.lib;

import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ImagesLoader implements SimpleResourceReloadListener<Map<ResourceLocation, BufferedImage>> {
    public static final ImagesLoader INSTANCE = new ImagesLoader();

    private static final BufferedImage DEFAULT = new BufferedImage(BufferedImage.TYPE_BYTE_GRAY, 1, 1);
    private static Map<ResourceLocation, BufferedImage> images = new HashMap<>();

    private final ResourceLocation id = new ResourceLocation("talonmap:map_images");
    @Override
    public ResourceLocation getFabricId() {
        return id;
    }

    public static BufferedImage getImage(ResourceLocation id) {
        return images.getOrDefault(id, DEFAULT);
    }

    @Override
    public CompletableFuture<Map<ResourceLocation, BufferedImage>> load(ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<ResourceLocation, BufferedImage> map = new HashMap<>();

            Collection<ResourceLocation> resources = manager.listResources("dimension/images", a -> a.endsWith(".png"));

            for (ResourceLocation fileId : resources) {
                ResourceLocation id = new ResourceLocation(fileId.getNamespace(), fileId.getPath().replace("dimension/images/", ""));

                try {
                    Resource resource = manager.getResource(fileId);

                    BufferedImage image = ImageIO.read(resource.getInputStream());

                    map.put(id, image);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }

            return map;
        });
    }

    @Override
    public CompletableFuture<Void> apply(Map<ResourceLocation, BufferedImage> data, ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> images = data);
    }
}
