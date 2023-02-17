package com.xcharge.charger.p006ui.p009c2.activity.data;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;

/* renamed from: com.xcharge.charger.ui.c2.activity.data.UiBackgroundColorContentObserver */
public class UiBackgroundColorContentObserver extends ContentObserver {
    private LinearLayout layout = null;

    public UiBackgroundColorContentObserver(LinearLayout layout2, Handler handler) {
        super(handler);
        this.layout = layout2;
    }

    public void onChange(boolean selfChange, Uri uri) {
        Log.d("UiBackgroundColorContentObserver.onChange", "selfChange: " + selfChange + ", uri: " + uri.toString());
        super.onChange(selfChange, uri);
        if (this.layout != null) {
            Utils.customizeUiBgColor(this.layout);
        }
    }
}
