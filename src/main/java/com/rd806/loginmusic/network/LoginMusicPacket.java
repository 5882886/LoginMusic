package com.rd806.loginmusic.network;

import com.rd806.loginmusic.LoginMusic;
import com.rd806.loginmusic.event.ClientLoginEvent;
import com.rd806.loginmusic.media.music.MusicConfig;
import com.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class LoginMusicPacket implements CustomPacketPayload {

    // 定义包类型
    public static final CustomPacketPayload.Type<LoginMusicPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(LoginMusic.MODID, "login_music")
            );

    // 音乐播放信息
    private final String musicID;
    private final Map<String, MusicEntry> musicConfig;
    private final boolean isConfig;

    // 构造器：播放音乐
    public LoginMusicPacket(String musicID) {
        this.musicID = musicID;
        this.musicConfig = null;
        this.isConfig = false;
    }

    // 构造器：同步配置
    public LoginMusicPacket(Map<String, MusicEntry> musicConfig) {
        this.musicID = null;
        this.musicConfig = musicConfig;
        this.isConfig = true;
    }

    // 自定义编解码器（因为需要处理两种不同的数据类型）
    public static final StreamCodec<FriendlyByteBuf, LoginMusicPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public void encode(FriendlyByteBuf buf, LoginMusicPacket packet) {
            buf.writeBoolean(packet.isConfig);

            if (packet.isConfig) {
                // 写入配置数据
                if (packet.musicConfig != null) {
                    buf.writeInt(packet.musicConfig.size());
                    for (Map.Entry<String, MusicEntry> entry : packet.musicConfig.entrySet()) {
                        buf.writeUtf(entry.getKey());
                        MusicEntry music = entry.getValue();
                        buf.writeUtf(music.getName());
                        buf.writeUtf(music.getUrl());
                        buf.writeUtf(music.getLyrics());
                    }
                } else {
                    buf.writeInt(0);
                }
            } else {
                // 写入音乐ID
                buf.writeUtf(packet.musicID != null ? packet.musicID : "");
            }
        }

        @Override
        public @NotNull LoginMusicPacket decode(FriendlyByteBuf buf) {
            boolean isConfig = buf.readBoolean();

            if (isConfig) {
                // 读取配置数据
                int size = buf.readInt();
                Map<String, MusicEntry> config = new HashMap<>();
                for (int i = 0; i < size; i++) {
                    // 获取信息
                    String id = buf.readUtf();
                    String name = buf.readUtf();
                    String url = buf.readUtf();
                    String lyrics = buf.readUtf();
                    // 写入信息
                    MusicEntry musicEntry = new MusicEntry();
                    musicEntry.setId(id);
                    musicEntry.setName(name);
                    musicEntry.setUrl(url);
                    musicEntry.setLyrics(lyrics);
                    config.put(id, musicEntry);
                }
                return new LoginMusicPacket(config);
            } else {
                // 读取音乐ID
                return new LoginMusicPacket(buf.readUtf());
            }
        }
    };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // 处理数据包
    public static void handle(final LoginMusicPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            if (packet.isConfig) {
                // 同步音乐配置到客户端
                LoginMusic.LOGGER.info("Receive musics from the server, total {} musics",
                        packet.musicConfig != null ? packet.musicConfig.size() : 0);

                // 只在客户端执行
                if (FMLEnvironment.dist == Dist.CLIENT) {
                    MusicConfig.receiveConfig(packet.musicConfig);
                }
            } else {
                // 播放音乐
                LoginMusic.LOGGER.info("Prepare music: {}", packet.musicID);
                // 只在客户端执行
                if (FMLEnvironment.dist == Dist.CLIENT) {
                    ClientLoginEvent.playLoginMusic(packet.musicID);
                }
            }
        });
    }

    public boolean isConfig() {
        return isConfig;
    }
}