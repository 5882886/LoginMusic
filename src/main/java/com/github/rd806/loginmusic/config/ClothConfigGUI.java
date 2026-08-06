package com.github.rd806.loginmusic.config;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.SelectionKey;
import com.github.rd806.loginmusic.media.lyric.LyricLayer;
import com.github.rd806.loginmusic.media.music.MusicConfig;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ClothConfigGUI {

    public static ConfigBuilder buildScreen() {

        ConfigBuilder builder = ConfigBuilder.create().setTitle(Component.translatable(LoginMusic.MODID + ".gui.config.title"));
        builder.setGlobalized(true);
        builder.setGlobalizedExpanded(false);
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        // 音乐条目
        ConfigCategory musicEntries = builder.getOrCreateCategory(Component.translatable(LoginMusic.MODID + ".gui.config.entries"));
        buildMusicEntries(entryBuilder, musicEntries);
        // 音乐播放设置
        ConfigCategory clientSettings = builder.getOrCreateCategory(Component.translatable(LoginMusic.MODID + ".gui.config.client"));
        buildClientSettings(entryBuilder, clientSettings);
        // 服务端设置
        ConfigCategory serverSettings = builder.getOrCreateCategory(Component.translatable(LoginMusic.MODID + ".gui.config.server"));
        buildServerSettings(entryBuilder, serverSettings);
        // 保存回调
        builder.setSavingRunnable(() -> {
            // 保存并触发 MusicConfig 重载
            MusicConfig.saveToFile();
            LyricLayer.getInstance().setLyricLayer(ClientConfig.LYRIC_POS.get(), ClientConfig.LYRIC_COLOR.get());
        });

        return builder;
    }

    // 音乐条目配置
    private static void buildMusicEntries(ConfigEntryBuilder entryBuilder, ConfigCategory musicEntries) {
        // 使用临时列表来存储 GUI 中的顺序
        List<MusicEntry> currentList = MusicConfig.getMusicList();
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
                                newEntry.setId("NewMusic" + (MusicConfig.getMusicList().size() + 1));
                                newEntry.setMusicName("");
                                newEntry.setMusicPath("");
                                newEntry.setLyricName("");
                                newEntry.setLyricPath("");
                                currentList.add(newEntry);
                                MusicConfig.setMusicList(currentList);
                            }
                        })
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
                        .build());
        // 添加音乐文件字段
        subCategoryBuilder.add(
                entryBuilder.startStrField(Component.translatable(LoginMusic.MODID + ".gui.config.music"), entry.getMusicName())
                        .setDefaultValue("")
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".gui.config.music.tooltip"))
                        .setSaveConsumer(entry::setMusicName)
                        .build());
        // 添加音乐 URL 字段
        subCategoryBuilder.add(
                entryBuilder.startStrField(Component.translatable(LoginMusic.MODID + ".gui.config.musicUrl"), entry.getMusicPath())
                        .setDefaultValue("")
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".gui.config.musicUrl.tooltip"))
                        .setSaveConsumer(entry::setMusicPath)
                        .build());
        // 本地歌词文件字段
        subCategoryBuilder.add(
                entryBuilder.startStrField(Component.translatable(LoginMusic.MODID + ".gui.config.lyric"), entry.getLyricName())
                        .setDefaultValue("")
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".gui.config.lyric.tooltip"))
                        .setSaveConsumer(entry::setLyricName)
                        .build());
        // 歌词 URL 字段
        subCategoryBuilder.add(
                entryBuilder.startStrField(Component.translatable(LoginMusic.MODID + ".gui.config.lyricUrl"), entry.getLyricPath())
                        .setDefaultValue("")
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".gui.config.lyricUrl.tooltip"))
                        .setSaveConsumer(entry::setLyricPath)
                        .build()
        );
        // 删除按钮
        subCategoryBuilder.add(
                entryBuilder.startBooleanToggle(Component.translatable(LoginMusic.MODID + ".gui.config.delete"), false)
                        .setSaveConsumer(shouldDelete -> {
                            if (shouldDelete) {
                                MusicConfig.getMusicList().remove(index);
                            }
                        })
                        .build()
        );

        return subCategoryBuilder.build();
    }

    // 音乐播放配置
    private static void buildClientSettings(ConfigEntryBuilder entryBuilder, ConfigCategory clientSettings) {
        // 音乐播放范围
        clientSettings.addEntry(
                entryBuilder.startIntField(Component.translatable(LoginMusic.MODID + ".configui.music_play_range"), ClientConfig.MUSIC_PLAY_RANGE.get())
                        .setDefaultValue(3)
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".configui.music_play_range.tooltip"))
                        .setSaveConsumer(range -> ClientConfig.MUSIC_PLAY_RANGE.set(range))
                        .build());
        // 是否允许下载
        clientSettings.addEntry(
                entryBuilder.startBooleanToggle(Component.translatable(LoginMusic.MODID + ".configui.allow_download"), ClientConfig.ALLOW_DOWNLOAD.get())
                        .setDefaultValue(false)
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".configui.allow_download.tooltip"))
                        .setSaveConsumer(allowDownload -> ClientConfig.ALLOW_DOWNLOAD.set(allowDownload))
                        .build());
        // 是否允许播放其他玩家的音乐
        clientSettings.addEntry(
                entryBuilder.startBooleanToggle(Component.translatable(LoginMusic.MODID + ".configui.allow_others_music"), ClientConfig.ALLOW_OTHERS_MUSIC.get())
                        .setDefaultValue(false)
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".configui.allow_others_music.tooltip"))
                        .setSaveConsumer(allowOthersMusic -> ClientConfig.ALLOW_OTHERS_MUSIC.set(allowOthersMusic))
                        .build());
        // 是否展示歌词
        clientSettings.addEntry(
                entryBuilder.startBooleanToggle(Component.translatable(LoginMusic.MODID + ".configui.allow_lyrics"), ClientConfig.ALLOW_LYRICS.get())
                        .setDefaultValue(true)
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".configui.allow_lyrics.tooltip"))
                        .setSaveConsumer(allowLyrics -> ClientConfig.ALLOW_LYRICS.set(allowLyrics))
                        .build());
        // 歌词位置设置
        clientSettings.addEntry(
                entryBuilder.startEnumSelector(Component.translatable(LoginMusic.MODID + ".configui.lyrics_pos"), ClientConfig.Position.class, ClientConfig.LYRIC_POS.get())
                        .setDefaultValue(ClientConfig.Position.DOWN)
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".configui.lyrics_pos.tooltip"))
                        .setSaveConsumer(lyricPos -> ClientConfig.LYRIC_POS.set(lyricPos))
                        .build());
        // 歌词颜色设置
        clientSettings.addEntry(
                entryBuilder.startColorField(Component.translatable(LoginMusic.MODID + ".configui.lyrics_color"), ClientConfig.LYRIC_COLOR.get())
                        .setDefaultValue(0xFFFFFF)
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".configui.lyrics_color.tooltip"))
                        .setSaveConsumer(lyricColor -> ClientConfig.LYRIC_COLOR.set(lyricColor))
                        .build());
    }

    private static void buildServerSettings(ConfigEntryBuilder entryBuilder, ConfigCategory serverSettings) {
        serverSettings.addEntry(
                entryBuilder.startEnumSelector(Component.translatable(LoginMusic.MODID + ".configui.music_id_type"), SelectionKey.class, ServerConfig.MUSIC_ID_TYPE.get())
                        .setDefaultValue(SelectionKey.NAME)
                        .setTooltip(Component.translatable(LoginMusic.MODID + ".configui.music_id_type.tooltip"))
                        .setSaveConsumer(selectionKey -> ServerConfig.MUSIC_ID_TYPE.set(selectionKey))
                        .build());
    }
}
