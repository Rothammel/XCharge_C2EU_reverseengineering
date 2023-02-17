package com.xcharge.charger.protocol.anyo.handler;

import android.util.Log;
import com.xcharge.charger.protocol.anyo.bean.AnyoMessage;
import com.xcharge.common.utils.FormatUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/* loaded from: classes.dex */
public class AnyoMessageEncoder extends ProtocolEncoderAdapter {
    @Override // org.apache.mina.filter.codec.ProtocolEncoder
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        AnyoMessage anyoMessage = (AnyoMessage) message;
        byte[] bytes = anyoMessage.toBytes();
        Log.d("AnyoMessageEncoder.encode", "send anyo msg: " + FormatUtils.bytesToHexString(bytes));
        IoBuffer buf = IoBuffer.allocate(bytes.length, false);
        buf.setAutoExpand(true);
        buf.put(bytes);
        buf.flip();
        out.write(buf);
    }
}
