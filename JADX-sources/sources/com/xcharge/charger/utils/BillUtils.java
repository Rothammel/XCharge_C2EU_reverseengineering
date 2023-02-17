package com.xcharge.charger.utils;

import com.xcharge.charger.protocol.monitor.bean.cap.ChargeStopCondition;
import com.xcharge.common.utils.TimeUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/* loaded from: classes.dex */
public class BillUtils {
    public static double calcIntervalCost(ArrayList<HashMap<String, Object>> priceSections, ArrayList<HashMap<String, Object>> meterSections) {
        double totalCost = 0.0d;
        Iterator<HashMap<String, Object>> it2 = meterSections.iterator();
        while (it2.hasNext()) {
            HashMap<String, Object> meterSection = it2.next();
            long meterSectionBeginTs = ((Long) meterSection.get("begin")).longValue();
            long meterSectionEndTs = ((Long) meterSection.get("end")).longValue();
            String meterSectionBegin = TimeUtils.getHHmmFormat(meterSectionBeginTs);
            String meterSectionEnd = TimeUtils.getHHmmFormat(meterSectionEndTs);
            if (meterSectionBegin.compareTo(meterSectionEnd) > 0) {
                meterSectionEnd = "24:00";
            }
            HashMap<String, Object> priceSection = getPriceSection(meterSectionBegin, meterSectionEnd, priceSections);
            double price = ((Double) priceSection.get("price")).doubleValue();
            double meter = ((Double) meterSection.get("meter")).doubleValue();
            double fee = price * meter;
            meterSection.put(ChargeStopCondition.TYPE_FEE, Double.valueOf(fee));
            totalCost += fee;
        }
        return new BigDecimal(totalCost).setScale(2, 4).doubleValue();
    }

    public static HashMap<String, Object> getPriceSection(String begin, String end, ArrayList<HashMap<String, Object>> priceSections) {
        Iterator<HashMap<String, Object>> it2 = priceSections.iterator();
        while (it2.hasNext()) {
            HashMap<String, Object> priceSection = it2.next();
            if (begin.compareTo((String) priceSection.get("begin")) >= 0 && end.compareTo((String) priceSection.get("end")) <= 0 && (!begin.equals(end) || !end.equals((String) priceSection.get("end")))) {
                return priceSection;
            }
        }
        return null;
    }

    public static ArrayList<HashMap<String, Object>> updateMeterSections(HashMap<String, Object> deltaMeter, ArrayList<HashMap<String, Object>> meterSections, ArrayList<HashMap<String, Object>> priceSections) {
        long deltaBeginTs = ((Long) deltaMeter.get("begin")).longValue();
        long deltaEndTs = ((Long) deltaMeter.get("end")).longValue();
        String deltaBegin = TimeUtils.getHHmmFormat(deltaBeginTs);
        String deltaEnd = TimeUtils.getHHmmFormat(deltaEndTs);
        if (deltaBegin.compareTo(deltaEnd) > 0) {
            deltaEnd = "24:00";
            deltaEndTs = TimeUtils.getDataTime(deltaEndTs, "00:00");
        }
        HashMap<String, Object> curPriceSection = getPriceSection(deltaBegin, deltaEnd, priceSections);
        if (curPriceSection == null) {
            Iterator<HashMap<String, Object>> it2 = priceSections.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                HashMap<String, Object> priceSection = it2.next();
                if (deltaBegin.compareTo((String) priceSection.get("end")) < 0 && deltaEnd.compareTo((String) priceSection.get("end")) >= 0) {
                    deltaEndTs = TimeUtils.getDataTime(deltaBeginTs, (String) priceSection.get("end"));
                    curPriceSection = priceSection;
                    break;
                }
            }
        }
        boolean isExisted = false;
        Iterator<HashMap<String, Object>> it3 = meterSections.iterator();
        while (true) {
            if (!it3.hasNext()) {
                break;
            }
            HashMap<String, Object> meterSection = it3.next();
            long meterSectionBeginTs = ((Long) meterSection.get("begin")).longValue();
            long meterSectionEndTs = ((Long) meterSection.get("end")).longValue();
            String meterSectionBegin = TimeUtils.getHHmmFormat(meterSectionBeginTs);
            String meterSectionEnd = TimeUtils.getHHmmFormat(meterSectionEndTs);
            if (meterSectionBegin.compareTo(meterSectionEnd) > 0) {
                meterSectionEnd = "24:00";
            }
            if (((String) curPriceSection.get("begin")).compareTo(meterSectionBegin) <= 0 && ((String) curPriceSection.get("end")).compareTo(meterSectionEnd) > 0) {
                meterSection.put("end", Long.valueOf(deltaEndTs));
                meterSection.put("meter", Double.valueOf(((Double) meterSection.get("meter")).doubleValue() + ((Double) deltaMeter.get("meter")).doubleValue()));
                isExisted = true;
                break;
            }
        }
        if (!isExisted) {
            HashMap<String, Object> section = new HashMap<>();
            section.put("begin", Long.valueOf(deltaBeginTs));
            section.put("end", Long.valueOf(deltaEndTs));
            section.put("meter", deltaMeter.get("meter"));
            meterSections.add(section);
        }
        return meterSections;
    }

    public static HashMap<String, Object> getPriceSection(long timestamp, ArrayList<HashMap<String, Object>> priceSections) {
        String ts = TimeUtils.getHHmmFormat(timestamp);
        Iterator<HashMap<String, Object>> it2 = priceSections.iterator();
        while (it2.hasNext()) {
            HashMap<String, Object> priceSection = it2.next();
            if (ts.compareTo((String) priceSection.get("begin")) >= 0 && ts.compareTo((String) priceSection.get("end")) < 0) {
                return priceSection;
            }
        }
        return null;
    }

    private static ArrayList<HashMap<String, Object>> normalizeMeterSections(ArrayList<HashMap<String, Object>> meterSections, ArrayList<HashMap<String, Object>> priceSections) {
        return null;
    }
}
