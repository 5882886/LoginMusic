package com.rd806.loginmusic.config;

import com.rd806.loginmusic.LoginMusic;
import com.rd806.loginmusic.media.lyric.LyricLayer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// 通用配置文件
@Mod.EventBusSubscriber(modid = LoginMusic.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {

    private static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    public enum Position {
        UP,
        MIDDLE,
        DOWN,
    }

    // 允许音乐播放的范围
    private static final ForgeConfigSpec.ConfigValue<Integer> MUSIC_PLAY_RANGE;
    // 是否允许从Url下载音乐
    private static final ForgeConfigSpec.BooleanValue ALLOW_DOWNLOAD;
    // 是否允许展示歌词
    private static final ForgeConfigSpec.BooleanValue ALLOW_LYRICS;
    // 歌词文本位置
    private static final ForgeConfigSpec.EnumValue<Position> LYRIC_POS;
    // 歌词颜色
    private static final ForgeConfigSpec.ConfigValue<String> LYRICS_COLOR;
    // 是否播放来自其他玩家的音频
    private static final ForgeConfigSpec.BooleanValue ALLOW_OTHERS_MUSIC;

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

    public static ForgeConfigSpec getSpec() { return SPEC; }

    public static Integer getRange() { return range; }
    public static boolean getAllowDownload() { return allowDownload; }
    public static boolean getAllowLyrics() { return allowLyrics; }
    public static boolean getAllowOthersMusic() { return allowOthersMusic; }
}
