package com.xcharge.charger.ui.c2.activity.charge.nfc;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xcharge.charger.R;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.ui.api.UIEventMessageProxy;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;

/* loaded from: classes.dex */
public class NFCConfigPersnalCardActivity extends BaseActivity {
    private long curTimerTime;
    private LinearLayout ll_timer;
    private TextView tv_bottom;
    private TextView tv_timer;
    private final int MSG_TIME_REPEAT = 1;
    private final int MSG_TIME_END = 2;
    Handler mHandler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.charge.nfc.NFCConfigPersnalCardActivity.1
        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    NFCConfigPersnalCardActivity.this.nextSecond();
                    return;
                case 2:
                    NFCConfigPersnalCardActivity.this.cancelConfigPersnalCard();
                    return;
                default:
                    return;
            }
        }
    };

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_config_persnal_card);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        Utils.setPermitNFC(true, false, true, false);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void initView() {
        this.ll_timer = (LinearLayout) findViewById(R.id.ll_timer);
        this.tv_timer = (TextView) findViewById(R.id.tv_timer);
        this.tv_bottom = (TextView) findViewById(R.id.tv_bottom);
        if (CountrySettingCacheProvider.getInstance().isSetRTL()) {
            this.ll_timer.setLayoutDirection(1);
        } else {
            this.ll_timer.setLayoutDirection(0);
        }
        this.curTimerTime = 60L;
        this.mHandler.sendEmptyMessage(1);
        this.tv_bottom.setText(R.string.waitting_config_persnal_card_status_hint);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void nextSecond() {
        this.tv_timer.setText(String.format("%02d", Long.valueOf(this.curTimerTime)));
        if (this.curTimerTime <= 0) {
            this.mHandler.sendEmptyMessage(2);
            return;
        }
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 1000L);
        this.curTimerTime--;
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        Log.d("NFCConfigPersnalCardActivity", "onResume");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        Log.d("NFCConfigPersnalCardActivity", "onPause");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        Log.d("NFCConfigPersnalCardActivity", "onStop");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        Log.d("NFCConfigPersnalCardActivity", "onDestroy");
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.removeCallbacksAndMessages(null);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        cancelConfigPersnalCard();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void cancelConfigPersnalCard() {
        Utils.skipNfcQrcode(context);
        finish();
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), "key", null, getClass().getName(), "up", null);
    }
}