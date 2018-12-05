package com.adai.camera.bean;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/11/10 10:26
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class WifiBean {
    public String SSID;
    public String BSSID;
    public String pwd;
    public int netId = -1;
    public int product;
    public String encrypt;
    @Override
    public String toString() {
        return "WifiBean{" +
                "SSID='" + SSID + '\'' +
                ", BSSID='" + BSSID + '\'' +
                ",PRODUCT=" + product +
                ", netId=" + netId +
                '}' + "\n";
    }
}
