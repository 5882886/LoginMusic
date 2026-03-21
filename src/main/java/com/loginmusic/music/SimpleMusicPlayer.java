package com.loginmusic.music;

import com.loginmusic.LoginMusic;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.sound.sampled.*;
import java.io.File;

@OnlyIn(Dist.CLIENT)
public class SimpleMusicPlayer {
    private static final Minecraft mc = Minecraft.getInstance();

    private static Clip currentClip;
    private static String currentMusicId;
    private static boolean isPlaying = false;

    private SimpleMusicPlayer() {}

    static {
        AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
        LoginMusic.LOGGER.info("Supported music file: ");
        for (AudioFileFormat.Type type : types) {
            LoginMusic.LOGGER.info("  - {}", type.getExtension());
        }

        // 检查MP3 SPI是否加载
        try {
            Class.forName("javazoom.spi.mpeg.sampled.file.MpegAudioFileReader");
            LoginMusic.LOGGER.info("MP3 support loaded!");
        } catch (ClassNotFoundException e) {
            LoginMusic.LOGGER.warn("MP3 support not found!");
        }
    }

    // 播放音乐
    public static void playMusic(String musicId, String musicName) {
        try {
            LoginMusic.LOGGER.info("Playing: {}", musicId);
            stopCurrentMusic();

            currentMusicId = musicId;
            isPlaying = true;

            File localFile = LoginMusic.CACHE_DIR.resolve(musicName).toFile();

            if (localFile.exists()) {
                // 获取音频输入流
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(localFile);
                AudioFormat sourceFormat = audioStream.getFormat();

                // 转换为PCM格式（如果需要）
                AudioFormat targetFormat = new AudioFormat(
                        AudioFormat.Encoding.PCM_SIGNED,
                        sourceFormat.getSampleRate(),
                        16,
                        sourceFormat.getChannels(),
                        sourceFormat.getChannels() * 2,
                        sourceFormat.getSampleRate(),
                        false
                );

                // 如果格式不匹配，进行转换
                if (!sourceFormat.matches(targetFormat)) {
                    LoginMusic.LOGGER.info("Changing format...");
                    audioStream = AudioSystem.getAudioInputStream(targetFormat, audioStream);
                }

                DataLine.Info info = new DataLine.Info(Clip.class, targetFormat);

                if (!AudioSystem.isLineSupported(info)) {
                    LoginMusic.LOGGER.error("Unsupported music file: {}", musicName);
                    return;
                }

                Clip clip = (Clip) AudioSystem.getLine(info);

                // 添加播放完成监听
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                        if (currentClip == clip) {
                            currentClip = null;
                            currentMusicId = null;
                            isPlaying = false;
                            // 播放结束通知
                            mc.execute(() -> {
                                if (Minecraft.getInstance().player != null) {
                                    Minecraft.getInstance().player.displayClientMessage(
                                            Component.translatable( LoginMusic.MODID + ".message.play_ended", musicName),
                                            false
                                    );
                                }
                            });
                        }
                    }
                });

                clip.open(audioStream);
                // 在MC线程中执行
                mc.execute(() -> {
                    currentClip = clip;
                    clip.start();
                    isPlaying = true;
                    // 通知玩家
                    if (Minecraft.getInstance().player != null) {
                        Minecraft.getInstance().player.displayClientMessage(
                                Component.translatable(LoginMusic.MODID + ".message.play_music", musicName),
                                true
                        );
                    }
                });

                LoginMusic.LOGGER.info("Playing music: {}", musicName);
            } else {
                LoginMusic.LOGGER.error("Music not found!");
            }
        } catch (UnsupportedAudioFileException e) {
            LoginMusic.LOGGER.error("Unsupported type: {}", e.getMessage());
        } catch (LineUnavailableException e) {
            LoginMusic.LOGGER.error("Not available: {}", e.getMessage());
        } catch (Exception e) {
            LoginMusic.LOGGER.error("Fail to play music: {} ", musicName);
        }
    }

    // 停止播放
    public static void stopCurrentMusic() {
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
            currentClip = null;
            currentMusicId = null;
            isPlaying = false;
        }
    }

    // 获取播放状态
    public static boolean isStopped() { return !isPlaying; }
}
