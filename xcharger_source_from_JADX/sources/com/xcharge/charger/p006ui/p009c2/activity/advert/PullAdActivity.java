package com.xcharge.charger.p006ui.p009c2.activity.advert;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.p000v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.charger.data.bean.type.ADVERT_POLICY;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CONTENT_MEDIA_TYPE;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.nfc.NFCChargeBalanceActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.online.ChargeCostActivity;
import com.xcharge.charger.p006ui.p009c2.activity.data.Variate;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;
import java.util.List;
import p015pl.droidsonroids.gif.GifDrawable;

/* renamed from: com.xcharge.charger.ui.c2.activity.advert.PullAdActivity */
public class PullAdActivity extends BaseActivity {
    private final int MSG_TIME_END = 2;
    private final int MSG_TIME_REPEAT = 1;
    /* access modifiers changed from: private */
    public List<ContentItem> adInfos = null;
    /* access modifiers changed from: private */
    public long curTime = 0;
    /* access modifiers changed from: private */
    public boolean isOnlyone = true;
    ImageView iv_ad;
    private int location = 0;
    AutoScrollViewPager mAutoScrollViewPager;
    private Handler mhHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    PullAdActivity.this.setTimerText();
                    if (PullAdActivity.this.curTime <= 1) {
                        sendEmptyMessage(2);
                        return;
                    }
                    PullAdActivity pullAdActivity = PullAdActivity.this;
                    pullAdActivity.curTime = pullAdActivity.curTime - 1;
                    sendEmptyMessageDelayed(1, 1000);
                    return;
                case 2:
                    if (!PullAdActivity.this.isOnlyone) {
                        PullAdActivity.this.mAutoScrollViewPager.stopAutoScroll();
                    }
                    PullAdActivity.this.exit();
                    return;
                default:
                    return;
            }
        }
    };
    private long totalTime = 0;
    TextView tv_timer;
    VideoView vv_ad;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0221R.layout.activity_ad);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, false);
    }

    /* access modifiers changed from: protected */
    public void initView() {
        this.mAutoScrollViewPager = (AutoScrollViewPager) findViewById(C0221R.C0223id.autoViewPager);
        this.tv_timer = (TextView) findViewById(C0221R.C0223id.tv_timer);
        this.iv_ad = (ImageView) findViewById(C0221R.C0223id.iv_ad);
        this.vv_ad = (VideoView) findViewById(C0221R.C0223id.vv_ad);
        initData();
        try {
            if (this.isOnlyone) {
                this.mAutoScrollViewPager.setVisibility(8);
                String type = this.adInfos.get(this.location).getType();
                if (CONTENT_MEDIA_TYPE.jpg.getType().equals(type) || CONTENT_MEDIA_TYPE.png.getType().equals(type) || CONTENT_MEDIA_TYPE.webp.getType().equals(type)) {
                    this.iv_ad.setVisibility(0);
                    this.vv_ad.setVisibility(8);
                    String localPath = this.adInfos.get(this.location).getLocalPath();
                    if (!TextUtils.isEmpty(localPath) || Utils.fileIsExists(localPath)) {
                        Utils.loadImage(localPath, this.iv_ad, new ImageLoadingListener() {
                            public void onLoadingStarted(String arg0, View arg1) {
                            }

                            public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                                PullAdActivity.this.exit();
                            }

                            public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                            }

                            public void onLoadingCancelled(String arg0, View arg1) {
                            }
                        }, this);
                        this.totalTime = this.adInfos.get(this.location).getDuration();
                    } else {
                        exit();
                    }
                } else if (CONTENT_MEDIA_TYPE.gif.getType().equals(type) || CONTENT_MEDIA_TYPE.gifv.getType().equals(type)) {
                    this.iv_ad.setVisibility(0);
                    this.vv_ad.setVisibility(8);
                    String localPath2 = this.adInfos.get(this.location).getLocalPath();
                    if (!TextUtils.isEmpty(localPath2) || Utils.fileIsExists(localPath2)) {
                        try {
                            GifDrawable drawable = new GifDrawable(localPath2);
                            this.iv_ad.setImageDrawable(drawable);
                            if (drawable.getDuration() > 0) {
                                this.totalTime = (long) (drawable.getDuration() / 1000);
                            } else {
                                this.totalTime = this.adInfos.get(this.location).getDuration();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        exit();
                    }
                } else if (CONTENT_MEDIA_TYPE.mp4.getType().equals(type)) {
                    this.iv_ad.setVisibility(8);
                    this.vv_ad.setVisibility(0);
                    String localPath3 = this.adInfos.get(this.location).getLocalPath();
                    if (!TextUtils.isEmpty(localPath3) || Utils.fileIsExists(localPath3)) {
                        this.vv_ad.setVideoURI(Uri.parse(localPath3));
                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(localPath3);
                        String duration = mmr.extractMetadata(9);
                        this.vv_ad.start();
                        this.vv_ad.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                            public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                                PullAdActivity.this.exit();
                                return false;
                            }
                        });
                        if (TextUtils.isEmpty(duration) || Integer.parseInt(duration) <= 0) {
                            this.totalTime = this.adInfos.get(this.location).getDuration();
                        } else {
                            this.totalTime = (long) (Integer.parseInt(duration) / 1000);
                        }
                    } else {
                        exit();
                    }
                }
            } else {
                this.iv_ad.setVisibility(8);
                this.vv_ad.setVisibility(8);
                this.mAutoScrollViewPager.setVisibility(0);
                Utils.initViewPagerScroll(this.mAutoScrollViewPager);
                for (int i = 0; i < this.adInfos.size(); i++) {
                    this.totalTime = this.adInfos.get(i).getDuration() + this.totalTime;
                }
                this.mAutoScrollViewPager.setInterval(this.adInfos.get(0).getDuration() * 1000);
                this.mAutoScrollViewPager.setDirection(1);
                this.mAutoScrollViewPager.setCycle(true);
                this.mAutoScrollViewPager.setAutoScrollDurationFactor(1.0d);
                this.mAutoScrollViewPager.setAdapter(new MyViewPagerAdapter(this, this.adInfos));
                this.mAutoScrollViewPager.startAutoScroll();
                final int size = this.adInfos.size();
                this.mAutoScrollViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    public void onPageSelected(int p) {
                        PullAdActivity.this.mAutoScrollViewPager.setInterval(((ContentItem) PullAdActivity.this.adInfos.get(p % size)).getDuration() * 1000);
                    }

                    public void onPageScrolled(int arg0, float arg1, int arg2) {
                    }

                    public void onPageScrollStateChanged(int arg0) {
                    }
                });
            }
            this.curTime = this.totalTime;
            setTimerText();
            this.curTime--;
            this.mhHandler.sendEmptyMessageDelayed(1, 1000);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private void initData() {
        if (RemoteSettingCacheProvider.getInstance().getAdvertContent(ADVERT_POLICY.pullAdvsite) == null) {
            exit();
            return;
        }
        this.adInfos = RemoteSettingCacheProvider.getInstance().getAdvertContent(ADVERT_POLICY.pullAdvsite);
        if (pullAdCount(this.adInfos) == 1) {
            this.isOnlyone = true;
        } else {
            this.isOnlyone = false;
        }
    }

    private int pullAdCount(List<ContentItem> adInfos2) {
        int count = 0;
        for (int i = 0; i < adInfos2.size(); i++) {
            if (adInfos2.get(i) != null && !TextUtils.isEmpty(adInfos2.get(i).getLocalPath()) && Utils.fileIsExists(adInfos2.get(i).getLocalPath())) {
                count++;
                this.location = i;
            }
        }
        return count;
    }

    /* access modifiers changed from: private */
    public void setTimerText() {
        if (CHARGE_PLATFORM.xcharge.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
            if (PLATFORM_CUSTOMER.anyo_private.equals(SystemSettingCacheProvider.getInstance().getPlatformCustomer())) {
                this.tv_timer.setVisibility(8);
            }
        }
        this.tv_timer.setText(getString(C0221R.string.ad_timer, new Object[]{Long.valueOf(this.curTime)}));
    }

    /* access modifiers changed from: private */
    public void exit() {
        if (Variate.getInstance().isNFC()) {
            startActivity(new Intent(this, NFCChargeBalanceActivity.class));
            finish();
            return;
        }
        startActivity(new Intent(this, ChargeCostActivity.class));
        finish();
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Log.d("PullAdActivity", "onResume");
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Log.d("PullAdActivity", "onPause");
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Log.d("PullAdActivity", "onStop");
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Log.d("PullAdActivity", "onDestroy");
        this.mhHandler.removeMessages(1);
        this.mhHandler.removeMessages(2);
        this.mhHandler.removeCallbacksAndMessages((Object) null);
    }

    public void onBackPressed() {
    }
}
