package me.talonos.talonmap;

import me.talonos.talonmap.lib.ImagesLoader;
import me.talonos.talonmap.world.ImageBiomeSource;
import me.talonos.talonmap.world.ImageChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("talonmap")
public class TalonMap {
    public TalonMap() {
        MinecraftForge.EVENT_BUS.addListener(this::resourceLoad);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onInitialize);
    }

    public void resourceLoad(AddReloadListenerEvent event) {
        event.addListener(ImagesLoader.INSTANCE);
    }

    public void onInitialize(FMLCommonSetupEvent event) {
        ImageChunkGenerator.init();
        ImageBiomeSource.init();
    };
}
