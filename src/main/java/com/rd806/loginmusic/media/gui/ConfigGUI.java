package com.rd806.loginmusic.media.gui;

import com.rd806.loginmusic.LoginMusic;
import com.rd806.loginmusic.media.music.MusicEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.SubCategoryListEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class ConfigGUI {
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
                .setTitle(Component.translatable(LoginMusic.MODID + ".config.title"));

        ConfigCategory musicCategory = builder.getOrCreateCategory(Component.translatable(LoginMusic.MODID + ".config.category"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // 使用临时列表来存储 GUI 中的顺序
        List<MusicEntry> currentList = visualWrapper.getMusicList();

        // 为每个音乐条目创建编辑界面
        for (int index = 0; index < currentList.size(); index++) {
            MusicEntry entry = currentList.get(index);

            SubCategoryListEntry subEntry = createMusicSubCategory(entryBuilder, entry, index);
            musicCategory.addEntry(subEntry);
        }

        // 添加新音乐的按钮
        musicCategory.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable(LoginMusic.MODID + ".config.addEnable"),
                        false)
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
                        visualWrapper.saveToFile();
                        // 刷新界面
                        refreshScreen();
                    }
                })
                .build());

        // 保存回调
        builder.setSavingRunnable(() -> {
            visualWrapper.saveToFile();  // 保存并触发 MusicConfig 重载
        });

        return builder.build();
    }

    // 刷新界面的方法
    private static void refreshScreen() {
        Minecraft.getInstance().setScreen(buildScreen());
    }

    private static SubCategoryListEntry createMusicSubCategory(ConfigEntryBuilder entryBuilder, MusicEntry entry, int index) {
        // 创建子分类构建器
        var subCategoryBuilder = entryBuilder.startSubCategory(Component.literal(" " + entry.getId()));

        // 添加 ID 字段
        subCategoryBuilder.add(
                entryBuilder.startStrField(Component.literal("ID"), entry.getId())
                        .setDefaultValue("" + index)
                        .setSaveConsumer(entry::setId)
                        .build()
        );

        // 添加音乐文件字段
        subCategoryBuilder.add(
                entryBuilder.startStrField(Component.translatable(LoginMusic.MODID + ".config.musicfile"), entry.getMusic())
                        .setDefaultValue("")
                        .setSaveConsumer(entry::setMusic)
                        .build()
        );

        // 添加音乐 URL 字段
        subCategoryBuilder.add(
                entryBuilder.startStrField(Component.translatable(LoginMusic.MODID + ".config.musicUrl"), entry.getMusicUrl())
                        .setDefaultValue("")
                        .setSaveConsumer(entry::setMusicUrl)
                        .build()
        );

        // 本地歌词文件字段
        subCategoryBuilder.add(
                entryBuilder.startStrField(Component.translatable(LoginMusic.MODID + ".config.lyric"), entry.getLyric())
                        .setDefaultValue("")
                        .setSaveConsumer(entry::setLyric)
                        .build()
        );

        // 歌词 URL 字段
        subCategoryBuilder.add(
                entryBuilder.startStrField(Component.translatable(LoginMusic.MODID + ".config.lyricUrl"), entry.getLyricUrl())
                        .setDefaultValue("")
                        .setTooltip(Component.literal("在线歌词地址"))
                        .setSaveConsumer(entry::setLyricUrl)
                        .build()
        );

        // 删除按钮
        subCategoryBuilder.add(
                entryBuilder.startBooleanToggle(Component.translatable(LoginMusic.MODID + ".config.delete"), false)
                        .setSaveConsumer(shouldDelete -> {
                            if (shouldDelete) {
                                visualWrapper.getMusicList().remove(index);
                                visualWrapper.saveToFile();
                                refreshScreen();
                            }
                        })
                        .build()
        );

        return subCategoryBuilder.build();
    }
}
