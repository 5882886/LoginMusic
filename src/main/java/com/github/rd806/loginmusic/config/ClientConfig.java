package com.github.rd806.loginmusic.config;

import net.neoforged.neoforge.common.ModConfigSpec;

// 客户端配置文件

public class ClientConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public enum Position {
        UP,
        MIDDLE,
        DOWN,
    }

    // 是否展示加载界面
    public static ModConfigSpec.BooleanValue SHOW_LOADING;
    // 允许音乐播放的范围
    public static ModConfigSpec.ConfigValue<Integer> MUSIC_PLAY_RANGE;
    // 是否允许展示歌词
    public static ModConfigSpec.BooleanValue ALLOW_LYRICS;
    // 歌词文本位置
    public static ModConfigSpec.EnumValue<Position> LYRIC_POS;
    // 歌词颜色
    public static ModConfigSpec.ConfigValue<Integer> LYRIC_COLOR;
    // 是否播放来自其他玩家的音频
    public static ModConfigSpec.BooleanValue ALLOW_OTHERS_MUSIC;
    // 缓存容量
    public static ModConfigSpec.ConfigValue<Integer> CACHE_SIZE;

    public static ModConfigSpec init() {
        BUILDER.push("Loading").translation("loginmusic.config.title.loading");
        SHOW_LOADING = BUILDER
                .comment("Whether to show loading screen when preparing music")
                .translation("loginmusic.config.show_loading")
                .define("show_loading", true);
        BUILDER.pop();

        BUILDER.push("Music").translation("loginmusic.config.title.music");
        MUSIC_PLAY_RANGE = BUILDER
                .comment("Range of music play (a non negative integer)")
                .translation("loginmusic.config.music_play_range")
                .defineInRange("range", 3, 0, 100);
        ALLOW_OTHERS_MUSIC = BUILDER
                .comment("Whether to play musics from other players")
                .translation("loginmusic.config.allow_others_music")
                .define("AllowOthersMusic", false);
        CACHE_SIZE = BUILDER
                .comment("The maximum number of cache entries")
                .defineInRange("CacheSize", 5, 0, 10);
        BUILDER.pop();

        BUILDER.push("Lyric").translation("loginmusic.config.title.lyric");
        ALLOW_LYRICS = BUILDER
                .comment("Whether to show lyrics while playing music")
                .translation("loginmusic.config.allow_lyrics")
                .define("ShowLyrics", true);
        LYRIC_POS = BUILDER
                .comment("Defines the position of the lyrics")
                .translation("loginmusic.config.lyrics_pos")
                .defineEnum("LyricsPosition", Position.DOWN);
        LYRIC_COLOR = BUILDER
                .comment("Defines the color of the lyrics you want to use")
                .translation("loginmusic.config.lyrics_color")
                .define("LyricsColor", 0xFFFFFF);
        BUILDER.pop();
        return BUILDER.build();
    }
}