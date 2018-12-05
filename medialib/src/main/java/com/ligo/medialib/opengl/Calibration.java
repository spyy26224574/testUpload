package com.ligo.medialib.opengl;

import java.util.List;

/**
 * @author huangxy
 * @date 2018/9/10 15:57.
 */
public class Calibration {

    public List<CalibrationBean> calibration;

    public static class CalibrationBean {

        public String size;
        public int width;
        public int height;
        public float x;
        public float y;
        public float radius;
    }
}
