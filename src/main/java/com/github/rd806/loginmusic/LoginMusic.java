package com.github.rd806.loginmusic;

import com.github.rd806.loginmusic.config.ClientConfig;
import com.github.rd806.loginmusic.config.ServerConfig;
import com.github.rd806.loginmusic.media.music.MusicConfig;
import com.github.rd806.loginmusic.network.NetworkConfig;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.nio.file.Path;
import java.nio.file.Paths;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(LoginMusic.MODID)
public class LoginMusic {

    // MODID
    public static final String MODID = "login_music";
    // 日志文件
    public static final Logger LOGGER = LogUtils.getLogger();
    // 配置文件目录
    public static final Path DATA_PATH = Paths.get("data/login_music");
    // 音乐缓存目录
    public static final Path MUSICS_DIR = DATA_PATH.resolve("MusicsCache");
    // 歌词缓存目录
    public static final Path LYRICS_DIR = DATA_PATH.resolve("LyricsCache");


    public LoginMusic(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        // 注册网络
        modEventBus.addListener(NetworkConfig::register);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (LoginMusic) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(new Command());

        // 生成配置文件
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.init());
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.init());
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        MusicConfig.loadFromConfig();
        LOGGER.info("[LoginMusic] If you have any issues with LoginMusic, please report it at https://github.com/rd806/LoginMusic!");
    }
}
