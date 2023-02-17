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

/* loaded from: classes.dex */
public class SettingUtils {
    public static void setDCAPRequest(String setId, HashMap<String, Object> values) {
        CAPDirectiveOption opt = new CAPDirectiveOption();
        opt.setSet_id(setId);
        SetDirective set = new SetDirective();
        set.setValues(values);
        String from = "server:" + SystemSettingCacheProvider.getInstance().getChargePlatform().getPlatform();
        DCAPMessage setRequest = createRequest(from, "set", opt, set);
        DCAPProxy.getInstance().sendRequest(setRequest);
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

    public static void downloadAdertResource(Context context, final ADVERT_POLICY type, ContentItem content, final int index, final ArrayList<ContentItem> restoreAdvtSite) {
        final String fileUrl = content.getFileUrl();
        String rsrcType = content.getType();
        if (!TextUtils.isEmpty(fileUrl) && !TextUtils.isEmpty(rsrcType)) {
            String fileSuffix = CONTENT_MEDIA_TYPE.getFileSuffix(CONTENT_MEDIA_TYPE.valueBy(rsrcType));
            final String toFile = "/data/data/com.xcharge.charger/download/advert/" + type.getPolicy() + MqttTopic.TOPIC_LEVEL_SEPARATOR + index + "." + fileSuffix;
            HttpDownloadManager.getInstance().downloadFile(context, fileUrl, toFile, new HttpDownloadManager.DownLoadListener() { // from class: com.xcharge.charger.protocol.monitor.util.SettingUtils.1
                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadPercentage(long curPosition, long total) {
                }

                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadPercentage(int p) {
                }

                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadFail() {
                    Log.w("SettingUtils.downloadAdertResource", "failed to download advert resource: " + fileUrl + ", policy: " + type.getPolicy() + ", index: " + index);
                    if (restoreAdvtSite != null) {
                        RemoteSettingCacheProvider.getInstance().updateAdvertContent(type, index, (ContentItem) restoreAdvtSite.get(index));
                    } else {
                        RemoteSettingCacheProvider.getInstance().updateAdvertContent(type, index, null);
                    }
                    RemoteSettingCacheProvider.getInstance().persist();
                }

                @Override // com.xcharge.common.utils.HttpDownloadManager.DownLoadListener
                public void onDownLoadComplete() {
                    Log.i("SettingUtils.downloadAdertResource", "succeed to download advert resource: " + fileUrl + ", policy: " + type.getPolicy() + ", index: " + index);
                    RemoteSettingCacheProvider.getInstance().updateAdvertResouce(type, index, toFile);
                    RemoteSettingCacheProvider.getInstance().persist();
                }
            });
        }
    }
}