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
    private static String type;

    static {
        BUILDER.comment("LoginMusic配置文件");

        BUILDER.push("Selection");

        MUSIC_ID_TYPE = BUILDER
                .comment("音乐选择的关键字", "可选：name/uuid")
                .define("type", "name");

        SPEC = BUILDER.build();
    }


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        type = MUSIC_ID_TYPE.get();
        LoginMusic.LOGGER.info("音乐选择的关键字为：{}", type);
    }

    public static ForgeConfigSpec getSpec() {
        return SPEC;
    }

    public static String getType() {
        return type;
    }
}
