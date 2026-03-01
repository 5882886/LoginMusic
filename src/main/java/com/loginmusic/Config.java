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

    // 音乐选择的关键字
    private static final ForgeConfigSpec.ConfigValue<String> MUSIC_ID_TYPE;
    // 允许音乐播放的范围
    private static final ForgeConfigSpec.ConfigValue<Integer> MUSIC_PLAY_RANGE;


    private static String type;
    private static Integer range;

    static {
        BUILDER.push("Selection");
        MUSIC_ID_TYPE = BUILDER
                .comment("Keywords for music selection (name/uuid)")
                .translation(LoginMusic.MODID + ".configui.music_id_type")
                .define("type", "name");
        BUILDER.pop();

        BUILDER.push("Range");
        MUSIC_PLAY_RANGE = BUILDER
                .comment("Range of music play (a non negative integer)")
                .translation(LoginMusic.MODID + ".configui.music_play_range")
                .defineInRange("range", 3, 0, 100);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        type = MUSIC_ID_TYPE.get();
        range = MUSIC_PLAY_RANGE.get();
        LoginMusic.LOGGER.info("音乐选择的关键字为：{}", type);
        LoginMusic.LOGGER.info("允许播放半径：{} 格", range);
    }

    public static ForgeConfigSpec getSpec() { return SPEC; }

    public static String getType() { return type; }

    public static Integer getRange() { return range; }
}
