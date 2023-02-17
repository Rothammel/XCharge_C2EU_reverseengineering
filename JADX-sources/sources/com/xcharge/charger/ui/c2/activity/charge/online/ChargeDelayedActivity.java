package com.xcharge.charger.ui.c2.activity.charge.online;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.xcharge.charger.R;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.data.Variate;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import com.xcharge.common.utils.HandlerTimer;

/* loaded from: classes.dex */
public class ChargeDelayedActivity extends BaseActivity {
    private ImageView iv_status;
    private HandlerTimer timerHandler;
    private TextView tv_bottom;
    private TextView tv_status_one;
    private TextView tv_status_two;
    private final int MSG_TIMER_REPEAT = 1;
    private long delayStartTime = 0;
    private long curDelayTime = 0;
    protected Handler handler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.charge.online.ChargeDelayedActivity.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus("1");
                    if (portStatus != null) {
                        ChargeDelayedActivity.this.tv_bottom.setText(ChargeDelayedActivity.this.getString(R.string.chargecomplete_hint_text1, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(portStatus.getDelayPrice())), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
                        ChargeDelayedActivity.this.delayStartTime = portStatus.getDelayStartTime();
                        if (ChargeDelayedActivity.this.delayStartTime == 0) {
                            ChargeDelayedActivity.this.curDelayTime = 0L;
                        } else {
                            ChargeDelayedActivity.this.curDelayTime = (System.currentTimeMillis() - portStatus.getDelayStartTime()) / 1000;
                        }
                        ChargeDelayedActivity.this.tv_status_two.setText(ChargeDelayedActivity.context.getResources().getString(R.string.drive_away_hint_text, Utils.fromatTotalTime(ChargeDelayedActivity.this.curDelayTime), CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(portStatus.getTotalDelayFee() / 100.0d)), CountrySettingCacheProvider.getInstance().getMoneyDisp()));
                    }
                    ChargeDelayedActivity.this.timerHandler.startTimer(1000L, 1, null);
                    return;
                default:
                    return;
            }
        }
    };

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        this.timerHandler = new HandlerTimer(this.handler);
        this.timerHandler.init(context);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_status);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        if (Variate.getInstance().isNFC) {
            Utils.setPermitNFC(true, false, false, false);
        } else {
            Utils.setPermitNFC(false, false, false, false);
        }
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void initView() {
        this.iv_status = (ImageView) findViewById(R.id.iv_status_one);
        this.tv_status_one = (TextView) findViewById(R.id.tv_status_one);
        this.tv_status_two = (TextView) findViewById(R.id.tv_status_two);
        this.tv_bottom = (TextView) findViewById(R.id.tv_bottom);
        PortStatus portStatus = ChargeStatusCacheProvider.getInstance().getPortStatus("1");
        if (portStatus != null) {
            this.tv_bottom.setText(getString(R.string.chargecomplete_hint_text1, new Object[]{CountrySettingCacheProvider.getInstance().format(BaseActivity.TWODP, Double.valueOf(portStatus.getDelayPrice())), CountrySettingCacheProvider.getInstance().getMoneyDisp()}));
        }
        this.iv_status.setImageResource(R.drawable.ic_drive_away_timeout);
        this.tv_status_one.setText(R.string.drive_away_status_text);
        this.tv_status_two.setVisibility(0);
        this.timerHandler.startTimer(1000L, 1, null);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        Log.d("ChargeDelayedActivity", "onResume");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onPause() {
        super.onPause();
        Log.d("ChargeDelayedActivity", "onPause");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onStop() {
        super.onStop();
        Log.d("ChargeDelayedActivity", "onStop");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        Log.d("ChargeDelayedActivity", "onDestroy");
        this.timerHandler.stopTimer(1);
        this.timerHandler.destroy();
    }

    @Override // android.app.Activity
    public void onBackPressed() {
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void keepScreenOn() {
        if (!PLATFORM_CUSTOMER.anyo_private.equals(SystemSettingCacheProvider.getInstance().getPlatformCustomer())) {
            getWindow().setFlags(128, 128);
        }
    }
}
