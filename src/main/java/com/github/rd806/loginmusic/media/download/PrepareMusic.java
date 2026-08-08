package com.github.rd806.loginmusic.media.download;

import com.github.rd806.loginmusic.LoginMusic;
import com.github.rd806.loginmusic.media.PreparedAudio;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;

@OnlyIn(Dist.CLIENT)
public class PrepareMusic {

    public static TYPE type = TYPE.DEFAULT;

    public enum TYPE {
        SOUND_EVENT,
        PREPARED_AUDIO,
        DEFAULT
    }

    // 准备外部音乐
    public static PreparedAudio prepareAudio(ByteArrayInputStream inputStream) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputStream);
            // 转换格式
            AudioFormat sourceFormat = audioInputStream.getFormat();
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
                audioInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
            }
            DataLine.Info info = new DataLine.Info(Clip.class, targetFormat);
            if (!AudioSystem.isLineSupported(info)) {
                throw new UnsupportedAudioFileException("Audio format not supported");
            }
            // 预加载音频数据到字节数组，减少Clip.open()时间
            byte[] audioData = audioInputStream.readAllBytes();
            type = TYPE.PREPARED_AUDIO;
            LoginMusic.LOGGER.info("Audio data prepared!");
            return new PreparedAudio(audioData, targetFormat, info);
        } catch (UnsupportedAudioFileException e) {
            LoginMusic.LOGGER.error("Unsupported type: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Fail to play music");
            return null;
        }
    }
}
