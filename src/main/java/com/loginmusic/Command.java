package com.loginmusic;

import com.loginmusic.music.MusicConfig;
import com.loginmusic.music.MusicEntry;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

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
