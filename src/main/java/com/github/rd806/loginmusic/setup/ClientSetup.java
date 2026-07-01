package com.github.rd806.loginmusic.setup;

import com.github.rd806.loginmusic.LoginMusic;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.io.IOException;
import java.nio.file.Files;

import static com.github.rd806.loginmusic.LoginMusic.CACHE_DIR;
import static com.github.rd806.loginmusic.LoginMusic.LYRICS_DIR;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = LoginMusic.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = LoginMusic.MODID, value = Dist.CLIENT)
public class ClientSetup {
    public ClientSetup(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        // 创建缓存目录
        try {
            Files.createDirectories(CACHE_DIR);
            Files.createDirectories(LYRICS_DIR);
        } catch (IOException e) {
            LoginMusic.LOGGER.warn("Failed to create cache directory!", e);
        }
        LoginMusic.LOGGER.info("Start LoginMusic on client!");
    }
}
