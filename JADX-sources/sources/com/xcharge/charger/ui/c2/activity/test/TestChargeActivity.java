package com.xcharge.charger.ui.c2.activity.test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import com.xcharge.charger.R;
import com.xcharge.charger.data.bean.ErrorCode;
import com.xcharge.charger.data.bean.device.Network;
import com.xcharge.charger.data.bean.device.Port;
import com.xcharge.charger.data.bean.status.PortStatus;
import com.xcharge.charger.data.bean.type.LOCK_STATUS;
import com.xcharge.charger.data.bean.type.PHASE;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.SoftwareStatusCacheProvider;
import com.xcharge.charger.device.api.PortStatusListener;
import com.xcharge.charger.device.c2.bean.PortRuntimeData;
import com.xcharge.charger.device.c2.service.C2DeviceEventDispatcher;
import com.xcharge.charger.device.c2.service.C2DeviceProxy;
import com.xcharge.charger.protocol.ocpp.bean.types.UnitOfMeasure;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.HomeActivity;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

/* loaded from: classes.dex */
public class TestChargeActivity extends BaseActivity {
    private static final int MSG_FULL = 6;
    private static final int MSG_INVALID = 3;
    private static final int MSG_PLGIN = 1;
    private static final int MSG_PLOUT = 2;
    private static final int MSG_START = 4;
    private static final int MSG_STOP = 5;
    private static final int MSG_UPDATE = 0;
    private static final int MSG_UPDATE_REFRESH = 7;
    private static long delayMillis = 1500;
    private TestHandler mTestHandler;
    private PortListener portListener;
    private ErrorCode restoreDeviceErrorCode = null;
    private TextView tv_bottom;
    private TextView tv_content;
    private TextView tv_error;
    private TextView tv_state;
    private TextView tv_wifi;

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_charge);
        Log.d("TestChargeActivity", "onCreate");
        this.portListener = new PortListener();
        C2DeviceEventDispatcher.getInstance().attachPortStatusListener(this.portListener);
        C2DeviceProxy.getInstance().enableGunLock("1");
        ChargeStatusCacheProvider.getInstance().updatePortLockStatus("1", LOCK_STATUS.unlock);
        C2DeviceProxy.getInstance().authValid("1", "U1", "000000");
        this.restoreDeviceErrorCode = HardwareStatusCacheProvider.getInstance().getDeviceFaultStatus();
        HardwareStatusCacheProvider.getInstance().updateDeviceFaultStatus(new ErrorCode(200));
    }

    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity
    protected void initView() {
        this.tv_content = (TextView) findViewById(R.id.tv_content);
        this.tv_state = (TextView) findViewById(R.id.tv_state);
        this.tv_bottom = (TextView) findViewById(R.id.tv_bottom);
        this.tv_error = (TextView) findViewById(R.id.tv_error);
        this.tv_wifi = (TextView) findViewById(R.id.tv_wifi);
        this.mTestHandler = new TestHandler(this);
        this.tv_content.setText("插枪开始充电，注意观察LED灯是否有变化、功率、电表读数、相数；");
        this.tv_bottom.setText("按压退出测试\n长按5秒关机");
        this.mTestHandler.sendEmptyMessageDelayed(7, delayMillis);
    }

    @Override // android.app.Activity
    protected void onStart() {
        super.onStart();
        Utils.setPermitNFC(true, true, true, true);
    }

    /* loaded from: classes.dex */
    static class TestHandler extends Handler {
        WeakReference<TestChargeActivity> weakReference;

        public TestHandler(TestChargeActivity testModeBase) {
            this.weakReference = new WeakReference<>(testModeBase);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (this.weakReference.get() != null && this.weakReference.get().tv_content != null) {
                Log.d("TestHandler.msg.what", new StringBuilder(String.valueOf(msg.what)).toString());
                switch (msg.what) {
                    case 1:
                        this.weakReference.get().tv_state.setText("充电枪已插入");
                        return;
                    case 2:
                        this.weakReference.get().tv_state.setText("等待连接");
                        return;
                    case 3:
                        this.weakReference.get().tv_state.setText("未授权");
                        return;
                    case 4:
                        this.weakReference.get().tv_state.setText("开始充电");
                        C2DeviceProxy.getInstance().lockGun("1");
                        ChargeStatusCacheProvider.getInstance().updatePortLockStatus("1", LOCK_STATUS.lock);
                        return;
                    case 5:
                        this.weakReference.get().tv_state.setText("结束充电");
                        return;
                    case 6:
                        this.weakReference.get().tv_state.setText("充满");
                        C2DeviceProxy.getInstance().unlockGun("1");
                        ChargeStatusCacheProvider.getInstance().updatePortLockStatus("1", LOCK_STATUS.unlock);
                        return;
                    case 7:
                        try {
                            removeMessages(7);
                            PortRuntimeData data = C2DeviceProxy.getInstance().getPortRuntimeInfo("1");
                            String ip = null;
                            Network network = HardwareStatusCacheProvider.getInstance().getNetworkStatus();
                            if (network.isConnected()) {
                                if (Network.NETWORK_TYPE_ETHERNET.equals(network.getActive())) {
                                    ip = network.getEthernet().getIp();
                                } else if (Network.NETWORK_TYPE_WIFI.equals(network.getActive())) {
                                    ip = network.getWifi().getIp();
                                } else if (Network.NETWORK_TYPE_MOBILE.equals(network.getActive())) {
                                    ip = network.getMobile().getIp();
                                }
                            }
                            StringBuffer buffer = new StringBuffer();
                            StringBuffer buffer2 = new StringBuffer();
                            String currentStr = null;
                            StringBuffer buffer3 = new StringBuffer();
                            String voltageStr = null;
                            StringBuffer buffer4 = new StringBuffer();
                            String firewareVer = SoftwareStatusCacheProvider.getInstance().getFirewareVer();
                            String appVer = SoftwareStatusCacheProvider.getInstance().getAppVer();
                            if (!TextUtils.isEmpty(firewareVer) && !TextUtils.isEmpty(appVer)) {
                                buffer.append(UnitOfMeasure.V + firewareVer + "-" + appVer).append("<br>");
                            }
                            if (!TextUtils.isEmpty(ip)) {
                                buffer.append("ip:").append(ip).append("<br>");
                            }
                            if (data != null) {
                                buffer.append("状态:");
                                switch (data.getStatus().intValue()) {
                                    case 0:
                                        buffer.append("空闲");
                                        break;
                                    case 1:
                                    default:
                                        Port port = HardwareStatusCacheProvider.getInstance().getPort("1");
                                        if (port != null && port.getDeviceError().getCode() == 30017 && port.getAvgAmp() != null && port.getAvgAmp().doubleValue() > 0.0d) {
                                            buffer.append("充电桩漏电异常");
                                            this.weakReference.get().tv_error.setText("异常状态:充电桩漏电异常");
                                            break;
                                        }
                                        break;
                                    case 2:
                                        buffer.append("等待连接");
                                        break;
                                    case 3:
                                        buffer.append("充电枪已连接/暂停充电");
                                        break;
                                    case 4:
                                        buffer.append("正在充电");
                                        break;
                                    case 5:
                                        buffer.append("充满");
                                        break;
                                    case 6:
                                        buffer.append("用户停止充电");
                                        break;
                                }
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                buffer.append(StringUtils.SPACE).append("时间:").append(sdf.format(new Date())).append("<br>").append("功率:").append(data.getPower()).append(StringUtils.SPACE).append("电表:").append(data.getEnergy()).append(StringUtils.SPACE).append("雷达:").append(data.getRader()).append(StringUtils.SPACE).append("校准:").append(data.getRaderCalibration()).append(StringUtils.SPACE).append("<br>").append("模式:").append(ChargeStatusCacheProvider.getInstance().getWorkMode().getMode()).append(StringUtils.SPACE).append("Max:").append(data.getCurrentMax()).append(StringUtils.SPACE).append("Used:").append(data.getCurrentUsed()).append("<br>");
                                Double current = data.getCurrent();
                                if (current != null) {
                                    buffer.append("电流:").append(current).append(StringUtils.SPACE);
                                }
                                Double currentA = data.getCurrentA();
                                Double currentB = data.getCurrentB();
                                Double currentC = data.getCurrentC();
                                if (currentA != null) {
                                    buffer2.append("电流A:").append(currentA).append(StringUtils.SPACE);
                                }
                                if (currentB != null) {
                                    buffer2.append("电流B:").append(currentB).append(StringUtils.SPACE);
                                }
                                if (currentC != null) {
                                    buffer2.append("电流C:").append(currentC).append(StringUtils.SPACE);
                                }
                                if (currentA.doubleValue() < 0.0d || currentB.doubleValue() < 0.0d || currentC.doubleValue() < 0.0d) {
                                    currentStr = "<font color='red'>" + buffer2.toString() + "</font>";
                                } else {
                                    currentStr = buffer2.toString();
                                }
                                Double voltageA = data.getVoltageA();
                                Double voltageB = data.getVoltageB();
                                Double voltageC = data.getVoltageC();
                                if (voltageA != null) {
                                    buffer3.append("电压A:").append(voltageA).append(StringUtils.SPACE);
                                }
                                if (voltageB != null) {
                                    buffer3.append("电压B:").append(voltageB).append(StringUtils.SPACE);
                                }
                                if (voltageC != null) {
                                    buffer3.append("电压C:").append(voltageC).append(StringUtils.SPACE);
                                }
                                voltageStr = buffer3.toString();
                                if (PHASE.THREE_PHASE.getPhase() == HardwareStatusCacheProvider.getInstance().getHardwareStatus().getPhase().getPhase() && voltageA != null && voltageB != null && voltageC != null && (Math.abs(voltageA.doubleValue() - voltageB.doubleValue()) > 1.0d || Math.abs(voltageA.doubleValue() - voltageC.doubleValue()) > 1.0d || Math.abs(voltageB.doubleValue() - voltageC.doubleValue()) > 1.0d)) {
                                    voltageStr = "<font color='red'>" + buffer3.toString() + "</font>";
                                }
                                buffer4.append("CP电压:").append(data.getCpVoltage()).append(StringUtils.SPACE).append("chipTemp:").append(data.getChipTemp());
                            }
                            this.weakReference.get().tv_content.setText(Html.fromHtml(String.valueOf(buffer.toString()) + currentStr + "<br>" + voltageStr + "<br>" + buffer4.toString() + "<br> 刷卡测试NFC，当有滴滴声音时表示测试成功"));
                            this.weakReference.get().tv_rader.setVisibility(0);
                            this.weakReference.get().tv_rader.setText(new StringBuilder().append(data.getRader()).toString());
                            sendEmptyMessageDelayed(7, TestChargeActivity.delayMillis);
                            return;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    default:
                        return;
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onPause() {
        super.onPause();
        Log.d("TestChargeActivity", "onPause");
        HardwareStatusCacheProvider.getInstance().updateDeviceFaultStatus(this.restoreDeviceErrorCode);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // com.xcharge.charger.ui.c2.activity.BaseActivity, android.app.Activity
    public void onDestroy() {
        super.onDestroy();
        Log.d("TestChargeActivity", "onDestroy");
        C2DeviceEventDispatcher.getInstance().dettachPortStatusListener(this.portListener);
        if (LOCK_STATUS.lock.equals(ChargeStatusCacheProvider.getInstance().getPortLockStatus("1"))) {
            C2DeviceProxy.getInstance().unlockGun("1");
            ChargeStatusCacheProvider.getInstance().updatePortLockStatus("1", LOCK_STATUS.unlock);
        }
        C2DeviceProxy.getInstance().authInValid("1", "U1", "000000");
    }

    @Override // android.app.Activity
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    /* loaded from: classes.dex */
    class PortListener implements PortStatusListener {
        PortListener() {
        }

        @Override // com.xcharge.charger.device.api.PortStatusListener
        public void onAuthValid(String port, PortStatus data) {
            TestChargeActivity.this.mTestHandler.obtainMessage(0, data).sendToTarget();
        }

        @Override // com.xcharge.charger.device.api.PortStatusListener
        public void onAuthInvalid(String port, PortStatus data) {
            TestChargeActivity.this.mTestHandler.sendEmptyMessage(3);
        }

        @Override // com.xcharge.charger.device.api.PortStatusListener
        public void onPlugin(String port, PortStatus data) {
            TestChargeActivity.this.mTestHandler.obtainMessage(1, null).sendToTarget();
        }

        @Override // com.xcharge.charger.device.api.PortStatusListener
        public void onPlugout(String port, PortStatus data) {
            TestChargeActivity.this.mTestHandler.obtainMessage(2, null).sendToTarget();
        }

        @Override // com.xcharge.charger.device.api.PortStatusListener
        public void onChargeStart(String port, PortStatus data) {
            TestChargeActivity.this.mTestHandler.sendEmptyMessage(4);
        }

        @Override // com.xcharge.charger.device.api.PortStatusListener
        public void onChargeFull(String port, PortStatus data) {
            TestChargeActivity.this.mTestHandler.sendEmptyMessage(6);
        }

        @Override // com.xcharge.charger.device.api.PortStatusListener
        public void onChargeStop(String port, PortStatus data) {
            TestChargeActivity.this.mTestHandler.sendEmptyMessage(5);
        }

        @Override // com.xcharge.charger.device.api.PortStatusListener
        public void onSuspend(String port, PortStatus data) {
            TestChargeActivity.this.mTestHandler.obtainMessage(0, data).sendToTarget();
        }

        @Override // com.xcharge.charger.device.api.PortStatusListener
        public void onResume(String port, PortStatus data) {
            TestChargeActivity.this.mTestHandler.obtainMessage(0, data).sendToTarget();
        }

        @Override // com.xcharge.charger.device.api.PortStatusListener
        public void onWarning(String port) {
        }

        @Override // com.xcharge.charger.device.api.PortStatusListener
        public void onUpdate(String port, PortStatus data) {
        }

        @Override // com.xcharge.charger.device.api.PortStatusListener
        public void onParkBusy(String port) {
        }

        @Override // com.xcharge.charger.device.api.PortStatusListener
        public void onParkIdle(String port) {
        }

        @Override // com.xcharge.charger.device.api.PortStatusListener
        public void onParkUnkow(String port) {
        }

        @Override // com.xcharge.charger.device.api.PortStatusListener
        public void onRadarCalibration(String port, boolean isSuccess) {
        }
    }
}
