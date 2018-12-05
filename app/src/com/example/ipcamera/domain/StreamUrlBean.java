package com.example.ipcamera.domain;

/**
 * Created by huangxy on 2017/10/20 18:09.
 */

public class StreamUrlBean {
    public String MovieLiveViewLink;

    public String PhotoLiveViewLink;

    @Override
    public String toString() {
        return "StreamUrlBean{" +
                "MovieLiveViewLink='" + MovieLiveViewLink + '\'' +
                ", PhotoLiveViewLink='" + PhotoLiveViewLink + '\'' +
                '}';
    }
}
