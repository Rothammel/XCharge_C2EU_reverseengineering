package org.java_websocket.protocols;

import org.apache.commons.lang3.StringUtils;

public class Protocol implements IProtocol {
    private final String providedProtocol;

    public Protocol(String providedProtocol2) {
        if (providedProtocol2 == null) {
            throw new IllegalArgumentException();
        }
        this.providedProtocol = providedProtocol2;
    }

    public boolean acceptProvidedProtocol(String inputProtocolHeader) {
        for (String header : inputProtocolHeader.replaceAll(StringUtils.SPACE, "").split(",")) {
            if (this.providedProtocol.equals(header)) {
                return true;
            }
        }
        return false;
    }

    public String getProvidedProtocol() {
        return this.providedProtocol;
    }

    public IProtocol copyInstance() {
        return new Protocol(getProvidedProtocol());
    }

    public String toString() {
        return getProvidedProtocol();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return this.providedProtocol.equals(((Protocol) o).providedProtocol);
    }

    public int hashCode() {
        return this.providedProtocol.hashCode();
    }
}
