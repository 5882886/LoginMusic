package com.zhisuan11.login_music.PlayMusic;

import com.zhisuan11.login_music.LoginMusic;
import com.zhisuan11.login_music.Network.NetworkConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = LoginMusic.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LoginEvents {
    @SubscribeEvent
    public static void PlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        // 服务端
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            NetworkConfig.sendLoginMusic(serverPlayer);
        }
    }
}
