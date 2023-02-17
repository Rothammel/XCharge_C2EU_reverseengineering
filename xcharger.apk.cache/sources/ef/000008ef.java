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

/* loaded from: classes.dex */
public class ShowInfoDialog extends Dialog {
    private final int MSG_DISMISS_DIALOG;
    private Context context;
    private Handler mHandler;
    private String text;
    private TextView tv_loading_text;

    public ShowInfoDialog(Context context, String text) {
        super(context, R.style.CustomDialog);
        this.MSG_DISMISS_DIALOG = 1;
        this.mHandler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.widget.ShowInfoDialog.1
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        ShowInfoDialog.this.mHandler.removeMessages(1);
                        if (ShowInfoDialog.this != null) {
                            ShowInfoDialog.this.dismiss();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            }
        };
        this.context = context;
        this.text = text;
        init();
    }

    private void init() {
        getWindow().setType(2003);
        getWindow().setFlags(128, 128);
        View view = LayoutInflater.from(this.context).inflate(R.layout.dialog_show_info, (ViewGroup) null);
        setContentView(view);
        this.tv_loading_text = (TextView) view.findViewById(R.id.tv_loading_text);
        this.tv_loading_text.setText(this.text);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = (int) this.context.getResources().getDimension(R.dimen.dialog_showinfo_width);
        lp.height = (int) this.context.getResources().getDimension(R.dimen.dialog_showinfo_height);
        autoSetTextSize(this.tv_loading_text.getText().toString(), lp.width, this.tv_loading_text.getText().length());
        lp.gravity = 17;
        window.setBackgroundDrawableResource(17170445);
        window.setAttributes(lp);
    }

    private void autoSetTextSize(String text, int maxWidth, int length) {
        if (length * 32.0f > maxWidth) {
            float newTextSize = ((maxWidth - 40.0f) * 1.0f) / length;
            if (newTextSize < 16.0f) {
                newTextSize = 16.0f;
            }
            this.tv_loading_text.setTextSize(newTextSize);
            this.tv_loading_text.setText(text);
            this.tv_loading_text.invalidate();
            return;
        }
        this.tv_loading_text.setText(text);
    }

    @Override // android.app.Dialog
    public void show() {
        super.show();
        this.mHandler.sendEmptyMessageDelayed(1, 5000L);
    }

    @Override // android.app.Dialog, android.content.DialogInterface
    public void dismiss() {
        super.dismiss();
        this.mHandler.removeMessages(1);
        this.mHandler.removeCallbacksAndMessages(null);
    }
}