package com.example.ipcamera.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxy on 2016/12/5.
 */

public class MinuteFile implements Serializable {
    private static final long serialVersionUID = 2063447489367349827L;
    public boolean isTitle;
    public boolean isTitleSelected;
    public boolean isChecked;
    public String hourTime;
    public String minuteTime;
    public String time;
    public List<FileDomain> fileDomains = new ArrayList<>();
    public String parentTime = "";
//    public HashMap<String, List<FileDomain>> fileDomains;
}
