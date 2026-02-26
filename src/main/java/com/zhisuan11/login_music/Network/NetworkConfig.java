package com.zhisuan11.login_music.Network;

import com.zhisuan11.login_music.LoginMusic;
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

        LoginMusic.LOGGER.info("网络包注册完成");
    }

    public static void sendLoginMusic(ServerPlayer player, String musicId) {
        if (player == null) return;

        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player),
                new LoginMusicPacket(musicId));

        LoginMusic.LOGGER.info("已发送音乐 {} 给 {}", musicId, player.getName().getString());
    }
}
