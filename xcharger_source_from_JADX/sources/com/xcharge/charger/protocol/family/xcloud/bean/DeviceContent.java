package com.xcharge.charger.protocol.family.xcloud.bean;

import com.xcharge.charger.data.bean.ContentItem;
import com.xcharge.common.bean.JsonBean;
import java.util.ArrayList;

public class DeviceContent extends JsonBean<DeviceContent> {
    private ArrayList<ContentItem> chargingAdvsite = null;
    private ArrayList<ContentItem> company = null;
    private ArrayList<ContentItem> idleAdvsite = null;
    private ArrayList<ContentItem> logo = null;
    private ArrayList<ContentItem> pullAdvsite = null;
    private ArrayList<ContentItem> scanAdvsite = null;
    private ArrayList<ContentItem> scanHintDesc = null;
    private ArrayList<ContentItem> scanHintTitle = null;
    private ArrayList<ContentItem> wakeUpAdvsite = null;
    private ArrayList<ContentItem> welcome = null;

    public ArrayList<ContentItem> getCompany() {
        return this.company;
    }

    public void setCompany(ArrayList<ContentItem> company2) {
        this.company = company2;
    }

    public ArrayList<ContentItem> getScanHintTitle() {
        return this.scanHintTitle;
    }

    public void setScanHintTitle(ArrayList<ContentItem> scanHintTitle2) {
        this.scanHintTitle = scanHintTitle2;
    }

    public ArrayList<ContentItem> getScanHintDesc() {
        return this.scanHintDesc;
    }

    public void setScanHintDesc(ArrayList<ContentItem> scanHintDesc2) {
        this.scanHintDesc = scanHintDesc2;
    }

    public ArrayList<ContentItem> getLogo() {
        return this.logo;
    }

    public void setLogo(ArrayList<ContentItem> logo2) {
        this.logo = logo2;
    }

    public ArrayList<ContentItem> getWelcome() {
        return this.welcome;
    }

    public void setWelcome(ArrayList<ContentItem> welcome2) {
        this.welcome = welcome2;
    }

    public ArrayList<ContentItem> getScanAdvsite() {
        return this.scanAdvsite;
    }

    public void setScanAdvsite(ArrayList<ContentItem> scanAdvsite2) {
        this.scanAdvsite = scanAdvsite2;
    }

    public ArrayList<ContentItem> getPullAdvsite() {
        return this.pullAdvsite;
    }

    public void setPullAdvsite(ArrayList<ContentItem> pullAdvsite2) {
        this.pullAdvsite = pullAdvsite2;
    }

    public ArrayList<ContentItem> getWakeUpAdvsite() {
        return this.wakeUpAdvsite;
    }

    public void setWakeUpAdvsite(ArrayList<ContentItem> wakeUpAdvsite2) {
        this.wakeUpAdvsite = wakeUpAdvsite2;
    }

    public ArrayList<ContentItem> getIdleAdvsite() {
        return this.idleAdvsite;
    }

    public void setIdleAdvsite(ArrayList<ContentItem> idleAdvsite2) {
        this.idleAdvsite = idleAdvsite2;
    }

    public ArrayList<ContentItem> getChargingAdvsite() {
        return this.chargingAdvsite;
    }

    public void setChargingAdvsite(ArrayList<ContentItem> chargingAdvsite2) {
        this.chargingAdvsite = chargingAdvsite2;
    }
}
