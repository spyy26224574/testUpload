package com.example.ipcamera.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxy on 2016/12/3.
 */

public class HourFile implements Serializable {
    private static final long serialVersionUID = 5195855622017271552L;
    public String time;
    public boolean isChecked;
    public List<MinuteFile> minuteFiles = new ArrayList<>();
//    public HashMap<String, List<MinuteFile>> minuteFiles;
}

