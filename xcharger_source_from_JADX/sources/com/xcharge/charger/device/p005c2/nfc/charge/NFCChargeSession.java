package com.xcharge.charger.device.p005c2.nfc.charge;

import com.xcharge.charger.data.bean.type.CHARGE_INIT_TYPE;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.NFC_CARD_TYPE;
import com.xcharge.charger.data.bean.type.USER_TC_TYPE;
import com.xcharge.charger.device.p005c2.bean.XSign;
import com.xcharge.common.bean.JsonBean;

/* renamed from: com.xcharge.charger.device.c2.nfc.charge.NFCChargeSession */
public class NFCChargeSession extends JsonBean<NFCChargeSession> {
    private String binded_user = null;
    private String cardNo = null;
    private NFC_CARD_TYPE cardType = null;
    private String cardUUID = null;
    private String charge_id = null;
    private CHARGE_PLATFORM charge_platform = null;
    private String device_id = null;
    private String fee_rate = null;
    private CHARGE_INIT_TYPE init_type = null;
    private boolean is3rdPartFin = false;
    private boolean is3rdPartStop = false;
    private boolean isPaid = false;
    private int is_free = -1;
    private byte[] key = null;
    private String port = null;

    /* renamed from: sn */
    private String f74sn = null;
    private int timeout_plugin = -1;
    private int timeout_plugout = -1;
    private int timeout_start = -1;
    private long user_balance = 0;
    private String user_code = null;
    private USER_TC_TYPE user_tc_type = null;
    private String user_tc_value = null;
    private String user_type = null;
    private XSign xsign = null;

    public String getUser_type() {
        return this.user_type;
    }

    public void setUser_type(String user_type2) {
        this.user_type = user_type2;
    }

    public String getUser_code() {
        return this.user_code;
    }

    public void setUser_code(String user_code2) {
        this.user_code = user_code2;
    }

    public String getDevice_id() {
        return this.device_id;
    }

    public void setDevice_id(String device_id2) {
        this.device_id = device_id2;
    }

    public String getPort() {
        return this.port;
    }

    public void setPort(String port2) {
        this.port = port2;
    }

    public String getCharge_id() {
        return this.charge_id;
    }

    public void setCharge_id(String charge_id2) {
        this.charge_id = charge_id2;
    }

    public String getFee_rate() {
        return this.fee_rate;
    }

    public void setFee_rate(String fee_rate2) {
        this.fee_rate = fee_rate2;
    }

    public CHARGE_INIT_TYPE getInit_type() {
        return this.init_type;
    }

    public void setInit_type(CHARGE_INIT_TYPE init_type2) {
        this.init_type = init_type2;
    }

    public String getCardUUID() {
        return this.cardUUID;
    }

    public void setCardUUID(String cardUUID2) {
        this.cardUUID = cardUUID2;
    }

    public String getCardNo() {
        return this.cardNo;
    }

    public void setCardNo(String cardNo2) {
        this.cardNo = cardNo2;
    }

    public String getSn() {
        return this.f74sn;
    }

    public void setSn(String sn) {
        this.f74sn = sn;
    }

    public XSign getXsign() {
        return this.xsign;
    }

    public void setXsign(XSign xsign2) {
        this.xsign = xsign2;
    }

    public NFC_CARD_TYPE getCardType() {
        return this.cardType;
    }

    public void setCardType(NFC_CARD_TYPE cardType2) {
        this.cardType = cardType2;
    }

    public int getTimeout_start() {
        return this.timeout_start;
    }

    public void setTimeout_start(int timeout_start2) {
        this.timeout_start = timeout_start2;
    }

    public int getTimeout_plugout() {
        return this.timeout_plugout;
    }

    public void setTimeout_plugout(int timeout_plugout2) {
        this.timeout_plugout = timeout_plugout2;
    }

    public int getTimeout_plugin() {
        return this.timeout_plugin;
    }

    public void setTimeout_plugin(int timeout_plugin2) {
        this.timeout_plugin = timeout_plugin2;
    }

    public USER_TC_TYPE getUser_tc_type() {
        return this.user_tc_type;
    }

    public void setUser_tc_type(USER_TC_TYPE user_tc_type2) {
        this.user_tc_type = user_tc_type2;
    }

    public String getUser_tc_value() {
        return this.user_tc_value;
    }

    public void setUser_tc_value(String user_tc_value2) {
        this.user_tc_value = user_tc_value2;
    }

    public long getUser_balance() {
        return this.user_balance;
    }

    public void setUser_balance(long user_balance2) {
        this.user_balance = user_balance2;
    }

    public int getIs_free() {
        return this.is_free;
    }

    public void setIs_free(int is_free2) {
        this.is_free = is_free2;
    }

    public CHARGE_PLATFORM getCharge_platform() {
        return this.charge_platform;
    }

    public void setCharge_platform(CHARGE_PLATFORM charge_platform2) {
        this.charge_platform = charge_platform2;
    }

    public String getBinded_user() {
        return this.binded_user;
    }

    public void setBinded_user(String binded_user2) {
        this.binded_user = binded_user2;
    }

    public boolean isIs3rdPartFin() {
        return this.is3rdPartFin;
    }

    public void setIs3rdPartFin(boolean is3rdPartFin2) {
        this.is3rdPartFin = is3rdPartFin2;
    }

    public boolean isIs3rdPartStop() {
        return this.is3rdPartStop;
    }

    public void setIs3rdPartStop(boolean is3rdPartStop2) {
        this.is3rdPartStop = is3rdPartStop2;
    }

    public byte[] getKey() {
        return this.key;
    }

    public void setKey(byte[] key2) {
        this.key = key2;
    }

    public boolean isPaid() {
        return this.isPaid;
    }

    public void setPaid(boolean isPaid2) {
        this.isPaid = isPaid2;
    }
}
