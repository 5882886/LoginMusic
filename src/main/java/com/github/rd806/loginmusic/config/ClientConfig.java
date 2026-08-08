package com.github.rd806.loginmusic.config;

import com.github.rd806.loginmusic.LoginMusic;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;

// 通用配置文件
@Mod.EventBusSubscriber(modid = LoginMusic.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {

    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public enum Position {
        UP,
        MIDDLE,
        DOWN,
    }

    // 允许音乐播放的范围
    public static ForgeConfigSpec.ConfigValue<Integer> MUSIC_PLAY_RANGE;
    // 是否允许从Url下载音乐
    public static ForgeConfigSpec.BooleanValue ALLOW_DOWNLOAD;
    // 是否允许展示歌词
    public static ForgeConfigSpec.BooleanValue ALLOW_LYRICS;
    // 歌词文本位置
    public static ForgeConfigSpec.EnumValue<Position> LYRIC_POS;
    // 歌词颜色
    public static ForgeConfigSpec.ConfigValue<Integer> LYRIC_COLOR;
    // 是否播放来自其他玩家的音频
    public static ForgeConfigSpec.BooleanValue ALLOW_OTHERS_MUSIC;
    // 缓存容量
    public static ForgeConfigSpec.ConfigValue<Integer> CACHE_SIZE;

    public static ForgeConfigSpec init() {
        BUILDER.push("Music").translation(LoginMusic.MODID + ".configui.title");
        MUSIC_PLAY_RANGE = BUILDER
                .comment("Range of music play (a non negative integer)")
                .translation(LoginMusic.MODID + ".configui.music_play_range")
                .defineInRange("range", 3, -1, 100);
        ALLOW_DOWNLOAD = BUILDER
                .comment("Whether to allow downloading music from the internet")
                .translation(LoginMusic.MODID + ".configui.allow_download")
                .define("InternetAccess", false);
        ALLOW_OTHERS_MUSIC = BUILDER
                .comment("Whether to play musics from other players")
                .translation(LoginMusic.MODID + ".configui.allow_others_music")
                .define("AllowOthersMusic", false);
        CACHE_SIZE = BUILDER
                .comment("The maximum number of cache entries")
                .defineInRange("CacheSize", 5, 0, 10);
        BUILDER.pop();

        BUILDER.push("Lyric").translation(LoginMusic.MODID + ".configui.lyrics");
        ALLOW_LYRICS = BUILDER
                .comment("Whether to show lyrics while playing music")
                .translation(LoginMusic.MODID + ".configui.allow_lyrics")
                .define("ShowLyrics", true);
        LYRIC_POS = BUILDER
                .comment("Defines the position of the lyrics")
                .translation(LoginMusic.MODID + ".configui.lyrics_pos")
                .defineEnum("LyricsPosition", Position.DOWN);
        LYRIC_COLOR = BUILDER
                .comment("Defines the color of the lyrics you want to use")
                .translation(LoginMusic.MODID + ".configui.lyrics_color")
                .define("LyricsColor", 0xFFFFFF);
        BUILDER.pop();
        return BUILDER.build();
    }
}
