package com.zhisuan11.login_music.Network;

import com.zhisuan11.login_music.Sounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;

public class ClientPacketHandler {
    public static void handleLoginMusic(LoginMusicPacket packet) {
        Minecraft.getInstance().getSoundManager().play(
                SimpleSoundInstance.forUI(Sounds.LOGIN_MUSIC.get(), 1.0f, 1.0f)
        );
    }
}
