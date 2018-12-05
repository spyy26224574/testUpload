package com.example.ipcamera.domain;

import java.util.ArrayList;
import java.util.List;

public class MenuItem {

    private int cmd;
    private String name;
    private List<MenuOption> option = new ArrayList<MenuOption>();

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<MenuOption> getOption() {
        return option;
    }

    public void setOption(List<MenuOption> option) {
        this.option.addAll(option);
    }
}