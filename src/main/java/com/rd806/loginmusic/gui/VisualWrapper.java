package com.rd806.loginmusic.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rd806.loginmusic.LoginMusic;
import com.rd806.loginmusic.media.music.MusicConfig;
import com.rd806.loginmusic.media.music.MusicEntry;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class VisualWrapper {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(LoginMusic.MODID).resolve("music.json");

    private List<MusicEntry> musicList = new ArrayList<>();

    public VisualWrapper() {
        loadFromFile();
    }

    // 从 JSON 文件加载
    private void loadFromFile() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                createDefault();
                return;
            }

            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                // 解析现有格式：{"musics": [...]}
                var json = gson.fromJson(reader, JsonWrapper.class);
                if (json != null && json.musics != null) {
                    this.musicList = json.musics;
                }
            }
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Failed to load config for GUI", e);
            createDefault();
        }
    }

    // 保存到 JSON 文件（保持与现有格式一致）
    public void saveToFile() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                JsonWrapper wrapper = new JsonWrapper();
                wrapper.musics = this.musicList;
                gson.toJson(wrapper, writer);
            }
            // 关键：保存后通知现有的 MusicConfig 重新加载
            MusicConfig.loadFromConfig();
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Failed to save config from GUI", e);
        }
    }

    // 创建默认配置
    private void createDefault() {
        this.musicList = new ArrayList<>();
        MusicEntry defaultEntry = new MusicEntry();
        defaultEntry.setId("Default");
        defaultEntry.setMusic("Default.mp3");
        defaultEntry.setMusicUrl("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3");
        defaultEntry.setLyric("example.lrc");
        defaultEntry.setLyricUrl("Default");
        this.musicList.add(defaultEntry);
        saveToFile();
    }

    // GUI 使用的 getter/setter
    public List<MusicEntry> getMusicList() { return musicList; }
    public void setMusicList(List<MusicEntry> musicList) { this.musicList = musicList; }

    // 辅助包装类，匹配 JSON 格式
    private static class JsonWrapper {
        List<MusicEntry> musics;
    }
}
