package com.xcharge.charger.data.bean.type;

public enum CONTENT_MEDIA_TYPE {
    text("text"),
    jpg("jpg"),
    png("png"),
    gif("gif"),
    webp("webp"),
    gifv("gif-v"),
    mp4("mp4");
    
    private String type;

    private CONTENT_MEDIA_TYPE(String type2) {
        this.type = null;
        this.type = type2;
    }

    public String getType() {
        return this.type;
    }

    public static CONTENT_MEDIA_TYPE valueBy(String type2) {
        if ("text".equals(type2)) {
            return text;
        }
        if ("jpg".equals(type2)) {
            return jpg;
        }
        if ("png".equals(type2)) {
            return png;
        }
        if ("gif".equals(type2)) {
            return gif;
        }
        if ("webp".equals(type2)) {
            return webp;
        }
        if ("gif-v".equals(type2)) {
            return gifv;
        }
        if ("mp4".equals(type2)) {
            return mp4;
        }
        return null;
    }

    public static String getFileSuffix(CONTENT_MEDIA_TYPE type2) {
        switch (m18x6599854d()[type2.ordinal()]) {
            case 1:
                return "txt";
            case 2:
            case 3:
            case 4:
            case 5:
            case 7:
                return type2.getType();
            case 6:
                return gif.getType();
            default:
                return null;
        }
    }
}
