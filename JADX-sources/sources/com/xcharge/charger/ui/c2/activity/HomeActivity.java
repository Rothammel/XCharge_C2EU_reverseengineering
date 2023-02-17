package com.xcharge.charger.ui.c2.activity;

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
import com.xcharge.charger.R;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.protocol.ocpp.bean.types.UnitOfMeasure;
import com.xcharge.charger.ui.api.UIEventMessageProxy;
import com.xcharge.charger.ui.api.bean.UICtrlMessage;
import com.xcharge.charger.ui.api.bean.UIEventMessage;
import com.xcharge.charger.ui.c2.activity.test.TestChargeActivity;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import com.xcharge.charger.ui.c2.activity.widget.LoadingDialog;
import it.sauronsoftware.ftp4j.FTPCodes;
import java.util.HashMap;

/* loaded from: classes.dex */
public class HomeActivity extends BaseActivity {
    private LoadingDialog initLoadingDialog;
    private ImageView iv_logo;
    private TextView tv_bottom1;
    private TextView tv_bottom2;
    private TextView tv_status_one;
    private TextView tv_version;
    private String welcome = "";

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    @SuppressLint({"NewApi"})
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        UIEventMessageProxy.getInstance().sendEvent(getClass().getName(), UIEventMessage.TYPE_UI_ACTIVITY, null, getClass().getName(), "create", null);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, true);
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void initView() {
        this.iv_logo = (ImageView) findViewById(R.id.iv_status_one);
        this.tv_status_one = (TextView) findViewById(R.id.tv_status_one);
        this.tv_version = (TextView) findViewById(R.id.tv_version);
        this.tv_bottom1 = (TextView) findViewById(R.id.tv_bottom1);
        this.tv_bottom2 = (TextView) findViewById(R.id.tv_bottom2);
        if (HardwareStatusCacheProvider.getInstance().getNetworkStatus().isConnected()) {
            String logoResouce = RemoteSettingCacheProvider.getInstance().getLogoResouce();
            if (TextUtils.isEmpty(logoResouce) || !Utils.fileIsExists(logoResouce)) {
                this.iv_logo.setImageResource(R.drawable.ic_welcome_logo);
            } else {
                Utils.loadImage(logoResouce, this.iv_logo, new ImageLoadingListener() { // from class: com.xcharge.charger.ui.c2.activity.HomeActivity.1
                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingStarted(String arg0, View arg1) {
                    }

                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingFailed(String arg0, View arg1, FailReason arg2) {
                        HomeActivity.this.iv_logo.setImageResource(R.drawable.ic_welcome_logo);
                    }

                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingComplete(String arg0, View arg1, Bitmap arg2) {
                    }

                    @Override // com.nostra13.universalimageloader.core.listener.ImageLoadingListener
                    public void onLoadingCancelled(String arg0, View arg1) {
                    }
                }, this);
            }
        } else {
            this.iv_logo.setImageResource(R.drawable.ic_welcome_logo);
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
        } else if (HardwareStatusCacheProvider.getInstance().getHardwareStatus() != null) {
            if (HardwareStatusCacheProvider.getInstance().getHardwareStatus().getDeviceError().getCode() == 200) {
                this.tv_version.setText(UnitOfMeasure.V + SoftwareStatusCacheProvider.getInstance().getFirewareVer() + "-" + SoftwareStatusCacheProvider.getInstance().getAppVer());
                this.tv_bottom1.setVisibility(0);
                this.tv_bottom2.setVisibility(0);
            } else if (HardwareStatusCacheProvider.getInstance().getHardwareStatus().getDeviceError().getCode() == 30001) {
                this.tv_bottom1.setVisibility(8);
                this.tv_bottom2.setVisibility(8);
                this.tv_version.setText(UnitOfMeasure.V + SoftwareStatusCacheProvider.getInstance().getFirewareVer() + "-" + SoftwareStatusCacheProvider.getInstance().getAppVer() + " SN NOT FOUND!");
            }
        }
    }

    private void getPlatform() {
        if (SystemSettingCacheProvider.getInstance().getChargePlatform() != null) {
            if (CHARGE_PLATFORM.xmsz.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
                this.welcome = "欢迎使用厦门市政充电桩";
            } else if (CHARGE_PLATFORM.anyo.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
                this.welcome = "欢迎使用安悦充电桩";
            } else {
                this.welcome = "欢迎使用智充充电桩";
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
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
                String status = (String) data.get("nfcDialogStatus");
                updateInitLoadingDidlog(status);
            }
        }
    }

    private void updateInitLoadingDidlog(String status) {
        if ("show".equals(status)) {
            if (this.initLoadingDialog == null) {
                this.initLoadingDialog = LoadingDialog.createDialog(this, getString(R.string.check_device_loading));
            } else {
                this.initLoadingDialog.changeLoadingText(getString(R.string.check_device_loading));
            }
            this.initLoadingDialog.show();
        } else if ("dismiss".equals(status)) {
            if (this.initLoadingDialog != null && this.initLoadingDialog.isShowing()) {
                this.initLoadingDialog.dismiss();
            }
            initView();
        }
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        startActivity(new Intent(this, TestChargeActivity.class));
        finish();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onResume() {
        super.onResume();
        Log.d("HomeActivity", "onResume");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onPause() {
        super.onPause();
        Log.d("HomeActivity", "onPause");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onStop() {
        super.onStop();
        Log.d("HomeActivity", "onStop");
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        Log.d("HomeActivity", "onDestroy");
    }

    public static float autoSetTextSize(int maxWidth, int length, float srcTextSize, float padding) {
        int maxWidth2 = (int) (maxWidth - padding);
        if (length * srcTextSize <= maxWidth2) {
            return srcTextSize;
        }
        float targetTextSize = (maxWidth2 * 1.0f) / length;
        return targetTextSize;
    }
}
