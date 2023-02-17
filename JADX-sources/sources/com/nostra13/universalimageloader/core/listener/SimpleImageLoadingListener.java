package com.nostra13.universalimageloader.core.listener;

import android.graphics.Bitmap;
import android.view.View;
import com.nostra13.universalimageloader.core.assist.FailReason;

/* loaded from: classes.dex */
public class SimpleImageLoadingListener implements ImageLoadingListener {
    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
    public void onLoadingStarted(String imageUri, View view) {
    }

    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
    }

    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
    }

    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
    public void onLoadingCancelled(String imageUri, View view) {
    }
}
