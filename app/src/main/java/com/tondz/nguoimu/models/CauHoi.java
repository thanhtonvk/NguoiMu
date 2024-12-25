package com.tondz.nguoimu.models;

public class CauHoi {
    private String id, cauhoi, a, b, c, d, dapan, giaithich;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCauhoi() {
        return cauhoi;
    }

    public void setCauhoi(String cauhoi) {
        this.cauhoi = cauhoi;
    }

    public String getA() {
        return a;
    }

    public void setA(String a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public String getD() {
        return d;
    }

    public void setD(String d) {
        this.d = d;
    }

    public String getDapan() {
        return dapan;
    }

    public void setDapan(String dapan) {
        this.dapan = dapan;
    }

    public String getGiaithich() {
        return giaithich;
    }

    public void setGiaithich(String giaithich) {
        this.giaithich = giaithich;
    }

    public CauHoi(String id, String cauhoi, String a, String b, String c, String d, String dapan, String giaithich) {
        this.id = id;
        this.cauhoi = cauhoi;
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.dapan = dapan;
        this.giaithich = giaithich;
    }

    public CauHoi() {

    }

    @Override
    public String toString() {
        return cauhoi;
    }
}

