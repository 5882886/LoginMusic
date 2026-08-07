package com.github.rd806.loginmusic.network;

import com.github.rd806.loginmusic.command.CommandType;
import com.github.rd806.loginmusic.media.music.MusicCache;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MusicCachePacket {

    private final CommandType type;

    public MusicCachePacket(CommandType type) {
        this.type = type;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeEnum(type);
    }

    public static MusicCachePacket decode(FriendlyByteBuf buf) {
        return new MusicCachePacket(buf.readEnum(CommandType.class));
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            switch(type) {
                case CACHE_LIST -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> MusicCache::showCache);
                case CACHE_CLEAR -> DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> MusicCache::clearCache);
            }
        });
        context.setPacketHandled(true);
    }
}
