package com.xcharge.charger.data.bean.type;

import com.xcharge.charger.device.p005c2.bean.PortRuntimeData;

public enum DEVICE_STATUS {
    idle(0),
    selfCheck(1),
    plugout(2),
    plugin(3),
    charging(4),
    chargeFull(5),
    stopped(6),
    notInited(10),
    noGround(11),
    lostPhase(12),
    emergencyStop(13),
    errorVolt(14),
    errorAmp(15),
    errorTemp(16),
    powerLeak(17),
    errorComm(18);
    
    private int status;

    private DEVICE_STATUS(int status2) {
        this.status = 0;
        this.status = status2;
    }

    public static DEVICE_STATUS valueBy(int status2) {
        switch (status2) {
            case 0:
                return idle;
            case 1:
                return selfCheck;
            case 2:
                return plugout;
            case 3:
                return plugin;
            case 4:
                return charging;
            case 5:
                return chargeFull;
            case 6:
                return stopped;
            case 10:
                return notInited;
            case PortRuntimeData.STATUS_EX_11 /*11*/:
                return noGround;
            case PortRuntimeData.STATUS_EX_12 /*12*/:
                return lostPhase;
            case 13:
                return emergencyStop;
            case 14:
                return errorVolt;
            case 15:
                return errorAmp;
            case 16:
                return errorTemp;
            case 17:
                return powerLeak;
            case 18:
                return errorComm;
            default:
                return notInited;
        }
    }

    public int getStatus() {
        return this.status;
    }
}
