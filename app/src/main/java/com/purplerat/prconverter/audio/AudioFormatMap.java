package com.purplerat.prconverter.audio;

import java.util.HashMap;
import java.util.Map;

public class AudioFormatMap {
    private static final Map<String,AudioFormats> audioFormatsMap = new HashMap<>();
    static{
        audioFormatsMap.put("mp3",AudioFormats.MP3);
        audioFormatsMap.put("m4a",AudioFormats.M4A);
        audioFormatsMap.put("aac",AudioFormats.AAC);
        audioFormatsMap.put("flac",AudioFormats.FLAC);
        audioFormatsMap.put("ogg",AudioFormats.OGG);
    }
    public static AudioFormats getAudioFormat(String key){
        return audioFormatsMap.get(key);
    }
}
