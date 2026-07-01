package com.github.rd806.loginmusic.config;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.media.lyric.LyricLayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

// 客户端配置文件
@EventBusSubscriber(modid = LoginMusic.MODID)
public class ClientConfig {

    private static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public enum Position {
        UP,
        MIDDLE,
        DOWN,
    }

    // 允许音乐播放的范围
    private static final ModConfigSpec.ConfigValue<Integer> MUSIC_PLAY_RANGE;
    // 是否允许先从Url下载音乐
    private static final ModConfigSpec.BooleanValue ALLOW_DOWNLOAD;
    // 是否允许展示歌词
    private static final ModConfigSpec.BooleanValue ALLOW_LYRICS;
    // 歌词文本位置
    private static final ModConfigSpec.EnumValue<Position> LYRIC_POS;
    // 歌词颜色
    private static final ModConfigSpec.ConfigValue<String> LYRICS_COLOR;
    // 是否播放来自其他玩家的音频
    private static final ModConfigSpec.BooleanValue ALLOW_OTHERS_MUSIC;

    private static Integer range;
    private static boolean allowDownload;
    private static boolean allowLyrics;
    private static boolean allowOthersMusic;

    static {
        BUILDER.push("Music").translation(LoginMusic.MODID + ".configui.title");
        MUSIC_PLAY_RANGE = BUILDER
                .comment("Range of music play (a non negative integer)")
                .translation(LoginMusic.MODID + ".configui.music_play_range")
                .defineInRange("range", 3, 0, 100);
        ALLOW_DOWNLOAD = BUILDER
                .comment("Whether to allow downloading music from the internet")
                .translation(LoginMusic.MODID + ".configui.allow_download")
                .define("InternetAccess", false);
        ALLOW_OTHERS_MUSIC = BUILDER
                .comment("Whether to play musics from other players")
                .translation(LoginMusic.MODID + ".configui.allow_others_music")
                .define("AllowOthersMusic", false);
        BUILDER.pop();

        BUILDER.push("Lyrics").translation(LoginMusic.MODID + ".configui.lyrics");
        ALLOW_LYRICS = BUILDER
                .comment("Whether to show lyrics while playing music")
                .translation(LoginMusic.MODID + ".configui.allow_lyrics")
                .define("ShowLyrics", true);
        LYRIC_POS = BUILDER
                .comment("Defines the position of the lyrics")
                .translation(LoginMusic.MODID + ".configui.lyrics_pos")
                .defineEnum("LyricsPosition", Position.DOWN);
        LYRICS_COLOR = BUILDER
                .comment("Defines the color of the lyrics you want to use")
                .translation(LoginMusic.MODID + ".configui.lyrics_color")
                .define("LyricsColor", "#FFFFFF");
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            range = MUSIC_PLAY_RANGE.get();
            allowDownload = ALLOW_DOWNLOAD.get();
            allowLyrics = ALLOW_LYRICS.get();
            allowOthersMusic = ALLOW_OTHERS_MUSIC.get();

            Position lyricPos = LYRIC_POS.get();
            String lyricsColor = LYRICS_COLOR.get();
            LyricLayer.getInstance().setLyricLayer(lyricPos, lyricsColor);

            LoginMusic.LOGGER.info("Music playing range: {} blocks", range);
        }
    }

    public static ModConfigSpec getSpec() { return SPEC; }

    public static Integer getRange() { return range; }
    public static boolean getAllowDownload() { return allowDownload; }
    public static boolean getAllowLyrics() { return allowLyrics; }
    public static boolean getAllowOthersMusic() { return allowOthersMusic; }
}
