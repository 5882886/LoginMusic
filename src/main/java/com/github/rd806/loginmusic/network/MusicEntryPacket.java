package com.github.rd806.loginmusic.network;

import com.github.rd806.loginmusic.event.ClientEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MusicEntryPacket {

    private final String musicID;

    public MusicEntryPacket(String musicID) {
        this.musicID = musicID;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(musicID);
    }

    public static MusicEntryPacket decode(FriendlyByteBuf buf) {
        return new MusicEntryPacket(buf.readUtf());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientEvent.playLoginMusic(musicID)));
        context.setPacketHandled(true);
    }

}
