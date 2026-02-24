package com.zhisuan11.login_music.Network;

import com.zhisuan11.login_music.LoginMusic;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkConfig {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            ResourceLocation.tryParse(LoginMusic.MODID + ":main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int packetID = 0;
    private static int id() {
        return packetID ++;
    }

    public static void register() {
        INSTANCE.messageBuilder(LoginMusicPacket.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .encoder(LoginMusicPacket::encode)
                .decoder(LoginMusicPacket::decode)
                .consumerMainThread(LoginMusicPacket::handle)
                .add();
    }

    public static void sendLoginMusic(ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), new LoginMusicPacket("login_music"));
        player.displayClientMessage(
                net.minecraft.network.chat.Component.literal("正在播放登录音乐！"),
                true
        );
    }
}
