package com.rd806.loginmusic.config.gui;

import com.rd806.loginmusic.LoginMusic;
import com.rd806.loginmusic.config.ClientConfig;
import com.rd806.loginmusic.media.music.MusicEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClothConfigGUI {
    private static VisualWrapper visualWrapper;
    private static Screen parentScreen;

    public static Screen createConfigScreen(Screen parent) {
        parentScreen = parent;
        visualWrapper = new VisualWrapper();
        return buildScreen();
    }


    public static Screen buildScreen() {
        visualWrapper = new VisualWrapper();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parentScreen)
                .setTitle(Component.translatable(LoginMusic.MODID + ".gui.config.title"));
        builder.setGlobalized(true);

        // 音乐条目
        ConfigCategory musicEntries = builder.getOrCreateCategory(Component.translatable(LoginMusic.MODID + ".gui.config.entries"));
        // 音乐播放设置
        ConfigCategory musicSettings = builder.getOrCreateCategory(Component.translatable(LoginMusic.MODID + ".gui.config.settings"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        buildMusicEntries(entryBuilder, musicEntries);
        buildMusicSettings(entryBuilder, musicSettings);

        // 保存回调
        builder.setSavingRunnable(() -> {
            // 保存并触发 MusicConfig 重载
            visualWrapper.saveToFile();
        });

        return builder.build();
    }

    // 音乐条目配置
    private static void buildMusicEntries(ConfigEntryBuilder entryBuilder, ConfigCategory musicEntries) {
        // 使用临时列表来存储 GUI 中的顺序
        List<MusicEntry> currentList = visualWrapper.getMusicList();

        // 为每个音乐条目创建编辑界面
        for (int index = 0; index < currentList.size(); index++) {
            MusicEntry entry = currentList.get(index);

            SubCategoryListEntry subEntry = createMusicSubCategory(entryBuilder, entry, index);
            musicEntries.addEntry(subEntry);
        }

        // 添加新音乐的按钮
        musicEntries.addEntry(
                entryBuilder.startBooleanToggle(Component.translatable(LoginMusic.MODID + ".gui.config.addEnable"), false)
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".gui.config.addEnable.tooltip"))
                        .setSaveConsumer(shouldAdd -> {
                            if (shouldAdd) {
                                MusicEntry newEntry = new MusicEntry();
                                newEntry.setId("NewMusic" + (visualWrapper.getMusicList().size() + 1));
                                newEntry.setMusic("");
                                newEntry.setMusicUrl("");
                                newEntry.setLyric("");
                                newEntry.setLyricUrl("");
                                currentList.add(newEntry);
                                visualWrapper.setMusicList(currentList);
                            }
                        })
                        .build());
    }

    // 音乐播放配置
    private static void buildMusicSettings(ConfigEntryBuilder entryBuilder, ConfigCategory musicSettings) {
        // 音乐播放范围
        musicSettings.addEntry(
                entryBuilder.startIntField(Component.translatable(LoginMusic.MODID + ".configui.music_play_range"), ClientConfig.getRange())
                        .setDefaultValue(3)
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".configui.music_play_range.tooltip"))
                        .setSaveConsumer(ClientConfig::setRange)
                        .build());

        // 是否允许下载
        musicSettings.addEntry(
                entryBuilder.startBooleanToggle(Component.translatable(LoginMusic.MODID + ".configui.allow_download"), ClientConfig.getAllowDownload())
                        .setDefaultValue(false)
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".configui.allow_download.tooltip"))
                        .setSaveConsumer(ClientConfig::setAllowDownload)
                        .build());

        // 是否允许播放其他玩家的音乐
        musicSettings.addEntry(
                entryBuilder.startBooleanToggle(Component.translatable(LoginMusic.MODID + ".configui.allow_others_music"), ClientConfig.getAllowOthersMusic())
                        .setDefaultValue(false)
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".configui.allow_others_music.tooltip"))
                        .setSaveConsumer(ClientConfig::setAllowOthersMusic)
                        .build());

        // 是否展示歌词
        musicSettings.addEntry(
                entryBuilder.startBooleanToggle(Component.translatable(LoginMusic.MODID + ".configui.allow_lyrics"), ClientConfig.getAllowLyrics())
                        .setDefaultValue(true)
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".configui.allow_lyrics.tooltip"))
                        .setSaveConsumer(ClientConfig::setAllowLyrics)
                        .build());

        musicSettings.addEntry(
                entryBuilder.startEnumSelector(Component.translatable(LoginMusic.MODID + ".configui.lyrics_pos"), ClientConfig.Position.class, ClientConfig.getPosition())
                        .setDefaultValue(ClientConfig.Position.DOWN)
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".configui.lyrics_pos.tooltip"))
                        .setSaveConsumer(ClientConfig::setPosition)
                        .build());
    }

    private static SubCategoryListEntry createMusicSubCategory(ConfigEntryBuilder entryBuilder, MusicEntry entry, int index) {
        // 创建子分类构建器
        var subCategoryBuilder = entryBuilder.startSubCategory(Component.literal(" " + entry.getId()));

        // 添加 ID 字段
        subCategoryBuilder.add(
                entryBuilder.startStrField(Component.literal("ID"), entry.getId())
                        .setDefaultValue("" + index)
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".gui.config.id.tooltip"))
                        .setSaveConsumer(entry::setId)
                        .build()
        );

        // 添加音乐文件字段
        subCategoryBuilder.add(
                entryBuilder.startStrField(Component.translatable(LoginMusic.MODID + ".gui.config.music"), entry.getMusic())
                        .setDefaultValue("")
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".gui.config.music.tooltip"))
                        .setSaveConsumer(entry::setMusic)
                        .build()
        );

        // 添加音乐 URL 字段
        subCategoryBuilder.add(
                entryBuilder.startStrField(Component.translatable(LoginMusic.MODID + ".gui.config.musicUrl"), entry.getMusicUrl())
                        .setDefaultValue("")
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".gui.config.musicUrl.tooltip"))
                        .setSaveConsumer(entry::setMusicUrl)
                        .build()
        );

        // 本地歌词文件字段
        subCategoryBuilder.add(
                entryBuilder.startStrField(Component.translatable(LoginMusic.MODID + ".gui.config.lyric"), entry.getLyric())
                        .setDefaultValue("")
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".gui.config.lyric.tooltip"))
                        .setSaveConsumer(entry::setLyric)
                        .build()
        );

        // 歌词 URL 字段
        subCategoryBuilder.add(
                entryBuilder.startStrField(Component.translatable(LoginMusic.MODID + ".gui.config.lyricUrl"), entry.getLyricUrl())
                        .setDefaultValue("")
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".gui.config.lyricUrl.tooltip"))
                        .setSaveConsumer(entry::setLyricUrl)
                        .build()
        );

        // 删除按钮
        subCategoryBuilder.add(
                entryBuilder.startBooleanToggle(Component.translatable(LoginMusic.MODID + ".gui.config.delete"), false)
                        .setSaveConsumer(shouldDelete -> {
                            if (shouldDelete) {
                                visualWrapper.getMusicList().remove(index);
                            }
                        })
                        .build()
        );

        return subCategoryBuilder.build();
    }
}
