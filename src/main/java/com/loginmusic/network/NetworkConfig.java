package com.loginmusic.network;

import com.loginmusic.LoginMusic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

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

        LoginMusic.LOGGER.info("网络包注册完成");
    }

    public static void sendLoginMusic(ServerPlayer player, String musicId) {
        if (player == null) return;

        // 发送数据包到客户端
        PacketDistributor.sendToPlayer(player, new LoginMusicPacket(musicId));

        LoginMusic.LOGGER.info("已发送音乐 {} 给 {}", musicId, player.getName().getString());
    }
}