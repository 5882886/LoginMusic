package com.loginmusic;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = LoginMusic.MODID)
public class Config {

    private static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 允许音乐播放的范围
    private static final ModConfigSpec.ConfigValue<Integer> MUSIC_PLAY_RANGE;
    // 是否允许从Url下载音乐
    private static final ModConfigSpec.BooleanValue ALLOW_DOWNLOAD;

    private static Integer range;
    private static boolean allowDownload;

    static {
        BUILDER.push("Basic");
        MUSIC_PLAY_RANGE = BUILDER
                .comment("Range of music play (a non negative integer)")
                .translation(LoginMusic.MODID + ".configui.music_play_range")
                .defineInRange("range", 3, 0, 100);
        ALLOW_DOWNLOAD = BUILDER
                .comment("Whether to allow downloading music from the internet")
                .translation(LoginMusic.MODID + ".configui.allow_download")
                .define("InternetAccess", false);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        range = MUSIC_PLAY_RANGE.get();
        allowDownload = ALLOW_DOWNLOAD.get();
        LoginMusic.LOGGER.info("允许播放半径：{} 格", range);
    }

    public static ModConfigSpec getSpec() { return SPEC; }

    public static Integer getRange() { return range; }
    public static boolean getAllowDownload() { return allowDownload; }
}
