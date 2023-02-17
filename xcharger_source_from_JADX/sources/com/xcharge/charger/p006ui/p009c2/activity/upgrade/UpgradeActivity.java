package com.xcharge.charger.p006ui.p009c2.activity.upgrade;

import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;
import com.xcharge.charger.p006ui.p009c2.activity.widget.LoadingDialog;

/* renamed from: com.xcharge.charger.ui.c2.activity.upgrade.UpgradeActivity */
public class UpgradeActivity extends BaseActivity {
    private final int MSG_CHECK_FIREWARE_PROGRESS = BaseActivity.MSG_NFC_DISMISS_MONEY;
    private final int MSG_CHECK_INTEGRITY = 4098;
    private final int MSG_DOWNLOAD_PROGRESS = 4097;
    private final int MSG_FINISH_UPDATE = 4102;
    private final int MSG_START_CHECK_FIREWARE = BaseActivity.MSG_NFC_CARD_AWAY;
    private final int MSG_START_UNZIP = 4099;
    private final int MSG_UPGRADE_APK_NOT_FOUND = 4114;
    private final int MSG_UPGRADE_APK_NOT_VALID = 4116;
    private final int MSG_UPGRADE_APK_SIGN_ERROR = 4115;
    private final int MSG_UPGRADE_APK_VERIFY_ERROR = 4117;
    private final int MSG_UPGRADE_DOWNLOAD_FAIL = 4105;
    private final int MSG_UPGRADE_ERROR = 4103;
    private final int MSG_UPGRADE_FIREWARE_VERIFY_ERROR = 4118;
    private final int MSG_UPGRADE_NOT_INTEGRATED = 4112;
    private final int MSG_UPGRADE_NOT_SUPPORTED_VERSION = 4104;
    private final int MSG_UPGRADE_UNZIP_FAIL = 4113;
    protected Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 4097:
                    UpgradeActivity.this.showUpgradeDialog(C0221R.string.upgrade_download_progress, SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getProgress());
                    return;
                case 4098:
                    UpgradeActivity.this.showUpgradeDialog(C0221R.string.upgrade_file_verify);
                    return;
                case 4099:
                    UpgradeActivity.this.showUpgradeDialog(C0221R.string.upgrade_start_unzip);
                    return;
                case BaseActivity.MSG_NFC_CARD_AWAY:
                    UpgradeActivity.this.showUpgradeDialog(C0221R.string.upgrade_start_check_fireware);
                    return;
                case BaseActivity.MSG_NFC_DISMISS_MONEY:
                    Log.e("UpgradeProgress", new StringBuilder(String.valueOf(SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getProgress())).toString());
                    UpgradeActivity.this.showUpgradeDialog(C0221R.string.upgrade_verify_system_progress, SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getProgress());
                    return;
                case 4102:
                    UpgradeActivity.this.finish();
                    return;
                case 4103:
                    UpgradeActivity.this.waitFinish();
                    return;
                case 4104:
                    UpgradeActivity.this.waitFinish();
                    return;
                case 4105:
                    UpgradeActivity.this.showUpgradeDialog(C0221R.string.upgrade_download_fail);
                    UpgradeActivity.this.waitFinish();
                    return;
                case 4112:
                    UpgradeActivity.this.showUpgradeDialog(C0221R.string.upgrade_not_integrated);
                    UpgradeActivity.this.waitFinish();
                    return;
                case 4113:
                    UpgradeActivity.this.showUpgradeDialog(C0221R.string.upgrade_unzip_fail);
                    UpgradeActivity.this.waitFinish();
                    return;
                case 4114:
                    UpgradeActivity.this.showUpgradeDialog(C0221R.string.upgrade_apk_not_found);
                    UpgradeActivity.this.waitFinish();
                    return;
                case 4115:
                    UpgradeActivity.this.showUpgradeDialog(C0221R.string.upgrade_apk_sign_error);
                    UpgradeActivity.this.waitFinish();
                    return;
                case 4116:
                    UpgradeActivity.this.showUpgradeDialog(C0221R.string.upgrade_apk_not_valid);
                    UpgradeActivity.this.waitFinish();
                    return;
                case 4117:
                    UpgradeActivity.this.showUpgradeDialog(C0221R.string.upgrade_apk_verify_error);
                    UpgradeActivity.this.waitFinish();
                    return;
                case 4118:
                    UpgradeActivity.this.showUpgradeDialog(C0221R.string.upgrade_verify_package_fail);
                    UpgradeActivity.this.waitFinish();
                    return;
                default:
                    return;
            }
        }
    };
    private LoadingDialog mLoadingDialog;
    private UpgradeInitViewContentObserver upgradeInitViewContentObserver;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0221R.layout.layout_base);
        this.upgradeInitViewContentObserver = new UpgradeInitViewContentObserver(new Handler());
        getContentResolver().registerContentObserver(SoftwareStatusCacheProvider.getInstance().getUriFor("upgrade"), false, this.upgradeInitViewContentObserver);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, false);
    }

    /* access modifiers changed from: protected */
    public void initView() {
        if (SoftwareStatusCacheProvider.getInstance().getUpgradeProgress() != null) {
            Log.e("UpgradeStatus", new StringBuilder(String.valueOf(SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getStatus())).toString());
            switch (SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getStatus()) {
                case 2:
                    this.mHandler.sendEmptyMessage(4097);
                    break;
                case 3:
                    this.mHandler.sendEmptyMessage(4098);
                    break;
                case 4:
                    this.mHandler.sendEmptyMessage(4099);
                    break;
                case 5:
                    this.mHandler.sendEmptyMessage(BaseActivity.MSG_NFC_CARD_AWAY);
                    break;
                case 6:
                    this.mHandler.sendEmptyMessage(BaseActivity.MSG_NFC_DISMISS_MONEY);
                    break;
                case 8:
                    this.mHandler.sendEmptyMessage(4102);
                    break;
            }
            Log.e("UpgradeErrorCode", new StringBuilder(String.valueOf(SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getError().getCode())).toString());
            switch (SoftwareStatusCacheProvider.getInstance().getUpgradeProgress().getError().getCode()) {
                case 60000:
                    this.mHandler.sendEmptyMessage(4103);
                    return;
                case ErrorCode.EC_UPGRADE_NOT_SUPPORTED_VERSION:
                    this.mHandler.sendEmptyMessage(4104);
                    return;
                case ErrorCode.EC_UPGRADE_DOWNLOAD_FAIL:
                    this.mHandler.sendEmptyMessage(4105);
                    return;
                case ErrorCode.EC_UPGRADE_NOT_INTEGRATED:
                    this.mHandler.sendEmptyMessage(4112);
                    return;
                case ErrorCode.EC_UPGRADE_UNZIP_FAIL:
                    this.mHandler.sendEmptyMessage(4113);
                    return;
                case ErrorCode.EC_UPGRADE_APK_NOT_FOUND:
                    this.mHandler.sendEmptyMessage(4114);
                    return;
                case ErrorCode.EC_UPGRADE_APK_SIGN_ERROR:
                    this.mHandler.sendEmptyMessage(4115);
                    return;
                case ErrorCode.EC_UPGRADE_APK_NOT_VALID:
                    this.mHandler.sendEmptyMessage(4116);
                    return;
                case ErrorCode.EC_UPGRADE_APK_VERIFY_ERROR:
                    this.mHandler.sendEmptyMessage(4117);
                    return;
                case ErrorCode.EC_UPGRADE_FIREWARE_VERIFY_ERROR:
                    this.mHandler.sendEmptyMessage(4118);
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void waitFinish() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Utils.skipNfcQrcode(UpgradeActivity.context);
                UpgradeActivity.this.finish();
            }
        }, 2000);
    }

    /* access modifiers changed from: private */
    public void showUpgradeDialog(int resId) {
        if (this.mLoadingDialog == null) {
            this.mLoadingDialog = LoadingDialog.createDialog(this, getString(resId));
        } else {
            this.mLoadingDialog.changeLoadingText(getString(resId));
        }
        if (this.mLoadingDialog != null && !this.mLoadingDialog.isShowing()) {
            this.mLoadingDialog.show();
        }
    }

    /* access modifiers changed from: private */
    public void showUpgradeDialog(int resId, int progress) {
        if (this.mLoadingDialog == null) {
            this.mLoadingDialog = LoadingDialog.createDialog(this, getString(resId, new Object[]{Integer.valueOf(progress)}));
        } else {
            this.mLoadingDialog.changeLoadingText(getString(resId, new Object[]{Integer.valueOf(progress)}));
        }
        if (this.mLoadingDialog != null && !this.mLoadingDialog.isShowing()) {
            this.mLoadingDialog.show();
        }
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.upgrade.UpgradeActivity$UpgradeInitViewContentObserver */
    public class UpgradeInitViewContentObserver extends ContentObserver {
        public UpgradeInitViewContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            UpgradeActivity.this.initView();
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Log.d("UpgradeActivity", "onResume");
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Log.d("UpgradeActivity", "onPause");
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Log.d("UpgradeActivity", "onDestroy");
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Log.d("UpgradeActivity", "onDestroy");
        this.mHandler.removeCallbacksAndMessages((Object) null);
        getContentResolver().unregisterContentObserver(this.upgradeInitViewContentObserver);
        if (this.mLoadingDialog != null) {
            this.mLoadingDialog.dismiss();
        }
    }

    public void onBackPressed() {
    }
}
