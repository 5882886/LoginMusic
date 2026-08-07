package com.github.rd806.loginmusic.media.music;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.MusicLRUCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.io.ByteArrayOutputStream;
import java.util.Set;

public class MusicCache {
    // 缓存
    public static final MusicLRUCache<String, ByteArrayOutputStream> MUSIC_CACHE = new MusicLRUCache<>();
    public static final MusicLRUCache<String, String> LYRIC_CACHE = new MusicLRUCache<>();

    // 放入
    public static void putMusic(String key, ByteArrayOutputStream value) {
        MUSIC_CACHE.put(key, value);
    }
    public static void putLyric(String key, String value) {
        LYRIC_CACHE.put(key, value);
    }

    // 取出
    public static ByteArrayOutputStream getMusic(String key) {
        ByteArrayOutputStream baos = MUSIC_CACHE.get(key);
        if (baos != null) return baos;
        return new ByteArrayOutputStream();
    }
    public static String getLyric(String key) {
        return LYRIC_CACHE.get(key);
    }

    // 查看缓存条目
    public static void showCache() {
        Set<String> musicKeys = MUSIC_CACHE.keySet();
        Set<String> lyricKeys = LYRIC_CACHE.keySet();
        Player player = Minecraft.getInstance().player;
        if (player == null) { return; }
        // 显示缓存的音乐
        if (musicKeys.isEmpty()) {
            player.displayClientMessage(Component.translatable(LoginMusic.MODID + ".command.cache.empty"), false);
        } else {
            player.displayClientMessage(Component.translatable(LoginMusic.MODID + ".command.cache.info"), false);
            for (String key : musicKeys) {
                player.displayClientMessage(Component.literal("- " + key), false);
            }
        }
        // 显示缓存的歌词
        if (lyricKeys.isEmpty()) {
            player.displayClientMessage(Component.translatable(LoginMusic.MODID + ".command.cache.empty"), false);
        } else {
            player.displayClientMessage(Component.translatable(LoginMusic.MODID + ".command.cache.info"), false);
            for (String key : lyricKeys) {
                player.displayClientMessage(Component.literal("- " + key), false);
            }
        }
    }

    // 清除缓存
    public static void clearCache() {
        MUSIC_CACHE.clear();
        LYRIC_CACHE.clear();
    }
}
