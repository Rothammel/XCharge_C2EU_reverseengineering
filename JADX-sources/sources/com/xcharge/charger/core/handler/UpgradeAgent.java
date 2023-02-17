package com.xcharge.charger.core.handler;

import android.content.Context;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.util.Log;
import com.xcharge.charger.R;
import com.xcharge.charger.core.controller.ChargeController;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.UpgradeData;
import com.xcharge.charger.data.bean.UpgradeProgress;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.charger.utils.ContextUtils;
import com.xcharge.common.utils.FileUtils;
import com.xcharge.common.utils.LogUtils;
import java.io.File;
import org.apache.commons.lang3.StringUtils;

/* loaded from: classes.dex */
public class UpgradeAgent {
    public static final String APP_UPDATE_FILE_PATH = "/system/app/xcharger.apk";
    public static final String APP_UPDATE_PATH = "/system/app/";
    public static final String LAUNCHER_UPDATE_APP_FILE_PATH = "/data/updatelauncher/app/xcharger.apk";
    public static final String LAUNCHER_UPDATE_FILE_PATH = "/data/updatelauncher/update.zip";
    public static final String LAUNCHER_UPDATE_PATH = "/data/updatelauncher/";
    public static final String SYSTEM_UPDATE_FILE_PATH = "/cache/recovery/update.zip";
    public static final String SYSTEM_UPDATE_PATH = "/cache/recovery/";
    private static UpgradeAgent instance = null;
    private Context context = null;
    private boolean isFirewareOk = false;
    private String upgradeFile = null;
    private String upgradePath = null;

    public static UpgradeAgent getInstance() {
        if (instance == null) {
            instance = new UpgradeAgent();
        }
        return instance;
    }

    public void init(Context context) {
        this.context = context;
    }

    public void destroy() {
    }

    public boolean update(UpgradeData upgrade) {
        try {
            updateProgress(200, 7, 0, upgrade);
            String str = String.valueOf(SoftwareStatusCacheProvider.getInstance().getFirewareVer()) + "-" + SoftwareStatusCacheProvider.getInstance().getAppVer();
            upgrade.getVersion();
            upgrade.getDependentVersion();
            if (ChargeController.getInstance().hasCharge(null, false)) {
                Log.w("UpgradeAgent.update", "in charging now, will install update later !!!");
                updateProgress(ErrorCode.EC_UPGRADE_CHARGE_REFUSE, 0, 0, null);
                return false;
            }
            String component = upgrade.getComponent();
            String srcResource = upgrade.getSrcPath();
            if (UpgradeData.COM_ALL.equals(component)) {
                this.upgradePath = SYSTEM_UPDATE_PATH;
                this.upgradeFile = SYSTEM_UPDATE_FILE_PATH;
                boolean isOk = prepareUpgradeResouces(srcResource);
                if (isOk && (isOk = unzipPackage())) {
                    isOk = verifySystemPackage();
                }
                if (isOk) {
                    Log.i("UpgradeAgent.update", "start install ...");
                    if (!setUpdateCmdline()) {
                        Log.w("UpgradeAgent.update", "failed set update cmd");
                        ((PowerManager) this.context.getSystemService(ChargeStopCondition.TYPE_POWER)).reboot("");
                        return false;
                    }
                    try {
                        RecoverySystem.installPackage(this.context, new File(this.upgradeFile));
                        return isOk;
                    } catch (Exception e) {
                        Log.e("UpgradeAgent.update", Log.getStackTraceString(e));
                        return false;
                    }
                }
                return isOk;
            } else if (UpgradeData.COM_LAUNCHER.equals(component)) {
                this.upgradePath = LAUNCHER_UPDATE_PATH;
                this.upgradeFile = LAUNCHER_UPDATE_FILE_PATH;
                boolean isOk2 = prepareUpgradeResouces(srcResource);
                if (isOk2 && (isOk2 = unzipPackage())) {
                    isOk2 = verifyLauncherPackage();
                }
                if (isOk2) {
                    Log.i("UpgradeAgent.update", "update launcher by reboot");
                    ((PowerManager) this.context.getSystemService(ChargeStopCondition.TYPE_POWER)).reboot("");
                    return isOk2;
                }
                return isOk2;
            } else {
                if ("app".equals(component) && verifyApk(new File(srcResource))) {
                    this.upgradePath = APP_UPDATE_PATH;
                    this.upgradeFile = APP_UPDATE_FILE_PATH;
                    if (FileUtils.execShell("adb remount;adb push " + srcResource + StringUtils.SPACE + this.upgradeFile + ".tmp") != 0) {
                        Log.e("UpgradeAgent.update", "failed to push file: " + srcResource + " to " + this.upgradeFile + ".tmp");
                        updateProgress(60000, 0, 0, null);
                        return false;
                    }
                    Log.i("UpgradeAgent.update", "update app by replace app file: " + this.upgradeFile);
                    if (new File("/data/data/com.xcharge.charger/files/apk_update_adb_shell.script").exists() || ContextUtils.getRawFileToContextPath(this.context, R.raw.apk_update_adb_shell, "apk_update_adb_shell.script")) {
                        if (FileUtils.execShell("adb remount;adb shell < /data/data/com.xcharge.charger/files/apk_update_adb_shell.script") != 0) {
                            Log.e("UpgradeAgent.update", "failed to update file: " + this.upgradeFile);
                            updateProgress(60000, 0, 0, null);
                            return false;
                        }
                        return true;
                    }
                    Log.e("UpgradeAgent.update", "failed to get adb shell script to /data/data/com.xcharge.charger/files/apk_update_adb_shell.script");
                    updateProgress(60000, 0, 0, null);
                    return false;
                }
                return false;
            }
        } catch (Exception e2) {
            Log.e("UpgradeAgent.update", "except: " + Log.getStackTraceString(e2));
            return false;
        }
    }

    private boolean prepareUpgradeResouces(String srcPath) {
        File upgradeDir = new File(this.upgradePath);
        if (!upgradeDir.exists()) {
            upgradeDir.mkdirs();
        }
        if (FileUtils.deleteFiles(this.upgradePath)) {
            long size = FileUtils.fileChannelCopy(new File(srcPath), new File(this.upgradeFile));
            if (size > 0) {
                return true;
            }
        }
        updateProgress(60000, 0, 0, null);
        return false;
    }

    private boolean setUpdateCmdline() {
        return FileUtils.saveStringToFile("--update_package=/cache/recovery/update.zip", "/cache/recovery/command", false);
    }

    private boolean unzipPackage() {
        boolean isOk = false;
        try {
            Log.i("UpgradeAgent.unzipPackage", "begin unzip");
            updateProgress(200, 4, 0, null);
            if (FileUtils.Unzip(this.upgradeFile, this.upgradePath)) {
                Log.i("UpgradeAgent.unzipPackage", "unzip finished");
                isOk = true;
            } else {
                Log.w("UpgradeAgent.unzipPackage", "unzip failed");
                LogUtils.applog("failed to unzip upgrade pack !!!");
                updateProgress(ErrorCode.EC_UPGRADE_UNZIP_FAIL, 0, 0, null);
            }
        } catch (Exception e) {
            Log.e("UpgradeAgent.unzipPackage", Log.getStackTraceString(e));
            LogUtils.applog("unzip upgrade pack exception: " + Log.getStackTraceString(e));
            updateProgress(ErrorCode.EC_UPGRADE_UNZIP_FAIL, 0, 0, null);
        }
        return isOk;
    }

    private boolean verifyApk(File apk) {
        boolean isOk = false;
        try {
            Log.i("UpgradeAgent.verifyApk", "begin to verify apk");
            if (ContextUtils.verifyApkAvailability(this.context, apk.getAbsolutePath())) {
                String uninstallSign = ContextUtils.getAPKSignature(apk.getAbsolutePath());
                String localSign = ContextUtils.getAPPSignature(this.context);
                if (localSign.equals(uninstallSign)) {
                    isOk = true;
                } else {
                    Log.w("UpgradeAgent.verifyApk", "apk sign: " + uninstallSign + " is not equal to local os sign: " + localSign);
                    LogUtils.applog("upgrade apk sign is not equal to local os sign !!!");
                    updateProgress(ErrorCode.EC_UPGRADE_APK_SIGN_ERROR, 0, 0, null);
                }
            } else {
                Log.w("UpgradeAgent.verifyApk", "apk is invalid");
                LogUtils.applog("upgrade apk is invalid !!!");
                updateProgress(ErrorCode.EC_UPGRADE_APK_NOT_VALID, 0, 0, null);
            }
        } catch (Exception e) {
            Log.e("UpgradeAgent.verifyApk", Log.getStackTraceString(e));
            LogUtils.applog("verify upgrade apk exception: " + Log.getStackTraceString(e));
            updateProgress(ErrorCode.EC_UPGRADE_APK_VERIFY_ERROR, 0, 0, null);
        }
        return isOk;
    }

    private boolean verifyLauncherPackage() {
        boolean isOk = false;
        try {
            Log.i("UpgradeAgent.verifyLauncherPackage", "begin check apk");
            File file = new File(LAUNCHER_UPDATE_APP_FILE_PATH);
            if (!file.exists()) {
                Log.w("UpgradeAgent.verifyLauncherPackage", "apk not found");
                updateProgress(ErrorCode.EC_UPGRADE_APK_NOT_FOUND, 0, 0, null);
            } else if (ContextUtils.verifyApkAvailability(this.context, file.getAbsolutePath())) {
                String uninstallSign = ContextUtils.getAPKSignature(file.getAbsolutePath());
                String localSign = ContextUtils.getAPPSignature(this.context);
                if (localSign.equals(uninstallSign)) {
                    isOk = true;
                } else {
                    Log.w("UpgradeAgent.verifyLauncherPackage", "apk sign: " + uninstallSign + " is not equal to local sign: " + localSign);
                    LogUtils.applog("upgrade launcher apk sign is not equal to local os sign !!!");
                    updateProgress(ErrorCode.EC_UPGRADE_APK_SIGN_ERROR, 0, 0, null);
                }
            } else {
                Log.w("UpgradeAgent.verifyLauncherPackage", "apk is invalid");
                LogUtils.applog("upgrade launcher apk is invalid");
                updateProgress(ErrorCode.EC_UPGRADE_APK_NOT_VALID, 0, 0, null);
            }
        } catch (Exception e) {
            Log.e("UpgradeAgent.verifyLauncherPackage", Log.getStackTraceString(e));
            LogUtils.applog("verify upgrade launcher apk exception: " + Log.getStackTraceString(e));
            updateProgress(ErrorCode.EC_UPGRADE_APK_VERIFY_ERROR, 0, 0, null);
        }
        return isOk;
    }

    private boolean verifySystemPackage() {
        this.isFirewareOk = false;
        try {
            Log.i("UpgradeAgent.verifySystemPackage", "begin check fireware");
            updateProgress(200, 5, 0, null);
            RecoverySystem.verifyPackage(new File(SYSTEM_UPDATE_FILE_PATH), new RecoverySystem.ProgressListener() { // from class: com.xcharge.charger.core.handler.UpgradeAgent.1
                @Override // android.os.RecoverySystem.ProgressListener
                public void onProgress(int progress) {
                    UpgradeAgent.this.updateProgress(200, 6, progress, null);
                    if (progress == 100) {
                        UpgradeAgent.this.isFirewareOk = true;
                    }
                }
            }, null);
        } catch (Exception e) {
            Log.e("UpgradeAgent.verifySysPackage", Log.getStackTraceString(e));
            LogUtils.applog("verify system upgrade pack exception: " + Log.getStackTraceString(e));
        }
        if (!this.isFirewareOk) {
            updateProgress(ErrorCode.EC_UPGRADE_FIREWARE_VERIFY_ERROR, 0, 0, null);
        }
        return this.isFirewareOk;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateProgress(int error, int status, int progress, UpgradeData upgradeData) {
        UpgradeProgress upgradeProgress = SoftwareStatusCacheProvider.getInstance().getUpgradeProgress();
        upgradeProgress.setStage("update");
        upgradeProgress.setUpgradeData(upgradeData);
        if (error != 200) {
            upgradeProgress.setError(new ErrorCode(error));
        } else {
            upgradeProgress.setStatus(status);
            upgradeProgress.setProgress(progress);
        }
        SoftwareStatusCacheProvider.getInstance().updateUpgradeProgress(upgradeProgress);
    }
}
