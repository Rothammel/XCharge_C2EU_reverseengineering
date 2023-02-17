package com.xcharge.charger.device.p005c2.bean;

import com.xcharge.common.bean.JsonBean;

/* renamed from: com.xcharge.charger.device.c2.bean.DeviceControlData */
public class DeviceControlData extends JsonBean<DeviceControlData> {
    public static final String CMD_AUTH = "C@Auth";
    public static final String CMD_BEEP_CTRL = "C@Beep";
    public static final String CMD_CHARGE_CTRL = "C@Charging";
    public static final String CMD_CP_WAIT_CTRL = "C@CPWaitting";
    public static final String CMD_ELECLOCK_ENABLE = "C@ElecLock";
    public static final String CMD_ELECLOCK_ON_OFF = "C@ElecLockUnlock";
    public static final String CMD_GET_INFO = "C@GetInfo";
    public static final String CMD_GET_UPDATE = "C@Update";
    public static final String CMD_GUN_LED_CTRL = "C@LEDPower";
    public static final String CMD_LED_CTRL = "C@LedState";
    public static final String CMD_SET_CAPACITY_AMP = "C@CurrentMax";
    public static final String CMD_SET_CP_RANGE = "C@CpRange";
    public static final String CMD_SET_EARTH_DISABLE = "C@EarthDisable";
    public static final String CMD_SET_ELECLOCK = "C@ElecLockMode";
    public static final String CMD_SET_LEAKAGE_TOLERANCE = "C@LN";
    public static final String CMD_SET_PWM_AMP = "C@ChargingMax";
    public static final String CMD_SET_VOLTAGE_RANGE = "C@VoltageRange";
    public static final String CMD_USBPOWER_ON_OFF = "C@USBPower";
    public static final String CMD_WWLAN_POLL_CTRL = "C@WWANPolling";
    private String cmd = null;
    private Object data = null;

    public String getCmd() {
        return this.cmd;
    }

    public void setCmd(String cmd2) {
        this.cmd = cmd2;
    }

    public Object getData() {
        return this.data;
    }

    public void setData(Object data2) {
        this.data = data2;
    }
}
