package com.loginmusic.network;

import com.loginmusic.LoginMusic;
import com.loginmusic.event.ClientLoginEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LoginMusicPacket {
    // 播放音乐的ID
    private final String musicID;

    public LoginMusicPacket(String musicID) { this.musicID = musicID; }

    // 写入音乐ID
    public void encode(FriendlyByteBuf buf) { buf.writeUtf(musicID); }

    // 读取音乐ID
    public static LoginMusicPacket decode(FriendlyByteBuf buf) { return new LoginMusicPacket(buf.readUtf()); }

    // 处理数据包：当客户端收到此包时调用
    public static void handle(LoginMusicPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            LoginMusic.LOGGER.info("准备播放");
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientLoginEvent.PlayLoginMusic(packet.musicID));
        });
        context.setPacketHandled(true);
    }
}
