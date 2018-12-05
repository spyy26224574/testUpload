package com.example.ipcamera.domain;


public class MovieSizeItem {

    private String name;
    private int index;
    private String size;
    private int framerate;
    private int type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public int getFramerate() {
        return framerate;
    }

    public void setFramerate(int framerate) {
        this.framerate = framerate;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}