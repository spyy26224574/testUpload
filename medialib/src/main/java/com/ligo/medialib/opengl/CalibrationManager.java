package com.ligo.medialib.opengl;

import android.content.Context;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author huangxy
 * @date 2018/9/10 15:08.
 */
public class CalibrationManager {
    public static Calibration.CalibrationBean getCalibration(Context context, int width, int height) {
        Calibration.CalibrationBean calibrationBean = null;
        try {
            InputStream gsonis = null;
            gsonis = context.getAssets().open("calibration.gson");
            InputStreamReader inputStreamReader = new InputStreamReader(gsonis);
            Calibration calibration = new Gson().fromJson(inputStreamReader, Calibration.class);
            for (Calibration.CalibrationBean calibrationBean1 : calibration.calibration) {
                if (calibrationBean1.size.equals("" + width + "*" + height)) {
                    calibrationBean = calibrationBean1;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return calibrationBean;
    }
}
