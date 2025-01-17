package com.purplerat.prconverter.audio;

import java.io.Serializable;

public enum AudioFormats implements Serializable {
    MP3("mp3"),
    M4A("m4a"),
    AAC("aac"),
    FLAC("flac"),
    OGG("ogg");
    private static final String[] ENABLE_AUDIO_FORMATS = {"mp3","m4a","aac","flac","ogg"};
    private final String value;
    AudioFormats(String value){
        this.value = value;
    }
    public String getValue() {
        return value;
    }

    public static String[] getEnableAudioFormats() {
        return ENABLE_AUDIO_FORMATS;
    }
}
