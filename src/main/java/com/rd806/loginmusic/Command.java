package com.rd806.loginmusic;

import com.rd806.loginmusic.music.MusicConfig;
import com.rd806.loginmusic.music.MusicEntry;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

public class Command {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("loginmusic")
                        // /loginmusic reload
                        .then(Commands.literal("reload")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    try {
                                        MusicConfig.loadFromConfig();
                                        context.getSource().sendSuccess(
                                                () -> Component.translatable(LoginMusic.MODID + ".commands.reload.success"),
                                                true
                                        );
                                        return 1;
                                    } catch (Exception e) {
                                        context.getSource().sendFailure(
                                                Component.translatable(LoginMusic.MODID + ".commands.reload.fail", e.getMessage())
                                        );
                                        return 0;
                                    }
                                })
                        )

                        // /loginmusic list
                        .then(Commands.literal("list")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                    Map<String, MusicEntry> tmpMap = MusicConfig.getMusicEntryMap();

                                    if (tmpMap.isEmpty()) {
                                        context.getSource().sendFailure(
                                                Component.translatable(LoginMusic.MODID + ".commands.list.empty")
                                        );
                                        return 0;
                                    }

                                    context.getSource().sendSuccess(
                                            () -> Component.translatable(LoginMusic.MODID + ".commands.list.success", tmpMap.size()),
                                            false
                                    );
                                    // 显示音乐配置信息
                                    for (Map.Entry<String, MusicEntry> entry : tmpMap.entrySet()) {
                                        context.getSource().sendSuccess(
                                                () -> Component.literal(entry.getKey() + ": " + entry.getValue().getName()),
                                                false
                                        );
                                    }
                                    return 1;
                                })
                        )
        );
    }
}