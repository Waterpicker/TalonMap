package me.talonos.talonmap;

import me.talonos.talonmap.lib.ImagesLoader;
import me.talonos.talonmap.world.ImageBiomeSource;
import me.talonos.talonmap.world.ImageChunkGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;

public class TalonMap implements ModInitializer {

    @Override
    public void onInitialize() {
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(ImagesLoader.INSTANCE);
        ImageChunkGenerator.init();
        ImageBiomeSource.init();
    };
}
