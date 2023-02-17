package com.xcharge.charger.p006ui.p009c2.activity.charge.online;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.data.Variate;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;
import com.xcharge.common.utils.HandlerTimer;

/* renamed from: com.xcharge.charger.ui.c2.activity.charge.online.ChargeDelayedActivity */
public class ChargeDelayedActivity extends BaseActivity {
    private final int MSG_TIMER_REPEAT = 1;
    /* access modifiers changed from: private */
    public long curDelayTime = 0;
    /* access modifiers changed from: private */
    public long delayStartTime = 0;
    protected Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus("1");
                    if (portStatus != null) {
                        ChargeDelayedActivity.this.tv_bottom.setText(ChargeDelayedActivity.this.getString(C0221R.string.chargecomplete_hint_text1, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(portStatus.getDelayPrice())), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
                        ChargeDelayedActivity.this.delayStartTime = portStatus.getDelayStartTime();
                        if (ChargeDelayedActivity.this.delayStartTime == 0) {
                            ChargeDelayedActivity.this.curDelayTime = 0;
                        } else {
                            ChargeDelayedActivity.this.curDelayTime = (System.currentTimeMillis() - portStatus.getDelayStartTime()) / 1000;
                        }
                        ChargeDelayedActivity.this.tv_status_two.setText(ChargeDelayedActivity.context.getResources().getString(C0221R.string.drive_away_hint_text, new Object[]{Utils.fromatTotalTime(ChargeDelayedActivity.this.curDelayTime), CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(((double) portStatus.getTotalDelayFee()) / 100.0d)), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
                    }
                    ChargeDelayedActivity.this.timerHandler.startTimer(1000, 1, (Object) null);
                    return;
                default:
                    return;
            }
        }
    };
    private ImageView iv_status;
    /* access modifiers changed from: private */
    public HandlerTimer timerHandler;
    /* access modifiers changed from: private */
    public TextView tv_bottom;
    private TextView tv_status_one;
    /* access modifiers changed from: private */
    public TextView tv_status_two;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        this.timerHandler = new HandlerTimer(this.handler);
        this.timerHandler.init(context);
        super.onCreate(savedInstanceState);
        setContentView(C0221R.layout.activity_base_status);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        if (Variate.getInstance().isNFC) {
            Utils.setPermitNFC(true, false, false, false);
        } else {
            Utils.setPermitNFC(false, false, false, false);
        }
    }

    /* access modifiers changed from: protected */
    public void initView() {
        this.iv_status = (ImageView) findViewById(C0221R.C0223id.iv_status_one);
        this.tv_status_one = (TextView) findViewById(C0221R.C0223id.tv_status_one);
        this.tv_status_two = (TextView) findViewById(C0221R.C0223id.tv_status_two);
        this.tv_bottom = (TextView) findViewById(C0221R.C0223id.tv_bottom);
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus("1");
        if (portStatus != null) {
            this.tv_bottom.setText(getString(C0221R.string.chargecomplete_hint_text1, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(portStatus.getDelayPrice())), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
        }
        this.iv_status.setImageResource(C0221R.C0222drawable.ic_drive_away_timeout);
        this.tv_status_one.setText(C0221R.string.drive_away_status_text);
        this.tv_status_two.setVisibility(0);
        this.timerHandler.startTimer(1000, 1, (Object) null);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Log.d("ChargeDelayedActivity", "onResume");
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Log.d("ChargeDelayedActivity", "onPause");
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Log.d("ChargeDelayedActivity", "onStop");
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Log.d("ChargeDelayedActivity", "onDestroy");
        this.timerHandler.stopTimer(1);
        this.timerHandler.destroy();
    }

    public void onBackPressed() {
    }

    /* access modifiers changed from: protected */
    public void keepScreenOn() {
        if (!PLATFORM_CUSTOMER.anyo_private.equals(SystemSettingCacheProvider.getInstance().getPlatformCustomer())) {
            getWindow().setFlags(128, 128);
        }
    }
}
