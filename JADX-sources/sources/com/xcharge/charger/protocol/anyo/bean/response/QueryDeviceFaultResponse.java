package com.xcharge.charger.protocol.anyo.bean.response;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.common.utils.EndianUtils;
import com.xcharge.common.utils.FormatUtils;

/* loaded from: classes.dex */
public class QueryDeviceFaultResponse extends AnyoMessage {
    public static final short AC_DEVICE_FAULT_CHARGE = 64;
    public static final short AC_DEVICE_FAULT_CHARGE_CONTROLLER_COMM = 2048;
    public static final short AC_DEVICE_FAULT_EMERGENCY_STOP = 256;
    public static final short AC_DEVICE_FAULT_GROUD_DETECT_MODULE = 1;
    public static final short AC_DEVICE_FAULT_GROUND = 32;
    public static final short AC_DEVICE_FAULT_LEAKAGE_CURRENT = 4;
    public static final short AC_DEVICE_FAULT_LN_DETECT_MODULE = 2;
    public static final short AC_DEVICE_FAULT_LOWER_VOLTAGE = 16;
    public static final short AC_DEVICE_FAULT_METER_MODULE = 4096;
    public static final short AC_DEVICE_FAULT_NFC_COMM = 1024;
    public static final short AC_DEVICE_FAULT_OVER_VOLTAGE = 8;
    public static final short AC_DEVICE_FAULT_SCREEN_COMM = 512;
    public static final short AC_DEVICE_FAULT_VEHICLE = 128;
    public static final short AC_DEVICE_OK = 0;
    private short acDeviceFault = 0;
    private int dcDeviceFault = 0;
    private byte[] dcModulesFault = null;

    public short getAcDeviceFault() {
        return this.acDeviceFault;
    }

    public void setAcDeviceFault(short acDeviceFault) {
        this.acDeviceFault = acDeviceFault;
    }

    public int getDcDeviceFault() {
        return this.dcDeviceFault;
    }

    public void setDcDeviceFault(int dcDeviceFault) {
        this.dcDeviceFault = dcDeviceFault;
    }

    public byte[] getDcModulesFault() {
        return this.dcModulesFault;
    }

    public void setDcModulesFault(byte[] dcModulesFault) {
        this.dcModulesFault = dcModulesFault;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public byte[] bodyToBytes() throws Exception {
        int dcModulesFaultLength = 0;
        if (this.dcModulesFault != null) {
            dcModulesFaultLength = this.dcModulesFault.length;
        }
        byte[] bytes = new byte[dcModulesFaultLength + 6];
        System.arraycopy(EndianUtils.shortToLittleBytes(this.acDeviceFault), 0, bytes, 0, 2);
        System.arraycopy(EndianUtils.intToLittleBytes(this.dcDeviceFault), 0, bytes, 2, 4);
        if (dcModulesFaultLength > 0) {
            System.arraycopy(this.dcModulesFault, 0, bytes, 6, dcModulesFaultLength);
        }
        return bytes;
    }

    @Override // com.xcharge.charger.protocol.anyo.bean.AnyoMessage
    public AnyoMessage bodyFromBytes(byte[] bytes) throws Exception {
        if (bytes.length < 6) {
            Log.e("QueryDeviceFaultResponse.bodyFromBytes", "body length must not less than 6 !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        int dcModulesFaultLength = bytes.length - 6;
        if (dcModulesFaultLength % 2 != 0) {
            Log.e("QueryDeviceFaultResponse.bodyFromBytes", "DC Modules Fault Length must be even number !!! body: " + FormatUtils.bytesToHexString(bytes));
            throw new IllegalArgumentException();
        }
        this.acDeviceFault = EndianUtils.littleBytesToShort(new byte[]{bytes[0], bytes[1]});
        this.dcDeviceFault = EndianUtils.littleBytesToInt(new byte[]{bytes[2], bytes[3], bytes[4], bytes[5]});
        if (dcModulesFaultLength > 0) {
            this.dcModulesFault = new byte[dcModulesFaultLength];
            System.arraycopy(bytes, 6, this.dcModulesFault, 0, dcModulesFaultLength);
        }
        return this;
    }
}
