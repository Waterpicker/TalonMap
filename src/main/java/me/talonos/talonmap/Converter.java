package me.talonos.talonmap;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Converter {
    public static void main(String[] args) throws IOException {
        String heightmap = "heightmap.png";
        String biomes =  "biomes.png";

        String[] type = args[0].split(":");

        String modid = type.length > 1 ? type[0] : "minecraft";
        String id;
        if (type.length == 1) id = type[0];
        else id = type[1];

        StringBuilder builder = new StringBuilder();

        createBiome(biomes, builder);
        createHeightmap(heightmap, builder);

        Writer writer = Files.newBufferedWriter(Paths.get(id + ".txt"), StandardOpenOption.CREATE);
        writer.write(builder.toString());
        writer.close();
    }

    private static void createHeightmap(String heightmap, StringBuilder builder) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream() {
            @Override
            public synchronized byte[] toByteArray() {
                return this.buf;
            }
        };

        ImageIO.write(ImageIO.read(new File(heightmap)), "png", output);
        builder.append("\nHeightmap Image: " + Base64.getMimeEncoder().encodeToString(output.toByteArray()));
    }

    private static void createBiome(String biomes, StringBuilder builder) throws IOException {
        BufferedImage input = ImageIO.read(new File(biomes));

        Map<Color, Integer> colors = new HashMap<>();

        Map<Color, Integer> colorsCount = new HashMap<>();

        AtomicInteger count = new AtomicInteger(0);

        int width = input.getWidth();
        int height = input.getHeight();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        WritableRaster raster = image.getRaster();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = new Color(input.getRGB(x,y));

                if(!colors.containsKey(color)) {
                    colors.put(color, count.getAndIncrement());
                }

                if(!colorsCount.containsKey(color)) {
                    colorsCount.merge(color, 1, Integer::sum);
                }

                raster.setSample(x,y, 0, colors.get(color));
            }
        }

        colorsCount.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getValue)).map(a -> Integer.toHexString(0xFF000000 | a.getKey().getRGB()).substring(2) + ": " + a.getValue() + "\n").forEach(System.out::println);

        final ByteArrayOutputStream output = new ByteArrayOutputStream() {
            @Override
            public synchronized byte[] toByteArray() {
                return this.buf;
            }
        };

        ImageIO.write(image, "png", output);

        String integer = Base64.getMimeEncoder().encodeToString(output.toByteArray());

        builder.append("Mapping Data:\n");

        colors.entrySet().stream().sorted(Comparator.comparingInt(Map.Entry::getValue)).map(Map.Entry::getKey).map(a -> Integer.toHexString(0xFF000000 | a.getRGB()).substring(2)).map(a -> "\t" + a + "\n").forEach(builder::append);
        builder.append("\n");
        builder.append("Biome Image: " + integer + "\n");
    }
}
