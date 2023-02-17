package net.xcharge.sdk.server.coder.model.rule.subitem;

/* loaded from: classes.dex */
public class SubField {
    private int arrayLength;
    private int id;
    private String name;
    private boolean productFamilyRelated;
    private int scaleFactor;
    private String subtype;
    private String type;
    private String valueMap;

    public int getId() {
        return this.id;
    }

    void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return this.type;
    }

    void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return this.subtype;
    }

    void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getValueMap() {
        return this.valueMap;
    }

    void setValueMap(String valueMap) {
        this.valueMap = valueMap;
    }

    public boolean isProductFamilyRelated() {
        return this.productFamilyRelated;
    }

    void setProductFamilyRelated(boolean productFamilyRelated) {
        this.productFamilyRelated = productFamilyRelated;
    }

    public int getScaleFactor() {
        return this.scaleFactor;
    }

    void setScaleFactor(int scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public int getArrayLength() {
        return this.arrayLength;
    }
}
