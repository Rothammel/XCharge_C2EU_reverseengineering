package com.xcharge.charger.ui.c2.activity.advert;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.xcharge.charger.R;
import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.charger.data.bean.type.CONTENT_MEDIA_TYPE;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import java.util.List;
import pl.droidsonroids.gif.GifDrawable;

/* loaded from: classes.dex */
public class MyViewPagerAdapter extends PagerAdapter {
    private List<ContentItem> adInfos;
    private Context context;

    public MyViewPagerAdapter(Context context, List<ContentItem> adInfos) {
        this.adInfos = adInfos;
        this.context = context;
    }

    @Override // android.support.v4.view.PagerAdapter
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    @Override // android.support.v4.view.PagerAdapter
    public Object instantiateItem(ViewGroup container, int position) {
        ContentItem adInfo = this.adInfos.get(position);
        ImageView iv = new ImageView(this.context);
        iv.setBackgroundResource(R.drawable.ad_bg);
        try {
            String type = adInfo.getType();
            if (CONTENT_MEDIA_TYPE.jpg.getType().equals(type) || CONTENT_MEDIA_TYPE.png.getType().equals(type) || CONTENT_MEDIA_TYPE.webp.getType().equals(type)) {
                String localPath = adInfo.getLocalPath();
                if (!TextUtils.isEmpty(localPath) && Utils.fileIsExists(localPath)) {
                    Utils.loadImage(localPath, iv, null, this.context);
                }
            } else if (CONTENT_MEDIA_TYPE.gif.getType().equals(type) || CONTENT_MEDIA_TYPE.gifv.getType().equals(type)) {
                String localPath2 = adInfo.getLocalPath();
                if (!TextUtils.isEmpty(localPath2)) {
                    GifDrawable drawable = new GifDrawable(localPath2);
                    iv.setImageDrawable(drawable);
                }
            }
            container.addView(iv, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return iv;
    }

    @Override // android.support.v4.view.PagerAdapter
    public int getCount() {
        return this.adInfos.size();
    }

    @Override // android.support.v4.view.PagerAdapter
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }
}