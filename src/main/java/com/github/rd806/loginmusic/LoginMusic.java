package com.github.rd806.loginmusic;

import com.mojang.logging.LogUtils;
import com.github.rd806.loginmusic.config.ClientConfig;
import com.github.rd806.loginmusic.config.ServerConfig;
import com.github.rd806.loginmusic.media.music.MusicConfig;
import com.github.rd806.loginmusic.network.NetworkConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;

// The value here should match an entry in the META-INF/mods.toml file
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
    // 模组加载上下文
    public static FMLJavaModLoadingContext fmlContext;

    public LoginMusic(FMLJavaModLoadingContext context) {
        fmlContext = context;
        IEventBus modEventBus = context.getModEventBus();
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // 注册网络
        NetworkConfig.register();
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new Command());
        // 生成配置文件
        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.init());
        context.registerConfig(ModConfig.Type.SERVER, ServerConfig.init());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        MusicConfig.loadFromConfig();
        LOGGER.info("[LoginMusic] If you have any issues with LoginMusic, please report it at https://github.com/rd806/LoginMusic!");
    }
}
