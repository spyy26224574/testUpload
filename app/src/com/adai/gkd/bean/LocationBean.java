package com.adai.gkd.bean;

import java.io.Serializable;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/11/24 15:42
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class LocationBean implements Serializable{
    private static final long serialVersionUID = 2624361106678437126L;
    public boolean isCheck;
    public String province;
    public String city;
    public String district;
    public String name;
    public String address;
    public double lat;
    public double lng;
}
