package com.purplerat.prconverter.image;

public enum ImageFormats {
    PNG("png"),
    JPEG("jpeg"),
    WEBP("webp");
    private final String extension;
    ImageFormats(String extension){
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }
}
