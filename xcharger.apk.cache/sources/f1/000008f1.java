package com.xcharge.charger.ui.c2.activity.widget;

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
import com.xcharge.charger.R;
import it.sauronsoftware.ftp4j.FTPCodes;

/* loaded from: classes.dex */
public class SmallDialog extends Dialog {
    private final int MSG_TIME_REPEAT;
    private Context context;
    private int countDown;
    Handler mHandler;

    private SmallDialog(Context context, String text, int time) {
        super(context, R.style.Dialog_lucency);
        this.MSG_TIME_REPEAT = 1;
        this.mHandler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.widget.SmallDialog.1
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        SmallDialog.this.mHandler.removeMessages(1);
                        SmallDialog smallDialog = SmallDialog.this;
                        smallDialog.countDown--;
                        if (SmallDialog.this.countDown < 0) {
                            SmallDialog.this.dismiss();
                            return;
                        } else {
                            SmallDialog.this.mHandler.sendEmptyMessageDelayed(1, 1000L);
                            return;
                        }
                    default:
                        return;
                }
            }
        };
        this.context = context;
        init(text, time);
    }

    public static SmallDialog createDialog(Context context, String text, int time) {
        return new SmallDialog(context, text, time);
    }

    public void init(String text, int time) {
        getWindow().setType(2003);
        LayoutInflater inflater = LayoutInflater.from(this.context);
        View view = inflater.inflate(R.layout.custom_toast_view, (ViewGroup) null);
        setContentView(view);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.y = FTPCodes.FILE_STATUS_OK;
        lp.gravity = 80;
        window.setAttributes(lp);
        this.countDown = time;
        TextView tvInfo = (TextView) view.findViewById(R.id.tv_custom_text);
        tvInfo.setText(text);
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // android.app.Dialog
    public void onBackPressed() {
        dismiss();
    }
}