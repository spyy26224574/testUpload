package com.adai.gkdnavi.adapter;

public class Addinfo {
    public String name;
    public String address;
    public String longitude;
    public String latitude;
    public int addresstype;//  0为地址内容  1为更多地址跳转

//    public Addinfo(String name, String address) {
//        this.name = name;
//        this.address = address;
//        this.addresstype = 0;
//    }
//
//    public Addinfo(String name, String address, int addresstype) {
//        this.name = name;
//        this.address = address;
//        this.addresstype = addresstype;
//    }

    public Addinfo(String name, String address, String longitude, String latitude) {
        this.name = name;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.addresstype = 0;
    }

    public Addinfo(String name, String address, String longitude, String latitude, int addresstype) {
        this.name = name;
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.addresstype = addresstype;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public int getAddresstype() {
        return addresstype;
    }

    public void setAddresstype(int addresstype) {
        this.addresstype = addresstype;
    }

    @Override
    public String toString() {
        return name;
    }
}