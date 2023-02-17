package com.xcharge.charger.ui.c2.activity.fault;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import com.xcharge.charger.R;
import com.xcharge.charger.device.c2.service.C2DeviceProxy;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;

/* loaded from: classes.dex */
public class EnableActivity extends BaseActivity {
    protected ImageView mIvStatus;
    protected TextView mtvStatus;

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_status);
        C2DeviceProxy.getInstance().setDeviceUnavailableBLN();
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, false);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void initView() {
        this.mIvStatus = (ImageView) findViewById(R.id.iv_status_one);
        this.mIvStatus.setImageResource(R.drawable.ic_invalid);
        this.mtvStatus = (TextView) findViewById(R.id.tv_status_one);
        this.mtvStatus.setText(R.string.charge_pile_not_available);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        Log.d("EnableActivity", "onResume");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        Log.d("EnableActivity", "onPause");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        Log.d("EnableActivity", "onStop");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        Log.d("EnableActivity", "onDestroy");
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void keepScreenOn() {
    }

    @Override // android.app.Activity
    public void onBackPressed() {
    }
}
