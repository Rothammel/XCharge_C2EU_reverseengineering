package com.xcharge.charger.p006ui.p009c2.activity.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import com.xcharge.charger.C0221R;
import p010it.sauronsoftware.ftp4j.FTPCodes;

/* renamed from: com.xcharge.charger.ui.c2.activity.widget.SmallDialog */
public class SmallDialog extends Dialog {
    private final int MSG_TIME_REPEAT = 1;
    private Context context;
    /* access modifiers changed from: private */
    public int countDown;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    SmallDialog.this.mHandler.removeMessages(1);
                    SmallDialog smallDialog = SmallDialog.this;
                    smallDialog.countDown = smallDialog.countDown - 1;
                    if (SmallDialog.this.countDown < 0) {
                        SmallDialog.this.dismiss();
                        return;
                    } else {
                        SmallDialog.this.mHandler.sendEmptyMessageDelayed(1, 1000);
                        return;
                    }
                default:
                    return;
            }
        }
    };

    private SmallDialog(Context context2, String text, int time) {
        super(context2, C0221R.style.Dialog_lucency);
        this.context = context2;
        init(text, time);
    }

    public static SmallDialog createDialog(Context context2, String text, int time) {
        return new SmallDialog(context2, text, time);
    }

    public void init(String text, int time) {
        getWindow().setType(2003);
        View view = LayoutInflater.from(this.context).inflate(C0221R.layout.custom_toast_view, (ViewGroup) null);
        setContentView(view);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.y = FTPCodes.FILE_STATUS_OK;
        lp.gravity = 80;
        window.setAttributes(lp);
        this.countDown = time;
        ((TextView) view.findViewById(C0221R.C0223id.tv_custom_text)).setText(text);
        this.mHandler.sendEmptyMessage(1);
    }

    public void onBackPressed() {
        dismiss();
    }
}
