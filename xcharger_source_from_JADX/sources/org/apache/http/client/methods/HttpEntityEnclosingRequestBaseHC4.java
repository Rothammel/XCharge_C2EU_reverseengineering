package org.apache.http.client.methods;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.client.utils.CloneUtilsHC4;

@NotThreadSafe
public abstract class HttpEntityEnclosingRequestBaseHC4 extends HttpRequestBaseHC4 implements HttpEntityEnclosingRequest {
    private HttpEntity entity;

    public HttpEntity getEntity() {
        return this.entity;
    }

    public void setEntity(HttpEntity entity2) {
        this.entity = entity2;
    }

    public boolean expectContinue() {
        Header expect = getFirstHeader(HttpHeaders.EXPECT);
        return expect != null && "100-continue".equalsIgnoreCase(expect.getValue());
    }

    public Object clone() throws CloneNotSupportedException {
        HttpEntityEnclosingRequestBaseHC4 clone = (HttpEntityEnclosingRequestBaseHC4) super.clone();
        if (this.entity != null) {
            clone.entity = (HttpEntity) CloneUtilsHC4.cloneObject(this.entity);
        }
        return clone;
    }
}
