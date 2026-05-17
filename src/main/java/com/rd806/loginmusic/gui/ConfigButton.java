package com.rd806.loginmusic.gui;

import com.rd806.loginmusic.LoginMusic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

// 创建配置入口按钮
@Mod.EventBusSubscriber(modid = LoginMusic.MODID, value = Dist.CLIENT)
public class ConfigButton {

    @SubscribeEvent
    public static void onGuiInit(ScreenEvent.Init event) {
        Screen screen = event.getScreen();
        // 仅在主菜单（TitleScreen）添加按钮
        if (screen instanceof TitleScreen && ModList.get().isLoaded("cloth_config")) {
            Button multiplayerButton = findMultiplayerButton(event);
            if (multiplayerButton != null) {
                // 获取"多人游戏"按钮的位置
                int multiX = multiplayerButton.getX();
                int multiY = multiplayerButton.getY();
                int multiHeight = multiplayerButton.getHeight();

                // 在按钮左侧放置
                int configButtonWidth = 20;
                int configButtonX = multiX - configButtonWidth - 5;  // 左侧5像素间距

                // 添加一个按钮，点击后打开配置界面
                event.addListener(Button.builder(
                        Component.literal("♫"),
                        button -> Minecraft.getInstance().setScreen(
                                ConfigGUI.createConfigScreen(screen)
                        )
                ).bounds(configButtonX, multiY, configButtonWidth, multiHeight).build());
            }
        }
    }

    private static Button findMultiplayerButton(ScreenEvent.Init event) {
        TitleScreen screen = (TitleScreen) event.getScreen();

        // 原版主菜单按钮的标准位置和顺序
        // "单人游戏": (宽度/2 - 100, 高度/4 + 48)
        // "多人游戏": (宽度/2 - 100, 高度/4 + 72)
        // "设置": (宽度/2 - 100, 高度/4 + 96)

        int screenWidth = screen.width;
        int screenHeight = screen.height;
        int expectedY = screenHeight / 4 + 72;  // 多人游戏的Y坐标
        int expectedX = screenWidth / 2 - 100;  // 多人游戏的X坐标

        return event.getScreen().children().stream()
                .filter(widget -> widget instanceof Button)
                .map(widget -> (Button) widget)
                .filter(button -> button.getX() == expectedX && button.getY() == expectedY)
                .findFirst()
                .orElse(null);
    }
}

