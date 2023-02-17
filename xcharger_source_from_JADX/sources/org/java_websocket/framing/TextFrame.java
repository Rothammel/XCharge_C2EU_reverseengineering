package org.java_websocket.framing;

import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.util.Charsetfunctions;

public class TextFrame extends DataFrame {
    public TextFrame() {
        super(Framedata.Opcode.TEXT);
    }

    public void isValid() throws InvalidDataException {
        super.isValid();
        if (!Charsetfunctions.isValidUTF8(getPayloadData())) {
            throw new InvalidDataException((int) CloseFrame.NO_UTF8, "Received text is no valid utf8 string!");
        }
    }
}
