package me.talonos.talonmap.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.talonos.talonmap.lib.ImagesLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.WorldgenRandom;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.function.Supplier;

public class ImageChunkGenerator extends NoiseBasedChunkGenerator {
    private static final BlockState AIR = Blocks.AIR.defaultBlockState();

    public static final Codec<ImageChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group((BiomeSource.CODEC.fieldOf("biome_source")).forGetter(noiseChunkGenerator -> noiseChunkGenerator.biomeSource), 
                            Codec.LONG.fieldOf("seed").stable().forGetter(noiseChunkGenerator -> noiseChunkGenerator.seed),
                            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(noiseChunkGenerator -> noiseChunkGenerator.settings), 
                            ResourceLocation.CODEC.fieldOf("image").forGetter(a -> a.image))
                    .apply(instance, instance.stable(ImageChunkGenerator::new)));

    private final short[][] heightMapArray;
    private final ResourceLocation image;

    public ImageChunkGenerator(BiomeSource biomeSource, long seed, Supplier<NoiseGeneratorSettings> settings, ResourceLocation image) {
        super(biomeSource, seed, settings);
        heightMapArray = extractHeightmap(ImagesLoader.getImage(image));
        this.image = image;
    }

    private short[][] extractHeightmap(BufferedImage image) {
        //TODO: Convert to using bytes?
        short[][] heightMapData = new short[image.getWidth()][image.getHeight()];
        WritableRaster r = image.getRaster();
        for (int z = 0; z < heightMapData[0].length; z++)
        {
            for (int x = 0; x < heightMapData.length; x++)
            {
                heightMapData[x][z]=(short)r.getSample(x, z, 0);
            }
        }
        return heightMapData;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmapType) {
        return getHeight(x, z);
    }

    public int getHeight(int realX, int realZ) {
        if (realX >= 0 && realZ >= 0 && realX < heightMapArray.length && realZ < heightMapArray[0].length) {
            return heightMapArray[realX][realZ];
        } else {
            return 0;
        }
    }

    private BlockState getBlockState(int y, int height) {
        BlockState state;
        if (y > height) state = AIR;
        else state = settings.get().getDefaultBlock();

        if (state == AIR && getSeaLevel() >= y) return settings.get().getDefaultFluid();
        return state;
    }

    @Override
    public BlockGetter getBaseColumn(int x, int z) {
        int height = Math.max(getHeight(x,z), getSeaLevel());
        BlockState[] states = new BlockState[height];

        for (int i = 0; i < states.length; i++) {
            states[i] = getBlockState(i, height);
        }

        return new NoiseColumn(states);
    }

    @Override
    public void buildSurfaceAndBedrock(WorldGenRegion region, ChunkAccess chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int i = chunkPos.x;
        int j = chunkPos.z;
        WorldgenRandom chunkRandom = new WorldgenRandom();
        chunkRandom.setBaseChunkSeed(i, j);
        ChunkPos chunkPos2 = chunk.getPos();
        int k = chunkPos2.getMinBlockX();
        int l = chunkPos2.getMinBlockZ();

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int m = 0; m < 16; ++m) {
            for (int n = 0; n < 16; ++n) {
                int o = k + m;
                int p = l + n;
                int q = chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, m, n) + 1;
                region.getBiome(mutable.set(k + m, q, l + n)).buildSurfaceAt(chunkRandom, chunk, o, p, q, 0, this.defaultBlock, this.defaultFluid, this.getSeaLevel(), region.getSeed());
            }
        }

        //Todo: customize bedrock generation
        for (BlockPos pos : BlockPos.betweenClosed(i, 0, j, i + 15, 0, j + 15)) {
            chunk.setBlockState(pos, Blocks.BEDROCK.defaultBlockState(), false);
        }
    }

    public void fillFromNoise(LevelAccessor world, StructureFeatureManager accessor, ChunkAccess chunk) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        Heightmap ocean_floor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap world_surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int height = getHeight(chunkX * 16 + x, chunkZ * 16 + z);

                for (int y = 0; y < Math.max(height, getSeaLevel()); ++y) {
                    BlockState blockState = getBlockState(y, height);
                    if (blockState == AIR) continue;
                    if (blockState.getLightEmission() != 0) {
                        mutable.set(x,y,z);
                        ((ProtoChunk) chunk).addLight(mutable);
                    }

                    chunk.setBlockState(mutable.set(x, y, z), blockState, false);
                    ocean_floor.update(x, y, z, blockState);
                    world_surface.update(x, y, z, blockState);
                }
            }
        }
    }

    public static void init() {
        Registry.register(Registry.CHUNK_GENERATOR, "image", ImageChunkGenerator.CODEC);
    }
}
