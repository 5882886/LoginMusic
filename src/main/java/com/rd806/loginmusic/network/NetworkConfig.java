package com.rd806.loginmusic.network;

import com.rd806.loginmusic.LoginMusic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

// 网络包类
public class NetworkConfig {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.tryParse(LoginMusic.MODID + ":main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetID = 0;
    private static int id() { return packetID ++; }

    // 注册网络音乐数据包
    public static void register() {
        INSTANCE.messageBuilder(LoginMusicPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LoginMusicPacket::encode)
                .decoder(LoginMusicPacket::decode)
                .consumerMainThread(LoginMusicPacket::handle)
                .add();

        LoginMusic.LOGGER.info("Network config registered!");
    }

    // 发送音乐给特定玩家
    public static void sendLoginMusic(ServerPlayer player, String musicId) {
        if (player == null) return;

        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new LoginMusicPacket(musicId));

        LoginMusic.LOGGER.info("Send music {} to {}", musicId, player.getName().getString());
    }

    // 同步数据给特定玩家
    public static void sendConfigToPlayer(Object packet, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
