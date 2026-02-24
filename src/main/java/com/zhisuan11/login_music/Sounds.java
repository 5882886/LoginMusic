package com.zhisuan11.login_music;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.zhisuan11.login_music.LoginMusic.MODID;

public class Sounds {
    // 创建声音事件
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MODID);

    public static final RegistryObject<SoundEvent> LOGIN_MUSIC = SOUND_EVENTS.register("login_music", () -> SoundEvent.createVariableRangeEvent(ResourceLocation.tryParse(MODID + ":login_music")));

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

}
