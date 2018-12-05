package com.adai.gkd.bean.params;

import java.io.File;
import java.util.List;

/**
 * Created by admin on 2016/12/1.
 */

public class FeedbackParam {
    public int platform=0;
    public int type;
    public String description;
    public char isReportError;
    public char isNeedContact;
    public String phoneNum;
    public String email;
    public List<File> log;
    public List<File> image;

    public String versionCategory;
    public String softVersion;
    public String deviceid;
    public String hudVersion;
    public String cameraVersion;
    public String obdVersion;
}
