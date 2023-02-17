package com.xcharge.charger.ui.c2.activity.fault;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import com.xcharge.charger.R;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.ui.c2.activity.BaseActivity;
import com.xcharge.charger.ui.c2.activity.utiles.Utils;

/* loaded from: classes.dex */
public class VerificationDialog extends BaseDialog {
    private final int MSG_DISMISS_DIALOG;
    private final int MSG_TIME_REPEAT;
    private Context context;
    private String customer;
    private int expired;
    Handler mHandler;
    private String xid;

    public VerificationDialog(Context context, String xid, String customer, int expired) {
        super(context, R.style.Dialog_Fullscreen);
        this.MSG_TIME_REPEAT = 1;
        this.MSG_DISMISS_DIALOG = 2;
        this.expired = 60;
        this.mHandler = new Handler() { // from class: com.xcharge.charger.ui.c2.activity.fault.VerificationDialog.1
            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        VerificationDialog.this.mHandler.removeMessages(1);
                        VerificationDialog verificationDialog = VerificationDialog.this;
                        verificationDialog.expired--;
                        if (VerificationDialog.this.expired <= 0) {
                            VerificationDialog.this.dismissDialog(false);
                        }
                        VerificationDialog.this.tv_status_one.setText(Utils.fromatTotalTime(VerificationDialog.this.expired));
                        VerificationDialog.this.mHandler.sendEmptyMessageDelayed(1, 1000L);
                        return;
                    case 2:
                        VerificationDialog.this.dismissDialog(true);
                        return;
                    default:
                        return;
                }
            }
        };
        this.context = context;
        this.xid = xid;
        this.customer = customer;
        this.expired = expired;
    }

    @Override // com.xcharge.charger.ui.c2.activity.fault.BaseDialog
    public void initView() {
        super.initView();
        this.tv_status_one.setVisibility(8);
        this.tv_status_one.setText(Utils.fromatTotalTime(this.expired));
        this.tv_status_one.setTextSize(52.0f);
        this.tv_status_two.setVisibility(0);
        this.tv_status_two.setText(this.context.getString(R.string.verification_customer, this.customer));
        this.tv_status_two.setTextSize(36.0f);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMargins(160, 30, 160, 0);
        this.tv_status_two.setLayoutParams(lp);
        this.tv_bottom.setVisibility(0);
        this.tv_bottom.setText(R.string.verification_bottom);
        this.mHandler.sendEmptyMessage(1);
    }

    @Override // com.xcharge.charger.ui.c2.activity.fault.BaseDialog, android.app.Dialog
    public void onBackPressed() {
        super.onBackPressed();
        this.mHandler.sendEmptyMessage(2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void dismissDialog(boolean isTimeout) {
        DCAPProxy.getInstance().responseVerification(0, isTimeout, this.xid);
        if (isTimeout) {
            BaseActivity.showSmallDialog(this.context.getString(R.string.verification_send));
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler.removeCallbacksAndMessages(null);
        Utils.anewSetPermitNFC(this.context);
        dismiss();
    }
}