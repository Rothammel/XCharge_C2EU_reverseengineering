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

/* loaded from: classes.dex */
public class MessageEncoder {
    private static String[] b = {XCloudMessage.ReportSettingResult, XCloudMessage.ReportActionResult, XCloudMessage.UploadLog, XCloudMessage.ReportChargeStarted, XCloudMessage.ReportChargeCancelled, XCloudMessage.ReportAutoStopResult, XCloudMessage.ReportVerification};
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Rule rule;

    private void setRule(Rule rule) {
        this.rule = rule;
    }

    public MessageEncoder() {
        String rule_file = PropertiesUtil.loadFile(GlobalConstant.class, "/rule.json");
        RuleJson ruleJson = (RuleJson) JsonUtil.GSON.fromJson(rule_file, (Class<Object>) RuleJson.class);
        this.logger.info(JsonUtil.GSON.toJson(ruleJson));
        setRule(new Rule(ruleJson));
    }

    public byte[] encode(String deviceSourceId, String msgName, String msgVersion, String msgData) throws MessageEncoderException {
        Coder coder = new Coder(this.rule);
        byte[] bytes = coder.encode(deviceSourceId, msgName, msgVersion, msgData);
        return bytes;
    }

    public String[] decode(String deviceSourceId, byte[] msg) throws MessageEncoderException {
        Coder coder = new Coder(this.rule);
        return coder.decode(deviceSourceId, msg);
    }

    private byte getMessageSendType(String msgName) {
        return ArrayUtils.contains(b, msgName) ? AnyoMessage.START_CODE_RESPONSE : AnyoMessage.START_CODE_REQUEST;
    }
}
