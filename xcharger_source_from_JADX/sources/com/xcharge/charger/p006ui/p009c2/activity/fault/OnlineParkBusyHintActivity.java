package com.xcharge.charger.p006ui.p009c2.activity.fault;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;
import com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent;

/* renamed from: com.xcharge.charger.ui.c2.activity.fault.OnlineParkBusyHintActivity */
public class OnlineParkBusyHintActivity extends BaseActivity {
    private final int MSG_TIME_END = 3;
    private final int MSG_TIME_REPEAT = 2;
    private int curTimerTime = XCloudProtocolAgent.TIMER_MQTT_CONNECT_BLOCKED;
    private ImageView iv_warning;
    private LinearLayout ll_timer;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    OnlineParkBusyHintActivity.this.waittingScanTimer();
                    return;
                case 3:
                    OnlineParkBusyHintActivity.this.waittingScanTimeout();
                    return;
                default:
                    return;
            }
        }
    };
    private TextView tv_bottom;
    private TextView tv_hint;
    private TextView tv_timer;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0221R.layout.activity_find_object);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Utils.setPermitNFC(true, true, true, false);
    }

    /* access modifiers changed from: protected */
    public void initView() {
        this.iv_warning = (ImageView) findViewById(C0221R.C0223id.iv_warning);
        this.ll_timer = (LinearLayout) findViewById(C0221R.C0223id.ll_timer);
        this.tv_timer = (TextView) findViewById(C0221R.C0223id.tv_timer);
        this.tv_hint = (TextView) findViewById(C0221R.C0223id.tv_hint);
        this.tv_bottom = (TextView) findViewById(C0221R.C0223id.tv_bottom);
        this.mHandler.sendEmptyMessage(2);
        this.tv_bottom.setText(C0221R.string.radar_start_hint_text);
    }

    /* access modifiers changed from: private */
    public void waittingScanTimer() {
        this.iv_warning.setVisibility(8);
        this.ll_timer.setVisibility(0);
        this.tv_timer.setText(String.format("%02d", new Object[]{Integer.valueOf(this.curTimerTime)}));
        this.tv_hint.setText(C0221R.string.radar_find_car_hint);
        if (this.curTimerTime <= 0) {
            this.mHandler.sendEmptyMessage(3);
            return;
        }
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessageDelayed(2, 1000);
        this.curTimerTime--;
    }

    /* access modifiers changed from: private */
    public void waittingScanTimeout() {
        this.iv_warning.setVisibility(0);
        this.ll_timer.setVisibility(8);
        this.tv_hint.setText(C0221R.string.radar_find_car_timerout_hint);
    }

    public void onBackPressed() {
        Utils.skipNfcQrcode(this);
        finish();
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Log.d("OnlineParkBusyHintActivity", "onResume");
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Log.d("OnlineParkBusyHintActivity", "onPause");
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Log.d("OnlineParkBusyHintActivity", "onStop");
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Log.d("OnlineParkBusyHintActivity", "onDestroy");
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(3);
    }
}
