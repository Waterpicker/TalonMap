package me.talonos.talonmap.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ShortArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import me.talonos.talonmap.ImagesLoader;
import me.talonos.talonmap.lib.ImageUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.RegistryLookupCodec;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import org.apache.logging.log4j.LogManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class ImageBiomeSource extends BiomeSource {
    private static final Codec<RegistryKey<Biome>> BIOME_KEY_CODEC = Identifier.CODEC.xmap(RegistryKey.createKeyFactory(Registry.BIOME_KEY), RegistryKey::getValue);

    public static final Codec<ImageBiomeSource> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("image").forGetter(a -> a.image),
            Codec.unboundedMap(Codec.STRING.xmap(Color::decode, ImageBiomeSource::colourToString), BIOME_KEY_CODEC).fieldOf("mappings").forGetter(a -> a.mappings),
            BIOME_KEY_CODEC.optionalFieldOf("filler", BiomeKeys.THE_VOID).forGetter(a -> a.filler),
            RegistryLookupCodec.of(Registry.BIOME_KEY).forGetter(vanillaLayeredBiomeSource -> vanillaLayeredBiomeSource.biomeRegistry))
            .apply(instance, ImageBiomeSource::new));
    private final Biome biomeFiller;
    private Identifier image;
    private Map<Color, RegistryKey<Biome>> mappings;
    private final RegistryKey<Biome> filler;

    private short[][] biomeArray;

    public static final Map<Color, Short> colorToShort = new Object2ShortArrayMap<>();
    public static final Map<Short, Biome> shortToBiome = new Short2ObjectArrayMap<>();

    private Registry<Biome> biomeRegistry;

    public ImageBiomeSource(Identifier image, Map<Color, RegistryKey<Biome>> mappings, RegistryKey<Biome> filler, Registry<Biome> biomeRegistry) {
        super(Stream.concat(mappings.values().stream(), Stream.of(filler)).map(registryKey -> () -> biomeRegistry.getOrThrow(registryKey)));
        this.image = image;
        this.mappings = mappings;
        this.biomeRegistry = biomeRegistry;

        this.filler = filler;
        this.biomeFiller = biomeRegistry.getOrThrow(filler);
        short talonMapInternalBiomeId = 0;

        for (Map.Entry<Color, RegistryKey<Biome>> i : mappings.entrySet()) {
            Biome m = biomeRegistry.getOrThrow(mappings.get(i.getKey()));
            colorToShort.put(i.getKey(), talonMapInternalBiomeId);
//            LogManager.getLogger(Constants.MODID).debug(Biome.REGISTRY.getObject(new ResourceLocation(m.name)));
            shortToBiome.put(talonMapInternalBiomeId, m);
            talonMapInternalBiomeId++;
        }

        biomeArray = biomesFromColorImage(ImagesLoader.INSTANCE.getImage(image), colorToShort);
    }

    private short[][] biomesFromColorImage(BufferedImage biomes, Map<Color, Short> colorToShort) {
        List<Color> colorsIveComplainedAbout = new ArrayList<>();

        Raster raster = biomes.getRaster();

        short[][] toReturn = new short[raster.getWidth()][raster.getHeight()];

        for (int x = 0; x < raster.getWidth(); x++) {
            for (int y = 0; y < raster.getHeight(); y++) {
                Color biomeColor = new Color(biomes.getRGB(x,y));
                if (colorToShort.get(biomeColor) != null) {
                    toReturn[x][y] = colorToShort.get(biomeColor);
                } else {
                    //Prevent 14 million lines of console spam if something goes wrong.
                    if (!colorsIveComplainedAbout.contains(biomeColor)) {
                        LogManager.getLogger("talonmap").warn("Talonmap: Color not found: " + biomeColor + " ("  + x + ", " + y + ")");
                        colorsIveComplainedAbout.add(biomeColor);
                    }
                }
            }
        }

        return toReturn;
    }


    @Override
    protected Codec<? extends BiomeSource> getCodec() {
        return CODEC;
    }

    @Override
    public BiomeSource withSeed(long seed) {
        return this;
    }

    @Override
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        //TODO: Properly get fill worker.
            if (contains(biomeX, biomeZ, 0, 0, biomeArray.length-1, biomeArray[0].length-1)) {
//                System.out.println(biomeX + " , " + biomeZ + " = " + biomeArray[biomeX][biomeZ]);

                return shortToBiome.get(biomeArray[biomeX][biomeZ]);
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
