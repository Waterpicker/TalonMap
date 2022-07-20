package me.talonos.talonmap.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ShortArrayMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectArrayMap;
import me.talonos.talonmap.lib.ImageUtil;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.PortalUtil;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.biome.source.BiomeSource;
import org.apache.logging.log4j.LogManager;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ImageBiomeSource extends BiomeSource {
    public static final Codec<ImageBiomeSource> CODEC = RecordCodecBuilder.create(instance -> {
        return instance.group(
                Codec.STRING.fieldOf("data").forGetter(a -> a.data),
                Codec.list(Biome.REGISTRY_CODEC).fieldOf("mappings").forGetter(a -> a.mappings),
                Biome.REGISTRY_CODEC.optionalFieldOf("filler", () -> BuiltinBiomes.PLAINS).forGetter(a -> () -> a.filler))
                .apply(instance, ImageBiomeSource::new);
    });
    private String data;
    private List<Supplier<Biome>> mappings;
    private final Biome filler;

    private short[][] biomeArray;

    public final Map<Short, Biome> shortToBiome = new Short2ObjectArrayMap<Biome>();
    private final Map<Integer, Short> colorToShort;

    public ImageBiomeSource(String data, List<Supplier<Biome>> mappings, Supplier<Biome> filler) {
        super(Stream.concat(mappings.stream(), Stream.of(filler)));
        this.data = data;
        this.mappings = mappings;

        this.filler = filler.get();

        colorToShort = new Int2ShortArrayMap();

        short talonMapInternalBiomeId = 0;

        for (int i = 0, mappingSize = mappings.size(); i < mappingSize; i++) {
            Supplier<Biome> m = mappings.get(i);
            colorToShort.put(i, talonMapInternalBiomeId);
//            LogManager.getLogger(Constants.MODID).debug(Biome.REGISTRY.getObject(new ResourceLocation(m.name)));
            shortToBiome.put(talonMapInternalBiomeId, m.get());
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
                return filler;
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
