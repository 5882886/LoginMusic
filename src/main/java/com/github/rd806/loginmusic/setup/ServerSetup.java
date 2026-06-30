package com.github.rd806.loginmusic.setup;

import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.github.rd806.loginmusic.LoginMusic.LOGGER;

public class ServerSetup {
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Start LoginMusic on server!");
    }
}
