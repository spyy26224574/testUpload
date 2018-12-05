package com.adai.camera.sunplus.bean;

import com.icatch.wificam.customer.type.ICatchFile;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by huangxy on 2017/4/11 11:57.
 */

public class SunplusMinuteFile implements Serializable {
    private static final long serialVersionUID = 1;
    public ArrayList<ICatchFile> fileDomains = new ArrayList<>();

    public String hourTime;
    public String minuteTime;
    public String time;
    public boolean isTitle;
    public boolean isTitleSelected;
    public boolean isChecked;
}
