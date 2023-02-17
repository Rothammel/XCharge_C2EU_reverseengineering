package com.xcharge.charger.protocol.family.xcloud.util;

import android.text.TextUtils;
import android.util.Log;
import com.google.gson.JsonObject;
import com.xcharge.charger.data.bean.type.CHARGE_STOP_CAUSE;
import com.xcharge.charger.data.bean.type.CHARGE_USER_TYPE;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.proxy.ChargeBill;
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import com.xcharge.charger.protocol.family.xcloud.bean.ChargeDetail;
import com.xcharge.charger.protocol.family.xcloud.bean.ChargeStopCondition;
import com.xcharge.charger.protocol.family.xcloud.bean.DeviceError;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportChargeEnded;
import com.xcharge.charger.protocol.family.xcloud.bean.device.ReportLocalChargeEnded;
import com.xcharge.charger.protocol.family.xcloud.type.EnumChargeBillStatus;
import com.xcharge.charger.protocol.xmsz.bean.XMSZHead;
import com.xcharge.common.utils.TimeUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.time.DateUtils;

/* loaded from: classes.dex */
public class BillUtils {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE;

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE;
        if (iArr == null) {
            iArr = new int[CHARGE_STOP_CAUSE.valuesCustom().length];
            try {
                iArr[CHARGE_STOP_CAUSE.car.ordinal()] = 7;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.fault.ordinal()] = 12;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.full.ordinal()] = 8;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.local_user.ordinal()] = 3;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.no_balance.ordinal()] = 9;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.plugout.ordinal()] = 6;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.reboot.ordinal()] = 11;
            } catch (NoSuchFieldError e7) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.remote_user.ordinal()] = 4;
            } catch (NoSuchFieldError e8) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.system_user.ordinal()] = 5;
            } catch (NoSuchFieldError e9) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.undefined.ordinal()] = 1;
            } catch (NoSuchFieldError e10) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.user.ordinal()] = 2;
            } catch (NoSuchFieldError e11) {
            }
            try {
                iArr[CHARGE_STOP_CAUSE.user_set.ordinal()] = 10;
            } catch (NoSuchFieldError e12) {
            }
            $SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE = iArr;
        }
        return iArr;
    }

    public static ReportLocalChargeEnded createReportLocalChargeEnded(ChargeBill chargeBill) {
        List<ChargeDetail> chargeDetails;
        ReportLocalChargeEnded reportLocalChargeEnded = new ReportLocalChargeEnded();
        reportLocalChargeEnded.setCardSourceId(chargeBill.getUser_code());
        if (chargeBill.getStart_ammeter() >= 0.0d) {
            reportLocalChargeEnded.setAmmeterStart(Double.valueOf(chargeBill.getStart_ammeter()));
        }
        if (chargeBill.getStop_ammeter() >= 0.0d) {
            reportLocalChargeEnded.setAmmeterEnd(Double.valueOf(chargeBill.getStop_ammeter()));
        }
        if (chargeBill.getStart_time() > 0) {
            reportLocalChargeEnded.setChargeStartTime(Long.valueOf(TimeUtils.getXCloudFormat(chargeBill.getStart_time(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone())));
        }
        if (chargeBill.getStop_time() > 0) {
            reportLocalChargeEnded.setChargeStopTime(Long.valueOf(TimeUtils.getXCloudFormat(chargeBill.getStop_time(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone())));
        }
        if (chargeBill.getFin_time() > 0) {
            reportLocalChargeEnded.setChargeEndTime(Long.valueOf(TimeUtils.getXCloudFormat(chargeBill.getFin_time(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone())));
        }
        int chargeInterval = 0;
        if (chargeBill.getStart_time() > 0 && chargeBill.getStop_time() >= chargeBill.getStart_time()) {
            chargeInterval = new BigDecimal((((float) (chargeBill.getStop_time() - chargeBill.getStart_time())) / 1000.0f) / 60.0f).setScale(0, 4).intValue();
        }
        reportLocalChargeEnded.setChargeInterval(Integer.valueOf(chargeInterval));
        reportLocalChargeEnded.setPowerCharged(Double.valueOf(chargeBill.getTotal_power()));
        reportLocalChargeEnded.setFeeTotal(Integer.valueOf(chargeBill.getTotal_fee()));
        reportLocalChargeEnded.setFeePower(Integer.valueOf(chargeBill.getPower_fee()));
        reportLocalChargeEnded.setFeeService(Integer.valueOf(chargeBill.getService_fee()));
        NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeBill.getUser_type());
        if (NFC_CARD_TYPE.U1.equals(nfcCardType)) {
            ChargeDetail u1ChargeDetail = new ChargeDetail();
            u1ChargeDetail.setStartTime(reportLocalChargeEnded.getChargeStartTime().longValue());
            u1ChargeDetail.setEndTime(reportLocalChargeEnded.getChargeStopTime().longValue());
            u1ChargeDetail.setPowerCharged(reportLocalChargeEnded.getPowerCharged().doubleValue());
            chargeDetails = new ArrayList<>();
            chargeDetails.add(u1ChargeDetail);
        } else {
            chargeDetails = createChargeDetails(chargeBill);
            if (chargeDetails == null || chargeDetails.size() == 0) {
                ChargeDetail chargeDetail = new ChargeDetail();
                chargeDetail.setStartTime(reportLocalChargeEnded.getChargeStartTime().longValue());
                chargeDetail.setEndTime(reportLocalChargeEnded.getChargeStopTime().longValue());
                chargeDetail.setPowerCharged(reportLocalChargeEnded.getPowerCharged().doubleValue());
                chargeDetails = new ArrayList<>();
                chargeDetails.add(chargeDetail);
            }
        }
        if (chargeDetails != null && chargeDetails.size() > 0) {
            chargeDetails.get(0).setStartTime(reportLocalChargeEnded.getChargeStartTime().longValue());
            chargeDetails.get(chargeDetails.size() - 1).setEndTime(reportLocalChargeEnded.getChargeEndTime().longValue());
            reportLocalChargeEnded.setChargeDetail(chargeDetails);
        }
        if (NFC_CARD_TYPE.U2.equals(nfcCardType)) {
            reportLocalChargeEnded.setCardBalance(Integer.valueOf((int) (chargeBill.getUser_balance() & XMSZHead.ID_BROADCAST)));
            if (chargeBill.getTotal_fee() <= 0 || chargeBill.getPay_flag() == 1) {
                reportLocalChargeEnded.setNotPaid(false);
            } else {
                reportLocalChargeEnded.setNotPaid(true);
            }
        }
        return reportLocalChargeEnded;
    }

    public static ReportChargeEnded createReportChargeEnded(ChargeBill chargeBill) {
        List<ChargeDetail> chargeDetails;
        ReportChargeEnded requestChargeEnded = new ReportChargeEnded();
        try {
            requestChargeEnded.setFeePolicyId(Long.valueOf(Long.parseLong(chargeBill.getFee_rate_id())));
        } catch (Exception e) {
            Log.w("BillUtils.createReportChargeEnded", "feeRate is not long number, ignore it !!!");
        }
        requestChargeEnded.setDevicePort(Integer.valueOf(Integer.parseInt(chargeBill.getPort())));
        requestChargeEnded.setStatus(EnumChargeBillStatus.ended.getStatus());
        requestChargeEnded.setPowerCharged(Double.valueOf(chargeBill.getTotal_power()));
        requestChargeEnded.setFeeTotal(Integer.valueOf(chargeBill.getTotal_fee()));
        requestChargeEnded.setFeePower(Integer.valueOf(chargeBill.getPower_fee()));
        requestChargeEnded.setFeeService(Integer.valueOf(chargeBill.getService_fee()));
        requestChargeEnded.setFeeDelay(Integer.valueOf(chargeBill.getDelay_fee()));
        if (chargeBill.getStart_ammeter() >= 0.0d) {
            requestChargeEnded.setAmmeterStart(Double.valueOf(chargeBill.getStart_ammeter()));
        }
        if (chargeBill.getStop_ammeter() >= 0.0d) {
            requestChargeEnded.setAmmeterEnd(Double.valueOf(chargeBill.getStop_ammeter()));
        }
        if (chargeBill.getStart_time() > 0) {
            requestChargeEnded.setChargeStartTime(Long.valueOf(TimeUtils.getXCloudFormat(chargeBill.getStart_time(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone())));
        }
        if (chargeBill.getStop_time() > 0) {
            requestChargeEnded.setChargeStopTime(Long.valueOf(TimeUtils.getXCloudFormat(chargeBill.getStop_time(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone())));
        }
        if (chargeBill.getFin_time() > 0) {
            requestChargeEnded.setChargeEndTime(Long.valueOf(TimeUtils.getXCloudFormat(chargeBill.getFin_time(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone())));
        }
        if (chargeBill.getDelay_start() > 0) {
            requestChargeEnded.setDelayFeeStartTime(Long.valueOf(TimeUtils.getXCloudFormat(chargeBill.getDelay_start(), RemoteSettingCacheProvider.getInstance().getProtocolTimezone())));
        }
        int chargeInterval = 0;
        if (chargeBill.getStart_time() > 0 && chargeBill.getStop_time() >= chargeBill.getStart_time()) {
            chargeInterval = new BigDecimal((((float) (chargeBill.getStop_time() - chargeBill.getStart_time())) / 1000.0f) / 60.0f).setScale(0, 4).intValue();
        }
        requestChargeEnded.setChargeInterval(Integer.valueOf(chargeInterval));
        requestChargeEnded.setDelayInterval(Integer.valueOf(new BigDecimal(chargeBill.getTotal_delay() / 60.0f).setScale(0, 4).intValue()));
        NFC_CARD_TYPE nfcCardType = getNFCTypeFromUserType(chargeBill.getUser_type());
        if (NFC_CARD_TYPE.U1.equals(nfcCardType)) {
            ChargeDetail u1ChargeDetail = new ChargeDetail();
            u1ChargeDetail.setStartTime(requestChargeEnded.getChargeStartTime().longValue());
            u1ChargeDetail.setEndTime(requestChargeEnded.getChargeStopTime().longValue());
            u1ChargeDetail.setPowerCharged(requestChargeEnded.getPowerCharged().doubleValue());
            chargeDetails = new ArrayList<>();
            chargeDetails.add(u1ChargeDetail);
        } else {
            chargeDetails = createChargeDetails(chargeBill);
            if (chargeDetails == null || chargeDetails.size() == 0) {
                ChargeDetail chargeDetail = new ChargeDetail();
                chargeDetail.setStartTime(requestChargeEnded.getChargeStartTime().longValue());
                chargeDetail.setEndTime(requestChargeEnded.getChargeStopTime().longValue());
                chargeDetail.setPowerCharged(requestChargeEnded.getPowerCharged().doubleValue());
                chargeDetails = new ArrayList<>();
                chargeDetails.add(chargeDetail);
            }
        }
        if (chargeDetails != null && chargeDetails.size() > 0) {
            chargeDetails.get(0).setStartTime(requestChargeEnded.getChargeStartTime().longValue());
            chargeDetails.get(chargeDetails.size() - 1).setEndTime(requestChargeEnded.getChargeEndTime().longValue());
            requestChargeEnded.setChargeDetail(chargeDetails);
        }
        String cause = DeviceError.OTHER;
        String errMsg = null;
        CHARGE_STOP_CAUSE stopCause = chargeBill.getStop_cause();
        if (stopCause != null) {
            switch ($SWITCH_TABLE$com$xcharge$charger$data$bean$type$CHARGE_STOP_CAUSE()[stopCause.ordinal()]) {
                case 2:
                case 4:
                    cause = DeviceError.USER_REMOTE;
                    break;
                case 3:
                    errMsg = "刷卡";
                    cause = DeviceError.USER_LOCAL;
                    break;
                case 5:
                    cause = DeviceError.ADMIN_REMOTE;
                    break;
                case 6:
                    errMsg = "拔枪";
                    cause = DeviceError.USER_LOCAL;
                    break;
                case 9:
                    cause = DeviceError.NO_FUND;
                    break;
                case PortRuntimeData.STATUS_EX_11 /* 11 */:
                    errMsg = "重启";
                    cause = DeviceError.USER_LOCAL;
                    break;
                case PortRuntimeData.STATUS_EX_12 /* 12 */:
                    cause = "ERROR";
                    break;
            }
        }
        DeviceError stopDeviceCause = new DeviceError(cause, errMsg, null);
        if (CHARGE_STOP_CAUSE.user_set.equals(stopCause)) {
            ChargeStopCondition userSetChargeStopCondition = new ChargeStopCondition();
            USER_TC_TYPE userTcType = chargeBill.getUser_tc_type();
            if (USER_TC_TYPE.fee.equals(userTcType)) {
                userSetChargeStopCondition.setFee(Integer.valueOf(Integer.parseInt(chargeBill.getUser_tc_value())));
            } else if (USER_TC_TYPE.power.equals(userTcType)) {
                userSetChargeStopCondition.setPower(Integer.valueOf(Double.valueOf(chargeBill.getUser_tc_value()).intValue()));
            } else if (USER_TC_TYPE.time.equals(userTcType)) {
                userSetChargeStopCondition.setInterval(Integer.valueOf(Integer.parseInt(chargeBill.getUser_tc_value()) / 60));
            } else {
                userSetChargeStopCondition = null;
            }
            stopDeviceCause = new DeviceError(DeviceError.AUTO_STOP, null, userSetChargeStopCondition);
        } else if (CHARGE_STOP_CAUSE.full.equals(stopCause)) {
            JsonObject deviceErrorData = new JsonObject();
            deviceErrorData.addProperty("code", "A000");
            stopDeviceCause = new DeviceError("ERROR", null, deviceErrorData);
        } else if (CHARGE_STOP_CAUSE.car.equals(stopCause)) {
            JsonObject deviceErrorData2 = new JsonObject();
            deviceErrorData2.addProperty("code", "501A");
            stopDeviceCause = new DeviceError("ERROR", null, deviceErrorData2);
        }
        requestChargeEnded.setCause(stopDeviceCause);
        if (NFC_CARD_TYPE.U2.equals(nfcCardType)) {
            requestChargeEnded.setCardBalance(Integer.valueOf((int) (chargeBill.getUser_balance() & XMSZHead.ID_BROADCAST)));
            if (chargeBill.getTotal_fee() <= 0 || chargeBill.getPay_flag() == 1) {
                requestChargeEnded.setNotPaid(false);
            } else {
                requestChargeEnded.setNotPaid(true);
            }
        }
        return requestChargeEnded;
    }

    private static List<ChargeDetail> createChargeDetails(ChargeBill chargeBill) {
        ChargeDetail chargeDetail;
        ChargeDetail chargeDetail2;
        ChargeDetail chargeDetail3;
        HashMap<String, ChargeDetail> chargeDetails = new HashMap<>();
        ArrayList<HashMap<String, Object>> powerInfo = chargeBill.getPower_info();
        ArrayList<HashMap<String, Object>> serviceInfo = chargeBill.getService_info();
        ArrayList<HashMap<String, Object>> delayInfo = chargeBill.getDelay_info();
        if (powerInfo != null) {
            int size = powerInfo.size();
            for (int i = 0; i < size; i++) {
                HashMap<String, Object> meterSection = powerInfo.get(i);
                long meterSectionBegin = Long.parseLong((String) meterSection.get("begin"));
                long meterSectionEnd = Long.parseLong((String) meterSection.get("end"));
                HashMap<String, Object> priceSection = getPriceSection(meterSectionBegin, meterSectionEnd, chargeBill.getFee_rate().getPowerPrice());
                if (priceSection == null) {
                    Log.w("BillUtils.createChargeDetails", "failed to get power price section, begin: " + meterSectionBegin + ", end: " + meterSectionEnd);
                } else {
                    long priceSectionBegin = ((Long) priceSection.get("begin")).longValue();
                    long priceSectionEnd = ((Long) priceSection.get("end")).longValue();
                    if (chargeDetails.containsKey(String.valueOf(priceSectionBegin))) {
                        ChargeDetail chargeDetail4 = chargeDetails.get(String.valueOf(priceSectionBegin));
                        chargeDetail3 = chargeDetail4;
                    } else {
                        chargeDetail3 = new ChargeDetail();
                    }
                    chargeDetail3.setStartTime(priceSectionBegin);
                    chargeDetail3.setEndTime(priceSectionEnd);
                    chargeDetail3.setPowerCharged(((Double) meterSection.get("meter")).doubleValue());
                    chargeDetail3.setFeePower(new BigDecimal(((Double) meterSection.get(com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition.TYPE_FEE)).doubleValue() * 100.0d).setScale(0, 4).intValue());
                    chargeDetail3.setFeeTotal(chargeDetail3.getFeePower() + chargeDetail3.getFeeService() + chargeDetail3.getFeeDelay() + chargeDetail3.getFeePark());
                    chargeDetails.put(String.valueOf(priceSectionBegin), chargeDetail3);
                }
            }
        }
        if (serviceInfo != null) {
            int size2 = serviceInfo.size();
            for (int i2 = 0; i2 < size2; i2++) {
                HashMap<String, Object> meterSection2 = serviceInfo.get(i2);
                long meterSectionBegin2 = Long.parseLong((String) meterSection2.get("begin"));
                long meterSectionEnd2 = Long.parseLong((String) meterSection2.get("end"));
                HashMap<String, Object> priceSection2 = getPriceSection(meterSectionBegin2, meterSectionEnd2, chargeBill.getFee_rate().getServicePrice());
                if (priceSection2 == null) {
                    Log.w("BillUtils.createChargeDetails", "failed to get service price section, begin: " + meterSectionBegin2 + ", end: " + meterSectionEnd2);
                } else {
                    long priceSectionBegin2 = ((Long) priceSection2.get("begin")).longValue();
                    long priceSectionEnd2 = ((Long) priceSection2.get("end")).longValue();
                    if (chargeDetails.containsKey(String.valueOf(priceSectionBegin2))) {
                        ChargeDetail chargeDetail5 = chargeDetails.get(String.valueOf(priceSectionBegin2));
                        chargeDetail2 = chargeDetail5;
                    } else {
                        chargeDetail2 = new ChargeDetail();
                    }
                    chargeDetail2.setStartTime(priceSectionBegin2);
                    chargeDetail2.setEndTime(priceSectionEnd2);
                    chargeDetail2.setFeeService(new BigDecimal(((Double) meterSection2.get(com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition.TYPE_FEE)).doubleValue() * 100.0d).setScale(0, 4).intValue());
                    chargeDetail2.setFeeTotal(chargeDetail2.getFeePower() + chargeDetail2.getFeeService() + chargeDetail2.getFeeDelay() + chargeDetail2.getFeePark());
                    chargeDetails.put(String.valueOf(priceSectionBegin2), chargeDetail2);
                }
            }
        }
        if (delayInfo != null) {
            int size3 = delayInfo.size();
            for (int i3 = 0; i3 < size3; i3++) {
                HashMap<String, Object> meterSection3 = delayInfo.get(i3);
                long meterSectionBegin3 = Long.parseLong((String) meterSection3.get("begin"));
                long meterSectionEnd3 = Long.parseLong((String) meterSection3.get("end"));
                HashMap<String, Object> priceSection3 = getPriceSection(meterSectionBegin3, meterSectionEnd3, chargeBill.getFee_rate().getDelayPrice());
                if (priceSection3 == null) {
                    Log.w("BillUtils.createChargeDetails", "failed to get delay price section, begin: " + meterSectionBegin3 + ", end: " + meterSectionEnd3);
                } else {
                    long priceSectionBegin3 = ((Long) priceSection3.get("begin")).longValue();
                    long priceSectionEnd3 = ((Long) priceSection3.get("end")).longValue();
                    if (chargeDetails.containsKey(String.valueOf(priceSectionBegin3))) {
                        ChargeDetail chargeDetail6 = chargeDetails.get(String.valueOf(priceSectionBegin3));
                        chargeDetail = chargeDetail6;
                    } else {
                        chargeDetail = new ChargeDetail();
                    }
                    chargeDetail.setStartTime(priceSectionBegin3);
                    chargeDetail.setEndTime(priceSectionEnd3);
                    chargeDetail.setDelayInterval(((Double) meterSection3.get("meter")).intValue());
                    chargeDetail.setFeeDelay(new BigDecimal(((Double) meterSection3.get(com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition.TYPE_FEE)).doubleValue() * 100.0d).setScale(0, 4).intValue());
                    chargeDetail.setFeeTotal(chargeDetail.getFeePower() + chargeDetail.getFeeService() + chargeDetail.getFeeDelay() + chargeDetail.getFeePark());
                    chargeDetails.put(String.valueOf(priceSectionBegin3), chargeDetail);
                }
            }
        }
        ArrayList<ChargeDetail> outOfSequence = new ArrayList<>(chargeDetails.values());
        if (outOfSequence.size() > 0) {
            ChargeDetail[] sections = new ChargeDetail[outOfSequence.size()];
            outOfSequence.toArray(sections);
            Arrays.sort(sections, new CompratorByLastModified());
            return Arrays.asList(sections);
        }
        return outOfSequence;
    }

    private static HashMap<String, Object> getPriceSection(long beginTs, long endTs, ArrayList<HashMap<String, Object>> priceSections) {
        HashMap<String, Object> inPriceSection = null;
        String begin = TimeUtils.getHHmmFormat(beginTs);
        String end = TimeUtils.getHHmmFormat(endTs);
        if (begin.compareTo(end) > 0) {
            end = "24:00";
        }
        Iterator<HashMap<String, Object>> it2 = priceSections.iterator();
        while (it2.hasNext()) {
            HashMap<String, Object> priceSection = it2.next();
            if (begin.compareTo((String) priceSection.get("begin")) >= 0 && end.compareTo((String) priceSection.get("end")) <= 0 && (!begin.equals(end) || !end.equals((String) priceSection.get("end")))) {
                inPriceSection = priceSection;
                break;
            }
        }
        if (inPriceSection == null) {
            return null;
        }
        long sectionBeginTs = TimeUtils.getDataTime(beginTs, (String) inPriceSection.get("begin"));
        String priceEnd = (String) inPriceSection.get("end");
        if ("24:00".equals(priceEnd)) {
            beginTs += DateUtils.MILLIS_PER_DAY;
            priceEnd = "00:00";
        }
        long sectionEndTs = TimeUtils.getDataTime(beginTs, priceEnd);
        HashMap<String, Object> ret = new HashMap<>();
        ret.put("begin", Long.valueOf(TimeUtils.getXCloudFormat(sectionBeginTs, RemoteSettingCacheProvider.getInstance().getProtocolTimezone())));
        ret.put("end", Long.valueOf(TimeUtils.getXCloudFormat(sectionEndTs, RemoteSettingCacheProvider.getInstance().getProtocolTimezone())));
        return ret;
    }

    private static NFC_CARD_TYPE getNFCTypeFromUserType(String userType) {
        if (TextUtils.isEmpty(userType)) {
            return null;
        }
        String[] userTypeSplit = userType.split("\\.");
        if (userTypeSplit.length == 2 && CHARGE_USER_TYPE.nfc.getUserType().equals(userTypeSplit[0])) {
            return NFC_CARD_TYPE.valueOf(userTypeSplit[1]);
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static class CompratorByLastModified implements Comparator<ChargeDetail> {
        CompratorByLastModified() {
        }

        @Override // java.util.Comparator
        public int compare(ChargeDetail f1, ChargeDetail f2) {
            long diff = f1.getStartTime() - f2.getEndTime();
            if (diff > 0) {
                return 1;
            }
            if (diff == 0) {
                return 0;
            }
            return -1;
        }
    }
}