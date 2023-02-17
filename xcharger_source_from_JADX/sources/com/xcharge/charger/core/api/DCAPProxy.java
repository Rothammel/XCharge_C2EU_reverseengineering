package com.xcharge.charger.core.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.p000v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.core.api.bean.AckDirective;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.EventDirective;
import com.xcharge.charger.core.api.bean.cap.FinDirective;
import com.xcharge.charger.core.api.bean.cap.QueryDirective;
import com.xcharge.charger.core.api.bean.cap.SetDirective;
import com.xcharge.charger.core.type.FIN_MODE;
import com.xcharge.charger.data.bean.FeeRate;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.p004db.ContentDB;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.NetworkUtils;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class DCAPProxy {
    public static final String ACTION_CANCEL_U1_BIND_EVENT = "com.xcharge.charger.core.api.ACTION_CANCEL_U1_BIND_EVENT";
    public static final String ACTION_DCAP_SERIVCE_EVENT = "com.xcharge.charger.core.api.ACTION_DCAP_SERIVCE_EVENT";
    public static final String ACTION_DEVICE_REBOOT_EVENT = "com.xcharge.charger.core.api.ACTION_DEVICE_REBOOT_EVENT";
    public static final String DCAP_SERIVCE_EVENT_CREATED = "created";
    public static final String DCAP_SERIVCE_EVENT_DESTROYED = "destroyed";
    private static DCAPProxy instance = null;
    private ConfirmReceiver confirmReceiver = null;
    private Context context = null;
    private AtomicBoolean isInited = new AtomicBoolean(false);
    /* access modifiers changed from: private */
    public ConcurrentHashMap<String, SyncConfirm> waitConfirmRequests = null;

    public static synchronized DCAPProxy getInstance() {
        DCAPProxy dCAPProxy;
        synchronized (DCAPProxy.class) {
            if (instance == null) {
                instance = new DCAPProxy();
            }
            dCAPProxy = instance;
        }
        return dCAPProxy;
    }

    public void init(Context context2) {
        this.context = context2;
        this.waitConfirmRequests = new ConcurrentHashMap<>();
        this.confirmReceiver = new ConfirmReceiver(this, (ConfirmReceiver) null);
        LocalBroadcastManager.getInstance(this.context).registerReceiver(this.confirmReceiver, new IntentFilter(DCAPMessage.ACTION_DCAP_CONFIRM));
        this.isInited.set(true);
    }

    public void destroy() {
        this.isInited.set(false);
        LocalBroadcastManager.getInstance(this.context).unregisterReceiver(this.confirmReceiver);
        Enumeration<SyncConfirm> enumeration = this.waitConfirmRequests.elements();
        while (enumeration.hasMoreElements()) {
            enumeration.nextElement().getExitFlag().set(true);
        }
    }

    public void sendDCAPServiceEvent(String event) {
        Intent intent = new Intent(ACTION_DCAP_SERIVCE_EVENT);
        intent.putExtra("event", event);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    private class SyncConfirm {
        private DCAPMessage confirm;
        private AtomicBoolean exitFlag;

        private SyncConfirm() {
            this.exitFlag = new AtomicBoolean(false);
            this.confirm = null;
        }

        /* synthetic */ SyncConfirm(DCAPProxy dCAPProxy, SyncConfirm syncConfirm) {
            this();
        }

        public AtomicBoolean getExitFlag() {
            return this.exitFlag;
        }

        public DCAPMessage getConfirm() {
            return this.confirm;
        }

        public void setConfirm(DCAPMessage confirm2) {
            this.confirm = confirm2;
        }
    }

    private class ConfirmReceiver extends BroadcastReceiver {
        private ConfirmReceiver() {
        }

        /* synthetic */ ConfirmReceiver(DCAPProxy dCAPProxy, ConfirmReceiver confirmReceiver) {
            this();
        }

        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DCAPMessage.ACTION_DCAP_CONFIRM)) {
                DCAPMessage confirm = (DCAPMessage) new DCAPMessage().fromJson(intent.getStringExtra("body"));
                Long seq = null;
                Object data = confirm.getData();
                if (data != null && "cap".equals(confirm.getType())) {
                    CAPMessage cap = (CAPMessage) new CAPMessage().fromJson(JsonBean.ObjectToJson(data));
                    confirm.setData(cap);
                    seq = cap.getOpt().getSeq();
                }
                if (seq != null) {
                    SyncConfirm syncConfirm = (SyncConfirm) DCAPProxy.this.waitConfirmRequests.get(String.valueOf(confirm.getTo()) + seq.longValue());
                    if (syncConfirm != null) {
                        Log.i("DCAPProxy.ConfirmReceiver", "receive waiting confirm: " + confirm.toJson());
                        syncConfirm.setConfirm(confirm);
                        syncConfirm.getExitFlag().set(true);
                    }
                }
            }
        }
    }

    public DCAPMessage syncRequest(DCAPMessage request, int timeout) {
        if (!this.isInited.get() || !sendRequest(request)) {
            return null;
        }
        String strSeq = String.valueOf(request.getFrom()) + request.getSeq();
        SyncConfirm syncConfirm = new SyncConfirm(this, (SyncConfirm) null);
        this.waitConfirmRequests.put(strSeq, syncConfirm);
        int timeout2 = timeout * 1000;
        while (syncConfirm.getExitFlag().compareAndSet(false, false) && timeout2 > 0) {
            try {
                Thread.sleep(10);
                timeout2 -= 10;
            } catch (InterruptedException e) {
            }
        }
        this.waitConfirmRequests.remove(strSeq);
        return syncConfirm.getConfirm();
    }

    public boolean sendRequest(DCAPMessage request) {
        if (!this.isInited.get()) {
            Log.w("DCAPProxy.sendRequest", "DCAPProxy not inited !!!");
            return false;
        }
        try {
            Intent intent = new Intent(DCAPMessage.ACTION_DCAP_REQUEST);
            String body = request.toJson();
            intent.putExtra("body", body);
            LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
            Log.i("DCAPProxy.sendRequest", "send request: " + body);
            return true;
        } catch (Exception e) {
            Log.e("DCAPProxy.sendRequest", Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean sendConfirm(DCAPMessage confirm) {
        if (!this.isInited.get()) {
            Log.w("DCAPProxy.sendConfirm", "DCAPProxy not inited !!!");
            return false;
        }
        try {
            Intent intent = new Intent(DCAPMessage.ACTION_DCAP_CONFIRM);
            String body = confirm.toJson();
            intent.putExtra("body", body);
            LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
            Log.i("DCAPProxy.sendConfirm", "send confirm: " + body);
            return true;
        } catch (Exception e) {
            Log.e("DCAPProxy.sendConfirm", Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean sendIndicate(DCAPMessage indicate) {
        if (!this.isInited.get()) {
            Log.w("DCAPProxy.sendIndicate", "DCAPProxy not inited !!!");
            return false;
        }
        try {
            Intent intent = new Intent(DCAPMessage.ACTION_DCAP_INDICATE);
            String body = indicate.toJson();
            intent.putExtra("body", body);
            LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
            Log.i("DCAPProxy.sendIndicate", "send indicate: " + body);
            return true;
        } catch (Exception e) {
            Log.e("DCAPProxy.sendIndicate", Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean sendResponse(DCAPMessage response) {
        if (!this.isInited.get()) {
            Log.w("DCAPProxy.sendResponse", "DCAPProxy not inited !!!");
            return false;
        }
        try {
            Intent intent = new Intent(DCAPMessage.ACTION_DCAP_RESPONSE);
            String body = response.toJson();
            intent.putExtra("body", body);
            LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
            Log.i("DCAPProxy.sendResponse", "send response: " + body);
            return true;
        } catch (Exception e) {
            Log.e("DCAPProxy.sendResponse", Log.getStackTraceString(e));
            return false;
        }
    }

    public boolean sendResponse(DCAPMessage response, String type, String op, Object data) {
        if (!"cap".equals(type)) {
            return false;
        }
        CAPMessage responseCap = (CAPMessage) response.getData();
        responseCap.setOp(op);
        responseCap.setData(data);
        response.setType(type);
        return sendResponse(response);
    }

    public boolean sendConfirm(DCAPMessage confirm, String type, String op, Object data) {
        if (!"cap".equals(type)) {
            return false;
        }
        CAPMessage confimCap = (CAPMessage) confirm.getData();
        confimCap.setOp(op);
        confimCap.setData(data);
        confirm.setType(type);
        return sendConfirm(confirm);
    }

    public DCAPMessage createCAPConfirmByRequest(DCAPMessage request) {
        CAPMessage cap = (CAPMessage) request.getData();
        CAPDirectiveOption opt = cap.getOpt();
        DCAPMessage confirm = new DCAPMessage();
        confirm.setFrom(getConfirmFromByRequest(request.getFrom(), request.getTo()));
        confirm.setTo(request.getFrom());
        confirm.setType(request.getType());
        confirm.setCtime(System.currentTimeMillis());
        confirm.setSeq(Sequence.getCoreDCAPSequence());
        CAPDirectiveOption confirmOpt = new CAPDirectiveOption();
        confirmOpt.setOp(cap.getOp());
        confirmOpt.setSeq(Long.valueOf(request.getSeq()));
        confirmOpt.setEvent_id(opt.getEvent_id());
        confirmOpt.setCharge_id(opt.getCharge_id());
        confirmOpt.setQuery_id(opt.getQuery_id());
        confirmOpt.setSet_id(opt.getSet_id());
        confirmOpt.setAuth_id(opt.getAuth_id());
        confirmOpt.setPort_id(opt.getPort_id());
        confirmOpt.setCondition_id(opt.getCondition_id());
        CAPMessage confirmCAP = new CAPMessage();
        confirmCAP.setOpt(confirmOpt);
        confirm.setData(confirmCAP);
        return confirm;
    }

    public DCAPMessage createCAPResponseByIndcate(DCAPMessage indicate) {
        CAPMessage cap = (CAPMessage) indicate.getData();
        CAPDirectiveOption opt = cap.getOpt();
        DCAPMessage response = new DCAPMessage();
        response.setFrom(indicate.getTo());
        response.setTo(indicate.getFrom());
        response.setType(indicate.getType());
        response.setCtime(System.currentTimeMillis());
        response.setSeq(Sequence.getAgentDCAPSequence());
        CAPDirectiveOption responseOpt = new CAPDirectiveOption();
        responseOpt.setOp(cap.getOp());
        responseOpt.setSeq(Long.valueOf(indicate.getSeq()));
        responseOpt.setEvent_id(opt.getEvent_id());
        responseOpt.setCharge_id(opt.getCharge_id());
        responseOpt.setQuery_id(opt.getQuery_id());
        responseOpt.setSet_id(opt.getSet_id());
        responseOpt.setAuth_id(opt.getAuth_id());
        responseOpt.setPort_id(opt.getPort_id());
        responseOpt.setCondition_id(opt.getCondition_id());
        CAPMessage responseCAP = new CAPMessage();
        responseCAP.setOpt(responseOpt);
        response.setData(responseCAP);
        return response;
    }

    public String getCoreSendDCAPFrom(String to) {
        String sn = HardwareStatusCacheProvider.getInstance().getSn();
        if (to.startsWith("server") || to.startsWith("broadcast") || to.startsWith("user") || to.startsWith(SetDirective.SET_ID_DEVICE)) {
            return "device:sn/" + sn;
        }
        Log.e("DCAPProxy.getCoreSendDCAPFrom", "illegal to: " + to);
        return null;
    }

    public String getConfirmFromByRequest(String from, String to) {
        if (TextUtils.isEmpty(to)) {
            return getCoreSendDCAPFrom(from);
        }
        if (to.startsWith("broadcast")) {
            return getCoreSendDCAPFrom(from);
        }
        if (to.startsWith(SetDirective.SET_ID_DEVICE)) {
            return to;
        }
        Log.w("DCAPProxy.getConfirmFromByRequest", "unsupported to: " + to);
        return getCoreSendDCAPFrom(from);
    }

    public FeeRate formatFeeRate(ArrayList<ArrayList<Integer>> cardFmt) {
        if (cardFmt == null) {
            Log.w("DCAPProxy.formatFeeRate", "illegal params !!!");
            return null;
        }
        FeeRate feeRate = new FeeRate();
        ArrayList<HashMap<String, Object>> powerPrice = new ArrayList<>();
        ArrayList<HashMap<String, Object>> servicePrice = new ArrayList<>();
        ArrayList<HashMap<String, Object>> delayPrice = new ArrayList<>();
        for (int i = 0; i < cardFmt.size(); i++) {
            ArrayList<Integer> section = cardFmt.get(i);
            if (section.size() != 6) {
                Log.w("DCAPProxy.formatFeeRate", "illegal section length !!!");
                return null;
            }
            HashMap<String, Object> powerPriceSection = new HashMap<>();
            HashMap<String, Object> servicePriceSection = new HashMap<>();
            HashMap<String, Object> delayPriceSection = new HashMap<>();
            int begin = section.get(0).intValue();
            int end = section.get(1).intValue();
            int power = section.get(2).intValue();
            int service = section.get(3).intValue();
            int delay = section.get(4).intValue();
            if (begin < 0 || end < 0 || begin > 2400 || end > 2400 || power < 0 || service < 0 || delay < 0) {
                Log.w("DCAPProxy.formatFeeRate", "illegal section element !!!");
                return null;
            }
            if (end == 0) {
                end = 2400;
                section.set(1, 2400);
            }
            if (begin > end) {
                ArrayList<Integer> section2 = new ArrayList<>();
                section2.add((Integer) null);
                section2.add(Integer.valueOf(end));
                section2.add(Integer.valueOf(power));
                section2.add(Integer.valueOf(service));
                section2.add(Integer.valueOf(delay));
                section2.add(section.get(5));
                section.set(1, 2400);
                end = section.get(1).intValue();
                cardFmt.add(section2);
            }
            String beginStr = String.valueOf(String.format("%02d", new Object[]{Integer.valueOf(begin / 100)})) + ":" + String.format("%02d", new Object[]{Integer.valueOf(begin % 100)});
            String endStr = String.valueOf(String.format("%02d", new Object[]{Integer.valueOf(end / 100)})) + ":" + String.format("%02d", new Object[]{Integer.valueOf(end % 100)});
            double powerDouble = new BigDecimal(((double) power) / 100.0d).setScale(2, 4).doubleValue();
            double serviceDouble = new BigDecimal(((double) service) / 100.0d).setScale(2, 4).doubleValue();
            double delayDouble = new BigDecimal(((double) delay) / 100.0d).setScale(2, 4).doubleValue();
            powerPriceSection.put("begin", beginStr);
            powerPriceSection.put("end", endStr);
            powerPriceSection.put("price", Double.valueOf(powerDouble));
            powerPrice.add(powerPriceSection);
            servicePriceSection.put("begin", beginStr);
            servicePriceSection.put("end", endStr);
            servicePriceSection.put("price", Double.valueOf(serviceDouble));
            servicePrice.add(servicePriceSection);
            delayPriceSection.put("begin", beginStr);
            delayPriceSection.put("end", endStr);
            delayPriceSection.put("price", Double.valueOf(delayDouble));
            delayPrice.add(delayPriceSection);
        }
        if (powerPrice.size() > 0) {
            if (checkFeeRateSection(powerPrice)) {
                feeRate.setPowerPrice(powerPrice);
            } else {
                Log.w("DCAPProxy.formatFeeRate", "illegal power fee rate !!!");
                return null;
            }
        }
        if (servicePrice.size() > 0) {
            if (checkFeeRateSection(servicePrice)) {
                feeRate.setServicePrice(servicePrice);
            } else {
                Log.w("DCAPProxy.formatFeeRate", "illegal service fee rate !!!");
                return null;
            }
        }
        if (delayPrice.size() <= 0) {
            return feeRate;
        }
        if (checkFeeRateSection(delayPrice)) {
            feeRate.setDelayPrice(delayPrice);
            return feeRate;
        }
        Log.w("DCAPProxy.formatFeeRate", "illegal delay fee rate !!!");
        return null;
    }

    public boolean checkFeeRateSection(ArrayList<HashMap<String, Object>> priceSections) {
        long sum = 0;
        Iterator<HashMap<String, Object>> it = priceSections.iterator();
        while (it.hasNext()) {
            HashMap<String, Object> section = it.next();
            sum += calcTimeSection((String) section.get("begin"), (String) section.get("end"));
        }
        if (sum == 86400) {
            sortPriceSections(priceSections);
            return true;
        }
        Log.w("DCAPProxy.checkFeeRateSection", "fee rate not cover 1 day !!! sum: " + sum + ", fee rate: " + JsonBean.getGsonBuilder().create().toJson((Object) priceSections));
        return false;
    }

    private void sortPriceSections(ArrayList<HashMap<String, Object>> priceSections) {
        int size = priceSections.size();
        for (int i = 0; i < size - 1; i++) {
            for (int j = i + 1; j < size; j++) {
                if (((String) priceSections.get(i).get("begin")).compareTo((String) priceSections.get(j).get("begin")) > 0) {
                    priceSections.set(i, priceSections.get(j));
                    priceSections.set(j, priceSections.get(i));
                }
            }
        }
    }

    private long calcTimeSection(String begin, String end) {
        String beginHour = begin.substring(0, 2);
        String beginMinute = begin.substring(3, 5);
        String endHour = end.substring(0, 2);
        String endMinute = end.substring(3, 5);
        int bh = Integer.parseInt(beginHour);
        int bm = Integer.parseInt(beginMinute);
        int eh = Integer.parseInt(endHour);
        int em = Integer.parseInt(endMinute);
        Calendar calendarBegin = Calendar.getInstance();
        calendarBegin.set(10, bh);
        calendarBegin.set(12, bm);
        calendarBegin.set(13, 0);
        calendarBegin.set(14, 0);
        Calendar calendarEnd = Calendar.getInstance();
        calendarEnd.set(10, eh);
        calendarEnd.set(12, em);
        calendarEnd.set(13, 0);
        calendarEnd.set(14, 0);
        if (calendarEnd.compareTo(calendarBegin) != 1) {
            calendarEnd.add(5, 1);
        }
        return (calendarEnd.getTimeInMillis() - calendarBegin.getTimeInMillis()) / 1000;
    }

    public void finRequest(FIN_MODE finMode, String userType, String userCode, String chargeId) {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setCharge_id(chargeId);
        FinDirective fin = new FinDirective();
        fin.setFin_mode(finMode);
        getInstance().sendRequest(createCAPRequest("user:" + userType + MqttTopic.TOPIC_LEVEL_SEPARATOR + userCode, "fin", opt, fin));
    }

    public void scanAdvertFinishedEvent(String port, String chargeId) {
        EventDirective event = new EventDirective();
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setPort_id(port);
        opt.setCharge_id(chargeId);
        opt.setEvent_id(EventDirective.EVENT_SCAN_ADVERT_FIN);
        getInstance().sendRequest(createCAPRequest("guest", "event", opt, event));
    }

    public boolean gunLockCtrl(String port, int timeout, LOCK_STATUS status) {
        if (LOCK_STATUS.fault.equals(status)) {
            ChargeStatusCacheProvider.getInstance().updatePortLockStatus(port, status);
            return true;
        }
        HashMap<String, Object> values = new HashMap<>();
        values.put(ContentDB.ChargeTable.PORT, port);
        if (LOCK_STATUS.unlock.equals(status)) {
            values.put("opr", SetDirective.OPR_UNLOCK);
        } else if (LOCK_STATUS.lock.equals(status)) {
            values.put("opr", SetDirective.OPR_LOCK);
        } else if (!LOCK_STATUS.disable.equals(status)) {
            return false;
        } else {
            values.put("opr", "disable");
        }
        return setRequest(timeout, false, SetDirective.SET_ID_PORT_GUNLOCK, values);
    }

    public String queryRequest(int timeout, boolean isServer, String queryId, HashMap<String, Object> params) {
        Object data;
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setQuery_id(queryId);
        QueryDirective query = new QueryDirective();
        query.setParams(params);
        String from = "guest";
        if (isServer) {
            from = "server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        }
        DCAPMessage request = createCAPRequest(from, "query", opt, query);
        if (timeout <= 0) {
            sendRequest(request);
            return request.toJson();
        }
        DCAPMessage confirm = syncRequest(request, timeout);
        if (confirm != null) {
            CAPMessage cap = (CAPMessage) confirm.getData();
            if ("ack".equals(cap.getOp())) {
                AckDirective ack = (AckDirective) new AckDirective().fromJson(JsonBean.ObjectToJson(cap.getData()));
                cap.setData(ack);
                HashMap<String, Object> attach = ack.getAttach();
                if (!(attach == null || (data = attach.get(queryId)) == null)) {
                    return JsonBean.ObjectToJson(data);
                }
            }
        }
        return null;
    }

    public boolean setRequest(int timeout, boolean isServer, String setId, HashMap<String, Object> values) {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setSet_id(setId);
        SetDirective set = new SetDirective();
        set.setValues(values);
        String from = "guest";
        if (isServer) {
            from = "server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        }
        DCAPMessage request = createCAPRequest(from, "set", opt, set);
        if (timeout <= 0) {
            return sendRequest(request);
        }
        DCAPMessage confirm = syncRequest(request, timeout);
        if (confirm == null || !"ack".equals(((CAPMessage) confirm.getData()).getOp())) {
            return false;
        }
        return true;
    }

    public DCAPMessage createCAPRequest(String from, String op, CAPDirectiveOption opt, Object directive) {
        String sn = HardwareStatusCacheProvider.getInstance().getSn();
        CAPMessage requestCap = new CAPMessage();
        DCAPMessage request = new DCAPMessage();
        request.setFrom(from);
        request.setTo("device:sn/" + sn);
        request.setType("cap");
        request.setCtime(System.currentTimeMillis());
        request.setSeq(Sequence.getAgentDCAPSequence());
        requestCap.setOp(op);
        requestCap.setOpt(opt);
        requestCap.setData(directive);
        request.setData(requestCap);
        return request;
    }

    public DCAPMessage createCAPConfirm(String to, CAPDirectiveOption opt) {
        String sn = HardwareStatusCacheProvider.getInstance().getSn();
        CAPMessage confirmCap = new CAPMessage();
        DCAPMessage confirm = new DCAPMessage();
        confirm.setFrom("device:sn/" + sn);
        confirm.setTo(to);
        confirm.setType("cap");
        confirm.setCtime(System.currentTimeMillis());
        confirm.setSeq(Sequence.getCoreDCAPSequence());
        confirmCap.setOpt(opt);
        confirm.setData(confirmCap);
        return confirm;
    }

    public Port getPortStatus(String port) {
        String data = queryRequest(5, false, "device.port." + port, (HashMap<String, Object>) null);
        if (!TextUtils.isEmpty(data)) {
            return (Port) new Port().fromJson(data);
        }
        return null;
    }

    public boolean responseVerification(int timeout, boolean isOk, String xid) {
        HashMap<String, Object> values = new HashMap<>();
        values.put("result", isOk ? "1" : "0");
        values.put("xid", xid);
        return setRequest(timeout, false, "device.verification", values);
    }

    public void updatePortPluginStatus(String port) {
        queryRequest(0, false, "plugin.update.port." + port, (HashMap<String, Object>) null);
    }

    public void networkConnectivityDiagnosis() {
        String gwIp = null;
        String dnsIp = null;
        String activeNet = HardwareStatusCacheProvider.getInstance().getActiveNetwork();
        if (Network.NETWORK_TYPE_MOBILE.equals(activeNet)) {
            dnsIp = HardwareStatusCacheProvider.getInstance().getMobileNetStatus().getDns();
        } else if (Network.NETWORK_TYPE_ETHERNET.equals(activeNet)) {
            gwIp = HardwareStatusCacheProvider.getInstance().getEthernetStatus().getGw();
            dnsIp = HardwareStatusCacheProvider.getInstance().getEthernetStatus().getDns();
        } else if (Network.NETWORK_TYPE_WIFI.equals(activeNet)) {
            gwIp = HardwareStatusCacheProvider.getInstance().getWiFiStatus().getGw();
            dnsIp = HardwareStatusCacheProvider.getInstance().getWiFiStatus().getDns();
        }
        if (!TextUtils.isEmpty(gwIp)) {
            if (NetworkUtils.ping2ip(gwIp, 10)) {
                Log.i("DCAPProxy.networkConnectivityDiagnosis", "succeed to ping gateway ip: " + gwIp);
                LogUtils.applog("succeed to ping gateway ip: " + gwIp);
            } else {
                Log.w("DCAPProxy.networkConnectivityDiagnosis", "failed to ping gateway ip: " + gwIp + ", timeout: " + 10 + " seconds");
                LogUtils.applog("failed to ping gateway ip: " + gwIp + ", timeout: " + 10 + " seconds");
            }
        }
        if (!TextUtils.isEmpty(dnsIp)) {
            if (NetworkUtils.ping2ip(dnsIp, 10)) {
                Log.i("DCAPProxy.networkConnectivityDiagnosis", "succeed to ping DNS ip: " + dnsIp);
                LogUtils.applog("succeed to ping DNS ip: " + dnsIp);
            } else {
                Log.w("DCAPProxy.networkConnectivityDiagnosis", "failed to ping DNS ip: " + dnsIp + ", timeout: " + 10 + " seconds");
                LogUtils.applog("failed to ping DNS ip: " + dnsIp + ", timeout: " + 10 + " seconds");
            }
        }
        if (NetworkUtils.checkServer("www.qq.com", 80, 10)) {
            Log.i("DCAPProxy.networkConnectivityDiagnosis", "succeed to check qq: " + "www.qq.com");
            LogUtils.applog("succeed to check qq: " + "www.qq.com");
            return;
        }
        Log.w("DCAPProxy.networkConnectivityDiagnosis", "failed to check qq: " + "www.qq.com" + ", timeout: " + 10 + " seconds");
        LogUtils.applog("failed to check qq: " + "www.qq.com" + ", timeout: " + 10 + " seconds");
    }
}
