package me.talonos.talonmap;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.talonos.talonmap.world.ImageBiomeSource;
import me.talonos.talonmap.world.ImageChunkGenerator;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

public class Talonmap implements ModInitializer {
    public static ServerResourceManager directory;

    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(a -> {
            System.out.println("I am a BIG DERP");
            setPath(((ServerExtension) a).getServerResourceManager());
        });
//        CommandRegistrationCallback.EVENT.register(a -> {
//            a.register(CommandManager.literal("sl-getBiomes").executes(b -> {
//                Entity entity = b.getSource().getEntityOrThrow();
//
//                BlockPos pos = entity.getBlockPos();
//
//                for (int x = 0; x < 5; x++) {
//                    for (int z = 0; z < 5; z++) {
//                        int y = entity.getEntityWorld().getTopY(Heightmap.Type.OCEAN_FLOOR_WG)pos.add(x,0, z);
//                    }
//                }
//
//                entity.getEntityWorld().getBiomeAccess().
//
//                return 1;
//            }));

            ImageChunkGenerator.init();
            ImageBiomeSource.init();
        };

    private static void setPath(ServerResourceManager manager) {
        Talonmap.directory = manager;
    }
}
