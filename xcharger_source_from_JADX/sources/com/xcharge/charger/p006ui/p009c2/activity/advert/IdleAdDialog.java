package com.xcharge.charger.p006ui.p009c2.activity.advert;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.p000v4.view.PagerAdapter;
import android.support.p000v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.charger.data.bean.type.ADVERT_POLICY;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.p006ui.p009c2.activity.fault.BaseDialog;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/* renamed from: com.xcharge.charger.ui.c2.activity.advert.IdleAdDialog */
public class IdleAdDialog extends BaseDialog {
    private final int MSG_WHAT = 1;
    /* access modifiers changed from: private */
    public List<ContentItem> adInfos = null;
    private long apartTime;
    private Context context;
    /* access modifiers changed from: private */
    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    IdleAdDialog.this.viewPager.setCurrentItem(IdleAdDialog.this.position);
                    if (IdleAdDialog.this.position >= IdleAdDialog.this.adInfos.size()) {
                        IdleAdDialog.this.viewPager.setCurrentItem(0, false);
                        IdleAdDialog.this.position = 0;
                    }
                    IdleAdDialog idleAdDialog = IdleAdDialog.this;
                    idleAdDialog.position = idleAdDialog.position + 1;
                    return;
                default:
                    return;
            }
        }
    };
    ImageView iv_ad;
    /* access modifiers changed from: private */
    public List<ImageView> list;
    /* access modifiers changed from: private */
    public int position = 0;
    private Timer timer;
    ViewPager viewPager;

    public IdleAdDialog(Context context2) {
        super(context2, C0221R.style.Dialog_Fullscreen);
        this.context = context2;
    }

    public void initView() {
        super.initView();
        setContentView(LayoutInflater.from(this.context).inflate(C0221R.layout.dialog_idle, (ViewGroup) null));
        this.viewPager = (ViewPager) findViewById(C0221R.C0223id.vp_ad);
        this.iv_ad = (ImageView) findViewById(C0221R.C0223id.iv_ad);
        this.list = new ArrayList();
        this.adInfos = RemoteSettingCacheProvider.getInstance().getAdvertContent(ADVERT_POLICY.idleAdvsite);
        for (int i = 0; i < this.adInfos.size(); i++) {
            ImageView imageView = new ImageView(this.context);
            ContentItem contentItem = this.adInfos.get(i);
            if (contentItem != null) {
                Utils.loadImage(contentItem.getLocalPath(), imageView, new ImageLoadingListener() {
                    public void onLoadingStarted(String arg0, View arg1) {
                    }

                    public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                        IdleAdDialog.this.dismiss();
                    }

                    public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                    }

                    public void onLoadingCancelled(String arg0, View arg1) {
                    }
                }, this.context);
                this.list.add(imageView);
            }
        }
        this.apartTime = this.adInfos.get(0).getDuration();
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() {
            public void run() {
                IdleAdDialog.this.handler.sendEmptyMessage(1);
            }
        }, 0, this.apartTime * 1000);
        this.viewPager.setAdapter(new MyAdapter(this, (MyAdapter) null));
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.advert.IdleAdDialog$MyAdapter */
    private class MyAdapter extends PagerAdapter {
        private MyAdapter() {
        }

        /* synthetic */ MyAdapter(IdleAdDialog idleAdDialog, MyAdapter myAdapter) {
            this();
        }

        public int getCount() {
            Log.e("idleDialog", new StringBuilder(String.valueOf(IdleAdDialog.this.adInfos.size())).toString());
            return IdleAdDialog.this.list.size();
        }

        public Object instantiateItem(ViewGroup container, int position) {
            ImageView iv = (ImageView) IdleAdDialog.this.list.get(position);
            ((ViewPager) container).addView(iv);
            return iv;
        }

        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        public Parcelable saveState() {
            return null;
        }

        public void startUpdate(View arg0) {
        }

        public void finishUpdate(View arg0) {
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        this.timer.cancel();
        this.handler.removeMessages(1);
        dismiss();
    }
}
