package me.talonos.talonmap.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ShortArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import me.talonos.talonmap.lib.ImageUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.RegistryLookupCodec;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.BiomeSource;
import org.apache.logging.log4j.LogManager;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ImageBiomeSource extends BiomeSource {
    private static final Codec<RegistryKey<Biome>> BIOME_KEY_CODEC = Identifier.CODEC.xmap(RegistryKey.createKeyFactory(Registry.BIOME_KEY), RegistryKey::getValue);

    public static final Codec<ImageBiomeSource> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.fieldOf("data").forGetter(a -> a.data),
                Codec.list(BIOME_KEY_CODEC).fieldOf("mappings").forGetter(a -> a.mappings),
                BIOME_KEY_CODEC.optionalFieldOf("filler", BiomeKeys.THE_VOID).forGetter(a -> a.filler),
                RegistryLookupCodec.of(Registry.BIOME_KEY).forGetter(vanillaLayeredBiomeSource -> vanillaLayeredBiomeSource.biomeRegistry))
                .apply(instance, ImageBiomeSource::new);
    });
    private final Biome biomeFiller;
    private String data;
    private List<RegistryKey<Biome>> mappings;
    private final RegistryKey<Biome> filler;

    private short[][] biomeArray;

    public final Map<Short, Biome> shortToBiome = new Short2ObjectArrayMap<Biome>();
    private final Map<Integer, Short> colorToShort;
    private Registry<Biome> biomeRegistry;

    public ImageBiomeSource(String data, List<RegistryKey<Biome>> mappings, RegistryKey<Biome> filler, Registry<Biome> biomeRegistry) {
        super(Stream.concat(mappings.stream(), Stream.of(filler)).map(registryKey -> () -> biomeRegistry.getOrThrow(registryKey)));
        this.data = data;
        this.mappings = mappings;
        this.biomeRegistry = biomeRegistry;

        this.filler = filler;
        this.biomeFiller = biomeRegistry.getOrThrow(filler);
        colorToShort = new Int2ShortArrayMap();

        short talonMapInternalBiomeId = 0;

        for (int i = 0, mappingSize = mappings.size(); i < mappingSize; i++) {
            Biome m = biomeRegistry.getOrThrow(mappings.get(i));
            colorToShort.put(i, talonMapInternalBiomeId);
//            LogManager.getLogger(Constants.MODID).debug(Biome.REGISTRY.getObject(new ResourceLocation(m.name)));
            shortToBiome.put(talonMapInternalBiomeId, m);
            talonMapInternalBiomeId++;
        }

        biomeArray = biomesFromColorImage(ImageUtil.loadImage(data), colorToShort);
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
                System.out.println(biomeX + " , " + biomeZ + " = " + biomeArray[biomeX][biomeZ]);

                return shortToBiome.get(biomeArray[biomeX][biomeZ]);
            } else {
                return biomeFiller;
            }
    }

    public static boolean contains(int x, int y, int xPos, int yPos, int width, int height) {
        return x >= xPos && x <= xPos + width && y >= yPos && y <= yPos + height;
    }

    /**
     * Generates a two-dimensional array of Biome pointers that represent biomes on a per-block
     * basis.
     * @param imageToOperate the image to read in.
     * @return an array of BiomeIDs.
     */
    private static short[][] biomesFromColorImage(BufferedImage imageToOperate, Map<Integer, Short> colorToShort) {
        //We could make this more memory efficient in large worlds by using bytes instead of shorts,
        //but for my modpack that'd only save about 13 MB and I don't want to have to deal with
        //Java's lack of unsigned bytes.



        List<Integer> colorsIveComplainedAbout = new ArrayList<>();

        Raster raster = imageToOperate.getRaster();

        short[][] toReturn = new short[raster.getWidth()][raster.getHeight()];

        for (int x = 0; x < raster.getWidth(); x++) {
            for (int y = 0; y < raster.getHeight(); y++) {
                int biomeColor = raster.getSample(x,y, 0);
                if (colorToShort.get(biomeColor) != null) {
                    toReturn[x][y] = colorToShort.get(biomeColor);
                } else {
                    //Prevent 14 million lines of console spam if something goes wrong.
                    if (!colorsIveComplainedAbout.contains(biomeColor)) {
                        LogManager.getLogger("talonmap").warn("Talonmap: Color not found: " + biomeColor);
                        colorsIveComplainedAbout.add(biomeColor);
                    }
                }
            }
        }

        return toReturn;
    }

    public static void init() {
        Registry.register(Registry.BIOME_SOURCE, "image", ImageBiomeSource.CODEC);
    }
}
