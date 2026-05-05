package com.rd806.loginmusic;

import com.rd806.loginmusic.config.ClientConfig;
import com.rd806.loginmusic.config.ServerConfig;
import com.rd806.loginmusic.media.music.MusicConfig;
import com.rd806.loginmusic.network.NetworkConfig;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.nio.file.Path;
import java.nio.file.Paths;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(LoginMusic.MODID)
public class LoginMusic {

    // MODID
    public static final String MODID = "login_music";
    // 日志文件
    public static final Logger LOGGER = LogUtils.getLogger();
    // 音乐缓存目录
    public static final Path CACHE_DIR = Paths.get("LoginMusic/MusicCache");
    // 歌词缓存目录
    public static final Path LYRICS_DIR = Paths.get("LoginMusic/Lyrics");


    public LoginMusic(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        // 注册网络
        modEventBus.addListener(NetworkConfig::register);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (LoginMusic) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(new Command());

        // 生成配置文件
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.getSpec());
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.getSpec());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("[LoginMusic] If you have any issues with LoginMusic, please report it at https://github.com/rd806/LoginMusic!");
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        MusicConfig.loadFromConfig();
        LOGGER.info("Start LoginMusic on server!");
    }
}
