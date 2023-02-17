package p015pl.droidsonroids.gif;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageButton;
import java.io.IOException;

/* renamed from: pl.droidsonroids.gif.GifImageButton */
public class GifImageButton extends ImageButton {
    public GifImageButton(Context context) {
        super(context);
    }

    public GifImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        trySetGifDrawable(attrs, getResources());
    }

    public GifImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        trySetGifDrawable(attrs, getResources());
    }

    public void setImageResource(int resId) {
        setResource(true, resId, getResources());
    }

    public void setBackgroundResource(int resId) {
        setResource(false, resId, getResources());
    }

    /* access modifiers changed from: package-private */
    public void trySetGifDrawable(AttributeSet attrs, Resources res) {
        if (attrs != null && res != null && !isInEditMode()) {
            int resId = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "src", -1);
            if (resId > 0 && "drawable".equals(res.getResourceTypeName(resId))) {
                setResource(true, resId, res);
            }
            int resId2 = attrs.getAttributeResourceValue("http://schemas.android.com/apk/res/android", "background", -1);
            if (resId2 > 0 && "drawable".equals(res.getResourceTypeName(resId2))) {
                setResource(false, resId2, res);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @TargetApi(16)
    public void setResource(boolean isSrc, int resId, Resources res) {
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
