package com.loginmusic.music;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loginmusic.LoginMusic;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 实现仅在服务端配置
@EventBusSubscriber(modid = LoginMusic.MODID)
public class MusicConfig {

    private static final ModConfigSpec SPEC;
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // 使用JSON配置文件
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG = "music.json";
    private static Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(LoginMusic.MODID).resolve(CONFIG);

    // 音乐选择的关键字
    private static final ModConfigSpec.ConfigValue<String> MUSIC_ID_TYPE;

    private static final Map<String, MusicEntry> MUSIC_ENTRY_MAP = new ConcurrentHashMap<>();
    private static String type;

    private static boolean configLoaded = false;

    static {
        BUILDER.push("Selection");
        MUSIC_ID_TYPE = BUILDER
                .comment("Keywords for music selection (name/uuid)")
                .define("type", "name");
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    public static ModConfigSpec getSpec() { return SPEC; }

    // 加载配置
    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            LoginMusic.LOGGER.info("Loading LoginMusic config!");
            CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(LoginMusic.MODID).resolve(CONFIG);
            loadFromConfig();
        }
    }

    // 从配置文件加载
    public static void loadFromConfig() {
        try {
            // 加载音乐播放范围
            type = MUSIC_ID_TYPE.get();
            LoginMusic.LOGGER.info("Select music by: {}", type);

            // 创建配置文件夹
            Files.createDirectories(CONFIG_PATH.getParent());
            if (!Files.exists(CONFIG_PATH)) {
                createDefaultConfig();
            }

            // 读取JSON文件
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                Type listType = new TypeToken<Map<String, List<MusicEntry>>>(){}.getType();

                Map<String, List<MusicEntry>> config = GSON.fromJson(reader, listType);

                if (config != null && config.containsKey("musics")) {
                    MUSIC_ENTRY_MAP.clear();
                    for (MusicEntry music : config.get("musics")) {
                        MUSIC_ENTRY_MAP.put(music.getId(), music);
                        LoginMusic.LOGGER.info("Loading music: {} -> {}", music.getName(), music.getId());
                    }
                }
            }

            configLoaded = true;
            LoginMusic.LOGGER.info("Loading completed，total {} musics", MUSIC_ENTRY_MAP.size());

        } catch (Exception e) {
            LoginMusic.LOGGER.error("Loading musics failed!", e);
        }
    }

    private static void createDefaultConfig() {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            String defaultConfig = """
                    {
                        "musics": [
                            {
                                "id": "Default",
                                "name": "Default.mp3",
                                "url": "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1"
                            }
                        ]
                    }
                    """;
            writer.write(defaultConfig);
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Fail to create default config！{}", e.getMessage());
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

    public static Map<String, MusicEntry> getMusicEntryMap() { return MUSIC_ENTRY_MAP; }

    public static boolean isConfigLoaded() { return configLoaded; }
}