package net.xcharge.sdk.server.coder.model.rule.subitem;

public class SubField {
    private int arrayLength;

    /* renamed from: id */
    private int f139id;
    private String name;
    private boolean productFamilyRelated;
    private int scaleFactor;
    private String subtype;
    private String type;
    private String valueMap;

    public int getId() {
        return this.f139id;
    }

    /* access modifiers changed from: package-private */
    public void setId(int id) {
        this.f139id = id;
    }

    public String getName() {
        return this.name;
    }

    /* access modifiers changed from: package-private */
    public void setName(String name2) {
        this.name = name2;
    }

    public String getType() {
        return this.type;
    }

    /* access modifiers changed from: package-private */
    public void setType(String type2) {
        this.type = type2;
    }

    public String getSubtype() {
        return this.subtype;
    }

    /* access modifiers changed from: package-private */
    public void setSubtype(String subtype2) {
        this.subtype = subtype2;
    }

    public String getValueMap() {
        return this.valueMap;
    }

    /* access modifiers changed from: package-private */
    public void setValueMap(String valueMap2) {
        this.valueMap = valueMap2;
    }

    public boolean isProductFamilyRelated() {
        return this.productFamilyRelated;
    }

    /* access modifiers changed from: package-private */
    public void setProductFamilyRelated(boolean productFamilyRelated2) {
        this.productFamilyRelated = productFamilyRelated2;
    }

    public int getScaleFactor() {
        return this.scaleFactor;
    }

    /* access modifiers changed from: package-private */
    public void setScaleFactor(int scaleFactor2) {
        this.scaleFactor = scaleFactor2;
    }

    public int getArrayLength() {
        return this.arrayLength;
    }
}
