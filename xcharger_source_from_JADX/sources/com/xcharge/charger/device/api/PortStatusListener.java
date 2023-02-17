package com.xcharge.charger.device.api;

import com.xcharge.charger.data.bean.status.PortStatus;

public interface PortStatusListener {
    void onAuthInvalid(String str, PortStatus portStatus);

    void onAuthValid(String str, PortStatus portStatus);

    void onChargeFull(String str, PortStatus portStatus);

    void onChargeStart(String str, PortStatus portStatus);

    void onChargeStop(String str, PortStatus portStatus);

    void onParkBusy(String str);

    void onParkIdle(String str);

    void onParkUnkow(String str);

    void onPlugin(String str, PortStatus portStatus);

    void onPlugout(String str, PortStatus portStatus);

    void onRadarCalibration(String str, boolean z);

    void onResume(String str, PortStatus portStatus);

    void onSuspend(String str, PortStatus portStatus);

    void onUpdate(String str, PortStatus portStatus);

    void onWarning(String str);
}
