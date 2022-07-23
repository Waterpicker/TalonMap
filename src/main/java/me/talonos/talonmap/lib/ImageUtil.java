package me.talonos.talonmap.lib;

import me.talonos.talonmap.Talonmap;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class ImageUtil {
    public static BufferedImage loadImage(@Nonnull String id, @Nonnull String name, @Nonnull String url) {
        Path path = Talonmap.imageFolder.resolve(id + "_" + name + ".png");

        BufferedImage image;

        if(Files.exists(path)) {
            try {
                return ImageIO.read(path.toFile());
            } catch (IOException e) {
                e.printStackTrace();
                return new BufferedImage(BufferedImage.TYPE_BYTE_GRAY, 1, 1);
            }
        } else {
            if (url.isEmpty()) {
                try {
                    image = ImageIO.read(new URL(url));
                    ImageIO.write(image, "png", path.toFile());
                } catch (IOException e) {
                    e.printStackTrace();
                     image = new BufferedImage(BufferedImage.TYPE_BYTE_GRAY, 1, 1);
                }
            } else {
                image = new BufferedImage(BufferedImage.TYPE_BYTE_GRAY, 1, 1);
            }
        }

        return image;
    }
}
