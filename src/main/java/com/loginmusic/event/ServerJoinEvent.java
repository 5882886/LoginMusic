package com.loginmusic.event;

import com.loginmusic.LoginMusic;
import com.loginmusic.music.MusicConfig;
import com.loginmusic.music.MusicEntry;
import com.loginmusic.network.NetworkConfig;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = LoginMusic.MODID)
public class ServerJoinEvent {
    @SubscribeEvent
    // 玩家登录事件
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        // 服务端
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // 根据玩家名称获取音乐
            String musicId = chooseMusic(serverPlayer);
            // 玩家登录时发送音乐配置
            if (MusicConfig.isConfigLoaded()) {
                MusicConfig.loadFromConfig();
            }

            LoginMusic.LOGGER.info("Player {} is logging in, sending music config", serverPlayer.getName().getString());
            NetworkConfig.sendLoginMusic(serverPlayer, musicId);
        }
    }

    // 选择音乐
    private static String chooseMusic(ServerPlayer player) {
        String result = "Default";
        if (MusicConfig.getType().equalsIgnoreCase("name")) {
            result = chooseMusicByName(player);
        } else if (MusicConfig.getType().equalsIgnoreCase("uuid")) {
            result = chooseMusicByUuid(player);
        }
        return result;
    }

    // 通过name选择音乐
    private static String chooseMusicByName(ServerPlayer player) {
        String MusicID = "Default";
        MusicEntry entry;
        // 获取玩家名称
        String playerName = player.getName().getString();
        // 根据玩家名称获取音乐
        entry = MusicConfig.getMusic(playerName);
        if (entry == null) {
            return MusicID;
        }
        MusicID = entry.getId();
        return MusicID;
    }

    // 通过uuid选择音乐
    private static String chooseMusicByUuid(ServerPlayer player) {
        String MusicID = "Default";
        MusicEntry entry;

        String playerStringUUID = player.getStringUUID();
        entry = MusicConfig.getMusic(playerStringUUID);
        if (entry == null) {
            return MusicID;
        }
        MusicID = entry.getId();
        return MusicID;
    }
}