package me.talonos.talonmap.lib;

import me.talonos.talonmap.Talonmap;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.util.Identifier;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class ImageUtil {
//    public static BufferedImage loadImage(@Nonnull String id, @Nonnull String name, @Nonnull String url) {
////        Path path = Talonmap.imageFolder.resolve(id + "_" + name + ".png");
//
//        BufferedImage image;
//
//        if(Files.exists(path)) {
//            try {
//                return ImageIO.read(path.toFile());
//            } catch (IOException e) {
//                e.printStackTrace();
//                return new BufferedImage(BufferedImage.TYPE_BYTE_GRAY, 1, 1);
//            }
//        } else {
//            if (url.isEmpty()) {
//                try {
//                    image = ImageIO.read(new URL(url));
//                    ImageIO.write(image, "png", path.toFile());
//                } catch (IOException e) {
//                    e.printStackTrace();
//                     image = new BufferedImage(BufferedImage.TYPE_BYTE_GRAY, 1, 1);
//                }
//            } else {
//                image = new BufferedImage(BufferedImage.TYPE_BYTE_GRAY, 1, 1);
//            }
//        }
//
//        return image;
//    }

    public static BufferedImage loadImage(@Nonnull Identifier location) {
        Identifier identifier = new Identifier(location.getNamespace(), "dimension/images/" + location.getPath());




        return Talonmap.getServerResourceManager().map(ServerResourceManager::getResourceManager).filter(a -> {
            boolean b = a.containsResource(identifier);
            return b;
                }).map(a -> {
            try {
                return a.getResource(identifier);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }).filter(Objects::nonNull)
                .map(Resource::getInputStream).map(a -> {
                    try {
                        return ImageIO.read(a);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }).filter(Objects::nonNull)
                .orElseGet(() -> new BufferedImage(BufferedImage.TYPE_BYTE_GRAY, 1, 1));
    }
}
