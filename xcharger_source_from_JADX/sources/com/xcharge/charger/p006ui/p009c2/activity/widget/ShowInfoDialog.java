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

/* renamed from: com.xcharge.charger.ui.c2.activity.widget.ShowInfoDialog */
public class ShowInfoDialog extends Dialog {
    private final int MSG_DISMISS_DIALOG = 1;
    private Context context;
    /* access modifiers changed from: private */
    public Handler mHandler = new Handler() {
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
    private String text;
    private TextView tv_loading_text;

    public ShowInfoDialog(Context context2, String text2) {
        super(context2, C0221R.style.CustomDialog);
        this.context = context2;
        this.text = text2;
        init();
    }

    private void init() {
        getWindow().setType(2003);
        getWindow().setFlags(128, 128);
        View view = LayoutInflater.from(this.context).inflate(C0221R.layout.dialog_show_info, (ViewGroup) null);
        setContentView(view);
        this.tv_loading_text = (TextView) view.findViewById(C0221R.C0223id.tv_loading_text);
        this.tv_loading_text.setText(this.text);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = (int) this.context.getResources().getDimension(C0221R.dimen.dialog_showinfo_width);
        lp.height = (int) this.context.getResources().getDimension(C0221R.dimen.dialog_showinfo_height);
        autoSetTextSize(this.tv_loading_text.getText().toString(), lp.width, this.tv_loading_text.getText().length());
        lp.gravity = 17;
        window.setBackgroundDrawableResource(17170445);
        window.setAttributes(lp);
    }

    private void autoSetTextSize(String text2, int maxWidth, int length) {
        if (((float) length) * 32.0f > ((float) maxWidth)) {
            float newTextSize = ((((float) maxWidth) - 40.0f) * 1.0f) / ((float) length);
            if (newTextSize < 16.0f) {
                newTextSize = 16.0f;
            }
            this.tv_loading_text.setTextSize(newTextSize);
            this.tv_loading_text.setText(text2);
            this.tv_loading_text.invalidate();
            return;
        }
        this.tv_loading_text.setText(text2);
    }

    public void show() {
        super.show();
        this.mHandler.sendEmptyMessageDelayed(1, 5000);
    }

    public void dismiss() {
        super.dismiss();
        this.mHandler.removeMessages(1);
        this.mHandler.removeCallbacksAndMessages((Object) null);
    }
}
