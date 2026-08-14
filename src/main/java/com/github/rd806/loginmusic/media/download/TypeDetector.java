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

    // 检测字符编码（增强版）
    public static String detectCharset(byte[] data) {
        if (data == null || data.length == 0) { return "UTF-8"; }
        // 检测 UTF-8 BOM
        if (data.length >= 3 && data[0] == (byte) 0xEF && data[1] == (byte) 0xBB && data[2] == (byte) 0xBF) { return "UTF-8"; }
        // 检测 UTF-16 BE BOM
        if (data.length >= 2 && data[0] == (byte) 0xFE && data[1] == (byte) 0xFF) { return "UTF-16BE"; }
        // 检测 UTF-16 LE BOM
        if (data.length >= 2 && data[0] == (byte) 0xFF && data[1] == (byte) 0xFE) { return "UTF-16LE"; }
        // 尝试判断是否为 GBK/GB2312
        // 简单检测：如果存在非 UTF-8 序列的字节，则认为是 GBK
        boolean isAscii = true;
        boolean hasChineseByte = false;
        // 只检测前1KB
        for (int i = 0; i < data.length && i < 1024; i++) {
            byte b = data[i];
            if (b < 0) {
                isAscii = false;
                // 检测是否可能是 GBK 编码（GBK 首字节范围 0x81-0xFE）
                if ((b & 0xFF) >= 0x81 && (b & 0xFF) <= 0xFE) { hasChineseByte = true; }
            }
        }
        if (isAscii) { return "US-ASCII"; }
        // 尝试 UTF-8 解码，检查是否有无效序列
        if (isValidUtf8(data)) { return "UTF-8"; }
        // 默认返回 GBK（中文环境下常见）
        return hasChineseByte ? "GBK" : "UTF-8";
    }

    // 检查是否为有效的 UTF-8 编码
    private static boolean isValidUtf8(byte[] data) {
        int i = 0;
        while (i < data.length) {
            byte b = data[i];
            if ((b & 0x80) == 0) {
                // ASCII 字符，1字节
                i++;
            } else if ((b & 0xE0) == 0xC0) {
                // 2字节 UTF-8
                if (i + 1 >= data.length) return false;
                if ((data[i+1] & 0xC0) != 0x80) return false;
                i += 2;
            } else if ((b & 0xF0) == 0xE0) {
                // 3字节 UTF-8
                if (i + 2 >= data.length) return false;
                if ((data[i+1] & 0xC0) != 0x80) return false;
                if ((data[i+2] & 0xC0) != 0x80) return false;
                i += 3;
            } else if ((b & 0xF8) == 0xF0) {
                // 4字节 UTF-8
                if (i + 3 >= data.length) return false;
                if ((data[i+1] & 0xC0) != 0x80) return false;
                if ((data[i+2] & 0xC0) != 0x80) return false;
                if ((data[i+3] & 0xC0) != 0x80) return false;
                i += 4;
            } else {
                // 无效的 UTF-8 序列
                return false;
            }
        }
        return true;
    }
}
