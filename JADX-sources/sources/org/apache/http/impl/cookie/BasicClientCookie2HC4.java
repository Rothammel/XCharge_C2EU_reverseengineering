package org.apache.http.impl.cookie;

import java.util.Date;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.cookie.SetCookie2;

@NotThreadSafe
/* loaded from: classes.dex */
public class BasicClientCookie2HC4 extends BasicClientCookieHC4 implements SetCookie2 {
    private static final long serialVersionUID = -7744598295706617057L;
    private String commentURL;
    private boolean discard;
    private int[] ports;

    public BasicClientCookie2HC4(String name, String value) {
        super(name, value);
    }

    @Override // org.apache.http.impl.cookie.BasicClientCookieHC4
    public int[] getPorts() {
        return this.ports;
    }

    public void setPorts(int[] ports) {
        this.ports = ports;
    }

    @Override // org.apache.http.impl.cookie.BasicClientCookieHC4
    public String getCommentURL() {
        return this.commentURL;
    }

    public void setCommentURL(String commentURL) {
        this.commentURL = commentURL;
    }

    public void setDiscard(boolean discard) {
        this.discard = discard;
    }

    @Override // org.apache.http.impl.cookie.BasicClientCookieHC4
    public boolean isPersistent() {
        return !this.discard && super.isPersistent();
    }

    @Override // org.apache.http.impl.cookie.BasicClientCookieHC4
    public boolean isExpired(Date date) {
        return this.discard || super.isExpired(date);
    }

    @Override // org.apache.http.impl.cookie.BasicClientCookieHC4
    public Object clone() throws CloneNotSupportedException {
        BasicClientCookie2HC4 clone = (BasicClientCookie2HC4) super.clone();
        if (this.ports != null) {
            clone.ports = (int[]) this.ports.clone();
        }
        return clone;
    }
}
