package com.github.rd806.loginmusic.event;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.config.ServerConfig;
import com.github.rd806.loginmusic.media.music.MusicConfig;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import com.github.rd806.loginmusic.network.NetworkConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LoginMusic.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvent {
    // 玩家登录事件
    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // 根据玩家名称获取音乐
            String musicId = chooseMusic(serverPlayer);
            MusicEntry entry = MusicConfig.getMusic(musicId);
            // 将音乐发送给全体玩家
            MinecraftServer server = serverPlayer.getServer();
            if (server != null) {
                PlayerList playerList = server.getPlayerList();
                for (ServerPlayer player : playerList.getPlayers()) {
                    NetworkConfig.sendMusicToPlayer(player, entry);
                }
            }
        } else {
            LoginMusic.LOGGER.error("Not a player {} !", event.getEntity().getName());
        }
    }

    // 选择音乐
    public static String chooseMusic(ServerPlayer player) {
        String result = "Default";
        switch (ServerConfig.MUSIC_ID_TYPE.get()) {
            case NAME ->  result = chooseMusicByName(player);
            case UUID ->  result = chooseMusicByUuid(player);
        }
        return result;
    }

    // 通过name选择音乐
    private static String chooseMusicByName(ServerPlayer player) {
        String musicID = "Default";
        // 获取玩家名称
        String playerName = player.getName().getString();
        MusicEntry entry = MusicConfig.getMusic(playerName);

        if (entry == null) { return musicID; }
        musicID = entry.getId();
        return musicID;
    }

    // 通过uuid选择音乐
    private static String chooseMusicByUuid(ServerPlayer player) {
        String musicID = "Default";
        // 获取玩家UUID
        String playerUUIDString = player.getStringUUID();
        MusicEntry entry = MusicConfig.getMusic(playerUUIDString);
        if (entry == null) { return musicID; }
        musicID = entry.getId();
        return musicID;
    }
}
