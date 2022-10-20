package com.purplerat.prconverter.video;

import java.util.HashMap;
import java.util.Map;

public class VideoFormatsMap {
    private static final Map<String, VideoFormats> videoFormatMap = new HashMap<>();
    static{
        videoFormatMap.put("mp4",VideoFormats.MP4);
        videoFormatMap.put("webm",VideoFormats.WEBM);
        videoFormatMap.put("mkv",VideoFormats.MKV);
        videoFormatMap.put("mov",VideoFormats.MOV);
    }
    public static VideoFormats getVideoFormat(String key){
        System.out.println(key);
        return videoFormatMap.get(key);
    }
}
