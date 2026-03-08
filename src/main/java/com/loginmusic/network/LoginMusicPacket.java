package com.loginmusic.network;

import com.loginmusic.LoginMusic;
import com.loginmusic.event.ClientLoginEvent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record LoginMusicPacket(String musicID) implements CustomPacketPayload {
    // 定义包类型
    public static final CustomPacketPayload.Type<LoginMusicPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(LoginMusic.MODID, "login_music")
            );

    // 定义流编解码器 - 使用 ByteBufCodecs
    public static final StreamCodec<FriendlyByteBuf, LoginMusicPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8,      // 编码器
                    LoginMusicPacket::musicID,      // 获取器
                    LoginMusicPacket::new           // 构造器
            );

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // 处理数据包：当客户端收到此包时调用
    public static void handle(final LoginMusicPacket packet, final IPayloadContext context) {
        context.enqueueWork(() -> {
            LoginMusic.LOGGER.info("准备播放音乐: {}", packet.musicID());

            // 只在客户端执行
            if (FMLEnvironment.dist == Dist.CLIENT) {
                ClientLoginEvent.PlayLoginMusic(packet.musicID());
            }
        });
    }
}