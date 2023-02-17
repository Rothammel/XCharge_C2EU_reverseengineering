package pl.droidsonroids.gif;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;
import java.io.IOException;

/* loaded from: classes.dex */
public class GifImageView extends ImageView {
    static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    public GifImageView(Context context) {
        super(context);
    }

    public GifImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        trySetGifDrawable(attrs, getResources());
    }

    public GifImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        trySetGifDrawable(attrs, getResources());
    }

    @Override // android.widget.ImageView
    public void setImageResource(int resId) {
        setResource(true, resId, getResources());
    }

    @Override // android.view.View
    public void setBackgroundResource(int resId) {
        setResource(false, resId, getResources());
    }

    void trySetGifDrawable(AttributeSet attrs, Resources res) {
        if (attrs != null && res != null && !isInEditMode()) {
            int resId = attrs.getAttributeResourceValue(ANDROID_NS, "src", -1);
            if (resId > 0 && "drawable".equals(res.getResourceTypeName(resId))) {
                setResource(true, resId, res);
            }
            int resId2 = attrs.getAttributeResourceValue(ANDROID_NS, "background", -1);
            if (resId2 > 0 && "drawable".equals(res.getResourceTypeName(resId2))) {
                setResource(false, resId2, res);
            }
        }
    }

    @TargetApi(16)
    void setResource(boolean isSrc, int resId, Resources res) {
        try {
            GifDrawable d = new GifDrawable(res, resId);
            if (isSrc) {
                setImageDrawable(d);
            } else if (Build.VERSION.SDK_INT >= 16) {
                setBackground(d);
            } else {
                setBackgroundDrawable(d);
            }
        } catch (Resources.NotFoundException | IOException e) {
            if (isSrc) {
                super.setImageResource(resId);
            } else {
                super.setBackgroundResource(resId);
            }
        }
    }

    @Override // android.widget.ImageView
    public void setImageURI(Uri uri) {
        if (uri != null) {
            try {
                setImageDrawable(new GifDrawable(getContext().getContentResolver(), uri));
                return;
            } catch (IOException e) {
            }
        }
        super.setImageURI(uri);
    }
}
