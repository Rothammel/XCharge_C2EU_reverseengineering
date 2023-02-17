package com.nostra13.universalimageloader.utils;

import android.opengl.GLES10;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.assist.ViewScaleType;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;

/* loaded from: classes.dex */
public final class ImageSizeUtils {
    private static final int DEFAULT_MAX_BITMAP_DIMENSION = 2048;
    private static ImageSize maxBitmapSize;

    static {
        int[] maxTextureSize = new int[1];
        GLES10.glGetIntegerv(3379, maxTextureSize, 0);
        int maxBitmapDimension = Math.max(maxTextureSize[0], 2048);
        maxBitmapSize = new ImageSize(maxBitmapDimension, maxBitmapDimension);
    }

    private ImageSizeUtils() {
    }

    public static ImageSize defineTargetSizeForView(ImageAware imageAware, ImageSize maxImageSize) {
        int width = imageAware.getWidth();
        if (width <= 0) {
            width = maxImageSize.getWidth();
        }
        int height = imageAware.getHeight();
        if (height <= 0) {
            height = maxImageSize.getHeight();
        }
        return new ImageSize(width, height);
    }

    public static int computeImageSampleSize(ImageSize srcSize, ImageSize targetSize, ViewScaleType viewScaleType, boolean powerOf2Scale) {
        int srcWidth = srcSize.getWidth();
        int srcHeight = srcSize.getHeight();
        int targetWidth = targetSize.getWidth();
        int targetHeight = targetSize.getHeight();
        int scale = 1;
        switch (viewScaleType) {
            case FIT_INSIDE:
                if (powerOf2Scale) {
                    int halfWidth = srcWidth / 2;
                    int halfHeight = srcHeight / 2;
                    while (true) {
                        if (halfWidth / scale <= targetWidth && halfHeight / scale <= targetHeight) {
                            break;
                        } else {
                            scale *= 2;
                        }
                    }
                } else {
                    scale = Math.max(srcWidth / targetWidth, srcHeight / targetHeight);
                    break;
                }
                break;
            case CROP:
                if (powerOf2Scale) {
                    int halfWidth2 = srcWidth / 2;
                    int halfHeight2 = srcHeight / 2;
                    while (halfWidth2 / scale > targetWidth && halfHeight2 / scale > targetHeight) {
                        scale *= 2;
                    }
                } else {
                    scale = Math.min(srcWidth / targetWidth, srcHeight / targetHeight);
                    break;
                }
                break;
        }
        if (scale < 1) {
            scale = 1;
        }
        return considerMaxTextureSize(srcWidth, srcHeight, scale, powerOf2Scale);
    }

    private static int considerMaxTextureSize(int srcWidth, int srcHeight, int scale, boolean powerOf2) {
        int maxWidth = maxBitmapSize.getWidth();
        int maxHeight = maxBitmapSize.getHeight();
        while (true) {
            if (srcWidth / scale > maxWidth || srcHeight / scale > maxHeight) {
                if (powerOf2) {
                    scale *= 2;
                } else {
                    scale++;
                }
            } else {
                return scale;
            }
        }
    }

    public static int computeMinImageSampleSize(ImageSize srcSize) {
        int srcWidth = srcSize.getWidth();
        int srcHeight = srcSize.getHeight();
        int targetWidth = maxBitmapSize.getWidth();
        int targetHeight = maxBitmapSize.getHeight();
        int widthScale = (int) Math.ceil(srcWidth / targetWidth);
        int heightScale = (int) Math.ceil(srcHeight / targetHeight);
        return Math.max(widthScale, heightScale);
    }

    public static float computeImageScale(ImageSize srcSize, ImageSize targetSize, ViewScaleType viewScaleType, boolean stretch) {
        int destWidth;
        int destHeight;
        int srcWidth = srcSize.getWidth();
        int srcHeight = srcSize.getHeight();
        int targetWidth = targetSize.getWidth();
        int targetHeight = targetSize.getHeight();
        float widthScale = srcWidth / targetWidth;
        float heightScale = srcHeight / targetHeight;
        if ((viewScaleType == ViewScaleType.FIT_INSIDE && widthScale >= heightScale) || (viewScaleType == ViewScaleType.CROP && widthScale < heightScale)) {
            destWidth = targetWidth;
            destHeight = (int) (srcHeight / widthScale);
        } else {
            destWidth = (int) (srcWidth / heightScale);
            destHeight = targetHeight;
        }
        if ((stretch || destWidth >= srcWidth || destHeight >= srcHeight) && (!stretch || destWidth == srcWidth || destHeight == srcHeight)) {
            return 1.0f;
        }
        float scale = destWidth / srcWidth;
        return scale;
    }
}