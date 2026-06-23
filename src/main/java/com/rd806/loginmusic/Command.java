package com.rd806.loginmusic;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.rd806.loginmusic.event.ClientEvent;
import com.rd806.loginmusic.media.music.MusicConfig;
import com.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.client.Minecraft;
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

                        //  /loginmusic play
                        .then(Commands.literal("play")
                                .executes(context -> {
                                    String musicId = "Default";
                                    if (Minecraft.getInstance().player != null) {
                                        musicId = Minecraft.getInstance().player.getName().getString();
                                    }
                                    ClientEvent.playLoginMusic(musicId);
                                    return 1;
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
                                                () -> Component.literal(entry.getKey() + ": " + entry.getValue().getMusic()),
                                                false
                                        );
                                    }
                                    return 1;
                                })
                        )

                        //  /loginmusic show
                        .then(Commands.literal("show")
                                .then(Commands.argument("targetPlayer", StringArgumentType.word())
                                    .requires(source -> source.hasPermission(2))
                                    .executes(context -> {

                                        String targetPlayerName = StringArgumentType.getString(context, "targetPlayer");
                                        MusicEntry entry = MusicConfig.getMusic(targetPlayerName);

                                        if (entry == null) {
                                            context.getSource().sendFailure(
                                                    Component.translatable(LoginMusic.MODID + ".commands.show.fail")
                                            );
                                            return 0;
                                        }

                                        context.getSource().sendSuccess(() -> Component.translatable(LoginMusic.MODID + ".commands.show.success"), false);
                                        context.getSource().sendSuccess(() -> Component.literal("id: " + entry.getId()), false);
                                        context.getSource().sendSuccess(() -> Component.literal("name: " + entry.getMusic()), false);
                                        context.getSource().sendSuccess(() -> Component.literal("musicUrl: " + entry.getMusicUrl()), false);
                                        context.getSource().sendSuccess(() -> Component.literal("lyrics: " + entry.getLyric()), false);
                                        context.getSource().sendSuccess(() -> Component.literal("lyrics: " + entry.getLyricUrl()), false);
                                        return 1;
                                    })
                                )
                        )
        );
    }
}