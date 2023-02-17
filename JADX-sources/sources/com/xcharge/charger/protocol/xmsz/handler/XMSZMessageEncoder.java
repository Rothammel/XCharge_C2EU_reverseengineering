package com.xcharge.charger.protocol.xmsz.handler;

import android.util.Log;
import com.xcharge.charger.protocol.xmsz.bean.XMSZMessage;
import com.xcharge.common.utils.FormatUtils;
import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

/* loaded from: classes.dex */
public class XMSZMessageEncoder extends ProtocolEncoderAdapter {
    @Override // org.apache.mina.filter.codec.ProtocolEncoder
    public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
        XMSZMessage xmszMessage = (XMSZMessage) message;
        byte[] bytes = xmszMessage.toBytes();
        Log.d("XMSZMessageEncoder.encode", "send xmsz msg: " + FormatUtils.bytesToHexString(bytes));
        IoBuffer buf = IoBuffer.allocate(bytes.length, false);
        buf.setAutoExpand(true);
        buf.putInt(bytes.length);
        buf.put(bytes);
        buf.flip();
        out.write(buf);
    }
}
