package com.xcharge.charger.p006ui.p009c2.activity.fault;

import android.content.Context;
import com.xcharge.charger.C0221R;
import com.xcharge.charger.data.bean.type.CHARGE_PLATFORM;
import com.xcharge.charger.data.bean.type.PLATFORM_CUSTOMER;
import com.xcharge.charger.data.provider.ChargeStatusCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;

/* renamed from: com.xcharge.charger.ui.c2.activity.fault.CloudFaultDialog */
public class CloudFaultDialog extends BaseDialog {
    public CloudFaultDialog(Context context) {
        super(context, C0221R.style.Dialog_Fullscreen);
    }

    public void initView() {
        super.initView();
        this.iv_cloud.setVisibility(8);
        this.iv_status_one.setImageResource(C0221R.C0222drawable.ic_cloud_error);
        CHARGE_PLATFORM platform = SystemSettingCacheProvider.getInstance().getChargePlatform();
        PLATFORM_CUSTOMER customer = SystemSettingCacheProvider.getInstance().getPlatformCustomer();
        if (!CHARGE_PLATFORM.xcharge.equals(platform) || !PLATFORM_CUSTOMER.anyo_private.equals(customer)) {
            this.tv_status_one.setText(C0221R.string.cloud_error_status_text);
        } else {
            this.tv_status_one.setText(C0221R.string.anyo_private_cloud_error_status_text);
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        if (ChargeStatusCacheProvider.getInstance().isCloudConnected()) {
            dismiss();
        }
    }
}
