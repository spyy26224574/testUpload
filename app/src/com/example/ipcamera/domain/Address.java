package com.example.ipcamera.domain;
/*
 * 地址的数据结构
 */
public class Address {
	
	private String name;
	private String address;
    private String collect_longitude;
    private String collect_latitude;
	public void setName(String nameAddress){
		this.name = nameAddress;
	}
	public String getName(){
		return name;
	}
	public void setAddress(String addAddress){
		this.address = addAddress;
	}
	public String getAddress(){
		return address;
	}
    public void setCollectLongitude(String collect_longitude) {
        this.collect_longitude = collect_longitude;
    }

    public String getCollectLongitude() {
        return collect_longitude;
    }

    public void setCollectLatitude(String collect_latitude) {
        this.collect_latitude = collect_latitude;
    }

    public String getCollectLatitude() {
        return collect_latitude;
    }
}
