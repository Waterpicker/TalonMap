package me.talonos.talonmap;

import me.talonos.talonmap.lib.ImageUtil;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.lwjgl.system.CallbackI;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ImagesLoader implements SimpleResourceReloadListener<Map<Identifier, BufferedImage>> {
    public static final ImagesLoader INSTANCE = new ImagesLoader();

    private static BufferedImage DEFAULT = new BufferedImage(BufferedImage.TYPE_BYTE_GRAY, 1, 1);
    private static Map<Identifier, BufferedImage> images = new HashMap<>();

    private Identifier id = new Identifier("talonmap:map_images");
    @Override
    public Identifier getFabricId() {
        return id;
    }

    public static BufferedImage getImage(Identifier id) {
        return images.getOrDefault(id, DEFAULT);
    }

//    @Override
//    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler, Executor prepareExecutor, Executor applyExecutor) {
//        return CompletableFuture.runAsync(() -> {
//            images.clear();
//            for (Identifier identifier : manager.findResources("dimension/images", a -> a.endsWith(".png"))) {
//                try {
//                    BufferedImage image = ImageIO.read(manager.getResource(identifier).getInputStream());
//
//                    images.put(identifier, image);
//
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    @Override
    public CompletableFuture<Map<Identifier, BufferedImage>> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Map<Identifier, BufferedImage> map = new HashMap<>();

            Collection<Identifier> resources = manager.findResources("dimension/images", a -> a.endsWith(".png"));

            for (Identifier fileId : resources) {
                Identifier id = new Identifier(fileId.getNamespace(), fileId.getPath().replace("dimension/images/", ""));

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
    public CompletableFuture<Void> apply(Map<Identifier, BufferedImage> data, ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.runAsync(() -> images = data);
    }
}
