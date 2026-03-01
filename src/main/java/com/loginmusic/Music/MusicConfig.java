package com.loginmusic.Music;

import com.loginmusic.LoginMusic;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 实现仅在服务端配置
// 文件位于/serverconfig中
@Mod.EventBusSubscriber(modid = LoginMusic.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MusicConfig {
    private static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> MUSIC_ENTRIES;
    private static final Map<String, MusicEntry> MUSIC_ENTRY_MAP = new ConcurrentHashMap<>();

    private static boolean configLoaded = false;

    static {
        BUILDER.comment("Configure server musics (automatically synchronized)")
                .translation(LoginMusic.MODID + ".configui.music_config");

        // push 创建一个配置节
        BUILDER.push("music");
        MUSIC_ENTRIES = BUILDER
                .comment("Each entry is a string: \"Target player | Music name | Music URL \"")
                .translation(LoginMusic.MODID + ".configui.music_entries")
                .defineList("entries",
                        new ArrayList<>(List.of("Default|Default.mp3|https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3")),
                        entry -> entry instanceof String
                );
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static ForgeConfigSpec getSpec() {
        return SPEC;
    }


    @SubscribeEvent
    // 加载配置
    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            LoginMusic.LOGGER.info("正在加载登录音乐");
            loadFromConfig();
        }
    }

    private static void loadFromConfig() {
        try {
            List<? extends String> entries = MUSIC_ENTRIES.get();
            MUSIC_ENTRY_MAP.clear();
            if (entries != null) {
                for (String entryStr : entries) {
                    try {
                        String[] parts = entryStr.split("\\|");
                        String id = parts[0].trim();
                        String name = parts[1].trim();
                        String url = parts[2].trim();

                        MusicEntry musicEntry = new MusicEntry();
                        musicEntry.setId(id);
                        musicEntry.setName(name);
                        musicEntry.setUrl(url);

                        MUSIC_ENTRY_MAP.put(id, musicEntry);
                        LoginMusic.LOGGER.info("加载音乐: {} -> {}", id, name);
                    } catch (Exception e) {
                        LoginMusic.LOGGER.warn("解析音乐条目失败");
                    }
                }
            }

            configLoaded = true;
            LoginMusic.LOGGER.info("音乐配置加载完成，共 {} 首音乐", MUSIC_ENTRY_MAP.size());

        } catch (Exception e) {
            LoginMusic.LOGGER.error("加载音乐配置失败", e);
        }
    }

    // 根据id获取音乐
    public static MusicEntry getMusic(String id) {
        if (!configLoaded) {
            // 如果配置还没加载，尝试直接读取
            loadFromConfig();
        }
        return MUSIC_ENTRY_MAP.get(id);
    }

    public static boolean isConfigLoaded() {
        return configLoaded;
    }
}

