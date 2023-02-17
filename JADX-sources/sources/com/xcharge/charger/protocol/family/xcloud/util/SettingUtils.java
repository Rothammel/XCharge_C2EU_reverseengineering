package com.xcharge.charger.protocol.family.xcloud.util;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.Sequence;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.SetDirective;
import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.charger.data.bean.FeeRate;
import com.xcharge.charger.data.bean.PortFeeRate;
import com.xcharge.charger.data.bean.setting.AdvertSetting;
import com.xcharge.charger.data.bean.setting.GunLockSetting;
import com.xcharge.charger.data.bean.setting.PortSetting;
import com.xcharge.charger.data.bean.setting.UserDefineUISetting;
import com.xcharge.charger.data.bean.type.ADVERT_POLICY;
import com.xcharge.charger.data.bean.type.CONTENT_MEDIA_TYPE;
import com.xcharge.charger.data.bean.type.GUN_LOCK_MODE;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceContent;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceSetting;
import com.xcharge.charger.protocol.family.xcloud.bean.FeePolicy;
import com.xcharge.charger.protocol.family.xcloud.bean.Price;
import com.xcharge.charger.protocol.family.xcloud.bean.XCloudPort;
import com.xcharge.charger.protocol.family.xcloud.handler.XCloudProtocolAgent;
import com.xcharge.common.utils.HttpDownloadManager;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.MD5Utils;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.eclipse.paho.client.mqttv3.MqttTopic;

/* loaded from: classes.dex */
public class SettingUtils {
    public static FeePolicy feeRate2FeePolicy(FeeRate feeRate) {
        FeePolicy feePolicy = new FeePolicy();
        feePolicy.setId(Long.valueOf(Long.parseLong(feeRate.getFeeRateId())));
        Price fixedPrice = new Price();
        ArrayList<HashMap<String, Object>> powerPrice = feeRate.getPowerPrice();
        if (powerPrice != null && powerPrice.size() == 1) {
            int power = new BigDecimal(((Double) powerPrice.get(0).get("price")).doubleValue() * 100.0d).setScale(0, 4).intValue();
            fixedPrice.setPower(power);
        }
        ArrayList<HashMap<String, Object>> servicePrice = feeRate.getServicePrice();
        if (servicePrice != null && servicePrice.size() == 1) {
            int service = new BigDecimal(((Double) servicePrice.get(0).get("price")).doubleValue() * 100.0d).setScale(0, 4).intValue();
            fixedPrice.setService(service);
        }
        ArrayList<HashMap<String, Object>> delayPrice = feeRate.getDelayPrice();
        if (delayPrice != null && delayPrice.size() == 1) {
            int delay = new BigDecimal(((Double) delayPrice.get(0).get("price")).doubleValue() * 100.0d).setScale(0, 4).intValue();
            fixedPrice.setDelay(delay);
        }
        if (fixedPrice.getPower() > 0 || fixedPrice.getService() > 0 || fixedPrice.getDelay() > 0 || fixedPrice.getPark() > 0) {
            feePolicy.setFixedPrice(fixedPrice);
        }
        ArrayList<ArrayList<Integer>> timedPrice = new ArrayList<>();
        ArrayList<HashMap<String, Object>> timedPriceSections = null;
        if (powerPrice != null) {
            timedPriceSections = powerPrice;
        } else if (servicePrice != null) {
            timedPriceSections = servicePrice;
        } else if (delayPrice != null) {
            timedPriceSections = delayPrice;
        }
        if (timedPriceSections != null) {
            for (int i = 0; i < timedPriceSections.size(); i++) {
                HashMap<String, Object> priceSection = timedPriceSections.get(i);
                String begin = (String) priceSection.get("begin");
                String end = (String) priceSection.get("end");
                ArrayList<Integer> timedPriceSection = new ArrayList<>();
                timedPriceSection.add(Integer.valueOf(getHHmmInt(begin)));
                timedPriceSection.add(Integer.valueOf(getHHmmInt(end)));
                if (powerPrice != null) {
                    int price = new BigDecimal(((Double) powerPrice.get(i).get("price")).doubleValue() * 100.0d).setScale(0, 4).intValue();
                    timedPriceSection.add(Integer.valueOf(price));
                } else {
                    timedPriceSection.add(0);
                }
                if (servicePrice != null) {
                    int price2 = new BigDecimal(((Double) servicePrice.get(i).get("price")).doubleValue() * 100.0d).setScale(0, 4).intValue();
                    timedPriceSection.add(Integer.valueOf(price2));
                } else {
                    timedPriceSection.add(0);
                }
                if (delayPrice != null) {
                    int price3 = new BigDecimal(((Double) delayPrice.get(i).get("price")).doubleValue() * 100.0d).setScale(0, 4).intValue();
                    timedPriceSection.add(Integer.valueOf(price3));
                } else {
                    timedPriceSection.add(0);
                }
                timedPriceSection.add(0);
                timedPrice.add(timedPriceSection);
            }
        }
        if (timedPrice.size() > 0) {
            feePolicy.setTimedPrice(timedPrice);
        }
        return feePolicy;
    }

    public static FeeRate feePolicy2feeRate(FeePolicy feePolicy) {
        if (feePolicy.getId() == null) {
            Log.w("SettingUtils.feePolicy2feeRate", "illegal fee policy: " + feePolicy.toJson());
            return null;
        }
        FeeRate feeRate = new FeeRate();
        feeRate.setCurrency(CountrySettingCacheProvider.getInstance().getMoney());
        feeRate.setFeeRateId(String.valueOf(feePolicy.getId()));
        ArrayList<HashMap<String, Object>> powerPrice = new ArrayList<>();
        ArrayList<HashMap<String, Object>> servicePrice = new ArrayList<>();
        ArrayList<HashMap<String, Object>> delayPrice = new ArrayList<>();
        boolean isTimedPowerPrice = true;
        boolean isTimedSerivePrice = true;
        boolean isTimedDelayPrice = true;
        Price fixedPrice = feePolicy.getFixedPrice();
        if (fixedPrice != null) {
            if (fixedPrice.getPower() > 0) {
                HashMap<String, Object> fullSection = new HashMap<>();
                fullSection.put("begin", "00:00");
                fullSection.put("end", "24:00");
                fullSection.put("price", Double.valueOf(new BigDecimal(fixedPrice.getPower() / 100.0f).setScale(2, 4).doubleValue()));
                powerPrice.add(fullSection);
                feeRate.setPowerPrice(powerPrice);
                isTimedPowerPrice = false;
            }
            if (fixedPrice.getService() > 0) {
                HashMap<String, Object> fullSection2 = new HashMap<>();
                fullSection2.put("begin", "00:00");
                fullSection2.put("end", "24:00");
                fullSection2.put("price", Double.valueOf(new BigDecimal(fixedPrice.getService() / 100.0f).setScale(2, 4).doubleValue()));
                servicePrice.add(fullSection2);
                feeRate.setServicePrice(servicePrice);
                isTimedSerivePrice = false;
            }
            if (fixedPrice.getDelay() > 0) {
                HashMap<String, Object> fullSection3 = new HashMap<>();
                fullSection3.put("begin", "00:00");
                fullSection3.put("end", "24:00");
                fullSection3.put("price", Double.valueOf(new BigDecimal(fixedPrice.getDelay() / 100.0f).setScale(2, 4).doubleValue()));
                delayPrice.add(fullSection3);
                feeRate.setDelayPrice(delayPrice);
                isTimedDelayPrice = false;
            }
        }
        ArrayList<ArrayList<Integer>> timedPrice = feePolicy.getTimedPrice();
        if (timedPrice != null) {
            for (int i = 0; i < timedPrice.size(); i++) {
                ArrayList<Integer> section = timedPrice.get(i);
                int begin = section.get(0).intValue();
                int end = section.get(1).intValue();
                if (begin > end) {
                    ArrayList<Integer> section2 = new ArrayList<>();
                    section2.add(0);
                    section2.add(Integer.valueOf(end));
                    for (int j = 2; j < section.size(); j++) {
                        section2.add(section.get(j));
                    }
                    section.set(1, 2400);
                    end = section.get(1).intValue();
                    timedPrice.add(section2);
                }
                int beginHour = begin / 100;
                int beginMinute = begin % 100;
                int endHour = end / 100;
                int endMinute = end % 100;
                String beginStr = String.valueOf(String.format("%02d", Integer.valueOf(beginHour))) + ":" + String.format("%02d", Integer.valueOf(beginMinute));
                String endStr = String.valueOf(String.format("%02d", Integer.valueOf(endHour))) + ":" + String.format("%02d", Integer.valueOf(endMinute));
                int pricePos = 2;
                if (isTimedPowerPrice) {
                    HashMap<String, Object> powerSection = new HashMap<>();
                    powerSection.put("begin", beginStr);
                    powerSection.put("end", endStr);
                    powerSection.put("price", Double.valueOf(new BigDecimal(section.get(2).intValue() / 100.0f).setScale(2, 4).doubleValue()));
                    powerPrice.add(powerSection);
                    pricePos = 2 + 1;
                }
                if (isTimedSerivePrice) {
                    HashMap<String, Object> serviceSection = new HashMap<>();
                    serviceSection.put("begin", beginStr);
                    serviceSection.put("end", endStr);
                    serviceSection.put("price", Double.valueOf(new BigDecimal(section.get(pricePos).intValue() / 100.0f).setScale(2, 4).doubleValue()));
                    servicePrice.add(serviceSection);
                    pricePos++;
                }
                if (isTimedDelayPrice) {
                    HashMap<String, Object> delaySection = new HashMap<>();
                    delaySection.put("begin", beginStr);
                    delaySection.put("end", endStr);
                    delaySection.put("price", Double.valueOf(new BigDecimal(section.get(pricePos).intValue() / 100.0f).setScale(2, 4).doubleValue()));
                    delayPrice.add(delaySection);
                    int i2 = pricePos + 1;
                }
            }
            if (isTimedPowerPrice) {
                if (DCAPProxy.getInstance().checkFeeRateSection(powerPrice)) {
                    feeRate.setPowerPrice(powerPrice);
                } else {
                    Log.w("SettingUtils.feePolicy2feeRate", "illegal power fee policy: " + feePolicy.toJson());
                    return null;
                }
            }
            if (isTimedSerivePrice) {
                if (DCAPProxy.getInstance().checkFeeRateSection(servicePrice)) {
                    feeRate.setServicePrice(servicePrice);
                } else {
                    Log.w("SettingUtils.feePolicy2feeRate", "illegal service fee policy: " + feePolicy.toJson());
                    return null;
                }
            }
            if (isTimedDelayPrice) {
                if (DCAPProxy.getInstance().checkFeeRateSection(delayPrice)) {
                    feeRate.setDelayPrice(delayPrice);
                    return feeRate;
                }
                Log.w("SettingUtils.feePolicy2feeRate", "illegal delay fee policy: " + feePolicy.toJson());
                return null;
            }
            return feeRate;
        }
        return feeRate;
    }

    public static boolean setFeePolicy(DeviceSetting deviceSetting) {
        Set<String> portNos = HardwareStatusCacheProvider.getInstance().getPorts().keySet();
        ArrayList<FeePolicy> feePolicys = deviceSetting.getFeePolicy();
        if (feePolicys != null && feePolicys.size() > 0) {
            for (String portNo : portNos) {
                PortFeeRate portFeeRate = RemoteSettingCacheProvider.getInstance().getPortFeeRate(portNo);
                if (portFeeRate == null) {
                    portFeeRate = new PortFeeRate();
                }
                HashMap<String, FeeRate> feeRates = portFeeRate.getFeeRates();
                if (feeRates == null) {
                    feeRates = new HashMap<>();
                }
                ArrayList<FeePolicy> errorFeePolicys = new ArrayList<>();
                for (int i = 0; i < feePolicys.size(); i++) {
                    FeeRate feeRate = feePolicy2feeRate(feePolicys.get(i));
                    if (feeRate != null) {
                        feeRates.put(feeRate.getFeeRateId(), feeRate);
                    } else {
                        errorFeePolicys.add(feePolicys.get(i));
                    }
                }
                if (errorFeePolicys.size() > 0) {
                    XCloudProtocolAgent.getInstance().getLatestDeviceSettingError().setFeePolicy(errorFeePolicys);
                }
                portFeeRate.setFeeRates(feeRates);
                if (deviceSetting.getDefaultFeePolicy() != null) {
                    portFeeRate.setActiveFeeRateId(String.valueOf(deviceSetting.getDefaultFeePolicy()));
                }
                RemoteSettingCacheProvider.getInstance().updatePortFeeRate(portNo, portFeeRate);
            }
            return true;
        }
        return true;
    }

    public static int getHHmmInt(String time) {
        if ("00:00".equals(time)) {
            return 0;
        }
        return Integer.parseInt(time.replaceAll("\\:", "").replaceFirst("^0+", ""));
    }

    public static XCloudPort getXCloudPortInfo(String port, PortSetting portSetting) {
        XCloudPort portInfo = new XCloudPort();
        portInfo.setEnabled(Boolean.valueOf(portSetting.isEnable()));
        portInfo.setLockEnabled(Boolean.valueOf(!LOCK_STATUS.disable.equals(ChargeStatusCacheProvider.getInstance().getPortLockStatus(port))));
        portInfo.setPowerPhase(Integer.valueOf(HardwareStatusCacheProvider.getInstance().getHardwareStatus().getPhase().getPhase() == 2 ? 3 : 1));
        portInfo.setPowerSupplyPercent(Integer.valueOf(new BigDecimal(portSetting.getAmpPercent().intValue() / 100.0f).setScale(0, 4).intValue()));
        portInfo.setPowerSupply(Double.valueOf(new BigDecimal(RemoteSettingCacheProvider.getInstance().getChargeSetting().getAdjustAmp() * (portSetting.getAmpPercent().intValue() / 10000.0f)).setScale(2, 4).doubleValue()));
        return portInfo;
    }

    public static boolean setPorts(HashMap<String, XCloudPort> ports) {
        for (Map.Entry<String, XCloudPort> entry : ports.entrySet()) {
            String portNo = entry.getKey();
            XCloudPort port = entry.getValue();
            if (!setPort(portNo, port)) {
                Log.w("SettingUtils.setPorts", "faild to set port: " + portNo + ", data: " + port.toJson());
                return false;
            }
        }
        return true;
    }

    private static boolean setPort(String portNo, XCloudPort port) {
        int i;
        PortSetting portSetting = RemoteSettingCacheProvider.getInstance().getChargePortSetting(portNo);
        if (portSetting == null) {
            portSetting = new PortSetting();
            portSetting.setAmpPercent(Integer.valueOf(10000 / HardwareStatusCacheProvider.getInstance().getPorts().size()));
        }
        Integer portPowerSupplyPercent = port.getPowerSupplyPercent();
        if (portPowerSupplyPercent != null) {
            if (portPowerSupplyPercent.intValue() >= 0 && portPowerSupplyPercent.intValue() <= 100) {
                portSetting.setAmpPercent(Integer.valueOf(portPowerSupplyPercent.intValue() * 100));
            } else {
                HashMap<String, XCloudPort> ports = XCloudProtocolAgent.getInstance().getLatestDeviceSettingError().getPorts();
                if (ports == null) {
                    ports = new HashMap<>();
                }
                XCloudPort errorPort = ports.get(portNo);
                if (errorPort == null) {
                    errorPort = new XCloudPort();
                }
                errorPort.setPowerSupplyPercent(portPowerSupplyPercent);
                ports.put(portNo, errorPort);
                XCloudProtocolAgent.getInstance().getLatestDeviceSettingError().setPorts(ports);
            }
        }
        Boolean portEnabled = port.isEnabled();
        if (portEnabled != null) {
            portSetting.setEnable(portEnabled.booleanValue());
            ChargeStatusCacheProvider.getInstance().updatePortSwitch(portNo, portEnabled.booleanValue());
        }
        Boolean portLockEnabled = port.isLockEnabled();
        if (portLockEnabled != null) {
            GunLockSetting gunLockSetting = new GunLockSetting();
            GUN_LOCK_MODE gunMode = portSetting.getGunLockSetting().getMode();
            if (portLockEnabled.booleanValue()) {
                i = !GUN_LOCK_MODE.disable.equals(gunMode) ? gunMode.getMode() : 3;
            } else {
                i = 0;
            }
            gunLockSetting.setMode(GUN_LOCK_MODE.valueBy(i));
            portSetting.setGunLockSetting(gunLockSetting);
        }
        RemoteSettingCacheProvider.getInstance().updateChargePortSetting(portNo, portSetting);
        Double portPowerSupply = port.getPowerSupply();
        if (portPowerSupply != null) {
            int ampCapacity = ChargeStatusCacheProvider.getInstance().getAmpCapacity();
            if (portPowerSupply.doubleValue() >= 6.0d && portPowerSupply.doubleValue() <= ampCapacity) {
                int adjustAmp = new BigDecimal(portPowerSupply.doubleValue() / (portSetting.getAmpPercent().intValue() / 10000.0f)).setScale(0, 4).intValue();
                if (adjustAmp >= 6 && adjustAmp <= ampCapacity) {
                    RemoteSettingCacheProvider.getInstance().getChargeSetting().setAdjustAmp(adjustAmp);
                    if (LocalSettingCacheProvider.getInstance().hasLocalSetting()) {
                        int localAdjustAmp = LocalSettingCacheProvider.getInstance().getChargeSetting().getAdjustAmp();
                        if (localAdjustAmp != adjustAmp) {
                            LocalSettingCacheProvider.getInstance().getChargeSetting().setAdjustAmp(adjustAmp);
                            LocalSettingCacheProvider.getInstance().persist();
                        }
                    }
                    HashMap<String, Object> values = new HashMap<>();
                    values.put(ContentDB.ChargeTable.PORT, portNo);
                    values.put("value", String.valueOf(portPowerSupply));
                    setDCAPRequest(SetDirective.SET_ID_PORT_AMP_WORK, values);
                    ChargeStatusCacheProvider.getInstance().updateAdjustAmp(adjustAmp);
                    return true;
                }
                Log.w("SettingUtils.setPort", "illegal port adjust amp !!! adjustAmp: " + adjustAmp + ", ampCapacity: " + ampCapacity + ", port: " + portNo);
                HashMap<String, XCloudPort> ports2 = XCloudProtocolAgent.getInstance().getLatestDeviceSettingError().getPorts();
                if (ports2 == null) {
                    ports2 = new HashMap<>();
                }
                XCloudPort errorPort2 = ports2.get(portNo);
                if (errorPort2 == null) {
                    errorPort2 = new XCloudPort();
                }
                errorPort2.setPowerSupply(portPowerSupply);
                ports2.put(portNo, errorPort2);
                XCloudProtocolAgent.getInstance().getLatestDeviceSettingError().setPorts(ports2);
                return true;
            }
            Log.w("SettingUtils.setPort", "illegal remote powerSupply set value: " + portPowerSupply + ", ampCapacity: " + ampCapacity + ", port: " + portNo);
            HashMap<String, XCloudPort> ports3 = XCloudProtocolAgent.getInstance().getLatestDeviceSettingError().getPorts();
            if (ports3 == null) {
                ports3 = new HashMap<>();
            }
            XCloudPort errorPort3 = ports3.get(portNo);
            if (errorPort3 == null) {
                errorPort3 = new XCloudPort();
            }
            errorPort3.setPowerSupply(portPowerSupply);
            ports3.put(portNo, errorPort3);
            XCloudProtocolAgent.getInstance().getLatestDeviceSettingError().setPorts(ports3);
            return true;
        }
        return true;
    }

    public static DeviceContent getDeviceContent() {
        HashMap<String, ArrayList<ContentItem>> contents;
        DeviceContent deviceContent = new DeviceContent();
        UserDefineUISetting userDefineUISetting = RemoteSettingCacheProvider.getInstance().getUserDefineUISetting();
        if (userDefineUISetting != null) {
            ContentItem contentItem = userDefineUISetting.getCompany();
            if (contentItem != null) {
                ArrayList<ContentItem> company = new ArrayList<>();
                company.add(contentItem);
                deviceContent.setCompany(company);
            }
            ContentItem contentItem2 = userDefineUISetting.getLogo();
            if (contentItem2 != null) {
                ArrayList<ContentItem> logo = new ArrayList<>();
                logo.add(contentItem2);
                deviceContent.setLogo(logo);
            }
            String contentText = userDefineUISetting.getWelcome();
            if (!TextUtils.isEmpty(contentText)) {
                ArrayList<ContentItem> welcome = new ArrayList<>();
                ContentItem welcomeItem = new ContentItem();
                welcomeItem.setType(CONTENT_MEDIA_TYPE.text.getType());
                welcomeItem.setText(contentText);
                welcome.add(welcomeItem);
                deviceContent.setWelcome(welcome);
            }
            String contentText2 = userDefineUISetting.getScanHintTitle();
            if (!TextUtils.isEmpty(contentText2)) {
                ArrayList<ContentItem> scanHintTitle = new ArrayList<>();
                ContentItem scanHintTitleItem = new ContentItem();
                scanHintTitleItem.setType(CONTENT_MEDIA_TYPE.text.getType());
                scanHintTitleItem.setText(contentText2);
                scanHintTitle.add(scanHintTitleItem);
                deviceContent.setScanHintTitle(scanHintTitle);
            }
            String contentText3 = userDefineUISetting.getScanHintDesc();
            if (!TextUtils.isEmpty(contentText3)) {
                ArrayList<ContentItem> scanHintDesc = new ArrayList<>();
                ContentItem scanHintDescItem = new ContentItem();
                scanHintDescItem.setType(CONTENT_MEDIA_TYPE.text.getType());
                scanHintDescItem.setText(contentText3);
                scanHintDesc.add(scanHintDescItem);
                deviceContent.setScanHintDesc(scanHintDesc);
            }
        }
        AdvertSetting advertSetting = RemoteSettingCacheProvider.getInstance().getAdvertSetting();
        if (advertSetting != null && (contents = advertSetting.getContent()) != null) {
            ArrayList<ContentItem> scanAdvsiteContent = contents.get(ADVERT_POLICY.scanAdvsite.getPolicy());
            if (scanAdvsiteContent != null && scanAdvsiteContent.size() > 0) {
                deviceContent.setScanAdvsite(trimNullInAdvsite(scanAdvsiteContent));
            }
            ArrayList<ContentItem> pullAdvsiteContent = contents.get(ADVERT_POLICY.pullAdvsite.getPolicy());
            if (pullAdvsiteContent != null && pullAdvsiteContent.size() > 0) {
                deviceContent.setPullAdvsite(trimNullInAdvsite(pullAdvsiteContent));
            }
            ArrayList<ContentItem> wakeUpAdvsiteContent = contents.get(ADVERT_POLICY.wakeUpAdvsite.getPolicy());
            if (wakeUpAdvsiteContent != null && wakeUpAdvsiteContent.size() > 0) {
                deviceContent.setWakeUpAdvsite(trimNullInAdvsite(wakeUpAdvsiteContent));
            }
            ArrayList<ContentItem> idleAdvsiteContent = contents.get(ADVERT_POLICY.idleAdvsite.getPolicy());
            if (idleAdvsiteContent != null && idleAdvsiteContent.size() > 0) {
                deviceContent.setIdleAdvsite(trimNullInAdvsite(idleAdvsiteContent));
            }
            ArrayList<ContentItem> chargingAdvsiteContent = contents.get(ADVERT_POLICY.chargingAdvsite.getPolicy());
            if (chargingAdvsiteContent != null && chargingAdvsiteContent.size() > 0) {
                deviceContent.setChargingAdvsite(trimNullInAdvsite(chargingAdvsiteContent));
            }
        }
        return deviceContent;
    }

    private static ArrayList<ContentItem> trimNullInAdvsite(ArrayList<ContentItem> advsite) {
        ArrayList<ContentItem> trimed = new ArrayList<>();
        Iterator<ContentItem> it2 = advsite.iterator();
        while (it2.hasNext()) {
            ContentItem item = it2.next();
            if (item != null) {
                trimed.add(item);
            }
        }
        if (trimed.size() > 0) {
            return trimed;
        }
        return null;
    }

    private static boolean needDownloadContent(ContentItem remoteContent, ContentItem localContent) {
        if (localContent == null) {
            return true;
        }
        String remoteFileUrl = remoteContent.getFileUrl();
        String localFileUrl = localContent.getFileUrl();
        String localFilePath = localContent.getLocalPath();
        return TextUtils.isEmpty(localFileUrl) || !localFileUrl.equals(remoteFileUrl) || TextUtils.isEmpty(localFilePath) || !new File(localFilePath).exists();
    }

    private static boolean needDownloadContent(ContentItem remoteContent, int idx, ArrayList<ContentItem> localContentList) {
        if (localContentList == null || localContentList.size() == 0) {
            return true;
        }
        return needDownloadContent(remoteContent, localContentList.get(idx));
    }

    private static ContentItem cloneContentItem(ContentItem src) {
        if (src == null) {
            return null;
        }
        return src.m7clone();
    }

    private static ArrayList<ContentItem> cloneContentItemList(ArrayList<ContentItem> src) {
        if (src == null) {
            return null;
        }
        ArrayList<ContentItem> cloned = new ArrayList<>();
        Iterator<ContentItem> it2 = src.iterator();
        while (it2.hasNext()) {
            ContentItem item = it2.next();
            cloned.add(item.m7clone());
        }
        return cloned;
    }

    public static boolean setDeviceContent(Context context, DeviceContent deviceContent, final Long sid) {
        try {
            ArrayList<ContentItem> companyContent = deviceContent.getCompany();
            if (companyContent != null && companyContent.size() == 1) {
                final ContentItem localCompany = RemoteSettingCacheProvider.getInstance().getCompanyContent();
                final ContentItem company = companyContent.get(0);
                if (needDownloadContent(company, localCompany)) {
                    final String fileUrl = company.getFileUrl();
                    String rsrcType = company.getType();
                    if (!TextUtils.isEmpty(fileUrl) && !TextUtils.isEmpty(rsrcType)) {
                        String fileSuffix = CONTENT_MEDIA_TYPE.getFileSuffix(CONTENT_MEDIA_TYPE.valueBy(rsrcType));
                        final String toFile = "/data/data/com.xcharge.charger/download/resource/company-" + MD5Utils.MD5(fileUrl) + "." + fileSuffix;
                        HttpDownloadManager.getInstance().downloadFile(context, fileUrl, toFile, new HttpDownloadManager.DownLoadListener() { // from class: com.xcharge.charger.protocol.family.xcloud.util.SettingUtils.1
                            @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                            public void onDownLoadPercentage(long curPosition, long total) {
                            }

                            @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                            public void onDownLoadPercentage(int p) {
                            }

                            @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                            public void onDownLoadFail() {
                                Log.w("SettingUtils.setDeviceContent", "failed to download company resource: " + fileUrl);
                                LogUtils.cloudlog("failed to download company resource: " + fileUrl);
                                ArrayList<String> fileUrls = new ArrayList<>();
                                fileUrls.add(fileUrl);
                                DeviceError error = new DeviceError(DeviceError.DOWNLOAD_FAILED, null, null);
                                XCloudProtocolAgent.getInstance().ReportContentDownloadResult(sid, fileUrls, error);
                            }

                            @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                            public void onDownLoadComplete() {
                                Log.i("SettingUtils.setDeviceContent", "succeed to download company resource: " + fileUrl);
                                if (localCompany != null) {
                                    String oldFilePath = localCompany.getLocalPath();
                                    if (!TextUtils.isEmpty(oldFilePath) && !toFile.equals(oldFilePath)) {
                                        File oldFile = new File(oldFilePath);
                                        if (oldFile.isFile() && oldFile.exists()) {
                                            oldFile.delete();
                                        }
                                    }
                                }
                                RemoteSettingCacheProvider.getInstance().updateCompanyContent(company);
                                RemoteSettingCacheProvider.getInstance().updateCompanyResouce(toFile);
                                RemoteSettingCacheProvider.getInstance().persist();
                                ArrayList<String> fileUrls = new ArrayList<>();
                                fileUrls.add(fileUrl);
                                XCloudProtocolAgent.getInstance().ReportContentDownloadResult(sid, fileUrls, null);
                            }
                        });
                    }
                } else if (!TextUtils.isEmpty(company.getFileUrl())) {
                    ArrayList<String> fileUrls = new ArrayList<>();
                    fileUrls.add(company.getFileUrl());
                    XCloudProtocolAgent.getInstance().ReportContentDownloadResult(sid, fileUrls, null);
                }
            } else if (companyContent != null && companyContent.size() == 0) {
                RemoteSettingCacheProvider.getInstance().updateCompanyContent(null);
            }
            ArrayList<ContentItem> logoContent = deviceContent.getLogo();
            if (logoContent != null && logoContent.size() == 1) {
                final ContentItem localLogo = RemoteSettingCacheProvider.getInstance().getLogoContent();
                final ContentItem logo = logoContent.get(0);
                if (needDownloadContent(logo, localLogo)) {
                    final String fileUrl2 = logo.getFileUrl();
                    String rsrcType2 = logo.getType();
                    if (!TextUtils.isEmpty(fileUrl2) && !TextUtils.isEmpty(rsrcType2)) {
                        String fileSuffix2 = CONTENT_MEDIA_TYPE.getFileSuffix(CONTENT_MEDIA_TYPE.valueBy(rsrcType2));
                        final String toFile2 = "/data/data/com.xcharge.charger/download/resource/logo-" + MD5Utils.MD5(fileUrl2) + "." + fileSuffix2;
                        HttpDownloadManager.getInstance().downloadFile(context, fileUrl2, toFile2, new HttpDownloadManager.DownLoadListener() { // from class: com.xcharge.charger.protocol.family.xcloud.util.SettingUtils.2
                            @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                            public void onDownLoadPercentage(long curPosition, long total) {
                            }

                            @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                            public void onDownLoadPercentage(int p) {
                            }

                            @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                            public void onDownLoadFail() {
                                Log.w("SettingUtils.setDeviceContent", "failed to download logo resource: " + fileUrl2);
                                LogUtils.cloudlog("failed to download logo resource: " + fileUrl2);
                                ArrayList<String> fileUrls2 = new ArrayList<>();
                                fileUrls2.add(fileUrl2);
                                DeviceError error = new DeviceError(DeviceError.DOWNLOAD_FAILED, null, null);
                                XCloudProtocolAgent.getInstance().ReportContentDownloadResult(sid, fileUrls2, error);
                            }

                            @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                            public void onDownLoadComplete() {
                                Log.i("SettingUtils.setDeviceContent", "succeed to download logo resource: " + fileUrl2);
                                if (localLogo != null) {
                                    String oldFilePath = localLogo.getLocalPath();
                                    if (!TextUtils.isEmpty(oldFilePath) && !toFile2.equals(oldFilePath)) {
                                        File oldFile = new File(oldFilePath);
                                        if (oldFile.isFile() && oldFile.exists()) {
                                            oldFile.delete();
                                        }
                                    }
                                }
                                RemoteSettingCacheProvider.getInstance().updateLogoContent(logo);
                                RemoteSettingCacheProvider.getInstance().updateLogoResouce(toFile2);
                                RemoteSettingCacheProvider.getInstance().persist();
                                ArrayList<String> fileUrls2 = new ArrayList<>();
                                fileUrls2.add(fileUrl2);
                                XCloudProtocolAgent.getInstance().ReportContentDownloadResult(sid, fileUrls2, null);
                            }
                        });
                    }
                } else if (!TextUtils.isEmpty(logo.getFileUrl())) {
                    ArrayList<String> fileUrls2 = new ArrayList<>();
                    fileUrls2.add(logo.getFileUrl());
                    XCloudProtocolAgent.getInstance().ReportContentDownloadResult(sid, fileUrls2, null);
                }
            } else if (logoContent != null && logoContent.size() == 0) {
                RemoteSettingCacheProvider.getInstance().updateLogoContent(null);
            }
            ArrayList<ContentItem> welcomeContent = deviceContent.getWelcome();
            if (welcomeContent != null && welcomeContent.size() == 1) {
                RemoteSettingCacheProvider.getInstance().updateWelcomeContent(welcomeContent.get(0).getText());
            } else if (welcomeContent != null && welcomeContent.size() == 0) {
                RemoteSettingCacheProvider.getInstance().updateWelcomeContent(null);
            }
            ArrayList<ContentItem> scanHintTitleContent = deviceContent.getScanHintTitle();
            if (scanHintTitleContent != null && scanHintTitleContent.size() == 1) {
                RemoteSettingCacheProvider.getInstance().updateScanHintTitleContent(scanHintTitleContent.get(0).getText());
            } else if (scanHintTitleContent != null && scanHintTitleContent.size() == 0) {
                RemoteSettingCacheProvider.getInstance().updateScanHintTitleContent(null);
            }
            ArrayList<ContentItem> scanHintDescContent = deviceContent.getScanHintDesc();
            if (scanHintDescContent != null && scanHintDescContent.size() == 1) {
                RemoteSettingCacheProvider.getInstance().updateScanHintDescContent(scanHintDescContent.get(0).getText());
            } else if (scanHintDescContent != null && scanHintDescContent.size() == 0) {
                RemoteSettingCacheProvider.getInstance().updateScanHintDescContent(null);
            }
            ArrayList<ContentItem> scanAdvsiteContent = deviceContent.getScanAdvsite();
            if (scanAdvsiteContent != null) {
                if (scanAdvsiteContent.size() > 0) {
                    for (int i = 0; i < scanAdvsiteContent.size(); i++) {
                        downloadAdvertResource(context, ADVERT_POLICY.scanAdvsite, scanAdvsiteContent.get(i), i, sid);
                    }
                } else {
                    RemoteSettingCacheProvider.getInstance().updateAdvertContent(ADVERT_POLICY.scanAdvsite, null);
                }
            }
            ArrayList<ContentItem> pullAdvsiteContent = deviceContent.getPullAdvsite();
            if (pullAdvsiteContent != null) {
                if (pullAdvsiteContent.size() > 0) {
                    for (int i2 = 0; i2 < pullAdvsiteContent.size(); i2++) {
                        downloadAdvertResource(context, ADVERT_POLICY.pullAdvsite, pullAdvsiteContent.get(i2), i2, sid);
                    }
                } else {
                    RemoteSettingCacheProvider.getInstance().updateAdvertContent(ADVERT_POLICY.pullAdvsite, null);
                }
            }
            ArrayList<ContentItem> wakeUpAdvsiteContent = deviceContent.getWakeUpAdvsite();
            if (wakeUpAdvsiteContent != null) {
                if (wakeUpAdvsiteContent.size() > 0) {
                    for (int i3 = 0; i3 < wakeUpAdvsiteContent.size(); i3++) {
                        downloadAdvertResource(context, ADVERT_POLICY.wakeUpAdvsite, wakeUpAdvsiteContent.get(i3), i3, sid);
                    }
                } else {
                    RemoteSettingCacheProvider.getInstance().updateAdvertContent(ADVERT_POLICY.wakeUpAdvsite, null);
                }
            }
            ArrayList<ContentItem> idleAdvsiteContent = deviceContent.getIdleAdvsite();
            if (idleAdvsiteContent != null) {
                if (idleAdvsiteContent.size() > 0) {
                    for (int i4 = 0; i4 < idleAdvsiteContent.size(); i4++) {
                        downloadAdvertResource(context, ADVERT_POLICY.idleAdvsite, idleAdvsiteContent.get(i4), i4, sid);
                    }
                } else {
                    RemoteSettingCacheProvider.getInstance().updateAdvertContent(ADVERT_POLICY.idleAdvsite, null);
                }
            }
            ArrayList<ContentItem> chargingAdvsiteContent = deviceContent.getChargingAdvsite();
            if (chargingAdvsiteContent != null) {
                if (chargingAdvsiteContent.size() > 0) {
                    for (int i5 = 0; i5 < chargingAdvsiteContent.size(); i5++) {
                        downloadAdvertResource(context, ADVERT_POLICY.chargingAdvsite, chargingAdvsiteContent.get(i5), i5, sid);
                    }
                } else {
                    RemoteSettingCacheProvider.getInstance().updateAdvertContent(ADVERT_POLICY.chargingAdvsite, null);
                }
            }
            return true;
        } catch (Exception e) {
            Log.e("SettingUtils.setDeviceContent", "except: " + Log.getStackTraceString(e) + ", when set device content: " + deviceContent.toJson());
            return false;
        }
    }

    public static void setDCAPRequest(String setId, HashMap<String, Object> values) {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setSet_id(setId);
        SetDirective set = new SetDirective();
        set.setValues(values);
        String from = "server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        DCAPMessage setRequest = createRequest(from, "set", opt, set);
        DCAPProxy.getInstance().sendRequest(setRequest);
    }

    private static DCAPMessage createRequest(String from, String op, CAPDirectiveOption opt, Object directive) {
        CAPMessage requestCap = new CAPMessage();
        DCAPMessage request = new DCAPMessage();
        request.setFrom(from);
        request.setTo("device:sn/" + HardwareStatusCacheProvider.getInstance().getSn());
        request.setType("cap");
        request.setCtime(System.currentTimeMillis());
        request.setSeq(Sequence.getAgentDCAPSequence());
        requestCap.setOp(op);
        requestCap.setOpt(opt);
        requestCap.setData(directive);
        request.setData(requestCap);
        return request;
    }

    private static void downloadAdvertResource(Context context, final ADVERT_POLICY type, final ContentItem content, final int index, final Long sid) {
        final ArrayList<ContentItem> localAdvtSite = RemoteSettingCacheProvider.getInstance().getAdvertContent(type);
        if (needDownloadContent(content, index, localAdvtSite)) {
            final String fileUrl = content.getFileUrl();
            final String rsrcType = content.getType();
            if (!TextUtils.isEmpty(fileUrl) && !TextUtils.isEmpty(rsrcType)) {
                String fileSuffix = CONTENT_MEDIA_TYPE.getFileSuffix(CONTENT_MEDIA_TYPE.valueBy(rsrcType));
                final String toFile = "/data/data/com.xcharge.charger/download/advert/" + type.getPolicy() + MqttTopic.TOPIC_LEVEL_SEPARATOR + index + "-" + MD5Utils.MD5(fileUrl) + "." + fileSuffix;
                HttpDownloadManager.getInstance().downloadFile(context, fileUrl, toFile, new HttpDownloadManager.DownLoadListener() { // from class: com.xcharge.charger.protocol.family.xcloud.util.SettingUtils.3
                    @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                    public void onDownLoadPercentage(long curPosition, long total) {
                    }

                    @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                    public void onDownLoadPercentage(int p) {
                    }

                    @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                    public void onDownLoadFail() {
                        Log.w("SettingUtils.downloadAdvertResource", "failed to download advert resource: " + fileUrl + ", policy: " + type.getPolicy() + ", index: " + index);
                        LogUtils.cloudlog("failed to download advert resource: " + fileUrl + ", site: " + type.getPolicy() + ", index: " + index);
                        ArrayList<String> fileUrls = new ArrayList<>();
                        fileUrls.add(fileUrl);
                        DeviceError error = new DeviceError(DeviceError.DOWNLOAD_FAILED, null, null);
                        XCloudProtocolAgent.getInstance().ReportContentDownloadResult(sid, fileUrls, error);
                    }

                    @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                    public void onDownLoadComplete() {
                        int totalTime;
                        ContentItem localContent;
                        Log.i("SettingUtils.downloadAdvertResource", "succeed to download advert resource: " + fileUrl + ", policy: " + type.getPolicy() + ", index: " + index);
                        if (localAdvtSite != null && localAdvtSite.size() > index && (localContent = (ContentItem) localAdvtSite.get(index)) != null) {
                            String oldFilePath = localContent.getLocalPath();
                            if (!TextUtils.isEmpty(oldFilePath) && !toFile.equals(oldFilePath)) {
                                File oldFile = new File(oldFilePath);
                                if (oldFile.isFile() && oldFile.exists()) {
                                    oldFile.delete();
                                }
                            }
                        }
                        RemoteSettingCacheProvider.getInstance().updateAdvertContent(type, index, content);
                        if (CONTENT_MEDIA_TYPE.mp4.getType().equals(rsrcType)) {
                            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                            mmr.setDataSource(toFile);
                            String duration = mmr.extractMetadata(9);
                            if (!TextUtils.isEmpty(duration) && (totalTime = Integer.parseInt(duration)) > 0) {
                                int totalTime2 = totalTime / 1000;
                                ContentItem contentNew = content.m7clone();
                                contentNew.setDuration(totalTime2);
                                Log.i("SettingUtils.downloadAdvertResource", "mp4 advert resource duration: " + totalTime2 + "s, policy: " + type.getPolicy() + ", index: " + index);
                                RemoteSettingCacheProvider.getInstance().updateAdvertContent(type, index, contentNew);
                            }
                        }
                        RemoteSettingCacheProvider.getInstance().updateAdvertResouce(type, index, toFile);
                        RemoteSettingCacheProvider.getInstance().persist();
                        ArrayList<String> fileUrls = new ArrayList<>();
                        fileUrls.add(fileUrl);
                        XCloudProtocolAgent.getInstance().ReportContentDownloadResult(sid, fileUrls, null);
                    }
                });
            }
        } else if (!TextUtils.isEmpty(content.getFileUrl())) {
            if (localAdvtSite != null && localAdvtSite.size() > index) {
                ContentItem localContent = localAdvtSite.get(index);
                if (localContent.getDuration() != content.getDuration() && !CONTENT_MEDIA_TYPE.mp4.getType().equals(localContent.getType())) {
                    localContent.setDuration(content.getDuration());
                    RemoteSettingCacheProvider.getInstance().updateAdvertContent(type, index, localContent);
                    RemoteSettingCacheProvider.getInstance().persist();
                }
            }
            ArrayList<String> fileUrls = new ArrayList<>();
            fileUrls.add(content.getFileUrl());
            XCloudProtocolAgent.getInstance().ReportContentDownloadResult(sid, fileUrls, null);
        }
    }
}
