package com.xcharge.charger.ui.c2.activity.advert;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xcharge.charger.R;
import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.charger.data.bean.type.ADVERT_POLICY;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.ui.c2.activity.fault.BaseDialog;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/* loaded from: classes.dex */
public class IdleAdDialog extends BaseDialog {
    private final int MSG_WHAT;
    private List<ContentItem> adInfos;
    private long apartTime;
    private Context context;
    private Handler handler;
    ImageView iv_ad;
    private List<ImageView> list;
    private int position;
    private Timer timer;
    ViewPager viewPager;

    public IdleAdDialog(Context context) {
        super(context, R.style.Dialog_Fullscreen);
        this.MSG_WHAT = 1;
        this.position = 0;
        this.adInfos = null;
        this.handler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.advert.IdleAdDialog.1
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        IdleAdDialog.this.viewPager.setCurrentItem(IdleAdDialog.this.position);
                        if (IdleAdDialog.this.position >= IdleAdDialog.this.adInfos.size()) {
                            IdleAdDialog.this.viewPager.setCurrentItem(0, false);
                            IdleAdDialog.this.position = 0;
                        }
                        IdleAdDialog.this.position++;
                        return;
                    default:
                        return;
                }
            }
        };
        this.context = context;
    }

    @Override // com.xcharge.charger.ui.c2.activity.fault.BaseDialog
    public void initView() {
        super.initView();
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View view = inflater.inflate(R.layout.dialog_idle, (ViewGroup) null);
        setContentView(view);
        this.viewPager = (ViewPager) findViewById(R.id.vp_ad);
        this.iv_ad = (ImageView) findViewById(R.id.iv_ad);
        this.list = new ArrayList();
        this.adInfos = RemoteSettingCacheProvider.getInstance().getAdvertContent(ADVERT_POLICY.idleAdvsite);
        for (int i = 0; i < this.adInfos.size(); i++) {
            ImageView imageView = new ImageView(this.context);
            ContentItem contentItem = this.adInfos.get(i);
            if (contentItem != null) {
                Utils.loadImage(contentItem.getLocalPath(), imageView, new ImageLoadingListener() { // from class: com.xcharge.charger.ui.c2.activity.advert.IdleAdDialog.2
                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingStarted(String arg0, View arg1) {
                    }

                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                        IdleAdDialog.this.dismiss();
                    }

                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                    }

                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingCancelled(String arg0, View arg1) {
                    }
                }, this.context);
                this.list.add(imageView);
            }
        }
        this.apartTime = this.adInfos.get(0).getDuration();
        this.timer = new Timer();
        this.timer.schedule(new TimerTask() { // from class: com.xcharge.charger.ui.c2.activity.advert.IdleAdDialog.3
            @Override // java.util.TimerTask, java.lang.Runnable
            public void run() {
                IdleAdDialog.this.handler.sendEmptyMessage(1);
            }
        }, 0L, this.apartTime * 1000);
        this.viewPager.setAdapter(new MyAdapter(this, null));
    }

    /* loaded from: classes.dex */
    private class MyAdapter extends PagerAdapter {
        private MyAdapter() {
        }

        /* synthetic */ MyAdapter(IdleAdDialog idleAdDialog, MyAdapter myAdapter) {
            this();
        }

        @Override // android.support.v4.view.PagerAdapter
        public int getCount() {
            Log.e("idleDialog", new StringBuilder(String.valueOf(IdleAdDialog.this.adInfos.size())).toString());
            return IdleAdDialog.this.list.size();
        }

        @Override // android.support.v4.view.PagerAdapter
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView iv = (ImageView) IdleAdDialog.this.list.get(position);
            ((ViewPager) container).addView(iv);
            return iv;
        }

        @Override // android.support.v4.view.PagerAdapter
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView((View) arg2);
        }

        @Override // android.support.v4.view.PagerAdapter
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override // android.support.v4.view.PagerAdapter
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        @Override // android.support.v4.view.PagerAdapter
        public Parcelable saveState() {
            return null;
        }

        @Override // android.support.v4.view.PagerAdapter
        public void startUpdate(View arg0) {
        }

        @Override // android.support.v4.view.PagerAdapter
        public void finishUpdate(View arg0) {
        }
    }

    @Override // com.xcharge.charger.ui.c2.activity.fault.BaseDialog, android.app.Dialog
    public void onBackPressed() {
        super.onBackPressed();
        this.timer.cancel();
        this.handler.removeMessages(1);
        dismiss();
    }
}
