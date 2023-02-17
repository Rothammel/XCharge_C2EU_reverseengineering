package com.xcharge.charger.ui.adpter.c2;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.xcharge.charger.ui.adapter.api.UIServiceProxy;
import com.xcharge.charger.ui.adapter.type.UI_MODE;
import com.xcharge.charger.ui.adapter.type.UI_STATUS;
import com.xcharge.charger.ui.adpter.c2.update.UpdateBaseView;
import com.xcharge.charger.ui.adpter.c2.update.UpdateChargeView;
import com.xcharge.charger.ui.adpter.c2.update.UpdateHomeView;
import com.xcharge.charger.ui.api.UICtrlMessageProxy;
import com.xcharge.charger.ui.api.UIEventMessageProxy;
import com.xcharge.charger.ui.api.bean.UIEventMessage;

/* loaded from: classes.dex */
public class UIService extends Service {
    private static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$UI_MODE = null;
    public static final int MSG_UI_MODE_EVENT = 36865;
    public static final int MSG_UI_WIDGET_EVENT = 36866;
    private UIHandler handler = null;
    private UIMsgReceiver uiMsgReceiver = null;
    public static UI_STATUS status = null;
    public static Bundle dataForWait = null;

    static /* synthetic */ int[] $SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$UI_MODE() {
        int[] iArr = $SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$UI_MODE;
        if (iArr == null) {
            iArr = new int[UI_MODE.valuesCustom().length];
            try {
                iArr[UI_MODE.advert.ordinal()] = 5;
            } catch (NoSuchFieldError e) {
            }
            try {
                iArr[UI_MODE.alert.ordinal()] = 6;
            } catch (NoSuchFieldError e2) {
            }
            try {
                iArr[UI_MODE.challenge.ordinal()] = 7;
            } catch (NoSuchFieldError e3) {
            }
            try {
                iArr[UI_MODE.charge.ordinal()] = 2;
            } catch (NoSuchFieldError e4) {
            }
            try {
                iArr[UI_MODE.home.ordinal()] = 1;
            } catch (NoSuchFieldError e5) {
            }
            try {
                iArr[UI_MODE.test.ordinal()] = 4;
            } catch (NoSuchFieldError e6) {
            }
            try {
                iArr[UI_MODE.upgrade.ordinal()] = 3;
            } catch (NoSuchFieldError e7) {
            }
            $SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$UI_MODE = iArr;
        }
        return iArr;
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class UIMsgReceiver extends BroadcastReceiver {
        private UIMsgReceiver() {
        }

        /* synthetic */ UIMsgReceiver(UIService uIService, UIMsgReceiver uIMsgReceiver) {
            this();
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UIServiceProxy.ACTION_UI_MODE_EVENT.equals(action)) {
                String mode = intent.getStringExtra("mode");
                Bundle data = intent.getBundleExtra("data");
                Message msg = UIService.this.handler.obtainMessage(UIService.MSG_UI_MODE_EVENT, mode);
                msg.setData(data);
                UIService.this.handler.sendMessage(msg);
            } else if (UIEventMessage.ACTION_UI_EVENT.equals(action)) {
                String body = intent.getStringExtra("body");
                UIService.this.handler.sendMessage(UIService.this.handler.obtainMessage(UIService.MSG_UI_WIDGET_EVENT, body));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class UIHandler extends Handler {
        private UIHandler() {
        }

        /* synthetic */ UIHandler(UIService uIService, UIHandler uIHandler) {
            this();
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UIService.MSG_UI_MODE_EVENT /* 36865 */:
                    UI_MODE mode = UI_MODE.valueOf((String) msg.obj);
                    Bundle data = msg.getData();
                    Log.i("UIService.UIHandler", "receive MSG_UI_MODE_EVENT, mode: " + ((String) msg.obj) + ", data: " + data.toString());
                    UIService.this.handleUIModeEvent(mode, data);
                    break;
                case UIService.MSG_UI_WIDGET_EVENT /* 36866 */:
                    Log.i("UIService.UIHandler", "receive MSG_UI_WIDGET_EVENT, event: " + ((String) msg.obj));
                    UIEventMessage event = new UIEventMessage().fromJson((String) msg.obj);
                    UIService.this.handleUIWidgetEvent(event);
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override // android.app.Service
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override // android.app.Service
    public void onCreate() {
        super.onCreate();
        init();
        UIServiceProxy.getInstance().sendUIServiceEvent(UIServiceProxy.UI_SERIVCE_EVENT_CREATED);
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override // android.app.Service
    public void onDestroy() {
        destroy();
        UIServiceProxy.getInstance().sendUIServiceEvent(UIServiceProxy.UI_SERIVCE_EVENT_DESTROYED);
        super.onDestroy();
    }

    private void init() {
        Context context = getApplicationContext();
        UIEventMessageProxy.getInstance().init(context);
        UICtrlMessageProxy.getInstance().init(context);
        this.handler = new UIHandler(this, null);
        this.uiMsgReceiver = new UIMsgReceiver(this, null);
        IntentFilter filter = new IntentFilter();
        filter.addAction(UIServiceProxy.ACTION_UI_MODE_EVENT);
        filter.addAction(UIEventMessage.ACTION_UI_EVENT);
        LocalBroadcastManager.getInstance(context).registerReceiver(this.uiMsgReceiver, filter);
        UIServiceProxy.getInstance().init(context);
    }

    private void destroy() {
        UIServiceProxy.getInstance().destroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(this.uiMsgReceiver);
        this.handler.removeMessages(MSG_UI_MODE_EVENT);
        this.handler.removeMessages(MSG_UI_WIDGET_EVENT);
        UICtrlMessageProxy.getInstance().destroy();
        UIEventMessageProxy.getInstance().destroy();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUIModeEvent(UI_MODE mode, Bundle data) {
        switch ($SWITCH_TABLE$com$xcharge$charger$ui$adapter$type$UI_MODE()[mode.ordinal()]) {
            case 1:
                new UpdateHomeView().homeUIMode(data, getApplicationContext());
                return;
            case 2:
                new UpdateChargeView().nfcChargeUIMode(data, getApplicationContext());
                return;
            case 7:
                new UpdateBaseView().veification(data);
                return;
            default:
                return;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleUIWidgetEvent(UIEventMessage event) {
        new UpdateBaseView().BaseUIWidgeKey(event, getApplicationContext());
        new UpdateHomeView().homeUIWidgetCreate(event, getApplicationContext());
        new UpdateChargeView().chargeUIWidgetCreate(event);
        new UpdateChargeView().chargeUIWidgetKey(event, getApplicationContext());
        new UpdateBaseView().cancelConfigPersnalCard(event, getApplicationContext());
    }
}
