package com.purplerat.prconverter.video;

import java.io.Serializable;

public enum VideoFormats implements Serializable {
    MP4("mp4"),
    WEBM("webm"),
    MKV("mkv"),
    MOV("mov");
    private final static String[] ENABLE_VIDEO_FORMATS = new String[]{"mp4","webm","mkv","mov"};
    private final String value;
    VideoFormats(String value){
        this.value = value;
    }
    public String getValue() {
        return value;
    }

    public static String[] getEnableVideoFormats() {
        return ENABLE_VIDEO_FORMATS;
    }
}
