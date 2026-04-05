package com.rd806.loginmusic.media.music;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.rd806.loginmusic.LoginMusic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 实现仅在服务端配置
public class MusicConfig {
    // 创建配置文件
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG = "music.json";
    private static final Path configPath = FMLPaths.CONFIGDIR.get().resolve(LoginMusic.MODID).resolve(CONFIG);

    private static final Map<String, MusicEntry> MUSIC_ENTRY_MAP = new ConcurrentHashMap<>();
    private static boolean configLoaded = false;

    // 从配置文件加载音乐
    public static void loadFromConfig() {
        try {
            // 创建配置文件夹
            Files.createDirectories(configPath.getParent());
            if (!Files.exists(configPath)) {
                createDefaultConfig();
            }

            // 读取json文件
            try (Reader reader = Files.newBufferedReader(configPath)) {
                Type listType = new TypeToken<Map<String, List<MusicEntry>>>(){}.getType();

                Map<String, List<MusicEntry>> config = GSON.fromJson(reader, listType);

                if (config != null && config.containsKey("musics")) {
                    MUSIC_ENTRY_MAP.clear();
                    for (MusicEntry music : config.get("musics")) {
                        MUSIC_ENTRY_MAP.put(music.getId(), music);
                        LoginMusic.LOGGER.info("Loading music: {} -> {}", music.getName(), music.getId());
                    }
                }
            }

            configLoaded = true;
            LoginMusic.LOGGER.info("Loading completed，total {} musics", MUSIC_ENTRY_MAP.size());
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Loading musics failed!", e);
        }
    }

    // 根据id获取音乐
    public static MusicEntry getMusic(String id) {
        if (!configLoaded) {
            // 如果配置还没加载，尝试直接读取
            loadFromConfig();
        }
        return MUSIC_ENTRY_MAP.get(id);
    }

    // 创建默认配置文件
    private static void createDefaultConfig() {
        try (Writer writer = Files.newBufferedWriter(configPath)) {
            String defaultConfig = """
                    {
                        "musics": [
                            {
                                "id": "Default",
                                "name": "Default.mp3",
                                "url": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                                "lyrics": "example.lrc",
                                "lyricsUrl": "Default"
                            }
                        ]
                    }
                    """;
            writer.write(defaultConfig);
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Fail to create default config！{}", e.getMessage());
        }
    }

    // 接收服务端的音乐配置
    @OnlyIn(Dist.CLIENT)
    public static void receiveConfig(Map<String, MusicEntry> config) {
        MUSIC_ENTRY_MAP.clear();
        MUSIC_ENTRY_MAP.putAll(config);
        configLoaded = true;
        LoginMusic.LOGGER.info("Client music config updated, total {} musics", MUSIC_ENTRY_MAP.size());
    }

    // 添加获取全部配置的方法（用于服务端发送）
    public static Map<String, MusicEntry> getMusicConfig() { return new HashMap<>(MUSIC_ENTRY_MAP);}
    // 获取当前配置
    public static Map<String, MusicEntry> getMusicEntryMap() { return MUSIC_ENTRY_MAP; }

    public static boolean isConfigLoaded() { return configLoaded; }
}

