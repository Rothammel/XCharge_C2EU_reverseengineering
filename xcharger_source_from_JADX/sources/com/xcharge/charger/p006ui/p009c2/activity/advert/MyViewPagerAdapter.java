package com.xcharge.charger.p006ui.p009c2.activity.advert;

import android.content.Context;
import android.support.p000v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.charger.data.bean.type.CONTENT_MEDIA_TYPE;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;
import java.util.List;
import p015pl.droidsonroids.gif.GifDrawable;

/* renamed from: com.xcharge.charger.ui.c2.activity.advert.MyViewPagerAdapter */
public class MyViewPagerAdapter extends PagerAdapter {
    private List<ContentItem> adInfos;
    private Context context;

    public MyViewPagerAdapter(Context context2, List<ContentItem> adInfos2) {
        this.adInfos = adInfos2;
        this.context = context2;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public Object instantiateItem(ViewGroup container, int position) {
        ContentItem adInfo = this.adInfos.get(position);
        ImageView iv = new ImageView(this.context);
        iv.setBackgroundResource(C0221R.C0222drawable.ad_bg);
        try {
            String type = adInfo.getType();
            if (CONTENT_MEDIA_TYPE.jpg.getType().equals(type) || CONTENT_MEDIA_TYPE.png.getType().equals(type) || CONTENT_MEDIA_TYPE.webp.getType().equals(type)) {
                String localPath = adInfo.getLocalPath();
                if (!TextUtils.isEmpty(localPath) && Utils.fileIsExists(localPath)) {
                    Utils.loadImage(localPath, iv, (ImageLoadingListener) null, this.context);
                }
            } else if (CONTENT_MEDIA_TYPE.gif.getType().equals(type) || CONTENT_MEDIA_TYPE.gifv.getType().equals(type)) {
                String localPath2 = adInfo.getLocalPath();
                if (!TextUtils.isEmpty(localPath2)) {
                    iv.setImageDrawable(new GifDrawable(localPath2));
                }
            }
            container.addView(iv, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return iv;
    }

    public int getCount() {
        return this.adInfos.size();
    }

    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }
}
