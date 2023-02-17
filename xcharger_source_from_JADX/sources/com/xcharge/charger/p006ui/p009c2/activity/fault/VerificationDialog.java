package com.xcharge.charger.p006ui.p009c2.activity.fault;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.p006ui.p009c2.activity.BaseActivity;
import com.xcharge.charger.p006ui.p009c2.activity.utiles.Utils;

/* renamed from: com.xcharge.charger.ui.c2.activity.fault.VerificationDialog */
public class VerificationDialog extends BaseDialog {
    private final int MSG_DISMISS_DIALOG = 2;
    private final int MSG_TIME_REPEAT = 1;
    private Context context;
    private String customer;
    /* access modifiers changed from: private */
    public int expired = 60;
    Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    VerificationDialog.this.mHandler.removeMessages(1);
                    VerificationDialog verificationDialog = VerificationDialog.this;
                    verificationDialog.expired = verificationDialog.expired - 1;
                    if (VerificationDialog.this.expired <= 0) {
                        VerificationDialog.this.dismissDialog(false);
                    }
                    VerificationDialog.this.tv_status_one.setText(Utils.fromatTotalTime((long) VerificationDialog.this.expired));
                    VerificationDialog.this.mHandler.sendEmptyMessageDelayed(1, 1000);
                    return;
                case 2:
                    VerificationDialog.this.dismissDialog(true);
                    return;
                default:
                    return;
            }
        }
    };
    private String xid;

    public VerificationDialog(Context context2, String xid2, String customer2, int expired2) {
        super(context2, C0221R.style.Dialog_Fullscreen);
        this.context = context2;
        this.xid = xid2;
        this.customer = customer2;
        this.expired = expired2;
    }

    public void initView() {
        super.initView();
        this.tv_status_one.setVisibility(8);
        this.tv_status_one.setText(Utils.fromatTotalTime((long) this.expired));
        this.tv_status_one.setTextSize(52.0f);
        this.tv_status_two.setVisibility(0);
        this.tv_status_two.setText(this.context.getString(C0221R.string.verification_customer, new Object[]{this.customer}));
        this.tv_status_two.setTextSize(36.0f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(160, 30, 160, 0);
        this.tv_status_two.setLayoutParams(lp);
        this.tv_bottom.setVisibility(0);
        this.tv_bottom.setText(C0221R.string.verification_bottom);
        this.mHandler.sendEmptyMessage(1);
    }

    public void onBackPressed() {
        super.onBackPressed();
        this.mHandler.sendEmptyMessage(2);
    }

    /* access modifiers changed from: private */
    public void dismissDialog(boolean isTimeout) {
        DCAPProxy.getInstance().responseVerification(0, isTimeout, this.xid);
        if (isTimeout) {
            BaseActivity.showSmallDialog(this.context.getString(C0221R.string.verification_send));
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.removeCallbacksAndMessages((Object) null);
        Utils.anewSetPermitNFC(this.context);
        dismiss();
    }
}
