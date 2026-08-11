package com.github.rd806.loginmusic.setup;

import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.github.rd806.loginmusic.LoginMusic.LOGGER;

@Mod.EventBusSubscriber
public class ServerSetup {
    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Start LoginMusic on server!");
    }
}
