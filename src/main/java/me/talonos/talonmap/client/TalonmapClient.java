package me.talonos.talonmap.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class TalonmapClient implements ClientModInitializer {
//    GeneratorType IMAGE = new GeneratorType("image") {
//
//        @Override
//        protected ChunkGenerator getChunkGenerator(Registry<Biome> biomeRegistry, Registry<ChunkGeneratorSettings> chunkGeneratorSettingsRegistry, long seed) {
//            return new ImageChunkGenerator(new ImageBiomeSource(Textures.biomes, Stream.of(BiomeKeys.OCEAN, BiomeKeys.BEACH, BiomeKeys.SAVANNA, BiomeKeys.PLAINS, BiomeKeys.JUNGLE_EDGE, BiomeKeys.JUNGLE).map(a -> (Supplier<Biome>) () -> biomeRegistry.get(a)).collect(Collectors.toList()), () -> biomeRegistry.get(BiomeKeys.THE_VOID)), seed, () -> chunkGeneratorSettingsRegistry.get(OVERWORLD),
//                    Textures.generator);
//        }
//    };

    @Override
    public void onInitializeClient() {
//        GeneratorTypeAccessor.getValues().add(IMAGE);
    }
}
