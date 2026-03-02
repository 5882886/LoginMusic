package com.loginmusic;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

// 通用配置文件
@Mod.EventBusSubscriber(modid = LoginMusic.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    private static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // 允许音乐播放的范围
    private static final ForgeConfigSpec.ConfigValue<Integer> MUSIC_PLAY_RANGE;
    // 是否允许从Url下载音乐
    private static final ForgeConfigSpec.BooleanValue ALLOW_DOWNLOAD;

    private static Integer range;
    private static boolean allowDownload;

    static {
        BUILDER.push("Basic client config").translation(LoginMusic.MODID + ".configui.title");
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

    public static ForgeConfigSpec getSpec() { return SPEC; }

    public static Integer getRange() { return range; }
    public static boolean getAllowDownload() { return allowDownload; }
}
