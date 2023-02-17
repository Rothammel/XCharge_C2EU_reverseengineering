package com.xcharge.charger.p006ui.p009c2.activity.test;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.p000v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.data.bean.device.MobileNet;
import com.xcharge.charger.data.bean.setting.APNSetting;
import com.xcharge.charger.data.bean.setting.CountrySetting;
import com.xcharge.charger.data.bean.status.ChargeStatus;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.CountrySettingCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.LocalSettingCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.charger.device.p005c2.service.C2DeviceProxy;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;
import com.xcharge.charger.p006ui.p009c2.activity.widget.KeyboardUtil;
import com.xcharge.charger.p006ui.p009c2.activity.widget.KyLetterfilter;
import com.xcharge.charger.protocol.monitor.bean.YZXDCAPOption;
import com.xcharge.common.bean.JsonBean;
import com.xcharge.common.utils.ContextUtils;
import com.xcharge.common.utils.FormatUtils;
import com.xcharge.common.utils.LogUtils;
import com.xcharge.common.utils.TimeUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.json.JSONObject;

/* renamed from: com.xcharge.charger.ui.c2.activity.test.SetActivity */
public class SetActivity extends BaseActivity {
    private static final String ANYO_CFG = "anyo_cfg.json";
    private static final String OCPP_CFG = "ocpp_cfg.json";
    private static final String TAB_APN = "apn";
    private static final String TAB_LOCALE = "locale";
    private static final String TAB_PLATFORM = "platform";
    private static final String TAB_SYSTEM = "system";
    private static final String XCLOUD = "xcloud_family_mqtt_cfg.json";
    private String anyoCfg = null;
    private Button bt_save;
    private CheckBox cb_default;
    private CheckBox cb_net;
    private CheckBox cb_supl;
    /* access modifiers changed from: private */
    public List<String> countrys = new ArrayList();
    private List<String> currencys = Arrays.asList(new String[]{"AUD", "CNY", "EUR", "ILS"});
    /* access modifiers changed from: private */
    public String currentTab = TAB_APN;
    /* access modifiers changed from: private */
    public EditText et_apn;
    /* access modifiers changed from: private */
    public EditText et_background;
    /* access modifiers changed from: private */
    public EditText et_broker;
    /* access modifiers changed from: private */
    public EditText et_carrier;
    /* access modifiers changed from: private */
    public EditText et_client_id;
    /* access modifiers changed from: private */
    public EditText et_cloud_host;
    /* access modifiers changed from: private */
    public EditText et_cloud_port;
    /* access modifiers changed from: private */
    public EditText et_cp;
    /* access modifiers changed from: private */
    public EditText et_current;
    /* access modifiers changed from: private */
    public EditText et_down_topic;
    /* access modifiers changed from: private */
    public EditText et_id;
    /* access modifiers changed from: private */
    public EditText et_leakage;
    /* access modifiers changed from: private */
    public EditText et_magic_number;
    /* access modifiers changed from: private */
    public EditText et_mcc;
    /* access modifiers changed from: private */
    public EditText et_mnc;
    /* access modifiers changed from: private */
    public EditText et_model;
    /* access modifiers changed from: private */
    public EditText et_money_disp;
    /* access modifiers changed from: private */
    public EditText et_password;
    /* access modifiers changed from: private */
    public EditText et_provider;
    /* access modifiers changed from: private */
    public EditText et_pwd;
    /* access modifiers changed from: private */
    public EditText et_qrcode;
    /* access modifiers changed from: private */
    public EditText et_type;
    /* access modifiers changed from: private */
    public EditText et_up_topic;
    /* access modifiers changed from: private */
    public EditText et_url;
    /* access modifiers changed from: private */
    public EditText et_user;
    /* access modifiers changed from: private */
    public EditText et_user_name;
    /* access modifiers changed from: private */
    public EditText et_vendor;
    /* access modifiers changed from: private */
    public EditText et_voltage;
    /* access modifiers changed from: private */
    public long firstTime = 0;
    /* access modifiers changed from: private */
    public boolean isBinaryMode = false;
    /* access modifiers changed from: private */
    public Boolean isCPWait = null;
    /* access modifiers changed from: private */
    public Boolean isEarthDisable = null;
    /* access modifiers changed from: private */
    public boolean isPlug2Charge = false;
    /* access modifiers changed from: private */
    public boolean isUseDaylightTime = false;
    /* access modifiers changed from: private */
    public Boolean isWWlanPolling = null;
    /* access modifiers changed from: private */
    public boolean isYZXMonitor = false;
    /* access modifiers changed from: private */
    public KyLetterfilter kyLetterfilter;
    private List<String> languages = Arrays.asList(new String[]{"de", "en", "iw", "zh"});
    private LinearLayout ll_anyo;
    /* access modifiers changed from: private */
    public LinearLayout ll_monitor;
    private LinearLayout ll_ocpp;
    private LinearLayout ll_xcharge;
    /* access modifiers changed from: private */
    public ListView lv_country;
    private int maxCurrent;
    private List<String> noTimezoneList = Arrays.asList(new String[]{"-12:00", "-11:00", "-10:00", "-09:30", "-09:00", "-08:00", "-07:00", "-06:00", "-05:00", "-04:30", "-04:00", "-03:00", "-02:00", "-01:00", "+00:00", "+01:00", "+02:00", "+03:00", "+04:00", "+04:30", "+05:00", "+05:30", "+06:00", "+06:30", "+07:00", "+08:00", "+09:00", "+09:30", "+10:00", "+11:00", "+11:30", "+12:00"});
    private String ocppCfg = null;
    /* access modifiers changed from: private */
    public List<String> platforms = Arrays.asList(new String[]{"anyo", "ocpp", "xcharge"});
    /* access modifiers changed from: private */
    public PopupWindow popupWindow;
    private RadioButton rb_binary_mode_no;
    private RadioButton rb_binary_mode_yes;
    private RadioButton rb_cp_wait_no;
    private RadioButton rb_cp_wait_yes;
    private RadioButton rb_daylight_time_no;
    private RadioButton rb_daylight_time_yes;
    private RadioButton rb_earth_no;
    private RadioButton rb_earth_yes;
    private RadioButton rb_monitor_no;
    private RadioButton rb_monitor_yes;
    private RadioButton rb_plug_charge_no;
    private RadioButton rb_plug_charge_yes;
    private RadioButton rb_wwlan_no;
    private RadioButton rb_wwlan_yes;
    private RadioButton rb_xcharge_logo_no;
    private RadioButton rb_xcharge_logo_yes;
    private List<String> regions = Arrays.asList(new String[]{"China", "Europe"});
    private RadioGroup rg_binary_mode;
    private RadioGroup rg_cp_wait;
    private RadioGroup rg_daylight_time;
    private RadioGroup rg_earth;
    private RadioGroup rg_monitor;
    private RadioGroup rg_plug_charge;
    private RadioGroup rg_wwlan;
    private RadioGroup rg_xcharge_logo;
    private Spinner sp_currency;
    private Spinner sp_language;
    private Spinner sp_platform;
    private Spinner sp_region;
    private Spinner sp_timezone;
    private TabHost tabHost;
    private ArrayAdapter<String> timezoneAdapter;
    private List<String> timezoneList = Arrays.asList(new String[]{"-10:00", "-09:00", "-08:00", "-07:00", "-06:00", "-05:00", "-04:00", "-03:30", "-03:00", "-01:00", "+00:00", "+01:00", "+02:00", "+03:30", "+04:00", "+09:30", "+10:00", "+10:30", "+12:00"});
    private List<String> timezones = new ArrayList();
    /* access modifiers changed from: private */
    public TreeMap<String, String> treeMap = new TreeMap<String, String>() {
        {
            put("Albania", "al");
            put("Andorra", "ad");
            put("Australia", "au");
            put("Austria", "at");
            put("Belarus", "by");
            put("Belgium", "be");
            put("Bosnia and Herzegovina", "ba");
            put("Bulgaria", "bg");
            put("China", "cn");
            put("Croatia", "hr");
            put("Czech Republic", "cz");
            put("Denmark", "dk");
            put("Estonia", "ee");
            put("Faroe Islands", "fo");
            put("Finland", "fi");
            put("France", "fr");
            put("Germany", "de");
            put("Greece", "gr");
            put("Hungary", "fu");
            put("Iceland", "is");
            put("Ireland", "ie");
            put("Israel", "il");
            put("Italy", "it");
            put("Japan", "jp");
            put("Kosovo", "rs");
            put("Latvia", "lv");
            put("Liechtenstein", "li");
            put("Lithuania", "lt");
            put("Luxembourg", "lu");
            put("Macedonia", "mk");
            put("Malta", "mt");
            put("Moldova", "md");
            put("Monaco", "mc");
            put("Montenegro", "me");
            put("Netherlands", "nl");
            put("Norway", "no");
            put("Poland", "pl");
            put("Portugal", "pt");
            put("Romania", "ro");
            put("Russia", "ru");
            put("San Marino", "sm");
            put("Serbia", "yu");
            put("Slovakia", "sk");
            put("Slovenia", "si");
            put("Spain", "es");
            put("Sweden", "se");
            put("Switzerland", "ch");
            put("Turkey", "tr");
            put("Ukraine", "ua");
            put("United Kiongdom", "gb");
            put("Vatican", "va");
        }
    };
    /* access modifiers changed from: private */
    public TextView tv_country;
    private TextView tv_current;
    /* access modifiers changed from: private */
    public boolean usingXChargeLogo = false;
    private String xcloud = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(C0221R.layout.activity_set);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        Utils.setPermitNFC(false, false, false, false);
    }

    /* access modifiers changed from: protected */
    public void initView() {
        this.tabHost = (TabHost) findViewById(C0221R.C0223id.tabhost);
        this.et_carrier = (EditText) findViewById(C0221R.C0223id.et_carrier);
        this.et_apn = (EditText) findViewById(C0221R.C0223id.et_apn);
        this.et_user = (EditText) findViewById(C0221R.C0223id.et_user);
        this.et_pwd = (EditText) findViewById(C0221R.C0223id.et_pwd);
        this.et_mcc = (EditText) findViewById(C0221R.C0223id.et_mcc);
        this.et_mnc = (EditText) findViewById(C0221R.C0223id.et_mnc);
        this.cb_default = (CheckBox) findViewById(C0221R.C0223id.cb_default);
        this.cb_supl = (CheckBox) findViewById(C0221R.C0223id.cb_supl);
        this.cb_net = (CheckBox) findViewById(C0221R.C0223id.cb_net);
        this.sp_platform = (Spinner) findViewById(C0221R.C0223id.sp_platform);
        this.ll_anyo = (LinearLayout) findViewById(C0221R.C0223id.ll_anyo);
        this.et_id = (EditText) findViewById(C0221R.C0223id.et_id);
        this.et_type = (EditText) findViewById(C0221R.C0223id.et_type);
        this.et_cloud_host = (EditText) findViewById(C0221R.C0223id.et_cloud_host);
        this.et_cloud_port = (EditText) findViewById(C0221R.C0223id.et_cloud_port);
        this.et_provider = (EditText) findViewById(C0221R.C0223id.et_provider);
        this.et_magic_number = (EditText) findViewById(C0221R.C0223id.et_magic_number);
        this.et_qrcode = (EditText) findViewById(C0221R.C0223id.et_qrcode);
        this.ll_ocpp = (LinearLayout) findViewById(C0221R.C0223id.ll_ocpp);
        this.et_url = (EditText) findViewById(C0221R.C0223id.et_url);
        this.et_model = (EditText) findViewById(C0221R.C0223id.et_model);
        this.et_vendor = (EditText) findViewById(C0221R.C0223id.et_vendor);
        this.ll_xcharge = (LinearLayout) findViewById(C0221R.C0223id.ll_xcharge);
        this.rg_binary_mode = (RadioGroup) findViewById(C0221R.C0223id.rg_binary_mode);
        this.rb_binary_mode_no = (RadioButton) findViewById(C0221R.C0223id.rb_binary_mode_no);
        this.rb_binary_mode_yes = (RadioButton) findViewById(C0221R.C0223id.rb_binary_mode_yes);
        this.sp_region = (Spinner) findViewById(C0221R.C0223id.sp_region);
        this.et_broker = (EditText) findViewById(C0221R.C0223id.et_broker);
        this.et_user_name = (EditText) findViewById(C0221R.C0223id.et_user_name);
        this.et_password = (EditText) findViewById(C0221R.C0223id.et_password);
        this.et_client_id = (EditText) findViewById(C0221R.C0223id.et_client_id);
        this.et_up_topic = (EditText) findViewById(C0221R.C0223id.et_up_topic);
        this.et_down_topic = (EditText) findViewById(C0221R.C0223id.et_down_topic);
        this.tv_country = (TextView) findViewById(C0221R.C0223id.tv_country);
        this.sp_language = (Spinner) findViewById(C0221R.C0223id.sp_language);
        this.sp_timezone = (Spinner) findViewById(C0221R.C0223id.sp_timezone);
        this.rg_daylight_time = (RadioGroup) findViewById(C0221R.C0223id.rg_daylight_time);
        this.rb_daylight_time_no = (RadioButton) findViewById(C0221R.C0223id.rb_daylight_time_no);
        this.rb_daylight_time_yes = (RadioButton) findViewById(C0221R.C0223id.rb_daylight_time_yes);
        this.sp_currency = (Spinner) findViewById(C0221R.C0223id.sp_currency);
        this.et_money_disp = (EditText) findViewById(C0221R.C0223id.et_money_disp);
        this.et_cp = (EditText) findViewById(C0221R.C0223id.et_cp);
        this.et_voltage = (EditText) findViewById(C0221R.C0223id.et_voltage);
        this.tv_current = (TextView) findViewById(C0221R.C0223id.tv_current);
        this.et_current = (EditText) findViewById(C0221R.C0223id.et_current);
        this.rg_earth = (RadioGroup) findViewById(C0221R.C0223id.rg_earth);
        this.rb_earth_no = (RadioButton) findViewById(C0221R.C0223id.rb_earth_no);
        this.rb_earth_yes = (RadioButton) findViewById(C0221R.C0223id.rb_earth_yes);
        this.et_leakage = (EditText) findViewById(C0221R.C0223id.et_leakage);
        this.rg_plug_charge = (RadioGroup) findViewById(C0221R.C0223id.rg_plug_charge);
        this.rb_plug_charge_no = (RadioButton) findViewById(C0221R.C0223id.rb_plug_charge_no);
        this.rb_plug_charge_yes = (RadioButton) findViewById(C0221R.C0223id.rb_plug_charge_yes);
        this.rg_wwlan = (RadioGroup) findViewById(C0221R.C0223id.rg_wwlan);
        this.rb_wwlan_no = (RadioButton) findViewById(C0221R.C0223id.rb_wwlan_no);
        this.rb_wwlan_yes = (RadioButton) findViewById(C0221R.C0223id.rb_wwlan_yes);
        this.rg_cp_wait = (RadioGroup) findViewById(C0221R.C0223id.rg_cp_wait);
        this.rb_cp_wait_no = (RadioButton) findViewById(C0221R.C0223id.rb_cp_wait_no);
        this.rb_cp_wait_yes = (RadioButton) findViewById(C0221R.C0223id.rb_cp_wait_yes);
        this.et_background = (EditText) findViewById(C0221R.C0223id.et_background);
        this.ll_monitor = (LinearLayout) findViewById(C0221R.C0223id.ll_monitor);
        this.rg_monitor = (RadioGroup) findViewById(C0221R.C0223id.rg_monitor);
        this.rb_monitor_no = (RadioButton) findViewById(C0221R.C0223id.rb_monitor_no);
        this.rb_monitor_yes = (RadioButton) findViewById(C0221R.C0223id.rb_monitor_yes);
        this.rg_xcharge_logo = (RadioGroup) findViewById(C0221R.C0223id.rg_xcharge_logo);
        this.rb_xcharge_logo_no = (RadioButton) findViewById(C0221R.C0223id.rb_xcharge_logo_no);
        this.rb_xcharge_logo_yes = (RadioButton) findViewById(C0221R.C0223id.rb_xcharge_logo_yes);
        this.bt_save = (Button) findViewById(C0221R.C0223id.bt_save);
        initTabHost();
        initApnData();
        initPlatformData();
        initLocaleData();
        initSystemData();
        initApnListener();
        initPlatformListener();
        initLocaleListener();
        initSystemListener();
        initSaveButton();
    }

    private void initTabHost() {
        this.tabHost.setup();
        this.tabHost.addTab(this.tabHost.newTabSpec(TAB_APN).setIndicator(getString(C0221R.string.set_apn)).setContent(C0221R.C0223id.ll_apn));
        this.tabHost.addTab(this.tabHost.newTabSpec(TAB_PLATFORM).setIndicator(getString(C0221R.string.set_platform)).setContent(C0221R.C0223id.ll_platform));
        this.tabHost.addTab(this.tabHost.newTabSpec(TAB_LOCALE).setIndicator(getString(C0221R.string.set_locale)).setContent(C0221R.C0223id.ll_locale));
        this.tabHost.addTab(this.tabHost.newTabSpec("system").setIndicator(getString(C0221R.string.set_system)).setContent(C0221R.C0223id.ll_system));
        this.tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                SetActivity.this.currentTab = tabId;
                SetActivity.this.setSaveButtonName();
                KeyboardUtil.shared(SetActivity.this, SetActivity.this.et_carrier).hideKeyboard();
            }
        });
    }

    private void initApnData() {
        APNSetting apnSetting = HardwareStatusCacheProvider.getInstance().getPreferAPN();
        if (apnSetting == null) {
            MobileNet mobile = HardwareStatusCacheProvider.getInstance().getNetworkStatus().getMobile();
            if (mobile != null) {
                String simMCC = mobile.getSimMCC();
                String simMNC = mobile.getSimMNC();
                if (!TextUtils.isEmpty(simMCC)) {
                    this.et_mcc.setText(simMCC);
                }
                if (!TextUtils.isEmpty(simMNC)) {
                    this.et_mnc.setText(simMNC);
                    return;
                }
                return;
            }
            return;
        }
        String carrier = apnSetting.getCarrier();
        String apn = apnSetting.getApn();
        String user = apnSetting.getUser();
        String password = apnSetting.getPassword();
        String mcc = apnSetting.getMcc();
        String mnc = apnSetting.getMnc();
        String type = apnSetting.getType();
        if (!TextUtils.isEmpty(carrier)) {
            this.et_carrier.setText(carrier);
        }
        if (!TextUtils.isEmpty(apn)) {
            this.et_apn.setText(apn);
        }
        if (!TextUtils.isEmpty(user)) {
            this.et_user.setText(user);
        }
        if (!TextUtils.isEmpty(password)) {
            this.et_pwd.setText(password);
        }
        if (!TextUtils.isEmpty(mcc)) {
            this.et_mcc.setText(mcc);
        }
        if (!TextUtils.isEmpty(mnc)) {
            this.et_mnc.setText(mnc);
        }
        if (!TextUtils.isEmpty(type)) {
            for (String string : type.split(",")) {
                if ("default".equals(string)) {
                    this.cb_default.setChecked(true);
                } else if ("supl".equals(string)) {
                    this.cb_supl.setChecked(true);
                } else if ("net".equals(string)) {
                    this.cb_net.setChecked(true);
                }
            }
        }
    }

    private void initPlatformData() {
        try {
            ArrayAdapter arrayAdapter = new ArrayAdapter(this, 17367048, this.platforms);
            arrayAdapter.setDropDownViewResource(C0221R.layout.layout_spinner_item);
            this.sp_platform.setAdapter(arrayAdapter);
            String platform = SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
            this.sp_platform.setSelection(this.platforms.indexOf(platform));
            selectPlatform(platform);
            ArrayAdapter arrayAdapter2 = new ArrayAdapter(this, 17367048, this.regions);
            arrayAdapter2.setDropDownViewResource(C0221R.layout.layout_spinner_item);
            this.sp_region.setAdapter(arrayAdapter2);
            HashMap<String, String> platformData = SystemSettingCacheProvider.getInstance().getPlatformCustomizedData();
            if (platformData != null) {
                String id = platformData.get("id");
                if (!TextUtils.isEmpty(id)) {
                    this.et_id.setText(id);
                }
                String type = platformData.get("type");
                if (!TextUtils.isEmpty(type)) {
                    this.et_type.setText(type);
                }
            }
            this.anyoCfg = ContextUtils.readFileData(ANYO_CFG, context);
            if (!TextUtils.isEmpty(this.anyoCfg)) {
                JSONObject jsonObject = new JSONObject(this.anyoCfg);
                String cloudHost = jsonObject.optString("cloudHost");
                if (!TextUtils.isEmpty(cloudHost)) {
                    this.et_cloud_host.setText(cloudHost);
                }
                String cloudPort = jsonObject.optString("cloudPort");
                if (!TextUtils.isEmpty(cloudPort)) {
                    this.et_cloud_port.setText(cloudPort);
                }
                String provider = jsonObject.optString("provider");
                if (!TextUtils.isEmpty(provider)) {
                    this.et_provider.setText(provider);
                }
                String magicNumber = jsonObject.optString("magicNumber");
                if (!TextUtils.isEmpty(magicNumber)) {
                    this.et_magic_number.setText(magicNumber);
                }
                String qrcode = jsonObject.optString(YZXDCAPOption.QRCODE);
                if (!TextUtils.isEmpty(qrcode)) {
                    this.et_qrcode.setText(qrcode);
                }
            }
            this.ocppCfg = ContextUtils.readFileData(OCPP_CFG, context);
            if (!TextUtils.isEmpty(this.ocppCfg)) {
                JSONObject jsonObject2 = new JSONObject(this.ocppCfg);
                String url = jsonObject2.optString("url");
                if (!TextUtils.isEmpty(url)) {
                    this.et_url.setText(url);
                }
                String chargePointModel = jsonObject2.optString("chargePointModel");
                if (!TextUtils.isEmpty(chargePointModel)) {
                    this.et_model.setText(chargePointModel);
                }
                String chargePointVendor = jsonObject2.optString("chargePointVendor");
                if (!TextUtils.isEmpty(chargePointVendor)) {
                    this.et_vendor.setText(chargePointVendor);
                }
            }
            this.xcloud = ContextUtils.readFileData(XCLOUD, context);
            if (!TextUtils.isEmpty(this.xcloud)) {
                JSONObject jsonObject3 = new JSONObject(this.xcloud);
                this.isBinaryMode = jsonObject3.optBoolean("binaryMode");
                if (this.isBinaryMode) {
                    this.rb_binary_mode_yes.setChecked(true);
                } else {
                    this.rb_binary_mode_no.setChecked(true);
                }
                String region = jsonObject3.optString("region");
                if (!TextUtils.isEmpty(region)) {
                    this.sp_region.setSelection(this.regions.indexOf(region));
                }
                String broker = jsonObject3.optString("broker");
                if (!TextUtils.isEmpty(broker)) {
                    this.et_broker.setText(broker);
                }
                String userName = jsonObject3.optString("userName");
                if (!TextUtils.isEmpty(userName)) {
                    this.et_user_name.setText(userName);
                }
                String password = jsonObject3.optString("password");
                if (!TextUtils.isEmpty(password)) {
                    this.et_password.setText(password);
                }
                String clientId = jsonObject3.optString("clientId");
                if (!TextUtils.isEmpty(clientId)) {
                    this.et_client_id.setText(clientId);
                }
                String upTopic = jsonObject3.optString("upTopic");
                if (!TextUtils.isEmpty(upTopic)) {
                    this.et_up_topic.setText(upTopic);
                }
                String downTopic = jsonObject3.optString("downTopic");
                if (!TextUtils.isEmpty(downTopic)) {
                    this.et_down_topic.setText(downTopic);
                }
            }
        } catch (Exception e) {
            Log.w("SetActivity.initPlatformData", Log.getStackTraceString(e));
        }
    }

    private void initLocaleData() {
        View view = LayoutInflater.from(this).inflate(C0221R.layout.layout_popwindow, (ViewGroup) null);
        this.popupWindow = new PopupWindow(view, 200, 400);
        this.popupWindow.setContentView(view);
        this.popupWindow.setFocusable(true);
        this.popupWindow.setOutsideTouchable(true);
        this.popupWindow.setBackgroundDrawable(new BitmapDrawable());
        this.lv_country = (ListView) view.findViewById(C0221R.C0223id.lv_country);
        this.kyLetterfilter = (KyLetterfilter) view.findViewById(C0221R.C0223id.kyLetterfilter);
        for (Map.Entry<String, String> entry : this.treeMap.entrySet()) {
            this.countrys.add(entry.getKey());
        }
        this.lv_country.setAdapter(new CountryAdapter(context));
        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(this, 17367048, this.languages);
        languageAdapter.setDropDownViewResource(C0221R.layout.layout_spinner_item);
        this.sp_language.setAdapter(languageAdapter);
        this.timezoneAdapter = new ArrayAdapter<>(this, 17367048, this.timezones);
        this.timezoneAdapter.setDropDownViewResource(C0221R.layout.layout_spinner_item);
        this.sp_timezone.setAdapter(this.timezoneAdapter);
        ArrayAdapter<String> currencyAdapter = new ArrayAdapter<>(this, 17367048, this.currencys);
        currencyAdapter.setDropDownViewResource(C0221R.layout.layout_spinner_item);
        this.sp_currency.setAdapter(currencyAdapter);
        String country = SystemSettingCacheProvider.getInstance().getCountry();
        if (!TextUtils.isEmpty(country)) {
            this.tv_country.setText(valueToKey(country));
        }
        loadLocale();
    }

    private void initSystemData() {
        ChargeStatus chargeSetting = ChargeStatusCacheProvider.getInstance().getChargeStatus();
        this.et_cp.setText(String.valueOf(chargeSetting.getCpRange()));
        this.et_voltage.setText(String.valueOf(chargeSetting.getVoltageRange()));
        this.maxCurrent = chargeSetting.getAmpCapacity();
        this.tv_current.setText(getString(C0221R.string.set_system_charge_current, new Object[]{Integer.valueOf(this.maxCurrent)}));
        this.et_current.setText(String.valueOf(chargeSetting.getAdjustAmp()));
        this.isEarthDisable = chargeSetting.isEarthDisable();
        if (this.isEarthDisable != null) {
            if (this.isEarthDisable.booleanValue()) {
                this.rb_earth_yes.setChecked(true);
            } else {
                this.rb_earth_no.setChecked(true);
            }
        }
        Integer leakageTolerance = chargeSetting.getLeakageTolerance();
        if (leakageTolerance != null) {
            this.et_leakage.setText(String.valueOf(leakageTolerance));
        }
        this.isPlug2Charge = SystemSettingCacheProvider.getInstance().isPlug2Charge();
        if (this.isPlug2Charge) {
            this.rb_plug_charge_yes.setChecked(true);
        } else {
            this.rb_plug_charge_no.setChecked(true);
        }
        this.isWWlanPolling = SystemSettingCacheProvider.getInstance().isWWlanPolling();
        if (this.isWWlanPolling != null) {
            if (this.isWWlanPolling.booleanValue()) {
                this.rb_wwlan_yes.setChecked(true);
            } else {
                this.rb_wwlan_no.setChecked(true);
            }
        }
        this.isCPWait = SystemSettingCacheProvider.getInstance().isCPWait();
        if (this.isCPWait != null) {
            if (this.isCPWait.booleanValue()) {
                this.rb_cp_wait_yes.setChecked(true);
            } else {
                this.rb_cp_wait_no.setChecked(true);
            }
        }
        String uiBackgroundColor = SystemSettingCacheProvider.getInstance().getUiBackgroundColor();
        if (!TextUtils.isEmpty(uiBackgroundColor)) {
            this.et_background.setText(uiBackgroundColor);
        }
        this.isYZXMonitor = SystemSettingCacheProvider.getInstance().isYZXMonitor();
        if (this.isYZXMonitor) {
            this.rb_monitor_yes.setChecked(true);
        } else {
            this.rb_monitor_no.setChecked(true);
        }
        this.usingXChargeLogo = SystemSettingCacheProvider.getInstance().isUsingXChargeLogo();
        if (this.usingXChargeLogo) {
            this.rb_xcharge_logo_yes.setChecked(true);
        } else {
            this.rb_xcharge_logo_no.setChecked(true);
        }
    }

    private void initApnListener() {
        this.et_carrier.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_carrier);
                return false;
            }
        });
        this.et_apn.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_apn);
                return false;
            }
        });
        this.et_user.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_user);
                return false;
            }
        });
        this.et_pwd.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_pwd);
                return false;
            }
        });
        this.et_mcc.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_mcc);
                return false;
            }
        });
        this.et_mnc.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_mnc);
                return false;
            }
        });
    }

    private void initPlatformListener() {
        this.sp_platform.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                SetActivity.this.selectPlatform((String) SetActivity.this.platforms.get(i));
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        this.et_id.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_id);
                return false;
            }
        });
        this.et_type.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_type);
                return false;
            }
        });
        this.et_cloud_host.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_cloud_host);
                return false;
            }
        });
        this.et_cloud_port.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_cloud_port);
                return false;
            }
        });
        this.et_provider.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_provider);
                return false;
            }
        });
        this.et_magic_number.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_magic_number);
                return false;
            }
        });
        this.et_qrcode.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_qrcode);
                return false;
            }
        });
        this.et_url.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_url);
                return false;
            }
        });
        this.et_model.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_model);
                return false;
            }
        });
        this.et_vendor.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_vendor);
                return false;
            }
        });
        this.rg_binary_mode.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == C0221R.C0223id.rb_binary_mode_no) {
                    SetActivity.this.isBinaryMode = false;
                } else if (checkedId == C0221R.C0223id.rb_binary_mode_yes) {
                    SetActivity.this.isBinaryMode = true;
                }
            }
        });
        this.et_broker.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_broker);
                return false;
            }
        });
        this.et_user_name.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_user_name);
                return false;
            }
        });
        this.et_password.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_password);
                return false;
            }
        });
        this.et_client_id.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_client_id);
                return false;
            }
        });
        this.et_up_topic.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_up_topic);
                return false;
            }
        });
        this.et_down_topic.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_down_topic);
                return false;
            }
        });
    }

    private void initLocaleListener() {
        this.tv_country.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                SetActivity.this.popupWindow.showAsDropDown(SetActivity.this.tv_country);
            }
        });
        this.kyLetterfilter.setOnLetterfilterListener(new KyLetterfilter.OnLetterfilterListener() {
            public void start() {
            }

            public void letterChanged(String letter) {
                int index = -1;
                int i = 0;
                while (true) {
                    if (i >= SetActivity.this.countrys.size()) {
                        break;
                    } else if (((String) SetActivity.this.countrys.get(i)).substring(0, 1).toUpperCase().equals(letter)) {
                        index = i;
                        break;
                    } else {
                        i++;
                    }
                }
                if (index != -1) {
                    SetActivity.this.lv_country.setSelection(index);
                }
            }

            public void end() {
            }
        });
        this.lv_country.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScrollStateChanged(AbsListView arg0, int arg1) {
            }

            public void onScroll(AbsListView absListView, int i, int j, int l) {
                if (SetActivity.this.countrys.size() - 1 >= i) {
                    SetActivity.this.kyLetterfilter.setCurrent(((String) SetActivity.this.countrys.get(i)).substring(0, 1).toUpperCase());
                }
            }
        });
        this.lv_country.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SetActivity.this.tv_country.setText((CharSequence) SetActivity.this.countrys.get(i));
                SetActivity.this.selectCountry((String) SetActivity.this.treeMap.get(SetActivity.this.countrys.get(i)));
                SetActivity.this.dismiss();
            }
        });
        this.rg_daylight_time.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == C0221R.C0223id.rb_daylight_time_no) {
                    SetActivity.this.isUseDaylightTime = false;
                } else if (checkedId == C0221R.C0223id.rb_daylight_time_yes) {
                    SetActivity.this.isUseDaylightTime = true;
                }
                SetActivity.this.selectCountry((String) null);
            }
        });
        this.et_money_disp.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_money_disp);
                return false;
            }
        });
    }

    private void initSystemListener() {
        this.et_cp.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_cp);
                return false;
            }
        });
        this.et_voltage.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_voltage);
                return false;
            }
        });
        this.et_current.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_current);
                return false;
            }
        });
        this.rg_earth.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == C0221R.C0223id.rb_earth_no) {
                    SetActivity.this.isEarthDisable = false;
                } else if (checkedId == C0221R.C0223id.rb_earth_yes) {
                    SetActivity.this.isEarthDisable = true;
                }
            }
        });
        this.et_leakage.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_leakage);
                return false;
            }
        });
        this.rg_plug_charge.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == C0221R.C0223id.rb_plug_charge_no) {
                    SetActivity.this.isPlug2Charge = false;
                } else if (checkedId == C0221R.C0223id.rb_plug_charge_yes) {
                    SetActivity.this.isPlug2Charge = true;
                }
            }
        });
        this.rg_wwlan.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == C0221R.C0223id.rb_wwlan_no) {
                    SetActivity.this.isWWlanPolling = false;
                } else if (checkedId == C0221R.C0223id.rb_wwlan_yes) {
                    SetActivity.this.isWWlanPolling = true;
                }
            }
        });
        this.rg_cp_wait.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == C0221R.C0223id.rb_cp_wait_no) {
                    SetActivity.this.isCPWait = false;
                } else if (checkedId == C0221R.C0223id.rb_cp_wait_yes) {
                    SetActivity.this.isCPWait = true;
                }
            }
        });
        this.et_background.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                SetActivity.this.showKeyboard(SetActivity.this.et_background);
                return false;
            }
        });
        this.rg_monitor.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == C0221R.C0223id.rb_monitor_no) {
                    SetActivity.this.isYZXMonitor = false;
                } else if (checkedId == C0221R.C0223id.rb_monitor_yes) {
                    SetActivity.this.isYZXMonitor = true;
                }
            }
        });
        this.rg_xcharge_logo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                if (checkedId == C0221R.C0223id.rb_xcharge_logo_no) {
                    SetActivity.this.usingXChargeLogo = false;
                } else if (checkedId == C0221R.C0223id.rb_xcharge_logo_yes) {
                    SetActivity.this.usingXChargeLogo = true;
                }
            }
        });
        this.iv_company.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                long secondTime = System.currentTimeMillis();
                if (secondTime - SetActivity.this.firstTime > 2000) {
                    SetActivity.this.firstTime = secondTime;
                } else {
                    SetActivity.this.ll_monitor.setVisibility(0);
                }
            }
        });
    }

    private void initSaveButton() {
        setSaveButtonName();
        this.bt_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (SetActivity.TAB_APN.equals(SetActivity.this.currentTab)) {
                    SetActivity.this.saveApnData();
                } else if (SetActivity.TAB_PLATFORM.equals(SetActivity.this.currentTab)) {
                    SetActivity.this.savePlatformData();
                } else if (SetActivity.TAB_LOCALE.equals(SetActivity.this.currentTab)) {
                    SetActivity.this.saveLocaleData();
                } else if ("system".equals(SetActivity.this.currentTab)) {
                    SetActivity.this.saveSystemData();
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void setSaveButtonName() {
        if (TAB_APN.equals(this.currentTab)) {
            if (HardwareStatusCacheProvider.getInstance().getPreferAPN() == null) {
                this.bt_save.setText(getString(C0221R.string.set_button_add));
            } else {
                this.bt_save.setText(getString(C0221R.string.set_button_update));
            }
        } else if (TAB_PLATFORM.equals(this.currentTab)) {
            this.bt_save.setText(getString(C0221R.string.set_button_save_reset));
        } else if (TAB_LOCALE.equals(this.currentTab) || "system".equals(this.currentTab)) {
            this.bt_save.setText(getString(C0221R.string.set_button_save));
        }
    }

    /* access modifiers changed from: private */
    public void selectPlatform(String platform) {
        if (this.platforms.get(0).equals(platform)) {
            this.ll_anyo.setVisibility(0);
            this.ll_ocpp.setVisibility(8);
            this.ll_xcharge.setVisibility(8);
        } else if (this.platforms.get(1).equals(platform)) {
            this.ll_anyo.setVisibility(8);
            this.ll_ocpp.setVisibility(0);
            this.ll_xcharge.setVisibility(8);
        } else if (this.platforms.get(2).equals(platform)) {
            this.ll_anyo.setVisibility(8);
            this.ll_ocpp.setVisibility(8);
            this.ll_xcharge.setVisibility(0);
        }
    }

    private void loadLocale() {
        CountrySetting countrySettting = CountrySettingCacheProvider.getInstance().getCountrySetting();
        this.timezones.clear();
        if (countrySettting.isUseDaylightTime()) {
            this.timezones.addAll(this.timezoneList);
        } else {
            this.timezones.addAll(this.noTimezoneList);
        }
        this.timezoneAdapter.notifyDataSetChanged();
        this.sp_language.setSelection(this.languages.indexOf(countrySettting.getLang()));
        this.sp_timezone.setSelection(this.timezones.indexOf(countrySettting.getZone()));
        this.isUseDaylightTime = countrySettting.isUseDaylightTime();
        if (this.isUseDaylightTime) {
            this.rb_daylight_time_yes.setChecked(true);
        } else {
            this.rb_daylight_time_no.setChecked(true);
        }
        this.sp_currency.setSelection(this.currencys.indexOf(countrySettting.getMoney().toUpperCase()));
        this.et_money_disp.setText(countrySettting.getMoneyDisp());
    }

    /* access modifiers changed from: private */
    public void selectCountry(String country) {
        boolean isDST = false;
        if (TextUtils.isEmpty(country)) {
            isDST = this.isUseDaylightTime;
        } else if (!"au".equals(country) && !"by".equals(country) && !"cn".equals(country) && !"jp".equals(country) && !"ru".equals(country)) {
            isDST = true;
        }
        this.timezones.clear();
        if (isDST) {
            this.timezones.addAll(this.timezoneList);
        } else {
            this.timezones.addAll(this.noTimezoneList);
        }
        this.timezoneAdapter.notifyDataSetChanged();
        if (TextUtils.isEmpty(country)) {
            return;
        }
        if ("fo".equals(country) || "gb".equals(country) || "ie".equals(country) || "is".equals(country) || "pt".equals(country)) {
            this.sp_language.setSelection(this.languages.indexOf("en"));
            this.sp_timezone.setSelection(this.timezoneList.indexOf("+00:00"));
            this.rb_daylight_time_yes.setChecked(true);
            this.isUseDaylightTime = true;
            this.sp_currency.setSelection(this.currencys.indexOf("EUR"));
            this.et_money_disp.setText("Euro");
        } else if ("ad".equals(country) || "al".equals(country) || "ba".equals(country) || "be".equals(country) || "cz".equals(country) || "dk".equals(country) || "es".equals(country) || "fr".equals(country) || "hr".equals(country) || "hu".equals(country) || "it".equals(country) || "li".equals(country) || "lu".equals(country) || "mc".equals(country) || "me".equals(country) || "mk".equals(country) || "mt".equals(country) || "nl".equals(country) || "no".equals(country) || "pl".equals(country) || "rs".equals(country) || "se".equals(country) || "si".equals(country) || "sk".equals(country) || "sm".equals(country) || "va".equals(country) || "yu".equals(country)) {
            this.sp_language.setSelection(this.languages.indexOf("en"));
            this.sp_timezone.setSelection(this.timezoneList.indexOf("+01:00"));
            this.rb_daylight_time_yes.setChecked(true);
            this.isUseDaylightTime = true;
            this.sp_currency.setSelection(this.currencys.indexOf("EUR"));
            this.et_money_disp.setText("Euro");
        } else if ("bg".equals(country) || "ee".equals(country) || "fi".equals(country) || "gr".equals(country) || "lt".equals(country) || "lv".equals(country) || "md".equals(country) || "ro".equals(country) || "tr".equals(country) || "ua".equals(country)) {
            this.sp_language.setSelection(this.languages.indexOf("en"));
            this.sp_timezone.setSelection(this.timezoneList.indexOf("+02:00"));
            this.rb_daylight_time_yes.setChecked(true);
            this.isUseDaylightTime = true;
            this.sp_currency.setSelection(this.currencys.indexOf("EUR"));
            this.et_money_disp.setText("Euro");
        } else if ("il".equals(country)) {
            this.sp_language.setSelection(this.languages.indexOf("iw"));
            this.sp_timezone.setSelection(this.timezoneList.indexOf("+02:00"));
            this.rb_daylight_time_yes.setChecked(true);
            this.isUseDaylightTime = true;
            this.sp_currency.setSelection(this.currencys.indexOf("ILS"));
            this.et_money_disp.setText("₪");
        } else if ("at".equals(country) || "ch".equals(country) || "de".equals(country)) {
            this.sp_language.setSelection(this.languages.indexOf("de"));
            this.sp_timezone.setSelection(this.timezoneList.indexOf("+01:00"));
            this.rb_daylight_time_yes.setChecked(true);
            this.isUseDaylightTime = true;
            this.sp_currency.setSelection(this.currencys.indexOf("EUR"));
            this.et_money_disp.setText("Euro");
        } else if ("by".equals(country) || "ru".equals(country)) {
            this.sp_language.setSelection(this.languages.indexOf("en"));
            this.sp_timezone.setSelection(this.noTimezoneList.indexOf("+03:00"));
            this.rb_daylight_time_no.setChecked(true);
            this.isUseDaylightTime = false;
            this.sp_currency.setSelection(this.currencys.indexOf("EUR"));
            this.et_money_disp.setText("Euro");
        } else if ("cn".equals(country)) {
            this.sp_language.setSelection(this.languages.indexOf("zh"));
            this.sp_timezone.setSelection(this.noTimezoneList.indexOf("+08:00"));
            this.rb_daylight_time_no.setChecked(true);
            this.isUseDaylightTime = false;
            this.sp_currency.setSelection(this.currencys.indexOf("CNY"));
            this.et_money_disp.setText("元");
        } else if ("jp".equals(country)) {
            this.sp_language.setSelection(this.languages.indexOf("en"));
            this.sp_timezone.setSelection(this.noTimezoneList.indexOf("+09:00"));
            this.rb_daylight_time_no.setChecked(true);
            this.isUseDaylightTime = false;
            this.sp_currency.setSelection(this.currencys.indexOf("JPY"));
            this.et_money_disp.setText("Yen");
        } else if ("au".equals(country)) {
            this.sp_language.setSelection(this.languages.indexOf("en"));
            this.sp_timezone.setSelection(this.noTimezoneList.indexOf("+10:00"));
            this.rb_daylight_time_no.setChecked(true);
            this.isUseDaylightTime = false;
            this.sp_currency.setSelection(this.currencys.indexOf("AUD"));
            this.et_money_disp.setText("AUD");
        }
    }

    /* access modifiers changed from: private */
    public void saveApnData() {
        final String carrier = this.et_carrier.getText().toString();
        final String apn = this.et_apn.getText().toString();
        final String user = this.et_user.getText().toString();
        final String pwd = this.et_pwd.getText().toString();
        final String mcc = this.et_mcc.getText().toString();
        final String mnc = this.et_mnc.getText().toString();
        final StringBuffer type = new StringBuffer();
        if (this.cb_default.isChecked()) {
            type.append(this.cb_default.getText().toString());
        }
        if (this.cb_supl.isChecked()) {
            if (type.length() > 0) {
                type.append("," + this.cb_supl.getText().toString());
            } else {
                type.append(this.cb_supl.getText().toString());
            }
        }
        if (this.cb_net.isChecked()) {
            if (type.length() > 0) {
                type.append("," + this.cb_net.getText().toString());
            } else {
                type.append(this.cb_net.getText().toString());
            }
        }
        if (TextUtils.isEmpty(carrier)) {
            showSmallDialog(getString(C0221R.string.set_apn_name_not_null));
        } else if (TextUtils.isEmpty(apn)) {
            showSmallDialog(getString(C0221R.string.set_apn_apn_not_null));
        } else if (TextUtils.isEmpty(mcc)) {
            showSmallDialog(getString(C0221R.string.set_apn_mcc_not_null));
        } else if (TextUtils.isEmpty(mnc)) {
            showSmallDialog(getString(C0221R.string.set_apn_mnc_not_null));
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(C0221R.string.set_apn_confirm_add_and_update));
            builder.setPositiveButton(getString(C0221R.string.set_apn_confirm), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    APNSetting apnSetting = new APNSetting();
                    apnSetting.setCarrier(carrier);
                    apnSetting.setApn(apn);
                    if (!TextUtils.isEmpty(user)) {
                        apnSetting.setUser(user);
                    }
                    if (!TextUtils.isEmpty(pwd)) {
                        apnSetting.setPassword(pwd);
                    }
                    apnSetting.setMcc(mcc);
                    apnSetting.setMnc(mnc);
                    if (type != null) {
                        apnSetting.setType(type.toString());
                    }
                    if (C2DeviceProxy.getInstance().setApn(apnSetting) == null) {
                        SetActivity.showSmallDialog(SetActivity.this.getString(C0221R.string.set_defeated));
                        return;
                    }
                    SetActivity.showSmallDialog(SetActivity.this.getString(C0221R.string.set_succeed));
                    Utils.skipNfcQrcode(SetActivity.this);
                    SetActivity.this.finish();
                }
            });
            builder.setNegativeButton(getString(C0221R.string.set_apn_cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    /* access modifiers changed from: private */
    public void savePlatformData() {
        try {
            String platform = this.sp_platform.getSelectedItem().toString();
            SystemSettingCacheProvider.getInstance().updatetChargePlatform(CHARGE_PLATFORM.valueOf(platform));
            SystemSettingCacheProvider.getInstance().persist();
            if (this.platforms.get(0).equals(platform)) {
                HashMap<String, String> platformData = SystemSettingCacheProvider.getInstance().getPlatformCustomizedData();
                if (platformData == null) {
                    platformData = new HashMap<>();
                }
                String id = this.et_id.getText().toString();
                if (!TextUtils.isEmpty(id)) {
                    platformData.put("id", id);
                }
                String type = this.et_type.getText().toString();
                if (!TextUtils.isEmpty(type)) {
                    platformData.put("type", type);
                }
                SystemSettingCacheProvider.getInstance().updatePlatformCustomizedData(platformData);
                SystemSettingCacheProvider.getInstance().persist();
                LogUtils.syslog("config anyo charger params: " + JsonBean.mapToJson(platformData));
                JSONObject jsonObject = new JSONObject();
                if (!TextUtils.isEmpty(this.anyoCfg)) {
                    jsonObject = new JSONObject(this.anyoCfg);
                }
                jsonObject.put("cloudHost", this.et_cloud_host.getText().toString());
                jsonObject.put("cloudPort", this.et_cloud_port.getText().toString());
                jsonObject.put("provider", this.et_provider.getText().toString());
                jsonObject.put("magicNumber", this.et_magic_number.getText().toString());
                jsonObject.put(YZXDCAPOption.QRCODE, this.et_qrcode.getText().toString());
                ContextUtils.writeFileData(ANYO_CFG, jsonObject.toString(), context);
                LogUtils.syslog("config anyo protocol params: " + jsonObject.toString());
            } else if (this.platforms.get(1).equals(platform)) {
                if (!TextUtils.isEmpty(this.ocppCfg)) {
                    JSONObject jsonObject2 = new JSONObject(this.ocppCfg);
                    String url = this.et_url.getText().toString();
                    if (jsonObject2.has("url") && !TextUtils.isEmpty(url)) {
                        jsonObject2.put("url", url);
                    }
                    String model = this.et_model.getText().toString();
                    if (jsonObject2.has("chargePointModel") && !TextUtils.isEmpty(model)) {
                        jsonObject2.put("chargePointModel", model);
                    }
                    String vendor = this.et_vendor.getText().toString();
                    if (jsonObject2.has("chargePointVendor") && !TextUtils.isEmpty(vendor)) {
                        jsonObject2.put("chargePointVendor", vendor);
                    }
                    ContextUtils.writeFileData(OCPP_CFG, jsonObject2.toString(), context);
                    LogUtils.syslog("config ocpp protocol params: " + jsonObject2.toString());
                }
            } else if (this.platforms.get(2).equals(platform) && !TextUtils.isEmpty(this.xcloud)) {
                JSONObject jsonObject3 = new JSONObject(this.xcloud);
                jsonObject3.put("binaryMode", Boolean.valueOf(this.isBinaryMode));
                jsonObject3.put("region", this.sp_region.getSelectedItem().toString());
                jsonObject3.put("broker", this.et_broker.getText().toString());
                jsonObject3.put("userName", this.et_user_name.getText().toString());
                jsonObject3.put("password", this.et_password.getText().toString());
                jsonObject3.put("clientId", this.et_client_id.getText().toString());
                jsonObject3.put("upTopic", this.et_up_topic.getText().toString());
                jsonObject3.put("downTopic", this.et_down_topic.getText().toString());
                ContextUtils.writeFileData(XCLOUD, jsonObject3.toString(), context);
                LogUtils.syslog("config xcloud protocol params: " + jsonObject3.toString());
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DCAPProxy.ACTION_DEVICE_REBOOT_EVENT));
        } catch (Exception e) {
            Log.w("SetActivity.savePlatformData", Log.getStackTraceString(e));
        }
    }

    /* access modifiers changed from: private */
    public void saveLocaleData() {
        SystemSettingCacheProvider.getInstance().setCountry(this.treeMap.get(this.tv_country.getText().toString()));
        SystemSettingCacheProvider.getInstance().persist();
        CountrySetting countrySetting = new CountrySetting();
        countrySetting.setZone(this.sp_timezone.getSelectedItem().toString());
        countrySetting.setUseDaylightTime(this.isUseDaylightTime);
        countrySetting.setLang(this.sp_language.getSelectedItem().toString());
        countrySetting.setMoney(this.sp_currency.getSelectedItem().toString());
        String moneyDisp = this.et_money_disp.getText().toString();
        if (TextUtils.isEmpty(moneyDisp)) {
            showSmallDialog(getString(C0221R.string.set_locale_money_disp_not_null));
            return;
        }
        countrySetting.setMoneyDisp(moneyDisp);
        String zoneId = TimeUtils.getTimezoneId(countrySetting.getZone(), countrySetting.isUseDaylightTime());
        if (TextUtils.isEmpty(zoneId)) {
            showSmallDialog(getString(C0221R.string.set_locale_timezone_format_error));
            return;
        }
        ((AlarmManager) context.getSystemService("alarm")).setTimeZone(zoneId);
        CountrySettingCacheProvider.getInstance().updateUseDaylightTime(countrySetting.isUseDaylightTime());
        if (!TextUtils.isEmpty(countrySetting.getZone())) {
            CountrySettingCacheProvider.getInstance().updateZone(countrySetting.getZone());
        }
        if (!TextUtils.isEmpty(countrySetting.getLang())) {
            CountrySettingCacheProvider.getInstance().updateLang(countrySetting.getLang());
        }
        if (!TextUtils.isEmpty(countrySetting.getMoney())) {
            CountrySettingCacheProvider.getInstance().updateMoney(countrySetting.getMoney());
        }
        if (!TextUtils.isEmpty(countrySetting.getMoneyDisp())) {
            CountrySettingCacheProvider.getInstance().updateMoneyDisp(countrySetting.getMoneyDisp());
        }
        if (CHARGE_PLATFORM.xcharge.equals(SystemSettingCacheProvider.getInstance().getChargePlatform())) {
            RemoteSettingCacheProvider.getInstance().updateCountrySetting(countrySetting);
            RemoteSettingCacheProvider.getInstance().persist();
        } else {
            CountrySettingCacheProvider.getInstance().persist();
        }
        showSmallDialog(getString(C0221R.string.set_succeed));
        LogUtils.syslog("config locale params: " + countrySetting.toJson());
        Utils.skipNfcQrcode(this);
        finish();
    }

    /* access modifiers changed from: private */
    public void saveSystemData() {
        String cp = this.et_cp.getText().toString();
        if (!TextUtils.isEmpty(cp)) {
            if (TextUtils.isDigitsOnly(cp)) {
                int cpErrorRange = Integer.parseInt(cp);
                if (cpErrorRange < 0 || cpErrorRange > 100) {
                    showSmallDialog(getString(C0221R.string.set_format_error));
                    return;
                }
                C2DeviceProxy.getInstance().setCPRange(cpErrorRange);
                RemoteSettingCacheProvider.getInstance().getChargeSetting().setCpRange(cpErrorRange);
                RemoteSettingCacheProvider.getInstance().persist();
                LocalSettingCacheProvider.getInstance().getChargeSetting().setCpRange(cpErrorRange);
                LocalSettingCacheProvider.getInstance().persist();
                ChargeStatusCacheProvider.getInstance().updateCPRange(cpErrorRange);
            } else {
                showSmallDialog(getString(C0221R.string.set_format_error));
                return;
            }
        }
        String voltage = this.et_voltage.getText().toString();
        if (!TextUtils.isEmpty(voltage)) {
            if (TextUtils.isDigitsOnly(voltage)) {
                int voltageErrorRange = Integer.parseInt(voltage);
                if (voltageErrorRange < 15 || voltageErrorRange > 30) {
                    showSmallDialog(getString(C0221R.string.set_format_error));
                    return;
                }
                C2DeviceProxy.getInstance().setVoltageRange(voltageErrorRange);
                RemoteSettingCacheProvider.getInstance().getChargeSetting().setVoltageRange(voltageErrorRange);
                RemoteSettingCacheProvider.getInstance().persist();
                LocalSettingCacheProvider.getInstance().getChargeSetting().setVoltageRange(voltageErrorRange);
                LocalSettingCacheProvider.getInstance().persist();
                ChargeStatusCacheProvider.getInstance().updateVoltageRange(voltageErrorRange);
            } else {
                showSmallDialog(getString(C0221R.string.set_format_error));
                return;
            }
        }
        String current = this.et_current.getText().toString();
        if (!TextUtils.isEmpty(current)) {
            if (TextUtils.isDigitsOnly(current)) {
                int adjustAmp = Integer.parseInt(current);
                if (adjustAmp < 6 || adjustAmp > this.maxCurrent) {
                    showSmallDialog(getString(C0221R.string.set_format_error));
                    return;
                }
                C2DeviceProxy.getInstance().ajustChargeAmp("1", adjustAmp);
                RemoteSettingCacheProvider.getInstance().getChargeSetting().setAdjustAmp(adjustAmp);
                RemoteSettingCacheProvider.getInstance().persist();
                LocalSettingCacheProvider.getInstance().getChargeSetting().setAdjustAmp(adjustAmp);
                LocalSettingCacheProvider.getInstance().persist();
                ChargeStatusCacheProvider.getInstance().updateAdjustAmp(adjustAmp);
            } else {
                showSmallDialog(getString(C0221R.string.set_format_error));
                return;
            }
        }
        if (this.isEarthDisable != null) {
            C2DeviceProxy.getInstance().setEarthDisable(this.isEarthDisable.booleanValue());
            RemoteSettingCacheProvider.getInstance().getChargeSetting().setEarthDisable(this.isEarthDisable);
            RemoteSettingCacheProvider.getInstance().persist();
            LocalSettingCacheProvider.getInstance().getChargeSetting().setEarthDisable(this.isEarthDisable);
            LocalSettingCacheProvider.getInstance().persist();
            ChargeStatusCacheProvider.getInstance().updateEarthDisable(this.isEarthDisable);
        }
        String leakage = this.et_leakage.getText().toString();
        if (!TextUtils.isEmpty(leakage)) {
            if (TextUtils.isDigitsOnly(leakage)) {
                int leakgeTolerance = Integer.parseInt(leakage);
                if (leakgeTolerance < 0 || leakgeTolerance > 200) {
                    showSmallDialog(getString(C0221R.string.set_format_error));
                    return;
                }
                C2DeviceProxy.getInstance().setLeakageTolerance(leakgeTolerance);
                RemoteSettingCacheProvider.getInstance().getChargeSetting().setLeakageTolerance(Integer.valueOf(leakgeTolerance));
                RemoteSettingCacheProvider.getInstance().persist();
                LocalSettingCacheProvider.getInstance().getChargeSetting().setLeakageTolerance(Integer.valueOf(leakgeTolerance));
                LocalSettingCacheProvider.getInstance().persist();
                ChargeStatusCacheProvider.getInstance().updateLeakageTolerance(Integer.valueOf(leakgeTolerance));
            } else {
                showSmallDialog(getString(C0221R.string.set_format_error));
                return;
            }
        }
        SystemSettingCacheProvider.getInstance().setPlug2Charge(this.isPlug2Charge);
        SystemSettingCacheProvider.getInstance().persist();
        LogUtils.syslog("config system param isPlug2Charge: " + this.isPlug2Charge);
        if (this.isWWlanPolling != null) {
            C2DeviceProxy.getInstance().switchWWlanPoll(this.isWWlanPolling.booleanValue());
            SystemSettingCacheProvider.getInstance().setWWlanPolling(this.isWWlanPolling);
            SystemSettingCacheProvider.getInstance().persist();
        }
        if (this.isCPWait != null) {
            C2DeviceProxy.getInstance().switchCPWait(this.isCPWait.booleanValue());
            SystemSettingCacheProvider.getInstance().setCPWait(this.isCPWait);
            SystemSettingCacheProvider.getInstance().persist();
        }
        String uiBackgroundColor = this.et_background.getText().toString();
        if (TextUtils.isEmpty(uiBackgroundColor) || (!TextUtils.isEmpty(uiBackgroundColor) && uiBackgroundColor.length() == 7 && uiBackgroundColor.startsWith(MqttTopic.MULTI_LEVEL_WILDCARD) && FormatUtils.isHexString(uiBackgroundColor.substring(1)))) {
            SystemSettingCacheProvider.getInstance().setUiBackgroundColor(uiBackgroundColor);
            SystemSettingCacheProvider.getInstance().persist();
            LogUtils.syslog("config system param uiBackgroundColor: " + uiBackgroundColor);
        } else if (!TextUtils.isEmpty(uiBackgroundColor)) {
            showSmallDialog(getString(C0221R.string.set_format_error));
            return;
        }
        SystemSettingCacheProvider.getInstance().setUsingXChargeLogo(this.usingXChargeLogo);
        SystemSettingCacheProvider.getInstance().persist();
        LogUtils.syslog("config system param usingXChargeLogo: " + this.usingXChargeLogo);
        showSmallDialog(getString(C0221R.string.set_succeed));
        if (this.isYZXMonitor == SystemSettingCacheProvider.getInstance().isYZXMonitor()) {
            Utils.skipNfcQrcode(this);
            finish();
            return;
        }
        SystemSettingCacheProvider.getInstance().setYZXMonitor(this.isYZXMonitor);
        SystemSettingCacheProvider.getInstance().persist();
        LogUtils.syslog("config system param isMonitor: " + this.isYZXMonitor);
        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(DCAPProxy.ACTION_DEVICE_REBOOT_EVENT));
    }

    /* access modifiers changed from: private */
    public void showKeyboard(EditText editText) {
        int inputType = editText.getInputType() | 524288;
        editText.setInputType(0);
        KeyboardUtil.shared(this, editText).showKeyboard();
        editText.setInputType(inputType);
        editText.setSelection(editText.getText().toString().length());
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.test.SetActivity$CountryAdapter */
    private class CountryAdapter extends BaseAdapter {
        private Context context;

        public CountryAdapter(Context context2) {
            this.context = context2;
        }

        public int getCount() {
            return SetActivity.this.countrys.size();
        }

        public Object getItem(int i) {
            return SetActivity.this.countrys.get(i);
        }

        public long getItemId(int i) {
            return (long) i;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                viewHolder = new ViewHolder();
                view = LayoutInflater.from(this.context).inflate(C0221R.layout.layout_spinner_item, (ViewGroup) null);
                viewHolder.tv_item = (TextView) view.findViewById(C0221R.C0223id.tv_item);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            viewHolder.tv_item.setText((CharSequence) SetActivity.this.countrys.get(i));
            return view;
        }
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.test.SetActivity$ViewHolder */
    class ViewHolder {
        public TextView tv_item;

        ViewHolder() {
        }
    }

    /* access modifiers changed from: private */
    public void dismiss() {
        if (this.popupWindow != null && this.popupWindow.isShowing()) {
            this.popupWindow.dismiss();
        }
    }

    private String valueToKey(String country) {
        for (Map.Entry<String, String> entry : this.treeMap.entrySet()) {
            String key = entry.getKey();
            if (country.equals(entry.getValue())) {
                return key;
            }
        }
        return "";
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        Log.d("SetActivity", "onResume");
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        Log.d("SetActivity", "onPause");
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        Log.d("SetActivity", "onStop");
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        Log.d("SetActivity", "onDestroy");
        KeyboardUtil.setmInstance((KeyboardUtil) null);
    }

    public void onBackPressed() {
        Utils.skipNfcQrcode(this);
        finish();
    }
}
