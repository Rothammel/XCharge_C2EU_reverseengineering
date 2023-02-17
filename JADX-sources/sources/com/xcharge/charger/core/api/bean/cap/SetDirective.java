package com.xcharge.charger.core.api.bean.cap;

import com.xcharge.common.bean.JsonBean;
import java.util.HashMap;

/* loaded from: classes.dex */
public class SetDirective extends JsonBean<SetDirective> {
    public static final String OPR_DISABLE = "disable";
    public static final String OPR_ENABLE = "enable";
    public static final String OPR_LOCK = "lock";
    public static final String OPR_REBOOT = "reboot";
    public static final String OPR_UNLOCK = "unlock";
    public static final String SET_ID_DEVICE = "device";
    public static final String SET_ID_DEVICE_CP_RANGE = "device.cp.range";
    public static final String SET_ID_DEVICE_EARTH_DISABLE = "device.earth.disable";
    public static final String SET_ID_DEVICE_LEAKAGE_TOLERANCE = "device.leakage.tolerance";
    public static final String SET_ID_DEVICE_LOCALE = "device.locale";
    public static final String SET_ID_DEVICE_TIME_CLOUDSYNCH = "device.time.cloudsynch";
    public static final String SET_ID_DEVICE_VERIFICATION = "device.verification";
    public static final String SET_ID_DEVICE_VOLT_RANGE = "device.input.volt.range";
    public static final String SET_ID_PORT_AMP_WORK = "device.port.amp.work";
    public static final String SET_ID_PORT_GUNLOCK = "device.port.gunlock";
    private HashMap<String, Object> values = null;

    public HashMap<String, Object> getValues() {
        return this.values;
    }

    public void setValues(HashMap<String, Object> values) {
        this.values = values;
    }
}
