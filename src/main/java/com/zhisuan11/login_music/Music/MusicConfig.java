package com.zhisuan11.login_music.Music;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zhisuan11.login_music.LoginMusic;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MusicConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "music.json";
    private static Path configPath;

    // 音乐缓存
    private static final Map<String, MusicEntry> MUSIC_ENTRY_MAP = new ConcurrentHashMap<>();

    // 创建配置文件
    public static void InitialConfig(String modId) {
        configPath = FMLPaths.CONFIGDIR.get().resolve(modId).resolve(CONFIG_FILE);
        LoadConfig();
    }

    public static void LoadConfig() {
        try {
            // 配置目录存在
            Files.createDirectories(configPath.getParent());

            // 配置文件不存在则创建
            if (!Files.exists(configPath)) {
                CreateDefaultConfig();
            }

            // 读取配置文件
            try (Reader reader = Files.newBufferedReader(configPath)) {
                Type type = new TypeToken<Map<String, List<MusicEntry>>>(){}.getType();

                Map<String, List<MusicEntry>> config = GSON.fromJson(reader, type);

                if (config != null && config.containsKey("musics")) {
                    MUSIC_ENTRY_MAP.clear();
                    for (MusicEntry music : config.get("musics")) {
                        MUSIC_ENTRY_MAP.put(music.getId(), music);
                        LoginMusic.LOGGER.info("正在加载音乐：{}->{}", music.getName(), music.getUrl());
                    }
                }
            }

            LoginMusic.LOGGER.info("成功加载：{} 首音乐", MUSIC_ENTRY_MAP.size());
        } catch (Exception e) {
            LoginMusic.LOGGER.warn("配置文件加载失败: {}", e.getMessage());
        }
    }

    private static void CreateDefaultConfig() {
        try (Writer writer = Files.newBufferedWriter(configPath)){
            String defaultConfig = """
                {
                    "musics": [
                        {
                            "id": "login_music",
                            "url": "https://example.com/login.ogg",
                            "name": "Default",
                            "duration": 60,
                            "volume": 1.0,
                            "stream": true
                        }
                    ]
                }
                """;
            writer.write(defaultConfig);
        } catch (IOException e) {
            LoginMusic.LOGGER.warn("创建默认配置文件失败: {}", e.getMessage());
        }
    }

    // 根据id获取音乐
    public static MusicEntry getMusic(String id) {
        return MUSIC_ENTRY_MAP.get(id);
    }

    public static Map<String, MusicEntry> getMusicEntryMap() {
        return new HashMap<>(MUSIC_ENTRY_MAP);
    }
}

