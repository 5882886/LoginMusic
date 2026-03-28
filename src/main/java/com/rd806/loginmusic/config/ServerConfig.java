package com.rd806.loginmusic.config;

import com.rd806.loginmusic.LoginMusic;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = LoginMusic.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ServerConfig {

    private static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // 音乐选择的关键字
    private static final ForgeConfigSpec.ConfigValue<String> MUSIC_ID_TYPE;

    private static String type;

    static {
        BUILDER.push("Selection");
        MUSIC_ID_TYPE = BUILDER
                .comment("Keywords for music selection (name/uuid)")
                .translation(LoginMusic.MODID + ".configui.music_id_type")
                .define("type", "name");
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            type = MUSIC_ID_TYPE.get();
            LoginMusic.LOGGER.info("Select music by: {}", type);
        }
    }

    public static ForgeConfigSpec getSpec() { return SPEC; }

    public static String getType() { return type; }
}
