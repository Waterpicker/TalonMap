package me.talonos.talonmap.client;

import com.google.common.collect.ImmutableList;
import me.talonos.talonmap.mixin.GeneratorTypeAccessor;
import me.talonos.talonmap.world.ImageBiomeSource;
import me.talonos.talonmap.world.ImageChunkGenerator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.world.GeneratorType;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.BuiltinBiomes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;

import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.minecraft.world.gen.chunk.ChunkGeneratorSettings.OVERWORLD;

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
