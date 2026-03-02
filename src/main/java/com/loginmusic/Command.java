package com.loginmusic;

import com.loginmusic.music.MusicConfig;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Command {

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("loginmusic")
                //  /loginmusic reload
                .then(Commands.literal("reload")
                    .requires(source -> source.hasPermission(2))
                    .executes(context -> {
                        try {
                            MusicConfig.loadFromConfig();
                            context.getSource().sendSuccess(
                                    () -> Component.translatable(LoginMusic.MODID + ".commands.reload.success"),
                                    true
                            );
                            return 1;
                        } catch (Exception e) {
                            context.getSource().sendFailure(
                                    Component.translatable(LoginMusic.MODID + ".commands.reload.fail", e.getMessage())
                            );
                            return 0;
                        }
                    })
                )
        );
    }
}