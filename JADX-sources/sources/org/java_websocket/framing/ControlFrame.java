package org.java_websocket.framing;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.exceptions.InvalidFrameException;
import org.java_websocket.framing.Framedata;

/* loaded from: classes.dex */
public abstract class ControlFrame extends FramedataImpl1 {
    public ControlFrame(Framedata.Opcode opcode) {
        super(opcode);
    }

    @Override // org.java_websocket.framing.FramedataImpl1
    public void isValid() throws InvalidDataException {
        if (!isFin()) {
            throw new InvalidFrameException("Control frame cant have fin==false set");
        }
        if (isRSV1()) {
            throw new InvalidFrameException("Control frame cant have rsv1==true set");
        }
        if (isRSV2()) {
            throw new InvalidFrameException("Control frame cant have rsv2==true set");
        }
        if (isRSV3()) {
            throw new InvalidFrameException("Control frame cant have rsv3==true set");
        }
    }
}
