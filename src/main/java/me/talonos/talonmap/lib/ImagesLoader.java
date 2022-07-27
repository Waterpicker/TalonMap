package me.talonos.talonmap.lib;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
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

public class ImagesLoader implements PreparableReloadListener {
    public static final ImagesLoader INSTANCE = new ImagesLoader();

    private static final BufferedImage DEFAULT = new BufferedImage(BufferedImage.TYPE_BYTE_GRAY, 1, 1);
    private static Map<ResourceLocation, BufferedImage> images = new HashMap<>();

    public static BufferedImage getImage(ResourceLocation id) {
        return images.getOrDefault(id, DEFAULT);
    }

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

    public CompletableFuture<Void> apply(Map<ResourceLocation, BufferedImage> data, ResourceManager manager, ProfilerFiller profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> images = data);
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier helper, ResourceManager manager, ProfilerFiller loadProfiler, ProfilerFiller applyProfiler, Executor loadExecutor, Executor applyExecutor) {
        return load(manager, loadProfiler, loadExecutor).thenCompose(helper::wait).thenCompose(
                (o) -> apply(o, manager, applyProfiler, applyExecutor)
        );
    }
}
