package com.adai.gkdnavi.gpsvideo;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import SunGps.SunGpsInterface;

/**
 * Created by admin on 2017/5/9.
 */

public class GpsWriter {
    /**
     * @param mp4
     * @param locations
     * @param type      0:不加密 1：加密
     */
    public void writeGps2mp4(String mp4, List<GpsInfoBean> locations, int type) {
        if (locations == null || locations.size() <= 0) return;
        SunGpsInterface gpsInterface = new SunGpsInterface();
        gpsInterface.SunSetEncType(type);
        int itemlen = 132;
        try {
            FileOutputStream fos = new FileOutputStream(mp4, true);
//        byte[] gpsdata=new byte[itemlen*locations.size()];
            fos.write(getHeader(locations.size() * itemlen, locations.size(), (byte) type));
            for (int i = 0; i < locations.size(); i++) {
                byte[] itemdata = getItemII(i, locations.get(i));
                //            System.arraycopy(itemdata,0,gpsdata,i*itemlen,itemlen);
                byte[] encrypt = new byte[itemlen];
                int ret = gpsInterface.MakeEncryptDataBlockII(itemdata, itemdata.length, encrypt);
                fos.write(encrypt);
            }
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        gpsInterface.SunEncrypt(gpsdata,gpsdata.length,)
    }

    //    private SimpleDateFormat mDateFormat=new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
    public void writeGps2mp4(String mp4, List<GpsInfoBean> locations) {
        if (locations == null || locations.size() <= 0) return;
        try {
            FileOutputStream fos = new FileOutputStream(mp4, true);
//            List<byte[]> gpsdata=new ArrayList<>();
            fos.write(getHeader(locations.size() * 132, locations.size(), (byte) 0));
            for (int i = 0; i < locations.size(); i++) {
                fos.write(getItem(i, locations.get(i)));
            }
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] getItem(int index, GpsInfoBean location) {
        byte[] data = new byte[132];
        data[3] = (byte) ((index >>> 24) & 0xff);
        data[2] = (byte) ((index >>> 16) & 0xff);
        data[1] = (byte) ((index >>> 8) & 0xff);
        data[0] = (byte) (index & 0xff);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(location.time).append(" ");
        String lat = location.latitude >= 0 ? "N:" : "S:";
        stringBuffer.append(lat).append(String.format("%.6f", Math.abs(location.latitude))).append("  ");
        String lon = location.longitude >= 0 ? "E:" : "W:";
        stringBuffer.append(lon).append(String.format("%.6f", Math.abs(location.longitude))).append(" ");
        stringBuffer.append(String.format("%.2f", location.speed)).append(" km/h ");
        stringBuffer.append("x:").append(location.x).append(" ");
        stringBuffer.append("y:").append(location.y).append(" ");
        stringBuffer.append("z:").append(location.z).append("\n");
        System.arraycopy(stringBuffer.toString().getBytes(), 0, data, 4, stringBuffer.length());
        return data;
    }

    private byte[] getItemII(int index, GpsInfoBean location) {
        byte[] data = new byte[100];
        data[3] = (byte) ((index >>> 24) & 0xff);
        data[2] = (byte) ((index >>> 16) & 0xff);
        data[1] = (byte) ((index >>> 8) & 0xff);
        data[0] = (byte) (index & 0xff);
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(location.time).append(" ");
        String lat = location.latitude >= 0 ? "N:" : "S:";
        stringBuffer.append(lat).append(String.format("%.6f", Math.abs(location.latitude))).append("  ");
        String lon = location.longitude >= 0 ? "E:" : "W:";
        stringBuffer.append(lon).append(String.format("%.6f", Math.abs(location.longitude))).append(" ");
        stringBuffer.append(String.format("%.2f", location.speed)).append(" km/h ");
        stringBuffer.append("x:").append(location.x).append(" ");
        stringBuffer.append("y:").append(location.y).append(" ");
        stringBuffer.append("z:").append(location.z).append("\n");
        System.arraycopy(stringBuffer.toString().getBytes(), 0, data, 4, stringBuffer.length());
        return data;
    }

    public byte[] getHeader(long length, int num, byte type) {
        byte[] header = new byte[8];
        byte[] dataheader = new byte[20];
        length += dataheader.length;
        length += header.length;
        header[0] = (byte) ((length >>> 24) & 0xff);
        header[1] = (byte) ((length >>> 16) & 0xff);
        header[2] = (byte) ((length >>> 8) & 0xff);
        header[3] = (byte) (length & 0xff);
        System.arraycopy("skip".getBytes(), 0, header, 4, 4);
        String headtext = "LIGOGPSINFO";
        System.arraycopy(headtext.getBytes(), 0, dataheader, 0, headtext.length());
        dataheader[15] = type;
        dataheader[19] = (byte) ((num >>> 24) & 0xff);
        dataheader[18] = (byte) ((num >>> 16) & 0xff);
        dataheader[17] = (byte) ((num >>> 8) & 0xff);
        dataheader[16] = (byte) (num & 0xff);
        byte[] data = new byte[header.length + dataheader.length];
        System.arraycopy(header, 0, data, 0, header.length);
        System.arraycopy(dataheader, 0, data, header.length, dataheader.length);
        return data;
    }
}
