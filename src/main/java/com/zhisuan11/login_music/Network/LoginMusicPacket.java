package com.zhisuan11.login_music.Network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class LoginMusicPacket {
    public LoginMusicPacket() {}

    public void encode(FriendlyByteBuf buf) {}

    public static LoginMusicPacket decode(FriendlyByteBuf buf) {
        return new LoginMusicPacket();
    }

    public static void handle(LoginMusicPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handleLoginMusic(packet));
        });
        context.setPacketHandled(true);
    }
}
