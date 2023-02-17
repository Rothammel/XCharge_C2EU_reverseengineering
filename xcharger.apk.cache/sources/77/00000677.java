package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

/* loaded from: classes.dex */
public class DeviceContent extends JsonBean<DeviceContent> {
    private ArrayList<ContentItem> welcome = null;
    private ArrayList<ContentItem> scanHintTitle = null;
    private ArrayList<ContentItem> scanHintDesc = null;
    private ArrayList<ContentItem> logo = null;
    private ArrayList<ContentItem> company = null;
    private ArrayList<ContentItem> scanAdvsite = null;
    private ArrayList<ContentItem> pullAdvsite = null;
    private ArrayList<ContentItem> wakeUpAdvsite = null;
    private ArrayList<ContentItem> idleAdvsite = null;
    private ArrayList<ContentItem> chargingAdvsite = null;

    public ArrayList<ContentItem> getCompany() {
        return this.company;
    }

    public void setCompany(ArrayList<ContentItem> company) {
        this.company = company;
    }

    public ArrayList<ContentItem> getScanHintTitle() {
        return this.scanHintTitle;
    }

    public void setScanHintTitle(ArrayList<ContentItem> scanHintTitle) {
        this.scanHintTitle = scanHintTitle;
    }

    public ArrayList<ContentItem> getScanHintDesc() {
        return this.scanHintDesc;
    }

    public void setScanHintDesc(ArrayList<ContentItem> scanHintDesc) {
        this.scanHintDesc = scanHintDesc;
    }

    public ArrayList<ContentItem> getLogo() {
        return this.logo;
    }

    public void setLogo(ArrayList<ContentItem> logo) {
        this.logo = logo;
    }

    public ArrayList<ContentItem> getWelcome() {
        return this.welcome;
    }

    public void setWelcome(ArrayList<ContentItem> welcome) {
        this.welcome = welcome;
    }

    public ArrayList<ContentItem> getScanAdvsite() {
        return this.scanAdvsite;
    }

    public void setScanAdvsite(ArrayList<ContentItem> scanAdvsite) {
        this.scanAdvsite = scanAdvsite;
    }

    public ArrayList<ContentItem> getPullAdvsite() {
        return this.pullAdvsite;
    }

    public void setPullAdvsite(ArrayList<ContentItem> pullAdvsite) {
        this.pullAdvsite = pullAdvsite;
    }

    public ArrayList<ContentItem> getWakeUpAdvsite() {
        return this.wakeUpAdvsite;
    }

    public void setWakeUpAdvsite(ArrayList<ContentItem> wakeUpAdvsite) {
        this.wakeUpAdvsite = wakeUpAdvsite;
    }

    public ArrayList<ContentItem> getIdleAdvsite() {
        return this.idleAdvsite;
    }

    public void setIdleAdvsite(ArrayList<ContentItem> idleAdvsite) {
        this.idleAdvsite = idleAdvsite;
    }

    public ArrayList<ContentItem> getChargingAdvsite() {
        return this.chargingAdvsite;
    }

    public void setChargingAdvsite(ArrayList<ContentItem> chargingAdvsite) {
        this.chargingAdvsite = chargingAdvsite;
    }
}