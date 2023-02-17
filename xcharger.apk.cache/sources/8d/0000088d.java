package com.xcharge.charger.ui.c2.activity.data;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;

/* loaded from: classes.dex */
public class UiBackgroundColorContentObserver extends ContentObserver {
    private LinearLayout layout;

    public UiBackgroundColorContentObserver(LinearLayout layout, Handler handler) {
        super(handler);
        this.layout = null;
        this.layout = layout;
    }

    @Override // android.database.ContentObserver
    public void onChange(boolean selfChange, Uri uri) {
        Log.d("UiBackgroundColorContentObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
        super.onChange(selfChange, uri);
        if (this.layout != null) {
            Utils.customizeUiBgColor(this.layout);
        }
    }
}