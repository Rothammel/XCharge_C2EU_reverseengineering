package com.xcharge.charger.ui.c2.activity.data;

/* loaded from: classes.dex */
public class Price {
    private static Price instance = null;
    public double powerPrice = 0.0d;
    public double servicePrice = 0.0d;

    public static synchronized Price getInstance() {
        Price price;
        synchronized (Price.class) {
            if (instance == null) {
                instance = new Price();
            }
            price = instance;
        }
        return price;
    }

    public double getPowerPrice() {
        return this.powerPrice;
    }

    public void setPowerPrice(double powerPrice) {
        this.powerPrice = powerPrice;
    }

    public double getServicePrice() {
        return this.servicePrice;
    }

    public void setServicePrice(double servicePrice) {
        this.servicePrice = servicePrice;
    }
}