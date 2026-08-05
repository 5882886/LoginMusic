package com.github.rd806.loginmusic.media;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.media.music.MusicEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

@OnlyIn(Dist.CLIENT)
public class PrepareMusic {

    public static TYPE type = TYPE.DEFAULT;

    public enum TYPE {
        SOUND_EVENT,
        PREPARED_AUDIO,
        DEFAULT
    }

    // 准备已注册到游戏内的音乐
    public static SoundEvent findMusic(MusicEntry entry) {
        ResourceLocation music = ResourceLocation.parse(entry.getMusicPath());
        SoundEvent soundEvent = ForgeRegistries.SOUND_EVENTS.getValue(music);
        if (soundEvent != null) {
            LoginMusic.LOGGER.info("Found audio music for {}: {}", entry.getMusicName(), soundEvent);
            type = TYPE.SOUND_EVENT;
            return soundEvent;
        }
        return null;
    }

    // 准备外部音乐
    public static PreparedAudio prepareAudio(MusicEntry entry) {
        File localFile = LoginMusic.MUSICS_DIR.resolve(entry.getMusicName()).toFile();
        AudioInputStream audioStream;
        // 加载音频流
        try {
            if (localFile.exists()) {
                audioStream = AudioSystem.getAudioInputStream(localFile);
            } else {
                URI uri = new URI(entry.getMusicPath());
                URL url = uri.toURL();
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                BufferedInputStream bufferedInputStream = new BufferedInputStream(connection.getInputStream());
                audioStream = AudioSystem.getAudioInputStream(bufferedInputStream);
            }
            // 转换格式
            AudioFormat sourceFormat = audioStream.getFormat();
            AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    sourceFormat.getSampleRate(),
                    16,
                    sourceFormat.getChannels(),
                    sourceFormat.getChannels() * 2,
                    sourceFormat.getSampleRate(),
                    false
            );
            if (!sourceFormat.matches(targetFormat)) {
                audioStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
            }
            DataLine.Info info = new DataLine.Info(Clip.class, targetFormat);
            if (!AudioSystem.isLineSupported(info)) {
                throw new UnsupportedAudioFileException("Audio format not supported");
            }
            // 预加载音频数据到字节数组，减少Clip.open()时间
            byte[] audioData = audioStream.readAllBytes();
            type = TYPE.PREPARED_AUDIO;
            return new PreparedAudio(audioData, targetFormat, info);
        } catch (UnsupportedAudioFileException e) {
            LoginMusic.LOGGER.error("Unsupported type: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Fail to play music: {} ", entry.getMusicName());
            return null;
        }
    }
}
