package net.xcharge.sdk.server;

import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.charger.protocol.family.xcloud.bean.XCloudMessage;
import net.xcharge.sdk.server.coder.Coder;
import net.xcharge.sdk.server.coder.constant.GlobalConstant;
import net.xcharge.sdk.server.coder.exceptions.MessageEncoderException;
import net.xcharge.sdk.server.coder.model.Rule;
import net.xcharge.sdk.server.coder.model.rule.RuleJson;
import net.xcharge.sdk.server.coder.utils.JsonUtil;
import net.xcharge.sdk.server.coder.utils.PropertiesUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageEncoder {

    /* renamed from: b */
    private static String[] f137b = {XCloudMessage.ReportSettingResult, XCloudMessage.ReportActionResult, XCloudMessage.UploadLog, XCloudMessage.ReportChargeStarted, XCloudMessage.ReportChargeCancelled, XCloudMessage.ReportAutoStopResult, XCloudMessage.ReportVerification};
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Rule rule;

    private void setRule(Rule rule2) {
        this.rule = rule2;
    }

    public MessageEncoder() {
        RuleJson ruleJson = (RuleJson) JsonUtil.GSON.fromJson(PropertiesUtil.loadFile(GlobalConstant.class, "/rule.json"), RuleJson.class);
        this.logger.info(JsonUtil.GSON.toJson((Object) ruleJson));
        setRule(new Rule(ruleJson));
    }

    public byte[] encode(String deviceSourceId, String msgName, String msgVersion, String msgData) throws MessageEncoderException {
        return new Coder(this.rule).encode(deviceSourceId, msgName, msgVersion, msgData);
    }

    public String[] decode(String deviceSourceId, byte[] msg) throws MessageEncoderException {
        return new Coder(this.rule).decode(deviceSourceId, msg);
    }

    private byte getMessageSendType(String msgName) {
        if (ArrayUtils.contains((Object[]) f137b, (Object) msgName)) {
            return AnyoMessage.START_CODE_RESPONSE;
        }
        return AnyoMessage.START_CODE_REQUEST;
    }
}
