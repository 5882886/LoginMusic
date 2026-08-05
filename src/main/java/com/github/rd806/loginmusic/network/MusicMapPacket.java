package com.github.rd806.loginmusic.network;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.media.music.MusicConfig;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class MusicMapPacket {

    // 音乐配置
    private final Map<String, MusicEntry> musicEntryMap;

    public MusicMapPacket(Map<String, MusicEntry> musicEntryMap) {
        this.musicEntryMap = musicEntryMap;
    }

    // 写入音乐ID
    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(musicEntryMap.size());
        for (Map.Entry<String, MusicEntry> entry : musicEntryMap.entrySet()) {
            buf.writeUtf(entry.getKey());
            MusicEntry music = entry.getValue();
            buf.writeUtf(music.getMusic());
            buf.writeUtf(music.getMusicUrl());
            buf.writeUtf(music.getLyric());
            buf.writeUtf(music.getLyricUrl());
        }
    }

    // 读取音乐ID
    public static MusicMapPacket decode(FriendlyByteBuf buf) {
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
            musicEntry.setMusic(name);
            musicEntry.setMusicUrl(musicUrl);
            musicEntry.setLyric(lyrics);
            musicEntry.setLyricUrl(lyricsUrl);
            config.put(id, musicEntry);
        }
        return new MusicMapPacket(config);
    }

    // 处理数据包：当客户端收到此包时调用
    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            // 同步音乐配置到客户端
            LoginMusic.LOGGER.info("Receive musics from the server, total {} musics", musicEntryMap.size());
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> MusicConfig.receiveConfig(musicEntryMap));
        });
        context.setPacketHandled(true);
    }
}
