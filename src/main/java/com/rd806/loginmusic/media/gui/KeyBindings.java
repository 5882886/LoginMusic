package com.rd806.loginmusic.media.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.rd806.loginmusic.LoginMusic;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = LoginMusic.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyBindings {
    // 快捷键：默认按 O 键打开配置界面
    public static final String KEY_CATEGORY = "key.category.loginmusic";
    public static final String KEY_OPEN_CONFIG = "key.loginmusic.open_config";

    public static KeyMapping openConfigKey = new KeyMapping(
            KEY_OPEN_CONFIG,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            KEY_CATEGORY
    );

    // 注册快捷键
    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(openConfigKey);
    }

    // 监听按键事件
    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        // 仅在按下快捷键时打开配置界面
        if (openConfigKey.consumeClick()) {
            Minecraft.getInstance().setScreen(
                    ConfigGUI.createConfigScreen(Minecraft.getInstance().screen)
            );
        }
    }
}
