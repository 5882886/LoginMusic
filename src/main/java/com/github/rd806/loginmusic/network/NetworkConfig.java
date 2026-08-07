package com.github.rd806.loginmusic.network;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.SelectionKey;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

// 网络包类
public class NetworkConfig {

    private static final String PROTOCOL_VERSION = "1";
    private static SimpleChannel CHANNEL;
    private static int packetID = 1;

    // 注册网络音乐数据包
    public static void register() {
        CHANNEL = NetworkRegistry.newSimpleChannel(
                ResourceLocation.tryParse(LoginMusic.MODID + ":main"),
                () -> PROTOCOL_VERSION,
                clientVersion -> true,
                PROTOCOL_VERSION::equals
        );

        CHANNEL.registerMessage(
                packetID++,
                MusicEntryPacket.class, MusicEntryPacket::encode, MusicEntryPacket::decode, MusicEntryPacket::handle);

        CHANNEL.registerMessage(
                packetID++,
                MusicCachePacket.class, MusicCachePacket::encode, MusicCachePacket::decode, MusicCachePacket::handle);

        LoginMusic.LOGGER.info("Network config registered!");
    }

    // 发送音乐给特定玩家
    public static void sendMusicToPlayer(ServerPlayer player, MusicEntry music, SelectionKey key) {
        LoginMusic.LOGGER.info("Send music {} to {}", music.getMusicName(), player.getName().getString());
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new MusicEntryPacket(music, key));
    }

    // 查看本地缓存
    public static void showPlayerMusicCache(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new MusicCachePacket());
    }
}
