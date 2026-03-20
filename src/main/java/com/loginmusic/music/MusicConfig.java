package com.loginmusic.music;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.loginmusic.LoginMusic;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 实现仅在服务端配置
@Mod.EventBusSubscriber(modid = LoginMusic.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MusicConfig {

    // 创建配置文件
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG = "music.json";
    private static Path configPath;

    private static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // 音乐选择的关键字
    private static final ForgeConfigSpec.ConfigValue<String> MUSIC_ID_TYPE;

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

        SPEC = BUILDER.build();
    }

    public static ForgeConfigSpec getSpec() { return SPEC; }


    @SubscribeEvent
    // 加载配置
    public static void onLoad(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() == SPEC) {
            LoginMusic.LOGGER.info("正在加载登录音乐");
            configPath = FMLPaths.CONFIGDIR.get().resolve(LoginMusic.MODID).resolve(CONFIG);
            loadFromConfig();
        }
    }

    public static void loadFromConfig() {
        try {
            // 加载音乐选择关键字
            type = MUSIC_ID_TYPE.get();
            LoginMusic.LOGGER.info("音乐选择的关键字为：{}", type);

            Files.createDirectories(configPath.getParent());

            if (!Files.exists(configPath)) {
                createDefaultConfig();
            }

            try (Reader reader = Files.newBufferedReader(configPath)) {
                Type listType = new TypeToken<Map<String, List<MusicEntry>>>(){}.getType();

                Map<String, List<MusicEntry>> config = GSON.fromJson(reader, listType);

                if (config != null && config.containsKey("musics")) {
                    MUSIC_ENTRY_MAP.clear();
                    for (MusicEntry music : config.get("musics")) {
                        MUSIC_ENTRY_MAP.put(music.getId(), music);
                        LoginMusic.LOGGER.info("加载音乐: {} -> {}", music.getName(), music.getId());
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

    // 创建默认配置文件
    private static void createDefaultConfig() {
        try (Writer writer = Files.newBufferedWriter(configPath)) {
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
            LoginMusic.LOGGER.error("创建配置文件失败！{}", e.getMessage());
        }
    }

    // 接收服务端的音乐配置
    @OnlyIn(Dist.CLIENT)
    public static void receiveConfig(Map<String, MusicEntry> config) {
        MUSIC_ENTRY_MAP.clear();
        MUSIC_ENTRY_MAP.putAll(config);
        configLoaded = true;
        LoginMusic.LOGGER.info("客户端音乐配置更新完成，共 {} 首音乐", MUSIC_ENTRY_MAP.size());
    }

    // 添加获取全部配置的方法（用于服务端发送）
    public static Map<String, MusicEntry> getMusicConfig() {
        return new HashMap<>(MUSIC_ENTRY_MAP);
    }


    public static String getType() { return type; }

    public static boolean isConfigLoaded() { return configLoaded; }
}

