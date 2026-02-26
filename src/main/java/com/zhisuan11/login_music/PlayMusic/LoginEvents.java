package com.zhisuan11.login_music.PlayMusic;

import com.zhisuan11.login_music.LoginMusic;
import com.zhisuan11.login_music.Music.MusicConfig;
import com.zhisuan11.login_music.Music.MusicEntry;
import com.zhisuan11.login_music.Network.NetworkConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = LoginMusic.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LoginEvents {
    @SubscribeEvent
    // 玩家登录事件
    public static void PlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        // 服务端
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // 根据玩家名称获取音乐
            String musicId = ChooseMusic(serverPlayer);
            NetworkConfig.sendLoginMusic(serverPlayer, musicId);
        }
    }

    private static String ChooseMusic(ServerPlayer player) {
        String MusicID;
        MusicEntry entry;

        // 获取玩家名称
        String playerName = player.getName().getString();
        // 根据玩家名称获取音乐
        entry = MusicConfig.getMusic(playerName);

        if (entry == null) {
            MusicID = "Default";
            return MusicID;
        }
        MusicID = entry.getId();
        return MusicID;
    }
}
