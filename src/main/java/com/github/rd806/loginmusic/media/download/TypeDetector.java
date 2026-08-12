package com.github.rd806.loginmusic.media.download;

import com.github.rd806.loginmusic.LoginMusic;

public class TypeDetector {

    // 检查是否为音频文件
    public static boolean isAudioMagic(byte[] header, int length) {
        if (length < 4) return false;
        // MP3 (ID3v2)
        if (header[0] == 0x49 && header[1] == 0x44 && header[2] == 0x33) {
            LoginMusic.LOGGER.info("MP3 file");
            return true;
        }
        // MP3帧头
        int sync = ((header[0] & 0xFF) << 4) | ((header[1] & 0xFF) >> 4);
        if (sync == 0xFFF || sync == 0xFFE) {
            LoginMusic.LOGGER.info("MP3 file");
            return true;
        }
        // WAV: "RIFF"
        if (header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46) {
            LoginMusic.LOGGER.info("WAV file");
            return true;
        }
        // FLAC: "fLaC"
        if (header[0] == 0x66 && header[1] == 0x4C && header[2] == 0x61 && header[3] == 0x43) {
            LoginMusic.LOGGER.info("FLAC file");
            return true;
        }
        // OGG: "OggS"
        if (header[0] == 0x4F && header[1] == 0x67 && header[2] == 0x67 && header[3] == 0x53) {
            LoginMusic.LOGGER.info("Ogg file");
            return true;
        }
        // M4A: ftyp box
        if (length >= 8 && header[4] == 0x66 && header[5] == 0x74 && header[6] == 0x79 && header[7] == 0x70) {
            LoginMusic.LOGGER.info("M4A file");
            return true;
        }
        // AMR: "#!AMR"
        if (length >= 5 && header[0] == 0x23 && header[1] == 0x21 &&
                header[2] == 0x41 && header[3] == 0x4D && header[4] == 0x52) {
            LoginMusic.LOGGER.info("AMR file");
            return true;
        }
        // MIDI: "MThd"
        if (header[0] == 0x4D && header[1] == 0x54 && header[2] == 0x68 && header[3] == 0x64) {
            LoginMusic.LOGGER.info("MTDI file");
            return true;
        }
        return false;
    }
}
