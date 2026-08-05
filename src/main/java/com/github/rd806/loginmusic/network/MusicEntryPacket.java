package com.github.rd806.loginmusic.network;

import com.github.rd806.loginmusic.event.ClientEvent;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MusicEntryPacket {

    private final MusicEntry music;

    public MusicEntryPacket(MusicEntry music) {
        this.music = music;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(music.getId());
        buf.writeUtf(music.getMusicName());
        buf.writeUtf(music.getMusicPath());
        buf.writeUtf(music.getLyricName());
        buf.writeUtf(music.getLyricPath());
    }

    public static MusicEntryPacket decode(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        String musicName = buf.readUtf();
        String musicPath = buf.readUtf();
        String lyricName = buf.readUtf();
        String lyricPath = buf.readUtf();
        MusicEntry music = new MusicEntry(id, musicName, musicPath, lyricName, lyricPath);
        return new MusicEntryPacket(music);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientEvent.playLoginMusic(music)));
        context.setPacketHandled(true);
    }

}
