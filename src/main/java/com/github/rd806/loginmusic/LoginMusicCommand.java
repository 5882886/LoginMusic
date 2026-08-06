package com.github.rd806.loginmusic;

import com.github.rd806.loginmusic.config.ServerConfig;
import com.github.rd806.loginmusic.event.ServerEvent;
import com.github.rd806.loginmusic.network.NetworkConfig;
import com.mojang.brigadier.Command;
import com.github.rd806.loginmusic.media.music.MusicConfig;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

public class LoginMusicCommand {

    private static final String ROOT = "loginmusic";
    private static final String PLAY = "play";
    private static final String LIST = "list";
    private static final String RELOAD = "reload";

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(ROOT)
                .requires(source -> source.hasPermission(2));
        LiteralArgumentBuilder<CommandSourceStack> play = Commands.literal(PLAY);
        LiteralArgumentBuilder<CommandSourceStack> list = Commands.literal(LIST);
        LiteralArgumentBuilder<CommandSourceStack> reload = Commands.literal(RELOAD);

        root.then(play.executes(LoginMusicCommand::playMusic));
        root.then(list.executes(LoginMusicCommand::showList));
        root.then(reload.executes(LoginMusicCommand::reloadConfig));
        return root;
    }

    // 播放音乐
    private static int playMusic(CommandContext<CommandSourceStack> context) {
        try {
            ServerPlayer serverPlayer = context.getSource().getPlayer();
            if (serverPlayer != null) {
                SelectionKey key = ServerConfig.MUSIC_ID_TYPE.get();
                MusicEntry music = MusicConfig.getMusic(ServerEvent.chooseMusic(serverPlayer, key));
                NetworkConfig.sendMusicToPlayer(serverPlayer, music, key);
            }
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Fail to send music: {}", e.getMessage());
        }
        return Command.SINGLE_SUCCESS;
    }

    // 展示列表
    private static int showList(CommandContext<CommandSourceStack> context) {
        try {
            Map<String, MusicEntry> tempMap = MusicConfig.getMusicEntryMap();
            if (tempMap.isEmpty()) {
                context.getSource().sendFailure(Component.translatable(LoginMusic.MODID + ".commands.list.empty"));
            } else {
                // 显示音乐主键
                SelectionKey key = ServerConfig.MUSIC_ID_TYPE.get();
                context.getSource().sendSuccess(
                        () -> Component.translatable(LoginMusic.MODID + ".commands.list.key", key),
                        false
                );
                // 显示音乐配置信息
                context.getSource().sendSuccess(
                        () -> Component.translatable(LoginMusic.MODID + ".commands.list.success", tempMap.size()),
                        false);
                for (Map.Entry<String, MusicEntry> entry : tempMap.entrySet()) {
                    String musicName = entry.getValue().getMusicName();
                    context.getSource().sendSuccess(
                            () -> Component.literal("§a▍ §7" + entry.getKey() + ": §r" + musicName),
                            false);
                }
            }
        } catch (Exception e) {
            LoginMusic.LOGGER.error(e.getMessage());
        }
        return Command.SINGLE_SUCCESS;
    }

    // 重新加载
    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        try {
            MusicConfig.loadFromConfig();
            context.getSource().sendSuccess(
                    () -> Component.translatable(LoginMusic.MODID + ".commands.reload.success"),
                    true);
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Fail to reload LoginMusic config: {}", e.getMessage());
        }
        return Command.SINGLE_SUCCESS;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(get());
    }
}