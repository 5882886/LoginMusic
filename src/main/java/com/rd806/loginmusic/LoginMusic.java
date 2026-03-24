package com.rd806.loginmusic;

import com.mojang.logging.LogUtils;
import com.rd806.loginmusic.config.ClientConfig;
import com.rd806.loginmusic.config.ServerConfig;
import com.rd806.loginmusic.media.lyric.LyricLayer;
import com.rd806.loginmusic.network.NetworkConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(LoginMusic.MODID)
public class LoginMusic {

    // MODID 标识每个mod的唯一性
    public static final String MODID = "login_music";
    // 日志文件
    public static final Logger LOGGER = LogUtils.getLogger();
    // 音乐缓存目录
    public static final Path CACHE_DIR = Paths.get("LoginMusic");
    // 歌词缓存目录
    public static final Path LYRICS_DIR = Paths.get("LoginMusic/Lyrics");

    public LoginMusic(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // 创建缓存目录
        try {
            Files.createDirectories(CACHE_DIR);
            Files.createDirectories(LYRICS_DIR);
        } catch (IOException e) {
            LOGGER.error("Failed to create cache directory!", e);
        }

        // 注册网络
        NetworkConfig.register();

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new Command());

        // 生成配置文件
        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.getSpec());
        context.registerConfig(ModConfig.Type.SERVER, ServerConfig.getSpec());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("[LoginMusic] If you have any issues with LoginMusic, please report it at https://github.com/rd806/LoginMusic!");
    }


    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        LOGGER.info("Start LoginMusic on server!");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LyricLayer.getInstance();
            LOGGER.info("Start LoginMusic on client!");
        }
    }
}
