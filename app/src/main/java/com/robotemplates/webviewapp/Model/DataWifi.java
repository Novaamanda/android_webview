package com.robotemplates.webviewapp.Model;

/**
 * Created by Manda on 11/08/2017.
 */
public class DataWifi {
    private String SSID;
    private int frekuensi;
    private int level;
    private double jarak;

    public DataWifi(String SSID, int frekuensi, int level) {
        this.SSID = SSID;
        this.frekuensi = frekuensi;
        this.level = level;
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public int getFrekuensi() {
        return frekuensi;
    }

    public void setFrekuensi(int frekuensi) {
        this.frekuensi = frekuensi;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getJarak() {
        return jarak;
    }

    public void setJarak(double jarak) {
        this.jarak = jarak;
    }
}
