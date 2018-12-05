package com.example.ipcamera.domain;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/11/2 10:24
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class CaptureResponse {
    public String cmd;
    public String status;
    public String name;
    public String path;
    public String freepicnum;

    @Override
    public String toString() {
        return "cmd=" + cmd + ",status=" + status + ",name=" + name + ",path=" + path + ",freepicnum=" + freepicnum;
    }
}
