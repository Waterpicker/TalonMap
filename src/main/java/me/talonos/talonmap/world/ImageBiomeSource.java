package me.talonos.talonmap.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ShortArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import me.talonos.talonmap.lib.ImagesLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryLookupCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import org.apache.logging.log4j.LogManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ImageBiomeSource extends BiomeSource {
    private static final Codec<ResourceKey<Biome>> BIOME_KEY_CODEC = ResourceLocation.CODEC.xmap(ResourceKey.elementKey(Registry.BIOME_REGISTRY), ResourceKey::location);

    public static final Codec<ImageBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("image").forGetter(a -> a.image),
            Codec.unboundedMap(Codec.STRING.xmap(Color::decode, ImageBiomeSource::colourToString), BIOME_KEY_CODEC).fieldOf("mappings").forGetter(a -> a.mappings),
            BIOME_KEY_CODEC.optionalFieldOf("filler", Biomes.THE_VOID).forGetter(a -> a.filler),
            RegistryLookupCodec.create(Registry.BIOME_REGISTRY).forGetter(vanillaLayeredBiomeSource -> vanillaLayeredBiomeSource.biomeRegistry))
            .apply(instance, ImageBiomeSource::new));

    private final Biome biomeFiller;
    private final ResourceLocation image;
    private final Map<Color, ResourceKey<Biome>> mappings;
    private final ResourceKey<Biome> filler;

    private final short[][] biomeArray;

    public static final Map<Color, Short> colorToShort = new Object2ShortArrayMap<>();
    public static final Map<Short, Biome> shortToBiome = new Short2ObjectArrayMap<>();

    private final Registry<Biome> biomeRegistry;

    public ImageBiomeSource(ResourceLocation image, Map<Color, ResourceKey<Biome>> mappings, ResourceKey<Biome> filler, Registry<Biome> biomeRegistry) {
        super(Stream.concat(mappings.values().stream(), Stream.of(filler)).map(ResourceKey -> () -> biomeRegistry.getOrThrow(ResourceKey)));
        this.image = image;
        this.mappings = mappings;
        this.biomeRegistry = biomeRegistry;

        this.filler = filler;
        this.biomeFiller = biomeRegistry.getOrThrow(filler);
        short biomeIdMap = 0;

        for (Map.Entry<Color, ResourceKey<Biome>> i : mappings.entrySet()) {
            Biome m = biomeRegistry.getOptional(mappings.get(i.getKey())).orElse(biomeFiller);
            colorToShort.put(i.getKey(), biomeIdMap);
            shortToBiome.put(biomeIdMap, m);
            biomeIdMap++;
        }

        biomeArray = extractBiomes(ImagesLoader.getImage(image));
    }

    private short[][] extractBiomes(BufferedImage biomes) {
        List<Color> incorrectColors = new ArrayList<>();

        Raster raster = biomes.getRaster();

        short[][] biomeArry = new short[raster.getWidth()][raster.getHeight()];

        for (int x = 0; x < raster.getWidth(); x++) {
            for (int y = 0; y < raster.getHeight(); y++) {
                Color biomeColor = new Color(biomes.getRGB(x,y));
                if (ImageBiomeSource.colorToShort.get(biomeColor) != null) {
                    biomeArry[x][y] = ImageBiomeSource.colorToShort.get(biomeColor);
                } else {
                    if (!incorrectColors.contains(biomeColor)) {
                        LogManager.getLogger("talonmap").warn("Talonmap: Color "+ colourToString(biomeColor) +" not found at ("  + x + ", " + y + ")");
                        incorrectColors.add(biomeColor);
                    }
                }
            }
        }

        return biomeArry;
    }


    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long seed) {
        return this;
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        if (contains(x, z, 0, 0, biomeArray.length-1, biomeArray[0].length-1)) {
            return shortToBiome.get(biomeArray[x][z]);
        } else {
            return biomeFiller;
        }
    }

    public static boolean contains(int x, int y, int xPos, int yPos, int width, int height) {
        return x >= xPos && x <= xPos + width && y >= yPos && y <= yPos + height;
    }

    public static void init() {
        Registry.register(Registry.BIOME_SOURCE, "image", ImageBiomeSource.CODEC);
    }

    public static String colourToString(java.awt.Color c) {
        return "#" + Integer.toHexString(0xFF000000 | c.getRGB()).substring(2);
    }
}
