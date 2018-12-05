package com.example.ipcamera.domain;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/8/22 20:15
 * @updateAuthor $Author$
 * @updateDate $Date$
 */
public class CameraVersion {
    private String cmd;
    private String status;
    private String string;

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }
}
