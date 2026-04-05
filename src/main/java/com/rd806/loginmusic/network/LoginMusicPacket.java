package com.rd806.loginmusic.network;

import com.rd806.loginmusic.LoginMusic;
import com.rd806.loginmusic.event.ClientEvent;
import com.rd806.loginmusic.media.music.MusicConfig;
import com.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class LoginMusicPacket {

    // 音乐播放信息
    private final String musicID;
    private final Map<String, MusicEntry> musicConfig;
    private final boolean isConfig;

    public LoginMusicPacket(String musicID) {
        this.musicID = musicID;
        this.musicConfig = null;
        this.isConfig = false;
    }

    public LoginMusicPacket(Map<String, MusicEntry> musicConfig) {
        this.musicID = null;
        this.musicConfig = musicConfig;
        this.isConfig = true;
    }

    // 写入音乐ID
    public void encode(FriendlyByteBuf buf) {
        boolean isConfig = this.isConfig;
        buf.writeBoolean(isConfig);

        if (isConfig) {
            if (musicConfig != null) {
                buf.writeInt(musicConfig.size());
                for (Map.Entry<String, MusicEntry> entry : musicConfig.entrySet()) {
                    buf.writeUtf(entry.getKey());
                    MusicEntry music = entry.getValue();
                    buf.writeUtf(music.getName());
                    buf.writeUtf(music.getMusicUrl());
                    buf.writeUtf(music.getLyrics());
                    buf.writeUtf(music.getLyricsUrl());
                }
            }
        } else {
            if (musicID != null) {
                buf.writeUtf(musicID);
            }
        }
    }

    // 读取音乐ID
    public static LoginMusicPacket decode(FriendlyByteBuf buf) {
        boolean isConfig = buf.readBoolean();

        if (isConfig) {
            // 读取配置同步数据
            int size = buf.readInt();
            Map<String, MusicEntry> config = new HashMap<>();
            for (int i = 0; i < size; i++) {
                String id = buf.readUtf();
                String name = buf.readUtf();
                String musicUrl = buf.readUtf();
                String lyrics = buf.readUtf();
                String lyricsUrl = buf.readUtf();
                // 写入数据
                MusicEntry musicEntry = new MusicEntry();
                musicEntry.setId(id);
                musicEntry.setName(name);
                musicEntry.setMusicUrl(musicUrl);
                musicEntry.setLyrics(lyrics);
                musicEntry.setLyricsUrl(lyricsUrl);
                config.put(id, musicEntry);
            }
            return new LoginMusicPacket(config);
        } else {
            // 读取音乐ID
            return new LoginMusicPacket(buf.readUtf());
        }
    }

    // 处理数据包：当客户端收到此包时调用
    public static void handle(LoginMusicPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            if (packet.isConfig) {
                // 同步音乐配置到客户端
                LoginMusic.LOGGER.info("Receive musics from the server, total {} musics", packet.musicConfig.size());
                DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () ->
                        MusicConfig.receiveConfig(packet.musicConfig));
            } else {
                // 播放音乐
                LoginMusic.LOGGER.info("Prepare music: {}", packet.musicID);
                DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () ->
                        ClientEvent. playLoginMusic(packet.musicID));
            }
        });
        context.setPacketHandled(true);
    }
}
