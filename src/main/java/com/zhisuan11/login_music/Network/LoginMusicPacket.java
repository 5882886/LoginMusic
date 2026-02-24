package com.zhisuan11.login_music.Network;

import com.zhisuan11.login_music.PlayMusic.ClientMusicHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LoginMusicPacket {
    // 播放音乐的ID
    private final String musicID;

    public LoginMusicPacket(String musicID) {
        this.musicID = musicID;
    }

    // 写入音乐ID
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(musicID);
    }

    // 读取音乐ID
    public static LoginMusicPacket decode(FriendlyByteBuf buf) {
        return new LoginMusicPacket(buf.readUtf());
    }

    public static void handle(LoginMusicPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () ->
                    ClientMusicHandler.PlayMusic(packet.musicID));
        });
        context.setPacketHandled(true);
    }
}
