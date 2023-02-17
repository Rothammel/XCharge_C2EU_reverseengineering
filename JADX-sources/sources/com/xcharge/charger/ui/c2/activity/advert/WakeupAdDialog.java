package com.xcharge.charger.ui.c2.activity.advert;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import java.util.List;
import pl.droidsonroids.gif.GifDrawable;

/* loaded from: classes.dex */
public class WakeupAdDialog extends Dialog {
    private final int MSG_TIME_END;
    private final int MSG_TIME_REPEAT;
    private List<ContentItem> adInfos;
    private Context context;
    private boolean isOnlyone;
    ImageView iv_ad;
    private int location;
    AutoScrollViewPager mAutoScrollViewPager;
    private Handler mhHandler;
    private long totalTime;
    TextView tv_timer;
    VideoView vv_ad;

    public WakeupAdDialog(Context context) {
        super(context, R.style.Dialog_Fullscreen);
        this.MSG_TIME_REPEAT = 1;
        this.MSG_TIME_END = 2;
        this.isOnlyone = true;
        this.totalTime = 0L;
        this.adInfos = null;
        this.location = 0;
        this.mhHandler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.advert.WakeupAdDialog.1
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        WakeupAdDialog.this.mhHandler.removeMessages(1);
                        WakeupAdDialog.this.setTimerText();
                        WakeupAdDialog.this.totalTime--;
                        if (WakeupAdDialog.this.totalTime < 0) {
                            sendEmptyMessage(2);
                            return;
                        } else {
                            sendEmptyMessageDelayed(1, 1000L);
                            return;
                        }
                    case 2:
                        if (!WakeupAdDialog.this.isOnlyone) {
                            WakeupAdDialog.this.mAutoScrollViewPager.stopAutoScroll();
                        }
                        WakeupAdDialog.this.dismissWakeupAdDialog();
                        return;
                    default:
                        return;
                }
            }
        };
        this.context = context;
        initView();
    }

    public static WakeupAdDialog createDialog(Context context) {
        return new WakeupAdDialog(context);
    }

    /* JADX WARN: Unsupported multi-entry loop pattern (BACK_EDGE: B:37:0x0189 -> B:60:0x00e2). Please submit an issue!!! */
    public void initView() {
        getWindow().setType(2003);
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View view = inflater.inflate(R.layout.activity_ad, (ViewGroup) null);
        setContentView(view);
        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = 768;
        lp.height = 768;
        dialogWindow.setAttributes(lp);
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
                        dismissWakeupAdDialog();
                    } else {
                        Utils.loadImage(localPath, this.iv_ad, new ImageLoadingListener() { // from class: com.xcharge.charger.ui.c2.activity.advert.WakeupAdDialog.2
                            @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                            public void onLoadingStarted(String arg0, View arg1) {
                            }

                            @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                            public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                                WakeupAdDialog.this.dismissWakeupAdDialog();
                            }

                            @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                            public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                            }

                            @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                            public void onLoadingCancelled(String arg0, View arg1) {
                            }
                        }, this.context);
                        this.totalTime = this.adInfos.get(this.location).getDuration();
                    }
                } else if (CONTENT_MEDIA_TYPE.gif.getType().equals(type) || CONTENT_MEDIA_TYPE.gifv.getType().equals(type)) {
                    this.iv_ad.setVisibility(0);
                    this.vv_ad.setVisibility(8);
                    String localPath2 = this.adInfos.get(this.location).getLocalPath();
                    if (TextUtils.isEmpty(localPath2) && !Utils.fileIsExists(localPath2)) {
                        dismissWakeupAdDialog();
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
                        dismissWakeupAdDialog();
                    } else {
                        this.vv_ad.setVideoURI(Uri.parse(localPath3));
                        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                        mmr.setDataSource(localPath3);
                        String duration = mmr.extractMetadata(9);
                        this.vv_ad.start();
                        this.vv_ad.setOnErrorListener(new MediaPlayer.OnErrorListener() { // from class: com.xcharge.charger.ui.c2.activity.advert.WakeupAdDialog.3
                            @Override // android.media.MediaPlayer.OnErrorListener
                            public boolean onError(MediaPlayer arg0, int arg1, int arg2) {
                                WakeupAdDialog.this.dismissWakeupAdDialog();
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
                this.mAutoScrollViewPager.setAdapter(new MyViewPagerAdapter(this.context, this.adInfos));
                this.mAutoScrollViewPager.startAutoScroll();
                final int size = this.adInfos.size();
                this.mAutoScrollViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() { // from class: com.xcharge.charger.ui.c2.activity.advert.WakeupAdDialog.4
                    @Override // android.support.v4.view.ViewPager.OnPageChangeListener
                    public void onPageSelected(int p) {
                        WakeupAdDialog.this.mAutoScrollViewPager.setInterval(((ContentItem) WakeupAdDialog.this.adInfos.get(p % size)).getDuration() * 1000);
                    }

                    @Override // android.support.v4.view.ViewPager.OnPageChangeListener
                    public void onPageScrolled(int arg0, float arg1, int arg2) {
                    }

                    @Override // android.support.v4.view.ViewPager.OnPageChangeListener
                    public void onPageScrollStateChanged(int arg0) {
                    }
                });
            }
            this.mhHandler.sendEmptyMessage(1);
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private void initData() {
        if (RemoteSettingCacheProvider.getInstance().getAdvertContent(ADVERT_POLICY.wakeUpAdvsite) == null) {
            dismissWakeupAdDialog();
            return;
        }
        this.adInfos = RemoteSettingCacheProvider.getInstance().getAdvertContent(ADVERT_POLICY.wakeUpAdvsite);
        if (wakeupAdCount(this.adInfos) == 1) {
            this.isOnlyone = true;
        } else {
            this.isOnlyone = false;
        }
    }

    private int wakeupAdCount(List<ContentItem> adInfos) {
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
        this.tv_timer.setText(this.context.getResources().getString(R.string.ad_timer, Long.valueOf(this.totalTime)));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissWakeupAdDialog() {
        this.mhHandler.removeMessages(1);
        this.mhHandler.removeMessages(2);
        this.mhHandler.removeCallbacksAndMessages(null);
        Utils.anewSetPermitNFC(this.context);
        dismiss();
    }
}
