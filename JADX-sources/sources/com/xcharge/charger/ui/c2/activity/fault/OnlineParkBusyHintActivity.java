package com.xcharge.charger.ui.c2.activity.fault;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.xcharge.charger.R;
import com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;

/* loaded from: classes.dex */
public class OnlineParkBusyHintActivity extends BaseActivity {
    private ImageView iv_warning;
    private LinearLayout ll_timer;
    private TextView tv_bottom;
    private TextView tv_hint;
    private TextView tv_timer;
    private final int MSG_TIME_REPEAT = 2;
    private final int MSG_TIME_END = 3;
    private int curTimerTime = XCloudProtocolAgent.TIMER_MQTT_CONNECT_BLOCKED;
    Handler mHandler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.fault.OnlineParkBusyHintActivity.1
        @Override // android.os.Handler
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

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_object);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        Utils.setPermitNFC(true, true, true, false);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void initView() {
        this.iv_warning = (ImageView) findViewById(R.id.iv_warning);
        this.ll_timer = (LinearLayout) findViewById(R.id.ll_timer);
        this.tv_timer = (TextView) findViewById(R.id.tv_timer);
        this.tv_hint = (TextView) findViewById(R.id.tv_hint);
        this.tv_bottom = (TextView) findViewById(R.id.tv_bottom);
        this.mHandler.sendEmptyMessage(2);
        this.tv_bottom.setText(R.string.radar_start_hint_text);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void waittingScanTimer() {
        this.iv_warning.setVisibility(8);
        this.ll_timer.setVisibility(0);
        this.tv_timer.setText(String.format("%02d", Integer.valueOf(this.curTimerTime)));
        this.tv_hint.setText(R.string.radar_find_car_hint);
        if (this.curTimerTime <= 0) {
            this.mHandler.sendEmptyMessage(3);
            return;
        }
        this.mHandler.removeMessages(2);
        this.mHandler.sendEmptyMessageDelayed(2, 1000L);
        this.curTimerTime--;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void waittingScanTimeout() {
        this.iv_warning.setVisibility(0);
        this.ll_timer.setVisibility(8);
        this.tv_hint.setText(R.string.radar_find_car_timerout_hint);
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        Utils.skipNfcQrcode(this);
        finish();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        Log.d("OnlineParkBusyHintActivity", "onResume");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onPause() {
        super.onPause();
        Log.d("OnlineParkBusyHintActivity", "onPause");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onStop() {
        super.onStop();
        Log.d("OnlineParkBusyHintActivity", "onStop");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        Log.d("OnlineParkBusyHintActivity", "onDestroy");
        this.mHandler.removeMessages(2);
        this.mHandler.removeMessages(3);
    }
}
