package com.xcharge.charger.core.handler;

import android.content.Context;
import android.os.RecoverySystem;
import android.util.Log;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.UpgradeData;
import com.xcharge.charger.data.bean.UpgradeProgress;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.utils.ContextUtils;
import com.xcharge.common.utils.FileUtils;
import com.xcharge.common.utils.LogUtils;
import java.io.File;

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
    /* access modifiers changed from: private */
    public boolean isFirewareOk = false;
    private String upgradeFile = null;
    private String upgradePath = null;

    public static UpgradeAgent getInstance() {
        if (instance == null) {
            instance = new UpgradeAgent();
        }
        return instance;
    }

    public void init(Context context2) {
        this.context = context2;
    }

    public void destroy() {
    }

    /* JADX WARNING: Code restructure failed: missing block: B:27:0x00d4, code lost:
        r3 = unzipPackage();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0074, code lost:
        r3 = unzipPackage();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean update(com.xcharge.charger.data.bean.UpgradeData r14) {
        /*
            r13 = this;
            r9 = 0
            r8 = 200(0xc8, float:2.8E-43)
            r10 = 7
            r11 = 0
            r13.updateProgress(r8, r10, r11, r14)     // Catch:{ Exception -> 0x00f8 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00f8 }
            com.xcharge.charger.data.provider.SoftwareStatusCacheProvider r10 = com.xcharge.charger.data.provider.SoftwareStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = r10.getFirewareVer()     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = java.lang.String.valueOf(r10)     // Catch:{ Exception -> 0x00f8 }
            r8.<init>(r10)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = "-"
            java.lang.StringBuilder r8 = r8.append(r10)     // Catch:{ Exception -> 0x00f8 }
            com.xcharge.charger.data.provider.SoftwareStatusCacheProvider r10 = com.xcharge.charger.data.provider.SoftwareStatusCacheProvider.getInstance()     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = r10.getAppVer()     // Catch:{ Exception -> 0x00f8 }
            java.lang.StringBuilder r8 = r8.append(r10)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r5 = r8.toString()     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r4 = r14.getVersion()     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r1 = r14.getDependentVersion()     // Catch:{ Exception -> 0x00f8 }
            com.xcharge.charger.core.controller.ChargeController r8 = com.xcharge.charger.core.controller.ChargeController.getInstance()     // Catch:{ Exception -> 0x00f8 }
            r10 = 0
            r11 = 0
            boolean r8 = r8.hasCharge(r10, r11)     // Catch:{ Exception -> 0x00f8 }
            if (r8 == 0) goto L_0x0055
            java.lang.String r8 = "UpgradeAgent.update"
            java.lang.String r10 = "in charging now, will install update later !!!"
            android.util.Log.w(r8, r10)     // Catch:{ Exception -> 0x00f8 }
            r8 = 60010(0xea6a, float:8.4092E-41)
            r10 = 0
            r11 = 0
            r12 = 0
            r13.updateProgress(r8, r10, r11, r12)     // Catch:{ Exception -> 0x00f8 }
            r3 = r9
        L_0x0054:
            return r3
        L_0x0055:
            r3 = 0
            java.lang.String r0 = r14.getComponent()     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r7 = r14.getSrcPath()     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r8 = "all"
            boolean r8 = r8.equals(r0)     // Catch:{ Exception -> 0x00f8 }
            if (r8 == 0) goto L_0x00be
            java.lang.String r8 = "/cache/recovery/"
            r13.upgradePath = r8     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r8 = "/cache/recovery/update.zip"
            r13.upgradeFile = r8     // Catch:{ Exception -> 0x00f8 }
            boolean r3 = r13.prepareUpgradeResouces(r7)     // Catch:{ Exception -> 0x00f8 }
            if (r3 == 0) goto L_0x007e
            boolean r3 = r13.unzipPackage()     // Catch:{ Exception -> 0x00f8 }
            if (r3 == 0) goto L_0x007e
            boolean r3 = r13.verifySystemPackage()     // Catch:{ Exception -> 0x00f8 }
        L_0x007e:
            if (r3 == 0) goto L_0x0054
            java.lang.String r8 = "UpgradeAgent.update"
            java.lang.String r10 = "start install ..."
            android.util.Log.i(r8, r10)     // Catch:{ Exception -> 0x00f8 }
            boolean r8 = r13.setUpdateCmdline()     // Catch:{ Exception -> 0x00f8 }
            if (r8 != 0) goto L_0x00a5
            java.lang.String r8 = "UpgradeAgent.update"
            java.lang.String r10 = "failed set update cmd"
            android.util.Log.w(r8, r10)     // Catch:{ Exception -> 0x00f8 }
            android.content.Context r8 = r13.context     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = "power"
            java.lang.Object r8 = r8.getSystemService(r10)     // Catch:{ Exception -> 0x00f8 }
            android.os.PowerManager r8 = (android.os.PowerManager) r8     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = ""
            r8.reboot(r10)     // Catch:{ Exception -> 0x00f8 }
            r3 = r9
            goto L_0x0054
        L_0x00a5:
            android.content.Context r8 = r13.context     // Catch:{ Exception -> 0x00b2 }
            java.io.File r10 = new java.io.File     // Catch:{ Exception -> 0x00b2 }
            java.lang.String r11 = r13.upgradeFile     // Catch:{ Exception -> 0x00b2 }
            r10.<init>(r11)     // Catch:{ Exception -> 0x00b2 }
            android.os.RecoverySystem.installPackage(r8, r10)     // Catch:{ Exception -> 0x00b2 }
            goto L_0x0054
        L_0x00b2:
            r2 = move-exception
            java.lang.String r8 = "UpgradeAgent.update"
            java.lang.String r10 = android.util.Log.getStackTraceString(r2)     // Catch:{ Exception -> 0x00f8 }
            android.util.Log.e(r8, r10)     // Catch:{ Exception -> 0x00f8 }
            r3 = r9
            goto L_0x0054
        L_0x00be:
            java.lang.String r8 = "launcher"
            boolean r8 = r8.equals(r0)     // Catch:{ Exception -> 0x00f8 }
            if (r8 == 0) goto L_0x0114
            java.lang.String r8 = "/data/updatelauncher/"
            r13.upgradePath = r8     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r8 = "/data/updatelauncher/update.zip"
            r13.upgradeFile = r8     // Catch:{ Exception -> 0x00f8 }
            boolean r3 = r13.prepareUpgradeResouces(r7)     // Catch:{ Exception -> 0x00f8 }
            if (r3 == 0) goto L_0x00de
            boolean r3 = r13.unzipPackage()     // Catch:{ Exception -> 0x00f8 }
            if (r3 == 0) goto L_0x00de
            boolean r3 = r13.verifyLauncherPackage()     // Catch:{ Exception -> 0x00f8 }
        L_0x00de:
            if (r3 == 0) goto L_0x0054
            java.lang.String r8 = "UpgradeAgent.update"
            java.lang.String r10 = "update launcher by reboot"
            android.util.Log.i(r8, r10)     // Catch:{ Exception -> 0x00f8 }
            android.content.Context r8 = r13.context     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = "power"
            java.lang.Object r8 = r8.getSystemService(r10)     // Catch:{ Exception -> 0x00f8 }
            android.os.PowerManager r8 = (android.os.PowerManager) r8     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = ""
            r8.reboot(r10)     // Catch:{ Exception -> 0x00f8 }
            goto L_0x0054
        L_0x00f8:
            r2 = move-exception
            java.lang.String r8 = "UpgradeAgent.update"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder
            java.lang.String r11 = "except: "
            r10.<init>(r11)
            java.lang.String r11 = android.util.Log.getStackTraceString(r2)
            java.lang.StringBuilder r10 = r10.append(r11)
            java.lang.String r10 = r10.toString()
            android.util.Log.e(r8, r10)
            r3 = r9
            goto L_0x0054
        L_0x0114:
            java.lang.String r8 = "app"
            boolean r8 = r8.equals(r0)     // Catch:{ Exception -> 0x00f8 }
            if (r8 == 0) goto L_0x0054
            java.io.File r8 = new java.io.File     // Catch:{ Exception -> 0x00f8 }
            r8.<init>(r7)     // Catch:{ Exception -> 0x00f8 }
            boolean r8 = r13.verifyApk(r8)     // Catch:{ Exception -> 0x00f8 }
            if (r8 != 0) goto L_0x012a
            r3 = r9
            goto L_0x0054
        L_0x012a:
            java.lang.String r8 = "/system/app/"
            r13.upgradePath = r8     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r8 = "/system/app/xcharger.apk"
            r13.upgradeFile = r8     // Catch:{ Exception -> 0x00f8 }
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = "adb remount;adb push "
            r8.<init>(r10)     // Catch:{ Exception -> 0x00f8 }
            java.lang.StringBuilder r8 = r8.append(r7)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = " "
            java.lang.StringBuilder r8 = r8.append(r10)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = r13.upgradeFile     // Catch:{ Exception -> 0x00f8 }
            java.lang.StringBuilder r8 = r8.append(r10)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = ".tmp"
            java.lang.StringBuilder r8 = r8.append(r10)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r8 = r8.toString()     // Catch:{ Exception -> 0x00f8 }
            int r8 = com.xcharge.common.utils.FileUtils.execShell(r8)     // Catch:{ Exception -> 0x00f8 }
            if (r8 == 0) goto L_0x018b
            java.lang.String r8 = "UpgradeAgent.update"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r11 = "failed to push file: "
            r10.<init>(r11)     // Catch:{ Exception -> 0x00f8 }
            java.lang.StringBuilder r10 = r10.append(r7)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r11 = " to "
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r11 = r13.upgradeFile     // Catch:{ Exception -> 0x00f8 }
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r11 = ".tmp"
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x00f8 }
            android.util.Log.e(r8, r10)     // Catch:{ Exception -> 0x00f8 }
            r8 = 60000(0xea60, float:8.4078E-41)
            r10 = 0
            r11 = 0
            r12 = 0
            r13.updateProgress(r8, r10, r11, r12)     // Catch:{ Exception -> 0x00f8 }
            r3 = 0
            goto L_0x0054
        L_0x018b:
            java.lang.String r8 = "UpgradeAgent.update"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r11 = "update app by replace app file: "
            r10.<init>(r11)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r11 = r13.upgradeFile     // Catch:{ Exception -> 0x00f8 }
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x00f8 }
            android.util.Log.i(r8, r10)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r6 = "/data/data/com.xcharge.charger/files/apk_update_adb_shell.script"
            java.io.File r8 = new java.io.File     // Catch:{ Exception -> 0x00f8 }
            r8.<init>(r6)     // Catch:{ Exception -> 0x00f8 }
            boolean r8 = r8.exists()     // Catch:{ Exception -> 0x00f8 }
            if (r8 != 0) goto L_0x01db
            android.content.Context r8 = r13.context     // Catch:{ Exception -> 0x00f8 }
            r10 = 2131099649(0x7f060001, float:1.7811657E38)
            java.lang.String r11 = "apk_update_adb_shell.script"
            boolean r3 = com.xcharge.charger.utils.ContextUtils.getRawFileToContextPath(r8, r10, r11)     // Catch:{ Exception -> 0x00f8 }
            if (r3 != 0) goto L_0x01db
            java.lang.String r8 = "UpgradeAgent.update"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r11 = "failed to get adb shell script to "
            r10.<init>(r11)     // Catch:{ Exception -> 0x00f8 }
            java.lang.StringBuilder r10 = r10.append(r6)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x00f8 }
            android.util.Log.e(r8, r10)     // Catch:{ Exception -> 0x00f8 }
            r8 = 60000(0xea60, float:8.4078E-41)
            r10 = 0
            r11 = 0
            r12 = 0
            r13.updateProgress(r8, r10, r11, r12)     // Catch:{ Exception -> 0x00f8 }
            r3 = r9
            goto L_0x0054
        L_0x01db:
            java.lang.StringBuilder r8 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = "adb remount;adb shell < "
            r8.<init>(r10)     // Catch:{ Exception -> 0x00f8 }
            java.lang.StringBuilder r8 = r8.append(r6)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r8 = r8.toString()     // Catch:{ Exception -> 0x00f8 }
            int r8 = com.xcharge.common.utils.FileUtils.execShell(r8)     // Catch:{ Exception -> 0x00f8 }
            if (r8 == 0) goto L_0x0212
            java.lang.String r8 = "UpgradeAgent.update"
            java.lang.StringBuilder r10 = new java.lang.StringBuilder     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r11 = "failed to update file: "
            r10.<init>(r11)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r11 = r13.upgradeFile     // Catch:{ Exception -> 0x00f8 }
            java.lang.StringBuilder r10 = r10.append(r11)     // Catch:{ Exception -> 0x00f8 }
            java.lang.String r10 = r10.toString()     // Catch:{ Exception -> 0x00f8 }
            android.util.Log.e(r8, r10)     // Catch:{ Exception -> 0x00f8 }
            r8 = 60000(0xea60, float:8.4078E-41)
            r10 = 0
            r11 = 0
            r12 = 0
            r13.updateProgress(r8, r10, r11, r12)     // Catch:{ Exception -> 0x00f8 }
            r3 = 0
            goto L_0x0054
        L_0x0212:
            r3 = 1
            goto L_0x0054
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.core.handler.UpgradeAgent.update(com.xcharge.charger.data.bean.UpgradeData):boolean");
    }

    private boolean prepareUpgradeResouces(String srcPath) {
        File upgradeDir = new File(this.upgradePath);
        if (!upgradeDir.exists()) {
            upgradeDir.mkdirs();
        }
        if (FileUtils.deleteFiles(this.upgradePath) && FileUtils.fileChannelCopy(new File(srcPath), new File(this.upgradeFile)) > 0) {
            return true;
        }
        updateProgress(60000, 0, 0, (UpgradeData) null);
        return false;
    }

    private boolean setUpdateCmdline() {
        return FileUtils.saveStringToFile("--update_package=/cache/recovery/update.zip", "/cache/recovery/command", false);
    }

    private boolean unzipPackage() {
        try {
            Log.i("UpgradeAgent.unzipPackage", "begin unzip");
            updateProgress(200, 4, 0, (UpgradeData) null);
            if (FileUtils.Unzip(this.upgradeFile, this.upgradePath)) {
                Log.i("UpgradeAgent.unzipPackage", "unzip finished");
                return true;
            }
            Log.w("UpgradeAgent.unzipPackage", "unzip failed");
            LogUtils.applog("failed to unzip upgrade pack !!!");
            updateProgress(ErrorCode.EC_UPGRADE_UNZIP_FAIL, 0, 0, (UpgradeData) null);
            return false;
        } catch (Exception e) {
            Log.e("UpgradeAgent.unzipPackage", Log.getStackTraceString(e));
            LogUtils.applog("unzip upgrade pack exception: " + Log.getStackTraceString(e));
            updateProgress(ErrorCode.EC_UPGRADE_UNZIP_FAIL, 0, 0, (UpgradeData) null);
            return false;
        }
    }

    private boolean verifyApk(File apk) {
        try {
            Log.i("UpgradeAgent.verifyApk", "begin to verify apk");
            if (ContextUtils.verifyApkAvailability(this.context, apk.getAbsolutePath())) {
                String uninstallSign = ContextUtils.getAPKSignature(apk.getAbsolutePath());
                String localSign = ContextUtils.getAPPSignature(this.context);
                if (localSign.equals(uninstallSign)) {
                    return true;
                }
                Log.w("UpgradeAgent.verifyApk", "apk sign: " + uninstallSign + " is not equal to local os sign: " + localSign);
                LogUtils.applog("upgrade apk sign is not equal to local os sign !!!");
                updateProgress(ErrorCode.EC_UPGRADE_APK_SIGN_ERROR, 0, 0, (UpgradeData) null);
                return false;
            }
            Log.w("UpgradeAgent.verifyApk", "apk is invalid");
            LogUtils.applog("upgrade apk is invalid !!!");
            updateProgress(ErrorCode.EC_UPGRADE_APK_NOT_VALID, 0, 0, (UpgradeData) null);
            return false;
        } catch (Exception e) {
            Log.e("UpgradeAgent.verifyApk", Log.getStackTraceString(e));
            LogUtils.applog("verify upgrade apk exception: " + Log.getStackTraceString(e));
            updateProgress(ErrorCode.EC_UPGRADE_APK_VERIFY_ERROR, 0, 0, (UpgradeData) null);
            return false;
        }
    }

    private boolean verifyLauncherPackage() {
        try {
            Log.i("UpgradeAgent.verifyLauncherPackage", "begin check apk");
            File file = new File(LAUNCHER_UPDATE_APP_FILE_PATH);
            if (!file.exists()) {
                Log.w("UpgradeAgent.verifyLauncherPackage", "apk not found");
                updateProgress(ErrorCode.EC_UPGRADE_APK_NOT_FOUND, 0, 0, (UpgradeData) null);
                return false;
            } else if (ContextUtils.verifyApkAvailability(this.context, file.getAbsolutePath())) {
                String uninstallSign = ContextUtils.getAPKSignature(file.getAbsolutePath());
                String localSign = ContextUtils.getAPPSignature(this.context);
                if (localSign.equals(uninstallSign)) {
                    return true;
                }
                Log.w("UpgradeAgent.verifyLauncherPackage", "apk sign: " + uninstallSign + " is not equal to local sign: " + localSign);
                LogUtils.applog("upgrade launcher apk sign is not equal to local os sign !!!");
                updateProgress(ErrorCode.EC_UPGRADE_APK_SIGN_ERROR, 0, 0, (UpgradeData) null);
                return false;
            } else {
                Log.w("UpgradeAgent.verifyLauncherPackage", "apk is invalid");
                LogUtils.applog("upgrade launcher apk is invalid");
                updateProgress(ErrorCode.EC_UPGRADE_APK_NOT_VALID, 0, 0, (UpgradeData) null);
                return false;
            }
        } catch (Exception e) {
            Log.e("UpgradeAgent.verifyLauncherPackage", Log.getStackTraceString(e));
            LogUtils.applog("verify upgrade launcher apk exception: " + Log.getStackTraceString(e));
            updateProgress(ErrorCode.EC_UPGRADE_APK_VERIFY_ERROR, 0, 0, (UpgradeData) null);
            return false;
        }
    }

    private boolean verifySystemPackage() {
        this.isFirewareOk = false;
        try {
            Log.i("UpgradeAgent.verifySystemPackage", "begin check fireware");
            updateProgress(200, 5, 0, (UpgradeData) null);
            RecoverySystem.verifyPackage(new File(SYSTEM_UPDATE_FILE_PATH), new RecoverySystem.ProgressListener() {
                public void onProgress(int progress) {
                    UpgradeAgent.this.updateProgress(200, 6, progress, (UpgradeData) null);
                    if (progress == 100) {
                        UpgradeAgent.this.isFirewareOk = true;
                    }
                }
            }, (File) null);
        } catch (Exception e) {
            Log.e("UpgradeAgent.verifySysPackage", Log.getStackTraceString(e));
            LogUtils.applog("verify system upgrade pack exception: " + Log.getStackTraceString(e));
        }
        if (!this.isFirewareOk) {
            updateProgress(ErrorCode.EC_UPGRADE_FIREWARE_VERIFY_ERROR, 0, 0, (UpgradeData) null);
        }
        return this.isFirewareOk;
    }

    /* access modifiers changed from: private */
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
