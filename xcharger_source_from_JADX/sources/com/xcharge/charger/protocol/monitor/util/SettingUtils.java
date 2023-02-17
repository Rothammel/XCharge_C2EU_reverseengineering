package com.xcharge.charger.protocol.monitor.util;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.xcharge.charger.core.api.DCAPProxy;
import com.xcharge.charger.core.api.Sequence;
import com.xcharge.charger.core.api.bean.DCAPMessage;
import com.xcharge.charger.core.api.bean.cap.CAPDirectiveOption;
import com.xcharge.charger.core.api.bean.cap.CAPMessage;
import com.xcharge.charger.core.api.bean.cap.SetDirective;
import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.charger.data.bean.type.ADVERT_POLICY;
import com.xcharge.charger.data.bean.type.CONTENT_MEDIA_TYPE;
import com.xcharge.charger.data.provider.HardwareStatusCacheProvider;
import com.xcharge.charger.data.provider.RemoteSettingCacheProvider;
import com.xcharge.charger.data.provider.SystemSettingCacheProvider;
import com.xcharge.common.utils.HttpDownloadManager;
import java.util.ArrayList;
import java.util.HashMap;
import org.eclipse.paho.client.mqttv3.MqttTopic;

public class SettingUtils {
    public static void setDCAPRequest(String setId, HashMap<String, Object> values) {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setSet_id(setId);
        SetDirective set = new SetDirective();
        set.setValues(values);
        DCAPProxy.getInstance().sendRequest(createRequest("server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform(), "set", opt, set));
    }

    private static DCAPMessage createRequest(String from, String op, CAPDirectiveOption opt, Object directive) {
        CAPMessage requestCap = new CAPMessage();
        DCAPMessage request = new DCAPMessage();
        request.setFrom(from);
        request.setTo("device:sn/" + HardwareStatusCacheProvider.getInstance().getSn());
        request.setType("cap");
        request.setCtime(System.currentTimeMillis());
        request.setSeq(Sequence.getAgentDCAPSequence());
        requestCap.setOp(op);
        requestCap.setOpt(opt);
        requestCap.setData(directive);
        request.setData(requestCap);
        return request;
    }

    public static void downloadAdertResource(Context context, ADVERT_POLICY type, ContentItem content, int index, ArrayList<ContentItem> restoreAdvtSite) {
        final String fileUrl = content.getFileUrl();
        String rsrcType = content.getType();
        if (!TextUtils.isEmpty(fileUrl) && !TextUtils.isEmpty(rsrcType)) {
            final String toFile = "/data/data/com.xcharge.charger/download/advert/" + type.getPolicy() + MqttTopic.TOPIC_LEVEL_SEPARATOR + index + "." + CONTENT_MEDIA_TYPE.getFileSuffix(CONTENT_MEDIA_TYPE.valueBy(rsrcType));
            final ADVERT_POLICY advert_policy = type;
            final int i = index;
            final ArrayList<ContentItem> arrayList = restoreAdvtSite;
            HttpDownloadManager.getInstance().downloadFile(context, fileUrl, toFile, new HttpDownloadManager.DownLoadListener() {
                public void onDownLoadPercentage(long curPosition, long total) {
                }

                public void onDownLoadPercentage(int p) {
                }

                public void onDownLoadFail() {
                    Log.w("SettingUtils.downloadAdertResource", "failed to download advert resource: " + fileUrl + ", policy: " + advert_policy.getPolicy() + ", index: " + i);
                    if (arrayList != null) {
                        RemoteSettingCacheProvider.getInstance().updateAdvertContent(advert_policy, i, (ContentItem) arrayList.get(i));
                    } else {
                        RemoteSettingCacheProvider.getInstance().updateAdvertContent(advert_policy, i, (ContentItem) null);
                    }
                    RemoteSettingCacheProvider.getInstance().persist();
                }

                public void onDownLoadComplete() {
                    Log.i("SettingUtils.downloadAdertResource", "succeed to download advert resource: " + fileUrl + ", policy: " + advert_policy.getPolicy() + ", index: " + i);
                    RemoteSettingCacheProvider.getInstance().updateAdvertResouce(advert_policy, i, toFile);
                    RemoteSettingCacheProvider.getInstance().persist();
                }
            });
        }
    }
}
