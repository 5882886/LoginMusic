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
    public static void onGuiInit(ScreenEvent.Init.Post event) {
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
            } else {
                // 若找不到锚点按钮，使用绝对位置
                event.addListener(Button.builder(
                        Component.literal("♫"),
                        button -> Minecraft.getInstance().setScreen(
                                ConfigGUI.createConfigScreen(screen)
                        )
                ).bounds(5, 5, 20, 20).build());
            }
        }
    }

    // 查找"多人游戏"按钮的方法
    private static Button findMultiplayerButton(ScreenEvent.Init.Post event) {
        return event.getScreen().children().stream()
                .filter(widget -> widget instanceof Button)
                .map(widget -> (Button) widget)
                .filter(button -> {
                    // 通过按钮文本匹配（支持中英文）
                    Component message = button.getMessage();
                    String text = message.getString();
                    return text.contains("Multiplayer") || text.contains("多人游戏");
                })
                .findFirst()
                .orElse(null);
    }
}

