package com.xcharge.charger.device.c2.nfc.charge;

import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.device.c2.bean.XSign;
import com.xcharge.common.bean.JsonBean;

/* loaded from: classes.dex */
public class NFCChargeSession extends JsonBean<NFCChargeSession> {
    private String user_type = null;
    private String user_code = null;
    private String device_id = null;
    private String port = null;
    private CHARGE_INIT_TYPE init_type = null;
    private USER_TC_TYPE user_tc_type = null;
    private String user_tc_value = null;
    private long user_balance = 0;
    private int is_free = -1;
    private CHARGE_PLATFORM charge_platform = null;
    private String binded_user = null;
    private String charge_id = null;
    private String fee_rate = null;
    private boolean isPaid = false;
    private int timeout_plugin = -1;
    private int timeout_plugout = -1;
    private int timeout_start = -1;
    private byte[] key = null;
    private String cardUUID = null;
    private String cardNo = null;
    private NFC_CARD_TYPE cardType = null;
    private String sn = null;
    private XSign xsign = null;
    private boolean is3rdPartFin = false;
    private boolean is3rdPartStop = false;

    public String getUser_type() {
        return this.user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getUser_code() {
        return this.user_code;
    }

    public void setUser_code(String user_code) {
        this.user_code = user_code;
    }

    public String getDevice_id() {
        return this.device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id) {
        this.charge_id = charge_id;
    }

    public String getFee_rate() {
        return this.fee_rate;
    }

    public void setFee_rate(String fee_rate) {
        this.fee_rate = fee_rate;
    }

    public CHARGE_INIT_TYPE getInit_type() {
        return this.init_type;
    }

    public void setInit_type(CHARGE_INIT_TYPE init_type) {
        this.init_type = init_type;
    }

    public String getCardUUID() {
        return this.cardUUID;
    }

    public void setCardUUID(String cardUUID) {
        this.cardUUID = cardUUID;
    }

    public String getCardNo() {
        return this.cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getSn() {
        return this.sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public XSign getXsign() {
        return this.xsign;
    }

    public void setXsign(XSign xsign) {
        this.xsign = xsign;
    }

    public NFC_CARD_TYPE getCardType() {
        return this.cardType;
    }

    public void setCardType(NFC_CARD_TYPE cardType) {
        this.cardType = cardType;
    }

    public int getTimeout_start() {
        return this.timeout_start;
    }

    public void setTimeout_start(int timeout_start) {
        this.timeout_start = timeout_start;
    }

    public int getTimeout_plugout() {
        return this.timeout_plugout;
    }

    public void setTimeout_plugout(int timeout_plugout) {
        this.timeout_plugout = timeout_plugout;
    }

    public int getTimeout_plugin() {
        return this.timeout_plugin;
    }

    public void setTimeout_plugin(int timeout_plugin) {
        this.timeout_plugin = timeout_plugin;
    }

    public USER_TC_TYPE getUser_tc_type() {
        return this.user_tc_type;
    }

    public void setUser_tc_type(USER_TC_TYPE user_tc_type) {
        this.user_tc_type = user_tc_type;
    }

    public String getUser_tc_value() {
        return this.user_tc_value;
    }

    public void setUser_tc_value(String user_tc_value) {
        this.user_tc_value = user_tc_value;
    }

    public long getUser_balance() {
        return this.user_balance;
    }

    public void setUser_balance(long user_balance) {
        this.user_balance = user_balance;
    }

    public int getIs_free() {
        return this.is_free;
    }

    public void setIs_free(int is_free) {
        this.is_free = is_free;
    }

    public CHARGE_PLATFORM getCharge_platform() {
        return this.charge_platform;
    }

    public void setCharge_platform(CHARGE_PLATFORM charge_platform) {
        this.charge_platform = charge_platform;
    }

    public String getBinded_user() {
        return this.binded_user;
    }

    public void setBinded_user(String binded_user) {
        this.binded_user = binded_user;
    }

    public boolean isIs3rdPartFin() {
        return this.is3rdPartFin;
    }

    public void setIs3rdPartFin(boolean is3rdPartFin) {
        this.is3rdPartFin = is3rdPartFin;
    }

    public boolean isIs3rdPartStop() {
        return this.is3rdPartStop;
    }

    public void setIs3rdPartStop(boolean is3rdPartStop) {
        this.is3rdPartStop = is3rdPartStop;
    }

    public byte[] getKey() {
        return this.key;
    }

    public void setKey(byte[] key) {
        this.key = key;
    }

    public boolean isPaid() {
        return this.isPaid;
    }

    public void setPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }
}
