package com.github.rd806.loginmusic.media.music;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.github.rd806.loginmusic.LoginMusic;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLPaths;

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
    // 使用JSON配置文件
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG = LoginMusic.MODID + "-music.json";
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(CONFIG);

    private static final Map<String, MusicEntry> MUSIC_ENTRY_MAP = new ConcurrentHashMap<>();

    private static boolean configLoaded = false;

    // 从配置文件加载
    public static void loadFromConfig() {
        try {
            // 创建配置文件夹
            Files.createDirectories(CONFIG_PATH.getParent());
            if (!Files.exists(CONFIG_PATH)) {
                createDefaultConfig();
            }
            // 读取JSON文件
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                Type listType = new TypeToken<Map<String, List<MusicEntry>>>(){}.getType();
                Map<String, List<MusicEntry>> config = GSON.fromJson(reader, listType);

                if (config != null && config.containsKey("musics")) {
                    MUSIC_ENTRY_MAP.clear();
                    for (MusicEntry music : config.get("musics")) {
                        MUSIC_ENTRY_MAP.put(music.getId(), music);
                        LoginMusic.LOGGER.info("Loading music: {} -> {}", music.getMusic(), music.getId());
                    }
                }
            }
            configLoaded = true;
            LoginMusic.LOGGER.info("Loading completed，total {} musics", MUSIC_ENTRY_MAP.size());
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Loading musics failed!", e);
        }
    }

    private static void createDefaultConfig() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            String defaultConfig = """
                    {
                      "musics": [
                        {
                          "id": "Default",
                          "music": "Default.mp3",
                          "musicUrl": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                          "lyric": "example.lrc",
                          "lyricUrl": "Default.lrc"
                         }
                      ]
                    }
                    """;
            writer.write(defaultConfig);
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Fail to create default config！{}", e.getMessage());
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
    // 获取配置文件
    public static Path getConfigPath() { return CONFIG_PATH; }
}