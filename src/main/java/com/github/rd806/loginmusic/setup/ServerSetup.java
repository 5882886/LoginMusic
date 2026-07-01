package com.github.rd806.loginmusic.setup;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.media.music.MusicConfig;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

public class ServerSetup {
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        MusicConfig.loadFromConfig();
        LoginMusic.LOGGER.info("Start LoginMusic on server!");
    }
}

