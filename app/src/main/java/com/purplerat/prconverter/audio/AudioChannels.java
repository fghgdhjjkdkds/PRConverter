package com.purplerat.prconverter.audio;

import java.io.Serializable;

public enum AudioChannels implements Serializable {
    MONO("Mono",1),STEREO("Stereo",2);
    private final static String[] ENABLE_CHANNELS = new String[]{"Mono","Stereo"};
    private final String value;
    private final int intValue;
    AudioChannels(String value,int intValue) {
        this.value = value;
        this.intValue = intValue;
    }
    public String getValue() {
        return value;
    }

    public int getIntValue() {
        return intValue;
    }

    public static String[] getEnableChannels() {
        return ENABLE_CHANNELS;
    }
}
