package com.adai.gkdnavi.gpsvideo;

import java.io.Serializable;

/**
 * Created by huangxy on 2017/5/26 10:04.
 */

public class GpsInfoBean implements Serializable {
    private static final long serialVersionUID = -3303047405203895452L;
    public int id;
    public String time;
    public double latitude;
    public double longitude;
    public float speed;
    public float bearing;
    public float x, y, z;
}
