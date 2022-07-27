package me.talonos.talonmap.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.talonos.talonmap.lib.ImagesLoader;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.chunk.VerticalBlockSample;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.function.Supplier;

public class ImageChunkGenerator extends NoiseChunkGenerator {
    private static final BlockState AIR = Blocks.AIR.getDefaultState();

    public static final Codec<ImageChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group((BiomeSource.CODEC.fieldOf("biome_source")).forGetter(noiseChunkGenerator -> noiseChunkGenerator.populationSource),
                    Codec.LONG.fieldOf("seed").stable().forGetter(noiseChunkGenerator -> noiseChunkGenerator.seed),
                    ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter(noiseChunkGenerator -> noiseChunkGenerator.settings),
                    Identifier.CODEC.fieldOf("image").forGetter(a -> a.image))
                    .apply(instance, instance.stable(ImageChunkGenerator::new)));

    private final short[][] heightMapArray;
    private final Identifier image;

    public ImageChunkGenerator(BiomeSource biomeSource, long seed, Supplier<ChunkGeneratorSettings> settings, Identifier image) {
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
    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    @Override
    public int getHeightInGround(int x, int z, Heightmap.Type heightmapType) {
        return super.getHeightInGround(x, z, heightmapType);
    }

    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmapType) {
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
    public BlockView getColumnSample(int x, int z) {
        int height = Math.max(getHeight(x,z), getSeaLevel());
        BlockState[] states = new BlockState[height];

        for (int i = 0; i < states.length; i++) {
            states[i] = getBlockState(i, height);
        }

        return new VerticalBlockSample(states);
    }

    @Override
    public void buildSurface(ChunkRegion region, Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int i = chunkPos.x;
        int j = chunkPos.z;
        ChunkRandom chunkRandom = new ChunkRandom();
        chunkRandom.setTerrainSeed(i, j);
        ChunkPos chunkPos2 = chunk.getPos();
        int k = chunkPos2.getStartX();
        int l = chunkPos2.getStartZ();

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int m = 0; m < 16; ++m) {
            for (int n = 0; n < 16; ++n) {
                int o = k + m;
                int p = l + n;
                int q = chunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE_WG, m, n) + 1;
                region.getBiome(mutable.set(k + m, q, l + n)).buildSurface(chunkRandom, chunk, o, p, q, 0, this.defaultBlock, this.defaultFluid, this.getSeaLevel(), region.getSeed());
            }
        }

        //Todo: customize bedrock generation
        for (BlockPos pos : BlockPos.iterate(i, 0, j, i + 15, 0, j + 15)) {
            chunk.setBlockState(pos, Blocks.BEDROCK.getDefaultState(), false);
        }
    }

    public void populateNoise(WorldAccess world, StructureAccessor accessor, Chunk chunk) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        Heightmap ocean_floor = chunk.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
        Heightmap world_surface = chunk.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);

        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;

        for (int x = 0; x < 16; ++x) {
            for (int z = 0; z < 16; ++z) {
                int height = getHeight(chunkX * 16 + x, chunkZ * 16 + z);

                for (int y = 0; y < Math.max(height, getSeaLevel()); ++y) {
                    BlockState blockState = getBlockState(y, height);
                    if (blockState == AIR) continue;
                    if (blockState.getLuminance() != 0) {
                        mutable.set(x,y,z);
                        ((ProtoChunk) chunk).addLightSource(mutable);
                    }

                    chunk.setBlockState(mutable.set(x, y, z), blockState, false);
                    ocean_floor.trackUpdate(x, y, z, blockState);
                    world_surface.trackUpdate(x, y, z, blockState);
                }
            }
        }
    }

    public static void init() {
        Registry.register(Registry.CHUNK_GENERATOR, "image", ImageChunkGenerator.CODEC);
    }
}
