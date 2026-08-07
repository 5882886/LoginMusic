package com.github.rd806.loginmusic.media.music;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.github.rd806.loginmusic.LoginMusic;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 服务端覆盖客户端
public class MusicConfig {
    // 创建配置文件
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG = LoginMusic.MODID + "-music.json";
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(CONFIG);
    // 播放配置Map（跟随服务端）
    private static final Map<String, MusicEntry> MUSIC_ENTRY_MAP = new ConcurrentHashMap<>();
    // 本地文件列表（跟随客户端）
    private static List<MusicEntry> MUSIC_ENTRY_LIST = new ArrayList<>();

    // 从配置文件加载音乐
    public static void loadFromConfig() {
        try {
            // 创建配置文件
            Files.createDirectories(CONFIG_PATH.getParent());
            if (!Files.exists(CONFIG_PATH)) {
                createDefaultConfig();
            }
            // 读取json文件
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                var json =  GSON.fromJson(reader, JsonWrapper.class);
                if (json != null && json.musics != null) {
                    MUSIC_ENTRY_LIST = json.musics;
                    MUSIC_ENTRY_MAP.clear();
                    for (MusicEntry music : json.musics) {
                        MUSIC_ENTRY_MAP.put(music.getId(), music);
                        LoginMusic.LOGGER.info("Loading music: {} -> {}", music.getId(), music.getMusicName());
                    }
                }
            } catch (IOException e) {
                LoginMusic.LOGGER.error("Failed to load music from json!", e);
            }
            LoginMusic.LOGGER.info("Loading completed，total {} musics", MUSIC_ENTRY_MAP.size());
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Loading musics failed!", e);
        }
    }

    // 根据id获取音乐
    public static MusicEntry getMusic(String id) {
        return MUSIC_ENTRY_MAP.get(id);
    }

    // 创建默认配置文件
    private static void createDefaultConfig() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            String defaultConfig = """
                    {
                      "musics": [
                        {
                          "id": "Default",
                          "musicName": "Default.mp3",
                          "musicPath": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                          "lyricName": "example.lrc",
                          "lyricPath": "Default.lrc"
                        }
                      ]
                    }
                    """;
            writer.write(defaultConfig);
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Fail to create default config！{}", e.getMessage());
        }
    }

    // 获取当前配置
    public static Map<String, MusicEntry> getMusicEntryMap() { return MUSIC_ENTRY_MAP; }
    // 获取当前列表
    public static List<MusicEntry> getMusicList() { return MUSIC_ENTRY_LIST; }
    public static void setMusicList(List<MusicEntry> musicList) { MUSIC_ENTRY_LIST = musicList; }

    // 保存到 JSON 文件（保持与现有格式一致）
    public static void saveToFile() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                JsonWrapper wrapper = new JsonWrapper();
                wrapper.musics = MUSIC_ENTRY_LIST;
                GSON.toJson(wrapper, writer);
            }
            // 保存后通知现有的 MusicConfig 重新加载
            loadFromConfig();
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Failed to save config from GUI", e);
        }
    }

    // 辅助包装类，匹配 JSON 格式
    private static class JsonWrapper {
        List<MusicEntry> musics;
    }
}

