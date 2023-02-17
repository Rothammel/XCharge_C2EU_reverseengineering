package com.xcharge.charger.p006ui.p009c2.activity.charge.nfc;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.p006ui.api.UIEventMessageProxy;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;
import java.util.HashMap;

/* renamed from: com.xcharge.charger.ui.c2.activity.charge.nfc.NFCConfigPersnalCardActivity */
public class NFCConfigPersnalCardActivity extends BaseActivity {
    private final int MSG_TIME_END = 2;
    private final int MSG_TIME_REPEAT = 1;
    private long curTimerTime;
    private LinearLayout ll_timer;
    Handler mHandler = new Handler() {
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
    private TextView tv_bottom;
    private TextView tv_timer;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0221R.layout.activity_nfc_config_persnal_card);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Utils.setPermitNFC(true, false, true, false);
    }

    /* access modifiers changed from: protected */
    public void initView() {
        this.ll_timer = (LinearLayout) findViewById(C0221R.C0223id.ll_timer);
        this.tv_timer = (TextView) findViewById(C0221R.C0223id.tv_timer);
        this.tv_bottom = (TextView) findViewById(C0221R.C0223id.tv_bottom);
        if (CountrySettingCacheProvider.getInstance().isSetRTL()) {
            this.ll_timer.setLayoutDirection(1);
        } else {
            this.ll_timer.setLayoutDirection(0);
        }
        this.curTimerTime = 60;
        this.mHandler.sendEmptyMessage(1);
        this.tv_bottom.setText(C0221R.string.waitting_config_persnal_card_status_hint);
    }

    /* access modifiers changed from: private */
    public void nextSecond() {
        this.tv_timer.setText(String.format("%02d", new Object[]{Long.valueOf(this.curTimerTime)}));
        if (this.curTimerTime <= 0) {
            this.mHandler.sendEmptyMessage(2);
            return;
        }
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, 1000);
        this.curTimerTime--;
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Log.d("NFCConfigPersnalCardActivity", "onResume");
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Log.d("NFCConfigPersnalCardActivity", "onPause");
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Log.d("NFCConfigPersnalCardActivity", "onStop");
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Log.d("NFCConfigPersnalCardActivity", "onDestroy");
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.removeCallbacksAndMessages((Object) null);
    }

    public void onBackPressed() {
        cancelConfigPersnalCard();
    }

    /* access modifiers changed from: private */
    public void cancelConfigPersnalCard() {
        Utils.skipNfcQrcode(context);
        finish();
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), "key", (String) null, getClass().getName(), "up", (HashMap) null);
    }
}
