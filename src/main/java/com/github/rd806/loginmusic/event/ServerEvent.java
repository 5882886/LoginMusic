package com.github.rd806.loginmusic.event;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.SelectionKey;
import com.github.rd806.loginmusic.config.ServerConfig;
import com.github.rd806.loginmusic.media.music.MusicConfig;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import com.github.rd806.loginmusic.network.NetworkConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = LoginMusic.MODID)
public class ServerEvent {
    @SubscribeEvent
    // 玩家登录事件
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        // 服务端
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // 根据玩家名称获取音乐
            SelectionKey key = ServerConfig.MUSIC_ID_TYPE.get();
            String musicId = chooseMusic(serverPlayer, key);
            MusicEntry entry = MusicConfig.getMusic(musicId);
            // 将音乐发送给全体玩家
            MinecraftServer server = serverPlayer.getServer();
            if (server != null) {
                PlayerList playerList = server.getPlayerList();
                for (ServerPlayer player : playerList.getPlayers()) {
                    NetworkConfig.sendMusicToPlayer(player, entry, key);
                }
            }
        }
    }

    // 选择音乐
    public static String chooseMusic(ServerPlayer player, SelectionKey key) {
        String result = "Default";
        switch (key) {
            case NAME -> result = chooseMusicByName(player);
            case UUID -> result = chooseMusicByUuid(player);
        }
        return result;
    }

    // 通过name选择音乐
    private static String chooseMusicByName(ServerPlayer player) {
        String MusicID = "Default";
        // 获取玩家名称
        String playerName = player.getName().getString();
        // 根据玩家名称获取音乐
        MusicEntry entry = MusicConfig.getMusic(playerName);
        if (entry == null) {
            return MusicID;
        }
        MusicID = entry.getId();
        return MusicID;
    }

    // 通过uuid选择音乐
    private static String chooseMusicByUuid(ServerPlayer player) {
        String MusicID = "Default";

        String playerStringUUID = player.getStringUUID();
        MusicEntry entry = MusicConfig.getMusic(playerStringUUID);
        if (entry == null) {
            return MusicID;
        }
        MusicID = entry.getId();
        return MusicID;
    }
}