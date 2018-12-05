package com.adai.camera.pano;


import android.util.Log;

import com.ligo.medialib.MediaPlayLib;

import org.apache.http.util.EncodingUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;

import SunGps.SunGpsInterface;

public class PanoWriter {
    /**
     * @param path
     * @param type 0，普通文件，1为360度文件，2位720度文件
     */
    public void writePano2mov(String path, int type) {
        Log.e("9999", "path = " + path+", type = " + type);
        if (type == 1 || type == 2) {
            MediaPlayLib mediaPlayLib = new MediaPlayLib();
            int writepano = mediaPlayLib.sunSetInfoType(path, type);
            Log.e("9999", "writepano = " + writepano);
//            BufferedWriter out = null;
//            try {
//                out = new BufferedWriter(new OutputStreamWriter(
//                        new FileOutputStream(path, true)));
//                out.write("0064LIGOLIGOGPSINFO000040002####108" + type + "####0123456789123456LIGO0056");
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                try {
//                    out.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
        }

    }

    /**
     * @param file
     * @param type 0，普通文件，1为360度文件，2位720度文件
     */
    public void writePano2mov(File file, int type) {
        writePano2mov(file.getPath(), type);
    }


    public int getPanotype(File file) {
        MediaPlayLib mediaPlayLib = new MediaPlayLib();
        Log.e("9999", "file.getPath() = " + file.getPath());
        int panoType = mediaPlayLib.sunGetInfoType(file.getPath());
        Log.e("9999", "panoType = " + panoType);
//        try {
//            InputStream in = new FileInputStream(file);
//            int length = in.available();
//            byte[] buffer1 = new byte[56];
//            in.skip(length - 56);
//            in.read(buffer1, 0, 56);
//            String str = EncodingUtils.getString(buffer1, "UTF-8");
//            in.close();
//            Log.e("9527", "str = " + str);
//            if (str.substring(48, 52).equals("LIGO") && str.substring(15, 16).equals("4")) {
//                int type = Integer.parseInt(str.substring(27, 28));
//                if (type == 1 || type == 2) {
//                    panoType = type;
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return panoType;
    }

    public int getPanotype(String path) {
        return getPanotype(new File((path)));
    }


}
