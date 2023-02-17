package com.xcharge.charger.data.bean.type;

/* loaded from: classes.dex */
public enum CONTENT_MEDIA_TYPE {
    text("text"),
    jpg("jpg"),
    png("png"),
    gif("gif"),
    webp("webp"),
    gifv("gif-v"),
    mp4("mp4");
    
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CONTENT_MEDIA_TYPE;
    private String type;

    /* renamed from: values  reason: to resolve conflict with enum method */
    public static CONTENT_MEDIA_TYPE[] valuesCustom() {
        CONTENT_MEDIA_TYPE[] valuesCustom = values();
        int length = valuesCustom.length;
        CONTENT_MEDIA_TYPE[] content_media_typeArr = new CONTENT_MEDIA_TYPE[length];
        System.arraycopy(valuesCustom, 0, content_media_typeArr, 0, length);
        return content_media_typeArr;
    }

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CONTENT_MEDIA_TYPE() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CONTENT_MEDIA_TYPE;
        if (iArr == null) {
            iArr = new int[valuesCustom().length];
            try {
                iArr[gif.ordinal()] = 4;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[gifv.ordinal()] = 6;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[jpg.ordinal()] = 2;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[mp4.ordinal()] = 7;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[png.ordinal()] = 3;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[text.ordinal()] = 1;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[webp.ordinal()] = 5;
            } catch (NoSuchFieldError e7) {
            }
            $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CONTENT_MEDIA_TYPE = iArr;
        }
        return iArr;
    }

    CONTENT_MEDIA_TYPE(String type) {
        this.type = null;
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public static CONTENT_MEDIA_TYPE valueBy(String type) {
        if ("text".equals(type)) {
            return text;
        }
        if ("jpg".equals(type)) {
            return jpg;
        }
        if ("png".equals(type)) {
            return png;
        }
        if ("gif".equals(type)) {
            return gif;
        }
        if ("webp".equals(type)) {
            return webp;
        }
        if ("gif-v".equals(type)) {
            return gifv;
        }
        if ("mp4".equals(type)) {
            return mp4;
        }
        return null;
    }

    public static String getFileSuffix(CONTENT_MEDIA_TYPE type) {
        switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CONTENT_MEDIA_TYPE()[type.ordinal()]) {
            case 1:
                return "txt";
            case 2:
            case 3:
            case 4:
            case 5:
            case 7:
                return type.getType();
            case 6:
                return gif.getType();
            default:
                return null;
        }
    }
}
