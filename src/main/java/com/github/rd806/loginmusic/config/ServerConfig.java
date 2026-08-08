package com.github.rd806.loginmusic.config;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.SelectionKey;
import net.neoforged.neoforge.common.ModConfigSpec;


public class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 音乐选择的关键字
    public static ModConfigSpec.EnumValue<SelectionKey> MUSIC_ID_TYPE;

    public static ModConfigSpec init() {
        BUILDER.push("Selection");
        MUSIC_ID_TYPE = BUILDER
                .comment("Keywords for music selection (name/uuid)")
                .translation(LoginMusic.MODID + ".configui.music_id_type")
                .defineEnum("type", SelectionKey.NAME);
        BUILDER.pop();

        return BUILDER.build();
    }
}
