package org.apache.http.client.entity;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.annotation.NotThreadSafe;
import org.apache.http.entity.AbstractHttpEntityHC4;
import org.apache.http.entity.BasicHttpEntityHC4;
import org.apache.http.entity.ByteArrayEntityHC4;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntityHC4;
import org.apache.http.entity.InputStreamEntityHC4;
import org.apache.http.entity.SerializableEntityHC4;
import org.apache.http.entity.StringEntityHC4;

@NotThreadSafe
/* loaded from: classes.dex */
public class EntityBuilder {
    private byte[] binary;
    private boolean chunked;
    private String contentEncoding;
    private ContentType contentType;
    private File file;
    private boolean gzipCompress;
    private List<NameValuePair> parameters;
    private Serializable serializable;
    private InputStream stream;
    private String text;

    EntityBuilder() {
    }

    public static EntityBuilder create() {
        return new EntityBuilder();
    }

    private void clearContent() {
        this.text = null;
        this.binary = null;
        this.stream = null;
        this.parameters = null;
        this.serializable = null;
        this.file = null;
    }

    public String getText() {
        return this.text;
    }

    public EntityBuilder setText(String text) {
        clearContent();
        this.text = text;
        return this;
    }

    public byte[] getBinary() {
        return this.binary;
    }

    public EntityBuilder setBinary(byte[] binary) {
        clearContent();
        this.binary = binary;
        return this;
    }

    public InputStream getStream() {
        return this.stream;
    }

    public EntityBuilder setStream(InputStream stream) {
        clearContent();
        this.stream = stream;
        return this;
    }

    public List<NameValuePair> getParameters() {
        return this.parameters;
    }

    public EntityBuilder setParameters(List<NameValuePair> parameters) {
        clearContent();
        this.parameters = parameters;
        return this;
    }

    public EntityBuilder setParameters(NameValuePair... parameters) {
        return setParameters(Arrays.asList(parameters));
    }

    public Serializable getSerializable() {
        return this.serializable;
    }

    public EntityBuilder setSerializable(Serializable serializable) {
        clearContent();
        this.serializable = serializable;
        return this;
    }

    public File getFile() {
        return this.file;
    }

    public EntityBuilder setFile(File file) {
        clearContent();
        this.file = file;
        return this;
    }

    public ContentType getContentType() {
        return this.contentType;
    }

    public EntityBuilder setContentType(ContentType contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getContentEncoding() {
        return this.contentEncoding;
    }

    public EntityBuilder setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
        return this;
    }

    public boolean isChunked() {
        return this.chunked;
    }

    public EntityBuilder chunked() {
        this.chunked = true;
        return this;
    }

    public boolean isGzipCompress() {
        return this.gzipCompress;
    }

    public EntityBuilder gzipCompress() {
        this.gzipCompress = true;
        return this;
    }

    private ContentType getContentOrDefault(ContentType def) {
        if (this.contentType != null) {
            ContentType def2 = this.contentType;
            return def2;
        }
        return def;
    }

    public HttpEntity build() {
        AbstractHttpEntityHC4 e;
        if (this.text != null) {
            e = new StringEntityHC4(this.text, getContentOrDefault(ContentType.DEFAULT_TEXT));
        } else if (this.binary != null) {
            e = new ByteArrayEntityHC4(this.binary, getContentOrDefault(ContentType.DEFAULT_BINARY));
        } else if (this.stream != null) {
            e = new InputStreamEntityHC4(this.stream, 1L, getContentOrDefault(ContentType.DEFAULT_BINARY));
        } else if (this.parameters != null) {
            e = new UrlEncodedFormEntityHC4(this.parameters, this.contentType != null ? this.contentType.getCharset() : null);
        } else if (this.serializable != null) {
            e = new SerializableEntityHC4(this.serializable);
            e.setContentType(ContentType.DEFAULT_BINARY.toString());
        } else if (this.file != null) {
            e = new FileEntityHC4(this.file, getContentOrDefault(ContentType.DEFAULT_BINARY));
        } else {
            e = new BasicHttpEntityHC4();
        }
        if (e.getContentType() != null && this.contentType != null) {
            e.setContentType(this.contentType.toString());
        }
        e.setContentEncoding(this.contentEncoding);
        e.setChunked(this.chunked);
        if (this.gzipCompress) {
            return new GzipCompressingEntity(e);
        }
        return e;
    }
}