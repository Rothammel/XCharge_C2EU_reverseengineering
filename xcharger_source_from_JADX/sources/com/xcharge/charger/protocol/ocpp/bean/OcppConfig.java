package com.xcharge.charger.protocol.ocpp.bean;

import android.content.Context;
import android.util.Log;
import com.xcharge.charger.protocol.ocpp.C0279R;
import com.xcharge.charger.protocol.ocpp.bean.types.ChargingProfile;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.ContextUtils;
import com.xcharge.common.utils.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.util.TextUtils;

public class OcppConfig extends JsonBean<OcppConfig> {
    private static final String configFileName = "ocpp_cfg.json";
    private ArrayList<AuthCache> authInfos = new ArrayList<>();
    private String chargePointModel = null;
    private String chargePointVendor = null;
    private HashMap<String, ArrayList<ChargingProfile>> defChargingProfiles = new HashMap<>();
    private int listVersion = 0;
    private HashMap<String, String> maps = new HashMap<>();
    private HashMap<String, ArrayList<ChargingProfile>> maxChargingProfiles = new HashMap<>();
    private String qrcode = null;
    private SecureConfig secureConfig = new SecureConfig();
    private String url = null;
    private boolean wsDebug = false;

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url2) {
        this.url = url2;
    }

    public String getChargePointModel() {
        return this.chargePointModel;
    }

    public void setChargePointModel(String chargePointModel2) {
        this.chargePointModel = chargePointModel2;
    }

    public String getChargePointVendor() {
        return this.chargePointVendor;
    }

    public void setChargePointVendor(String chargePointVendor2) {
        this.chargePointVendor = chargePointVendor2;
    }

    public String getQrcode() {
        return this.qrcode;
    }

    public void setQrcode(String qrcode2) {
        this.qrcode = qrcode2;
    }

    public ArrayList<AuthCache> getAuthInfos() {
        return this.authInfos;
    }

    public void setAuthInfos(ArrayList<AuthCache> authInfos2) {
        this.authInfos = authInfos2;
    }

    public HashMap<String, String> getMaps() {
        return this.maps;
    }

    public void setMaps(HashMap<String, String> maps2) {
        this.maps = maps2;
    }

    public HashMap<String, ArrayList<ChargingProfile>> getMaxChargingProfiles() {
        return this.maxChargingProfiles;
    }

    public void setMaxChargingProfiles(HashMap<String, ArrayList<ChargingProfile>> maxChargingProfiles2) {
        this.maxChargingProfiles = maxChargingProfiles2;
    }

    public HashMap<String, ArrayList<ChargingProfile>> getDefChargingProfiles() {
        return this.defChargingProfiles;
    }

    public void setDefChargingProfiles(HashMap<String, ArrayList<ChargingProfile>> defChargingProfiles2) {
        this.defChargingProfiles = defChargingProfiles2;
    }

    public int getListVersion() {
        return this.listVersion;
    }

    public void setListVersion(int listVersion2) {
        this.listVersion = listVersion2;
    }

    public boolean isWsDebug() {
        return this.wsDebug;
    }

    public void setWsDebug(boolean wsDebug2) {
        this.wsDebug = wsDebug2;
    }

    public SecureConfig getSecureConfig() {
        return this.secureConfig;
    }

    public void setSecureConfig(SecureConfig secureConfig2) {
        this.secureConfig = secureConfig2;
    }

    public synchronized void init(Context context) {
        try {
            String cfg = ContextUtils.readFileData(configFileName, context);
            if (TextUtils.isEmpty(cfg)) {
                cfg = ContextUtils.getRawFileToString(context, C0279R.raw.ocpp_cfg);
            }
            if (!TextUtils.isEmpty(cfg)) {
                Log.d("OcppConfig.init", "config: " + cfg);
                OcppConfig config = (OcppConfig) new OcppConfig().fromJson(cfg);
                this.url = config.url;
                this.chargePointModel = config.chargePointModel;
                this.chargePointVendor = config.chargePointVendor;
                this.qrcode = config.qrcode;
                this.authInfos = config.authInfos;
                this.maps = addMap(config.maps);
                this.maxChargingProfiles = config.maxChargingProfiles;
                this.defChargingProfiles = config.defChargingProfiles;
                this.listVersion = config.listVersion;
                this.wsDebug = config.wsDebug;
                this.secureConfig = config.secureConfig;
            }
            persist(context);
        } catch (Exception e) {
            Log.w("OcppConfig.init", Log.getStackTraceString(e));
        }
        LogUtils.applog("use ocpp config: " + toJson());
        return;
    }

    public HashMap<String, String> addMap(HashMap<String, String> maps2) {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put(OcppMessage.AuthorizeRemoteTxRequests, "false");
        hashMap.put(OcppMessage.ClockAlignedDataInterval, "900");
        hashMap.put(OcppMessage.ConnectionTimeOut, "90");
        hashMap.put(OcppMessage.GetConfigurationMaxKeys, "100");
        hashMap.put(OcppMessage.HeartbeatInterval, "1800");
        hashMap.put(OcppMessage.LocalAuthorizeOffline, "true");
        hashMap.put(OcppMessage.LocalPreAuthorize, "true");
        hashMap.put(OcppMessage.MeterValuesAlignedData, "Temperature,Current.Import,Voltage,Power.Active.Import,Energy.Active.Import.Register");
        hashMap.put(OcppMessage.MeterValuesSampledData, "Temperature,Current.Import,Voltage,Power.Active.Import,Energy.Active.Import.Register");
        hashMap.put(OcppMessage.MeterValueSampleInterval, "60");
        hashMap.put(OcppMessage.NumberOfConnectors, "1");
        hashMap.put(OcppMessage.ResetRetries, "0");
        hashMap.put(OcppMessage.ConnectorPhaseRotation, "NotApplicable,RST");
        hashMap.put(OcppMessage.StopTransactionOnEVSideDisconnect, "true");
        hashMap.put(OcppMessage.StopTransactionOnInvalidId, "true");
        hashMap.put(OcppMessage.StopTxnAlignedData, "Temperature,Current.Import,Voltage,Power.Active.Import,Energy.Active.Import.Register");
        hashMap.put(OcppMessage.StopTxnSampledData, "Temperature,Current.Import,Voltage,Power.Active.Import,Energy.Active.Import.Register");
        hashMap.put(OcppMessage.SupportedFeatureProfiles, "Core,FirmwareManagement,LocalAuthListManagement,Reservation,SmartCharging,RemoteTrigger");
        hashMap.put(OcppMessage.TransactionMessageAttempts, "0");
        hashMap.put(OcppMessage.TransactionMessageRetryInterval, "60");
        hashMap.put(OcppMessage.UnlockConnectorOnEVSideDisconnect, "true");
        hashMap.put(OcppMessage.LocalAuthListEnabled, "true");
        hashMap.put(OcppMessage.LocalAuthListMaxLength, "10000");
        hashMap.put(OcppMessage.SendLocalListMaxLength, "10000");
        hashMap.put(OcppMessage.ChargeProfileMaxStackLevel, "100");
        hashMap.put(OcppMessage.ChargingScheduleAllowedChargingRateUnit, "Current,Power");
        hashMap.put(OcppMessage.ChargingScheduleMaxPeriods, "30");
        hashMap.put(OcppMessage.MaxChargingProfilesInstalled, "100");
        hashMap.put(OcppMessage.SupportedFileTransferProtocols, "ftp,ftps,http,https");
        hashMap.put(OcppMessage.AllowOfflineTxForUnknownId, "true");
        hashMap.put(OcppMessage.AuthorizationCacheEnabled, "true");
        hashMap.put(OcppMessage.MaxEnergyOnInvalidId, "0");
        hashMap.put(OcppMessage.ConnectorPhaseRotationMaxLength, "8");
        hashMap.put(OcppMessage.WebSocketPingInterval, "60");
        hashMap.put(OcppMessage.ReserveConnectorZeroSupported, "true");
        for (Map.Entry<String, String> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (maps2.get(key) == null) {
                maps2.put(key, value);
            }
        }
        return maps2;
    }

    public synchronized boolean persist(Context context) {
        return ContextUtils.writeFileData(configFileName, toJson(), context);
    }
}
