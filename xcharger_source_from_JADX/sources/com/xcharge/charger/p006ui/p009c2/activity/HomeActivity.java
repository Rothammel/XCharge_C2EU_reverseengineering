package com.xcharge.charger.p006ui.p009c2.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.p006ui.api.UIEventMessageProxy;
import com.xcharge.charger.p006ui.api.bean.UICtrlMessage;
import com.xcharge.charger.p006ui.api.bean.UIEventMessage;
import com.xcharge.charger.p006ui.p009c2.activity.test.TestChargeActivity;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;
import com.xcharge.charger.p006ui.p009c2.activity.widget.LoadingDialog;
import com.xcharge.charger.protocol.ocpp.bean.types.UnitOfMeasure;
import java.util.HashMap;
import p010it.sauronsoftware.ftp4j.FTPCodes;

/* renamed from: com.xcharge.charger.ui.c2.activity.HomeActivity */
public class HomeActivity extends BaseActivity {
    private LoadingDialog initLoadingDialog;
    /* access modifiers changed from: private */
    public ImageView iv_logo;
    private TextView tv_bottom1;
    private TextView tv_bottom2;
    private TextView tv_status_one;
    private TextView tv_version;
    private String welcome = "";

    /* access modifiers changed from: protected */
    @SuppressLint({"NewApi"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0221R.layout.activity_welcome);
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), UIEventMessage.TYPE_UI_ACTIVITY, (String) null, getClass().getName(), "create", (HashMap) null);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, true);
    }

    /* access modifiers changed from: protected */
    public void initView() {
        this.iv_logo = (ImageView) findViewById(C0221R.C0223id.iv_status_one);
        this.tv_status_one = (TextView) findViewById(C0221R.C0223id.tv_status_one);
        this.tv_version = (TextView) findViewById(C0221R.C0223id.tv_version);
        this.tv_bottom1 = (TextView) findViewById(C0221R.C0223id.tv_bottom1);
        this.tv_bottom2 = (TextView) findViewById(C0221R.C0223id.tv_bottom2);
        if (HardwareStatusCacheProvider.getInstance().getNetworkStatus().isConnected()) {
            String logoResouce = RemoteSettingCacheProvider.getInstance().getLogoResouce();
            if (TextUtils.isEmpty(logoResouce) || !Utils.fileIsExists(logoResouce)) {
                this.iv_logo.setImageResource(C0221R.C0222drawable.ic_welcome_logo);
            } else {
                Utils.loadImage(logoResouce, this.iv_logo, new ImageLoadingListener() {
                    public void onLoadingStarted(String arg0, View arg1) {
                    }

                    public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                        HomeActivity.this.iv_logo.setImageResource(C0221R.C0222drawable.ic_welcome_logo);
                    }

                    public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                    }

                    public void onLoadingCancelled(String arg0, View arg1) {
                    }
                }, this);
            }
        } else {
            this.iv_logo.setImageResource(C0221R.C0222drawable.ic_welcome_logo);
        }
        if (HardwareStatusCacheProvider.getInstance().getNetworkStatus().isConnected()) {
            if (RemoteSettingCacheProvider.getInstance().getUserDefineUISetting() == null) {
                getPlatform();
            } else if (TextUtils.isEmpty(RemoteSettingCacheProvider.getInstance().getUserDefineUISetting().getWelcome())) {
                getPlatform();
            } else {
                this.welcome = RemoteSettingCacheProvider.getInstance().getUserDefineUISetting().getWelcome();
            }
        } else if (LocalSettingCacheProvider.getInstance().getUserDefineUISetting() == null) {
            getPlatform();
        } else if (TextUtils.isEmpty(LocalSettingCacheProvider.getInstance().getUserDefineUISetting().getWelcome())) {
            getPlatform();
        } else {
            this.welcome = LocalSettingCacheProvider.getInstance().getUserDefineUISetting().getWelcome();
        }
        this.tv_status_one.setTextSize(70.0f);
        this.tv_status_one.setText(this.welcome);
        this.tv_status_one.setTextSize(autoSetTextSize(FTPCodes.SYNTAX_ERROR, this.welcome.length(), this.tv_status_one.getTextSize(), 0.0f));
        if (SoftwareStatusCacheProvider.getInstance().getAppVer() == null || SoftwareStatusCacheProvider.getInstance().getFirewareVer() == null) {
            this.tv_version.setText("");
        } else if (HardwareStatusCacheProvider.getInstance().getHardwareStatus() == null) {
        } else {
            if (HardwareStatusCacheProvider.getInstance().getHardwareStatus().getDeviceError().getCode() == 200) {
                this.tv_version.setText(UnitOfMeasure.f121V + SoftwareStatusCacheProvider.getInstance().getFirewareVer() + "-" + SoftwareStatusCacheProvider.getInstance().getAppVer());
                this.tv_bottom1.setVisibility(0);
                this.tv_bottom2.setVisibility(0);
            } else if (HardwareStatusCacheProvider.getInstance().getHardwareStatus().getDeviceError().getCode() == 30001) {
                this.tv_bottom1.setVisibility(8);
                this.tv_bottom2.setVisibility(8);
                this.tv_version.setText(UnitOfMeasure.f121V + SoftwareStatusCacheProvider.getInstance().getFirewareVer() + "-" + SoftwareStatusCacheProvider.getInstance().getAppVer() + " SN NOT FOUND!");
            }
        }
    }

    private void getPlatform() {
        if (SystemSettingCacheProvider.getInstance().getChargePlatform() == null) {
            return;
        }
        if (CHARGE_PLATFORM.xmsz.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
            this.welcome = "欢迎使用厦门市政充电桩";
        } else if (CHARGE_PLATFORM.anyo.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
            this.welcome = "欢迎使用安悦充电桩";
        } else {
            this.welcome = "欢迎使用智充充电桩";
        }
    }

    /* access modifiers changed from: protected */
    public void onUICtrlReceived(UICtrlMessage msg) {
        super.onUICtrlReceived(msg);
        String activity = msg.getActivity();
        if (!TextUtils.isEmpty(activity) && getClass().getName().equals(activity)) {
            String type = msg.getType();
            String subType = msg.getSubType();
            String name = msg.getName();
            String opr = msg.getOpr();
            HashMap<String, Object> data = msg.getData();
            if (UIEventMessage.TYPE_UI_ELEMENT.equals(type) && UIEventMessage.SUBTYPE_UI_LOADING_DIALOG.equals(subType) && "initLoadingDialog".equals(name) && "update".equals(opr)) {
                updateInitLoadingDidlog((String) data.get("nfcDialogStatus"));
            }
        }
    }

    private void updateInitLoadingDidlog(String status) {
        if ("show".equals(status)) {
            if (this.initLoadingDialog == null) {
                this.initLoadingDialog = LoadingDialog.createDialog(this, getString(C0221R.string.check_device_loading));
            } else {
                this.initLoadingDialog.changeLoadingText(getString(C0221R.string.check_device_loading));
            }
            this.initLoadingDialog.show();
        } else if ("dismiss".equals(status)) {
            if (this.initLoadingDialog != null && this.initLoadingDialog.isShowing()) {
                this.initLoadingDialog.dismiss();
            }
            initView();
        }
    }

    public void onBackPressed() {
        startActivity(new Intent(this, TestChargeActivity.class));
        finish();
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Log.d("HomeActivity", "onResume");
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Log.d("HomeActivity", "onPause");
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Log.d("HomeActivity", "onStop");
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Log.d("HomeActivity", "onDestroy");
    }

    public static float autoSetTextSize(int maxWidth, int length, float srcTextSize, float padding) {
        float targetTextSize = srcTextSize;
        int maxWidth2 = (int) (((float) maxWidth) - padding);
        if (((float) length) * srcTextSize > ((float) maxWidth2)) {
            return (((float) maxWidth2) * 1.0f) / ((float) length);
        }
        return targetTextSize;
    }
}
