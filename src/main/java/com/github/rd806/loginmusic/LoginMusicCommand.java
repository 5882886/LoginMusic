package com.github.rd806.loginmusic;

import com.github.rd806.loginmusic.network.MusicMapPacket;
import com.github.rd806.loginmusic.network.NetworkConfig;
import com.mojang.brigadier.Command;
import com.github.rd806.loginmusic.event.ClientEvent;
import com.github.rd806.loginmusic.media.music.MusicConfig;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
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
            String musicId = "Default";
            if (Minecraft.getInstance().player != null) {
                musicId = Minecraft.getInstance().player.getName().getString();
            }
            ClientEvent.playLoginMusic(musicId);
        } catch (Exception e) {
            LoginMusic.LOGGER.error(e.getMessage());
        }
        return Command.SINGLE_SUCCESS;
    }

    // 展示列表
    private static int showList(CommandContext<CommandSourceStack> context) {
        try {
            Map<String, MusicEntry> tempMap = MusicConfig.getMusicEntryMap();
            if (tempMap.isEmpty()) {
                context.getSource().sendFailure(Component.translatable(LoginMusic.MODID + ".commands.list.empty"));
                return 0;
            }
            context.getSource().sendSuccess(
                    () -> Component.translatable(LoginMusic.MODID + ".commands.list.success", tempMap.size()),
                    false
            );
            // 显示音乐配置信息
            for (Map.Entry<String, MusicEntry> entry : tempMap.entrySet()) {
                context.getSource().sendSuccess(
                        () -> Component.literal(entry.getKey() + ": " + entry.getValue().getMusic()),
                        false
                );
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
                    true
            );
            ServerPlayer serverPlayer = context.getSource().getPlayer();
            MinecraftServer server = null;
            if (serverPlayer != null) {
                server = serverPlayer.getServer();
            }
            // 同步完整配置
            if (server != null) {
                PlayerList playerList = server.getPlayerList();
                for (ServerPlayer player : playerList.getPlayers()) {
                    NetworkConfig.sendConfigToPlayer(new MusicMapPacket(MusicConfig.newMusicEntryMap()), player);
                }
            }
        } catch (Exception e) {
            LoginMusic.LOGGER.error(e.getMessage());
        }
        return Command.SINGLE_SUCCESS;
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(get());
    }
}