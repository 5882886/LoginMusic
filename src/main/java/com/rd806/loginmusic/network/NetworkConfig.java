package com.rd806.loginmusic.network;

import com.rd806.loginmusic.LoginMusic;
import com.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Map;

// 网络包类
public class NetworkConfig {
    public static final ResourceLocation CHANNEL_ID =
            ResourceLocation.fromNamespaceAndPath(LoginMusic.MODID, "main");

    // 注册网络音乐数据包
    public static void register(final RegisterPayloadHandlersEvent event) {
        // 获取注册器
        final PayloadRegistrar registrar = event.registrar(CHANNEL_ID.getNamespace())
                .versioned("1")
                .optional();

        // 注册数据包
        registrar.playToClient(
                LoginMusicPacket.TYPE,
                LoginMusicPacket.STREAM_CODEC,
                LoginMusicPacket::handle
        );

        LoginMusic.LOGGER.info("Network config registered!");
    }

    // 发送音乐给特定玩家
    public static void sendLoginMusic(ServerPlayer player, String musicId) {
        if (player == null) return;

        // 发送数据包到客户端
        PacketDistributor.sendToPlayer(player, new LoginMusicPacket(musicId));

        LoginMusic.LOGGER.info("Send music {} to {}", musicId, player.getName().getString());
    }

    // 同步数据给玩家
    public static void sendConfigToPlayer(Map<String, MusicEntry> musicConfig, ServerPlayer player) {
        if (player == null || musicConfig == null) return;

        // 发送配置同步包到客户端
        PacketDistributor.sendToPlayer(player, new LoginMusicPacket(musicConfig));

        LoginMusic.LOGGER.info("Sending music config to {}，total {} musics", player.getName().getString(), musicConfig.size());
    }
}