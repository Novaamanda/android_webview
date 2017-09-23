package com.robotemplates.webviewapp.Model;

import java.util.List;

/**
 * Created by Manda on 19/09/2017.
 */

public class Hazard {
    private String cookieshazard;
    private String id_user;
    private List<String> id_userjsonType;
    private List<String> jsonPenyebab;
    private String deskripsi;
    private String kode_bandara;
    private String image;
    private String image1;
    private String lat;
    private String longitude;
    private String waktu;

    public String getCookieshazard() {
        return cookieshazard;
    }

    public void setCookieshazard(String cookieshazard) {
        this.cookieshazard = cookieshazard;
    }

    public String getId_user() {
        return id_user;
    }

    public void setId_user(String id_user) {
        this.id_user = id_user;
    }

    public List<String> getId_userjsonType() {
        return id_userjsonType;
    }

    public void setId_userjsonType(List<String> id_userjsonType) {
        this.id_userjsonType = id_userjsonType;
    }

    public List<String> getJsonPenyebab() {
        return jsonPenyebab;
    }

    public void setJsonPenyebab(List<String> jsonPenyebab) {
        this.jsonPenyebab = jsonPenyebab;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getKode_bandara() {
        return kode_bandara;
    }

    public void setKode_bandara(String kode_bandara) {
        this.kode_bandara = kode_bandara;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage1() {
        return image1;
    }

    public void setImage1(String image1) {
        this.image1 = image1;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getWaktu() {
        return waktu;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
