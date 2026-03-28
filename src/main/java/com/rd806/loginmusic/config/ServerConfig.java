package com.rd806.loginmusic.config;

import com.rd806.loginmusic.LoginMusic;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = LoginMusic.MODID)
public class ServerConfig {
    private static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 音乐选择的关键字
    private static final ModConfigSpec.ConfigValue<String> MUSIC_ID_TYPE;

    private static String type = "name";

    static {
        BUILDER.push("Selection");
        MUSIC_ID_TYPE = BUILDER
                .comment("Keywords for music selection (name/uuid)")
                .translation(LoginMusic.MODID + ".configui.music_id_type")
                .define("type", "name");
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    // NeoForge更改了加载方式
    // 服务端配置只能在进入世界后获取
    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            String value = MUSIC_ID_TYPE.get();
            if (!value.isEmpty()) {
                type = value;
            }
        }
        LoginMusic.LOGGER.info("Loading server config");
    }

    public static ModConfigSpec getSpec() { return SPEC; }

    public static String getType() { return type; }
}
