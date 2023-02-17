package it.sauronsoftware.ftp4j;

import com.xcharge.charger.data.bean.device.MobileNet;
import java.util.Date;

/* loaded from: classes.dex */
public class FTPFile {
    public static final int TYPE_DIRECTORY = 1;
    public static final int TYPE_FILE = 0;
    public static final int TYPE_LINK = 2;
    private int type;
    private String name = null;
    private String link = null;
    private Date modifiedDate = null;
    private long size = -1;

    public Date getModifiedDate() {
        return this.modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {
        this.modifiedDate = modifiedDate;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getSize() {
        return this.size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(getClass().getName());
        buffer.append(" [name=");
        buffer.append(this.name);
        buffer.append(", type=");
        if (this.type == 0) {
            buffer.append("FILE");
        } else if (this.type == 1) {
            buffer.append("DIRECTORY");
        } else if (this.type == 2) {
            buffer.append("LINK");
            buffer.append(", link=");
            buffer.append(this.link);
        } else {
            buffer.append(MobileNet.OPR_UNKNOWN);
        }
        buffer.append(", size=");
        buffer.append(this.size);
        buffer.append(", modifiedDate=");
        buffer.append(this.modifiedDate);
        buffer.append("]");
        return buffer.toString();
    }
}