package com.xcharge.charger.p006ui.p009c2.activity.utiles;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.p000v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.charger.data.bean.setting.LocalSetting;
import com.xcharge.charger.data.bean.setting.RemoteSetting;
import com.xcharge.charger.data.bean.setting.SwipeCardPermission;
import com.xcharge.charger.data.bean.type.ADVERT_POLICY;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.p006ui.api.bean.UIEventMessage;
import com.xcharge.charger.p006ui.p009c2.activity.advert.ViewPagerScroller;
import com.xcharge.charger.p006ui.p009c2.activity.charge.nfc.NFCChargeBalanceActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.nfc.NFCChargeFinActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.nfc.NFCChargeInitActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.nfc.NFCChargeWaittingStartActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.nfc.NFCChargingActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.nfc.NFCConfigPersnalCardActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.online.ChargeCompleteActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.online.ChargeCostActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.online.ChargingActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.online.QrcodeActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.online.ReservedActivity;
import com.xcharge.charger.p006ui.p009c2.activity.charge.online.WaittingStartChargeActivity;
import com.xcharge.charger.p006ui.p009c2.activity.data.InitImageLoader;
import com.xcharge.charger.p006ui.p009c2.activity.data.Price;
import com.xcharge.charger.p006ui.p009c2.activity.data.Variate;
import com.xcharge.charger.p006ui.p009c2.activity.fault.EnableActivity;
import com.xcharge.charger.p006ui.p009c2.activity.test.SetActivity;
import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.common.utils.FormatUtils;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.TimeUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* renamed from: com.xcharge.charger.ui.c2.activity.utiles.Utils */
public class Utils {
    public static void loadImage(String uri, ImageView iv, ImageLoadingListener imageLoadingListener, Context context) {
        try {
            InitImageLoader.getInstance(context).getLoader().displayImage("file:///" + uri, iv, new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisc(false).bitmapConfig(Bitmap.Config.RGB_565).build(), imageLoadingListener);
        } catch (Exception e) {
            String logTxt = "load image: " + uri + ", except: " + Log.getStackTraceString(e);
            Log.e("Utils.loadImage", logTxt);
            LogUtils.applog(logTxt);
        }
    }

    public static boolean getPricingStrategy(boolean isRemote) {
        Price.getInstance().setPowerPrice(0.0d);
        Price.getInstance().setServicePrice(0.0d);
        if (isRemote) {
            RemoteSetting remoteSetting = RemoteSettingCacheProvider.getInstance().getRemoteSetting();
            if (remoteSetting == null || remoteSetting.getFeeRateSetting() == null || remoteSetting.getFeeRateSetting().getPortsFeeRate() == null || remoteSetting.getFeeRateSetting().getPortsFeeRate().get("1").getFeeRates() == null || remoteSetting.getFeeRateSetting().getPortsFeeRate().get("1").getActiveFeeRateId() == null || remoteSetting.getFeeRateSetting().getPortsFeeRate().get("1").getFeeRates().get(remoteSetting.getFeeRateSetting().getPortsFeeRate().get("1").getActiveFeeRateId()) == null) {
                return false;
            }
            ArrayList<HashMap<String, Object>> powerPriceArrayList = remoteSetting.getFeeRateSetting().getPortsFeeRate().get("1").getFeeRates().get(remoteSetting.getFeeRateSetting().getPortsFeeRate().get("1").getActiveFeeRateId()).getPowerPrice();
            ArrayList<HashMap<String, Object>> servicePriceArrayList = remoteSetting.getFeeRateSetting().getPortsFeeRate().get("1").getFeeRates().get(remoteSetting.getFeeRateSetting().getPortsFeeRate().get("1").getActiveFeeRateId()).getServicePrice();
            if (powerPriceArrayList == null && servicePriceArrayList == null) {
                return false;
            }
            if (powerPriceArrayList != null) {
                Price.getInstance().setPowerPrice(getMoney(remoteSetting.getFeeRateSetting().getPortsFeeRate().get("1").getFeeRates().get(remoteSetting.getFeeRateSetting().getPortsFeeRate().get("1").getActiveFeeRateId()).getPowerPrice()));
            }
            if (servicePriceArrayList != null) {
                Price.getInstance().setServicePrice(getMoney(remoteSetting.getFeeRateSetting().getPortsFeeRate().get("1").getFeeRates().get(remoteSetting.getFeeRateSetting().getPortsFeeRate().get("1").getActiveFeeRateId()).getServicePrice()));
            }
            return true;
        }
        LocalSetting localSetting = LocalSettingCacheProvider.getInstance().getLocalSetting();
        if (localSetting == null || localSetting.getFeeRateSetting() == null || localSetting.getFeeRateSetting().getPortsFeeRate() == null || localSetting.getFeeRateSetting().getPortsFeeRate().get("1").getFeeRates() == null || localSetting.getFeeRateSetting().getPortsFeeRate().get("1").getActiveFeeRateId() == null || localSetting.getFeeRateSetting().getPortsFeeRate().get("1").getFeeRates().get(localSetting.getFeeRateSetting().getPortsFeeRate().get("1").getActiveFeeRateId()) == null) {
            return false;
        }
        ArrayList<HashMap<String, Object>> powerPriceArrayList2 = localSetting.getFeeRateSetting().getPortsFeeRate().get("1").getFeeRates().get(localSetting.getFeeRateSetting().getPortsFeeRate().get("1").getActiveFeeRateId()).getPowerPrice();
        ArrayList<HashMap<String, Object>> servicePriceArrayList2 = localSetting.getFeeRateSetting().getPortsFeeRate().get("1").getFeeRates().get(localSetting.getFeeRateSetting().getPortsFeeRate().get("1").getActiveFeeRateId()).getServicePrice();
        if (powerPriceArrayList2 == null && servicePriceArrayList2 == null) {
            return false;
        }
        if (powerPriceArrayList2 != null) {
            Price.getInstance().setPowerPrice(getMoney(localSetting.getFeeRateSetting().getPortsFeeRate().get("1").getFeeRates().get(localSetting.getFeeRateSetting().getPortsFeeRate().get("1").getActiveFeeRateId()).getPowerPrice()));
        }
        if (servicePriceArrayList2 != null) {
            Price.getInstance().setServicePrice(getMoney(localSetting.getFeeRateSetting().getPortsFeeRate().get("1").getFeeRates().get(localSetting.getFeeRateSetting().getPortsFeeRate().get("1").getActiveFeeRateId()).getServicePrice()));
        }
        return true;
    }

    public static double getMoney(ArrayList<HashMap<String, Object>> priceSections) {
        long now = System.currentTimeMillis();
        Iterator<HashMap<String, Object>> it = priceSections.iterator();
        while (it.hasNext()) {
            HashMap<String, Object> priceSection = it.next();
            if (now >= TimeUtils.getDataTime(now, (String) priceSection.get("begin")) && now < TimeUtils.getDataTime(now, (String) priceSection.get("end"))) {
                return ((Double) priceSection.get("price")).doubleValue();
            }
        }
        return 0.0d;
    }

    public static boolean fileIsEmpty(String strFile) {
        try {
            File file = new File(strFile);
            if (!file.isDirectory() || file.listFiles().length == 0) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean fileNameIsEqual(ArrayList<ContentItem> contentItems) {
        if (contentItems != null) {
            try {
                Iterator<ContentItem> it = contentItems.iterator();
                while (it.hasNext()) {
                    ContentItem item = it.next();
                    if (item != null) {
                        String localFile = item.getLocalPath();
                        if (!TextUtils.isEmpty(localFile) && new File(localFile).exists()) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean fileIsExists(String strFile) {
        try {
            if (new File(strFile).exists()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static String platformAdPath(String ad) {
        if (CHARGE_PLATFORM.anyo.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
            if (ad.equals(ADVERT_POLICY.scanAdvsite.getPolicy())) {
                return "/data/data/com.xcharge.charger/advert/scanAdvsite";
            }
            if (ad.equals(ADVERT_POLICY.pullAdvsite.getPolicy())) {
                return "/data/data/com.xcharge.charger/advert/pullAdvsite";
            }
            if (ad.equals(ADVERT_POLICY.wakeUpAdvsite.getPolicy())) {
                return "/data/data/com.xcharge.charger/advert/wakeUpAdvsite";
            }
            if (ad.equals(ADVERT_POLICY.idleAdvsite.getPolicy())) {
                return "/data/data/com.xcharge.charger/advert/idleAdvsite";
            }
        } else if (ad.equals(ADVERT_POLICY.scanAdvsite.getPolicy())) {
            return "/data/data/com.xcharge.charger/download/advert/scanAdvsite";
        } else {
            if (ad.equals(ADVERT_POLICY.pullAdvsite.getPolicy())) {
                return "/data/data/com.xcharge.charger/download/advert/pullAdvsite";
            }
            if (ad.equals(ADVERT_POLICY.wakeUpAdvsite.getPolicy())) {
                return "/data/data/com.xcharge.charger/download/advert/wakeUpAdvsite";
            }
            if (ad.equals(ADVERT_POLICY.idleAdvsite.getPolicy())) {
                return "/data/data/com.xcharge.charger/download/advert/idleAdvsite";
            }
        }
        return "";
    }

    public static String formatTime(long totalTime) {
        long totalTime2 = totalTime / 1000;
        return String.valueOf(String.format("%02d", new Object[]{Long.valueOf(totalTime2 / 3600)})) + ":" + String.format("%02d", new Object[]{Long.valueOf((totalTime2 % 3600) / 60)}) + ":" + String.format("%02d", new Object[]{Long.valueOf(totalTime2 % 60)});
    }

    public static synchronized String fromatTotalTime(long totalTime) {
        String str;
        synchronized (Utils.class) {
            str = String.valueOf(String.format("%02d", new Object[]{Long.valueOf(totalTime / 3600)})) + ":" + String.format("%02d", new Object[]{Long.valueOf((totalTime % 3600) / 60)}) + ":" + String.format("%02d", new Object[]{Long.valueOf(totalTime % 60)});
        }
        return str;
    }

    public static Bitmap createQRImage(String url, int width, int height) {
        if (url != null) {
            try {
                if (!"".equals(url) && url.length() >= 1) {
                    Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
                    hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
                    hints.put(EncodeHintType.MARGIN, 2);
                    BitMatrix bitMatrix = new QRCodeWriter().encode(url, BarcodeFormat.QR_CODE, width, height, hints);
                    int[] pixels = new int[(width * height)];
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            if (bitMatrix.get(x, y)) {
                                pixels[(y * width) + x] = -16777216;
                            } else {
                                pixels[(y * width) + x] = -1;
                            }
                        }
                    }
                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
                    bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
                    return bitmap;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public static boolean listIsEmpty(ArrayList<ContentItem> adContent) {
        boolean localPath = false;
        Iterator<ContentItem> it = adContent.iterator();
        while (it.hasNext()) {
            ContentItem adContents = it.next();
            if (adContents != null && !TextUtils.isEmpty(adContents.getLocalPath())) {
                localPath = true;
            }
        }
        return localPath;
    }

    public static boolean isZh(Context context) {
        if (context.getResources().getConfiguration().locale.getLanguage().endsWith("zh")) {
            return true;
        }
        return false;
    }

    public static void skipNfcQrcode(Context context) {
        if (ChargeStatusCacheProvider.getInstance().isCloudConnected()) {
            Variate.getInstance().setOnline(true);
            Intent intent = new Intent(context, QrcodeActivity.class);
            intent.addFlags(268435456);
            context.startActivity(intent);
            return;
        }
        CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        PLATFORM_CUSTOMER customer = SystemSettingCacheProvider.getInstance().getPlatformCustomer();
        if ((!CHARGE_PLATFORM.xcharge.equals(platform) || !PLATFORM_CUSTOMER.anyo_private.equals(customer)) && !CHARGE_PLATFORM.xmsz.equals(platform) && ((!CHARGE_PLATFORM.anyo.equals(platform) || PLATFORM_CUSTOMER.anyo_svw.equals(customer)) && !CHARGE_PLATFORM.cddz.equals(platform))) {
            Variate.getInstance().setOnline(false);
            Intent intent2 = new Intent(context, NFCChargeInitActivity.class);
            intent2.addFlags(268435456);
            context.startActivity(intent2);
            return;
        }
        Variate.getInstance().setOnline(true);
        Intent intent3 = new Intent(context, QrcodeActivity.class);
        intent3.addFlags(268435456);
        context.startActivity(intent3);
    }

    public static void anewSetPermitNFC(Context context) {
        if (getCurrentClassName(context).equals(QrcodeActivity.class.getName())) {
            setPermitNFC(true, true, true, false);
        } else if (getCurrentClassName(context).equals(WaittingStartChargeActivity.class.getName())) {
            setPermitNFC(false, false, false, false);
        } else if (getCurrentClassName(context).equals(ChargingActivity.class.getName())) {
            if (CHARGE_PLATFORM.ocpp.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
                setPermitNFC(true, false, false, false);
            } else {
                setPermitNFC(false, false, false, false);
            }
        } else if (getCurrentClassName(context).equals(ChargeCompleteActivity.class.getName())) {
            setPermitNFC(false, false, false, false);
        } else if (getCurrentClassName(context).equals(ChargeCostActivity.class.getName())) {
            setPermitNFC(false, false, false, false);
        } else if (getCurrentClassName(context).equals(NFCChargeInitActivity.class.getName())) {
            setPermitNFC(true, true, true, false);
        } else if (getCurrentClassName(context).equals(NFCChargeWaittingStartActivity.class.getName())) {
            setPermitNFC(true, false, false, false);
        } else if (getCurrentClassName(context).equals(NFCChargingActivity.class.getName())) {
            setPermitNFC(true, false, false, false);
        } else if (getCurrentClassName(context).equals(NFCChargeFinActivity.class.getName())) {
            setPermitNFC(true, false, false, false);
        } else if (getCurrentClassName(context).equals(NFCChargeBalanceActivity.class.getName())) {
            setPermitNFC(false, false, false, false);
        } else if (getCurrentClassName(context).equals(NFCConfigPersnalCardActivity.class.getName())) {
            setPermitNFC(true, false, true, false);
        } else if (getCurrentClassName(context).equals(ReservedActivity.class.getName())) {
            setPermitNFC(false, false, false, false);
        } else if (getCurrentClassName(context).equals(EnableActivity.class.getName())) {
            setPermitNFC(false, false, false, false);
        } else if (getCurrentClassName(context).equals(SetActivity.class.getName())) {
            setPermitNFC(false, false, false, false);
        }
    }

    public static void setPermitNFC(boolean ctrl, boolean setting, boolean binding, boolean test) {
        if (SystemSettingCacheProvider.getInstance().getPortSwipeCardPermission("1") != null) {
            SwipeCardPermission permission = SystemSettingCacheProvider.getInstance().getPortSwipeCardPermission("1");
            permission.setPermitChargeCtrl(ctrl);
            permission.setPermitSetting(setting);
            permission.setPermitBinding(binding);
            permission.setPermitTest(test);
            SystemSettingCacheProvider.getInstance().updatePortSwipeCardPermission("1", permission);
        }
    }

    public static String getCurrentClassName(Context context) {
        return ((ActivityManager) context.getSystemService(UIEventMessage.TYPE_UI_ACTIVITY)).getRunningTasks(1).get(0).topActivity.getClassName();
    }

    @SuppressLint({"NewApi"})
    public static void releaseScreenLock(Context context) {
        ((PowerManager) context.getSystemService(ChargeStopCondition.TYPE_POWER)).wakeUp(SystemClock.uptimeMillis());
    }

    public static void initViewPagerScroll(ViewPager viewPager) {
        try {
            Field mScroller = ViewPager.class.getDeclaredField("mScroller");
            mScroller.setAccessible(true);
            mScroller.set(viewPager, new ViewPagerScroller(viewPager.getContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void customizeUiBgColor(LinearLayout layout) {
        String uiBackgroundColor = SystemSettingCacheProvider.getInstance().getUiBackgroundColor();
        if (TextUtils.isEmpty(uiBackgroundColor) || uiBackgroundColor.length() != 7 || !uiBackgroundColor.startsWith(MqttTopic.MULTI_LEVEL_WILDCARD) || !FormatUtils.isHexString(uiBackgroundColor.substring(1))) {
            layout.setBackgroundResource(C0221R.C0222drawable.base_bg);
        } else {
            layout.setBackgroundColor(Color.parseColor(uiBackgroundColor));
        }
    }

    public static void screenshot(String name, View view) {
        try {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas();
            canvas.setBitmap(bitmap);
            view.draw(canvas);
            File file = new File("/data/data/com.xcharge.charger/download/screenshot/");
            if (!file.exists()) {
                file.mkdirs();
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(String.valueOf("/data/data/com.xcharge.charger/download/screenshot/") + TimeUtils.getXCloudFormat(System.currentTimeMillis(), (String) null) + ".png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
