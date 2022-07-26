package me.talonos.talonmap.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.talonos.talonmap.lib.ImageUtil;

import java.awt.image.BufferedImage;

public class ImageData {
    public static final Codec<ImageData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(a -> a.id),
            Codec.STRING.optionalFieldOf("url", "").forGetter(a -> a.url)
    ).apply(instance, ImageData::new));

    public ImageData(String id, String url) {
        this.id = id;
        this.url = url;
    }

    String url;
    String id;

//    public BufferedImage image(String type) {
//        return ImageUtil.loadImage(id, type, url);
//    };
}
