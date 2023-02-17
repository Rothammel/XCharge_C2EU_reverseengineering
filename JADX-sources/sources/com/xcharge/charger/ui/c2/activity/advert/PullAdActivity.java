package com.xcharge.charger.ui.c2.activity.advert;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xcharge.charger.R;
import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.charger.data.bean.type.ADVERT_POLICY;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.CONTENT_MEDIA_TYPE;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.charge.nfc.NFCChargeBalanceActivity;
import com.xcharge.charger.ui.c2.activity.charge.online.ChargeCostActivity;
import com.xcharge.charger.ui.c2.activity.data.Variate;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import java.util.List;
import pl.droidsonroids.gif.GifDrawable;

/* loaded from: classes.dex */
public class PullAdActivity extends BaseActivity {
    ImageView iv_ad;
    AutoScrollViewPager mAutoScrollViewPager;
    TextView tv_timer;
    VideoView vv_ad;
    private final int MSG_TIME_REPEAT = 1;
    private final int MSG_TIME_END = 2;
    private boolean isOnlyone = true;
    private long curTime = 0;
    private long totalTime = 0;
    private List<ContentItem> adInfos = null;
    private int location = 0;
    private Handler mhHandler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.advert.PullAdActivity.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    PullAdActivity.this.setTimerText();
                    if (PullAdActivity.this.curTime <= 1) {
                        sendEmptyMessage(2);
                        return;
                    }
                    PullAdActivity.this.curTime--;
                    sendEmptyMessageDelayed(1, 1000L);
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

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, false);
    }

    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:37:0x012a -> B:60:0x0094). Please submit an issue!!! */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void initView() {
        this.mAutoScrollViewPager = (AutoScrollViewPager) findViewById(R.id.autoViewPager);
        this.tv_timer = (TextView) findViewById(R.id.tv_timer);
        this.iv_ad = (ImageView) findViewById(R.id.iv_ad);
        this.vv_ad = (VideoView) findViewById(R.id.vv_ad);
        initData();
        try {
            if (this.isOnlyone) {
                this.mAutoScrollViewPager.setVisibility(8);
                String type = this.adInfos.get(this.location).getType();
                if (CONTENT_MEDIA_TYPE.jpg.getType().equals(type) || CONTENT_MEDIA_TYPE.png.getType().equals(type) || CONTENT_MEDIA_TYPE.webp.getType().equals(type)) {
                    this.iv_ad.setVisibility(0);
                    this.vv_ad.setVisibility(8);
                    String localPath = this.adInfos.get(this.location).getLocalPath();
                    if (TextUtils.isEmpty(localPath) && !Utils.fileIsExists(localPath)) {
                        exit();
                    } else {
                        Utils.loadImage(localPath, this.iv_ad, new ImageLoadingListener() { // from class: com.xcharge.charger.ui.c2.activity.advert.PullAdActivity.2
                            @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                            public void onLoadingStarted(String arg0, View arg1) {
                            }

                            @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                            public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                                PullAdActivity.this.exit();
                            }

                            @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                            public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                            }

                            @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                            public void onLoadingCancelled(String arg0, View arg1) {
                            }
                        }, this);
                        this.totalTime = this.adInfos.get(this.location).getDuration();
                    }
                } else if (CONTENT_MEDIA_TYPE.gif.getType().equals(type) || CONTENT_MEDIA_TYPE.gifv.getType().equals(type)) {
                    this.iv_ad.setVisibility(0);
                    this.vv_ad.setVisibility(8);
                    String localPath2 = this.adInfos.get(this.location).getLocalPath();
                    if (TextUtils.isEmpty(localPath2) && !Utils.fileIsExists(localPath2)) {
                        exit();
                    } else {
                        try {
                            GifDrawable drawable = new GifDrawable(localPath2);
                            this.iv_ad.setImageDrawable(drawable);
                            if (drawable.getDuration() > 0) {
                                this.totalTime = drawable.getDuration() / 1000;
                            } else {
                                this.totalTime = this.adInfos.get(this.location).getDuration();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else if (CONTENT_MEDIA_TYPE.mp4.getType().equals(type)) {
                    this.iv_ad.setVisibility(8);
                    this.vv_ad.setVisibility(0);
                    String localPath3 = this.adInfos.get(this.location).getLocalPath();
                    if (TextUtils.isEmpty(localPath3) && !Utils.fileIsExists(localPath3)) {
                        exit();
                    } else {
                        this.vv_ad.setVideoURI(Uri.parse(localPath3));
                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(localPath3);
                        String duration = mmr.extractMetadata(9);
                        this.vv_ad.start();
                        this.vv_ad.setOnErrorListener(new MediaPlayer.OnErrorListener() { // from class: com.xcharge.charger.ui.c2.activity.advert.PullAdActivity.3
                            @Override // android.media.MediaPlayer.OnErrorListener
                            public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                                PullAdActivity.this.exit();
                                return false;
                            }
                        });
                        if (!TextUtils.isEmpty(duration) && Integer.parseInt(duration) > 0) {
                            this.totalTime = Integer.parseInt(duration) / 1000;
                        } else {
                            this.totalTime = this.adInfos.get(this.location).getDuration();
                        }
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
                this.mAutoScrollViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() { // from class: com.xcharge.charger.ui.c2.activity.advert.PullAdActivity.4
                    @Override // android.support.v4.view.ViewPager.OnPageChangeListener
                    public void onPageSelected(int p) {
                        PullAdActivity.this.mAutoScrollViewPager.setInterval(((ContentItem) PullAdActivity.this.adInfos.get(p % size)).getDuration() * 1000);
                    }

                    @Override // android.support.v4.view.ViewPager.OnPageChangeListener
                    public void onPageScrolled(int arg0, float arg1, int arg2) {
                    }

                    @Override // android.support.v4.view.ViewPager.OnPageChangeListener
                    public void onPageScrollStateChanged(int arg0) {
                    }
                });
            }
            this.curTime = this.totalTime;
            setTimerText();
            this.curTime--;
            this.mhHandler.sendEmptyMessageDelayed(1, 1000L);
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

    private int pullAdCount(List<ContentItem> adInfos) {
        int count = 0;
        for (int i = 0; i < adInfos.size(); i++) {
            if (adInfos.get(i) != null && !TextUtils.isEmpty(adInfos.get(i).getLocalPath()) && Utils.fileIsExists(adInfos.get(i).getLocalPath())) {
                count++;
                this.location = i;
            }
        }
        return count;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void setTimerText() {
        CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        if (CHARGE_PLATFORM.xcharge.equals(platform)) {
            PLATFORM_CUSTOMER customer = SystemSettingCacheProvider.getInstance().getPlatformCustomer();
            if (PLATFORM_CUSTOMER.anyo_private.equals(customer)) {
                this.tv_timer.setVisibility(8);
            }
        }
        this.tv_timer.setText(getString(R.string.ad_timer, new Object[]{Long.valueOf(this.curTime)}));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void exit() {
        if (Variate.getInstance().isNFC()) {
            startActivity(new Intent(this, NFCChargeBalanceActivity.class));
            finish();
            return;
        }
        startActivity(new Intent(this, ChargeCostActivity.class));
        finish();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        Log.d("PullAdActivity", "onResume");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onPause() {
        super.onPause();
        Log.d("PullAdActivity", "onPause");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onStop() {
        super.onStop();
        Log.d("PullAdActivity", "onStop");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        Log.d("PullAdActivity", "onDestroy");
        this.mhHandler.removeMessages(1);
        this.mhHandler.removeMessages(2);
        this.mhHandler.removeCallbacksAndMessages(null);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
    }
}
