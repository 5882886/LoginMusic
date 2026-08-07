package com.github.rd806.loginmusic.network;

import com.github.rd806.loginmusic.media.music.MusicConfig;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MusicCachePacket {


    public MusicCachePacket() {}

    public void encode(FriendlyByteBuf ignoredBuf) {}

    public static MusicCachePacket decode(FriendlyByteBuf ignoredBuf) {
        return new MusicCachePacket();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.safeRunWhenOn(
                Dist.CLIENT,
                () -> MusicConfig::showCache));
        context.setPacketHandled(true);
    }
}
