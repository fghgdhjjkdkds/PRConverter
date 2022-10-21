package com.purplerat.prconverter.audio;

import java.util.HashMap;
import java.util.Map;

public class AudioChannelsMap {
    private static final Map<String,AudioChannels> audioChannelsMap = new HashMap<>();
    static{
        audioChannelsMap.put("Mono",AudioChannels.MONO);
        audioChannelsMap.put("Stereo",AudioChannels.STEREO);
    }
    public static AudioChannels getAudioChannel(String key){
        return audioChannelsMap.get(key);
    }
}
