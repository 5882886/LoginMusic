package com.loginmusic.music;

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

    // 音乐选择的关键字
    private static final ForgeConfigSpec.ConfigValue<String> MUSIC_ID_TYPE;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> MUSIC_ENTRIES;

    private static final Map<String, MusicEntry> MUSIC_ENTRY_MAP = new ConcurrentHashMap<>();
    private static String type;

    private static boolean configLoaded = false;

    static {
        BUILDER.push("Selection");
        MUSIC_ID_TYPE = BUILDER
                .comment("Keywords for music selection (name/uuid)")
                .translation(LoginMusic.MODID + ".configui.music_id_type")
                .define("type", "name");
        BUILDER.pop();

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

    public static ForgeConfigSpec getSpec() { return SPEC; }


    @SubscribeEvent
    // 加载配置
    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            LoginMusic.LOGGER.info("正在加载登录音乐");
            loadFromConfig();
        }
    }
    // 重载配置
    @SubscribeEvent
    public static void onReload(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            LoginMusic.LOGGER.info("检测到配置文件变更，执行热重载");
            loadFromConfig();
        }
    }


    public static void loadFromConfig() {
        try {
            // 加载音乐播放范围
            type = MUSIC_ID_TYPE.get();
            LoginMusic.LOGGER.info("音乐选择的关键字为：{}", type);
            // 加载音乐配置
            MUSIC_ENTRY_MAP.clear();
            List<? extends String> entries = MUSIC_ENTRIES.get();
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

    public static String getType() { return type; }

    public static boolean isConfigLoaded() { return configLoaded; }
}

