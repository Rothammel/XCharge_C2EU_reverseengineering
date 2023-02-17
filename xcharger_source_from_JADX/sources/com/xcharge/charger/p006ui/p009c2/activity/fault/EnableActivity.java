package com.xcharge.charger.p006ui.p009c2.activity.fault;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.device.p005c2.service.C2DeviceProxy;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;

/* renamed from: com.xcharge.charger.ui.c2.activity.fault.EnableActivity */
public class EnableActivity extends BaseActivity {
    protected ImageView mIvStatus;
    protected TextView mtvStatus;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0221R.layout.activity_base_status);
        C2DeviceProxy.getInstance().setDeviceUnavailableBLN();
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, false);
    }

    /* access modifiers changed from: protected */
    public void initView() {
        this.mIvStatus = (ImageView) findViewById(C0221R.C0223id.iv_status_one);
        this.mIvStatus.setImageResource(C0221R.C0222drawable.ic_invalid);
        this.mtvStatus = (TextView) findViewById(C0221R.C0223id.tv_status_one);
        this.mtvStatus.setText(C0221R.string.charge_pile_not_available);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Log.d("EnableActivity", "onResume");
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Log.d("EnableActivity", "onPause");
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Log.d("EnableActivity", "onStop");
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Log.d("EnableActivity", "onDestroy");
    }

    /* access modifiers changed from: protected */
    public void keepScreenOn() {
    }

    public void onBackPressed() {
    }
}
