package com.adai.camera.mstar.data;

import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Xml;

import com.adai.camera.mstar.CameraCommand;
import com.adai.camera.mstar.MstarCamera;
import com.adai.camera.CameraConstant;
import com.adai.gkdnavi.utils.SpUtils;
import com.adai.gkdnavi.utils.UIUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangxy on 2017/10/14 11:12.
 */

public class MstarRepository implements MstarDataSource {
    private static volatile MstarRepository INSTANCE = null;
    private static List<Menu> cammenu = null;
    private String version = "";
    private String mVideoresRet;
    private String mImageresRet;
    private String mMTDRet;
    private String mAWBRet;
    private String mFlickerRet;
    private String mEVRet;
    private String mFWVersionRet;
    private String mGsensorRet;

    public static List<Menu> getCammenu() {
        return cammenu;
    }

    public String getVideoresRet() {
        return mVideoresRet;
    }

    public void setVideoresRet(String videoresRet) {
        mVideoresRet = videoresRet;
    }

    public String getImageresRet() {
        return mImageresRet;
    }

    public void setImageresRet(String imageresRet) {
        mImageresRet = imageresRet;
    }

    public String getMTDRet() {
        return mMTDRet;
    }

    public String getAWBRet() {
        return mAWBRet;
    }

    public String getFlickerRet() {
        return mFlickerRet;
    }

    public String getEVRet() {
        return mEVRet;
    }

    public String getFWVersionRet() {
        return mFWVersionRet;
    }

    public String getGsensorRet() {
        return mGsensorRet;
    }

    private MstarRepository() {

    }

    public static MstarRepository getInstance() {
        if (INSTANCE == null) {
            synchronized (MstarRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MstarRepository();
                }
            }
        }
        return INSTANCE;
    }

    @Override
    public void initDataSource(@NonNull final DataSourceSimpleCallBack dataSourceSimpleCallBack) {
        try {
            URL url = new URL("http://" + MstarCamera.CAM_IP + "/cammenu.xml");
            CameraCommand.asynSendRequest(url, new CameraCommand.RequestListener() {
                @Override
                public void onResponse(String result) {
                    if (result != null) {
                        InputStream in = null;
                        try {
                            in = new ByteArrayInputStream(result.getBytes("UTF-8"));
                        } catch (IOException e) {
                            return;
                        }
                        try {
                            cammenu = parse(in);
                            dataSourceSimpleCallBack.success();
                        } catch (IOException | XmlPullParserException ignored) {
                            dataSourceSimpleCallBack.error("can not find menu");
                        }
                    } else {
                        dataSourceSimpleCallBack.error("can not find menu");
                    }
                }

                @Override
                public void onErrorResponse(String message) {
                    dataSourceSimpleCallBack.error(message);
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
            dataSourceSimpleCallBack.error(e.getMessage());
        }
    }

    public String getVersion() {
        return version;
    }

    @Override
    public void getStatus(@NonNull final DataSourceSimpleCallBack dataSourceSimpleCallBack) {
        CameraCommand.asynSendRequest(CameraCommand.commandGetMenuSettingsValuesUrl(), new CameraCommand.RequestListener() {
            @Override
            public void onResponse(String result) {
                if (result != null) {
                    //String[] value;
                    //value = GetCameraSettingValue(result, MENU_ID.menuVIDEO_RES);
                    String[] lines;
                    String[] lines_temp;
                    try {
                        lines_temp = result.split("VideoRes=");

                        ///add compatibility for "fps" in "VideoRes=720P30fps"
                        if (lines_temp[1].contains("fps")) {
                            lines_temp = lines_temp[1].split("fps");
                            lines = lines_temp[0].split(System.getProperty("line.separator"));
                        } else {
                            lines = lines_temp[1].split(System.getProperty("line.separator"));
                        }

                        Log.d("WW", "eeeeeee = " + lines[0]);
//                        mVideoresRet = AutoMenuCheck(MENU_ID.menuVIDEO_RES, lines[0]);
                        mVideoresRet = lines[0];
                    } catch (Exception e) { /* TODO: Show Error */ }
                    //
                    try {
                        lines_temp = result.split("ImageRes=");
                        lines = lines_temp[1].split(System.getProperty("line.separator"));

                        ///add compatibility for "2M" in "ImageRes=2MP"


                        mImageresRet = lines[0];
                    } catch (Exception e) { /* TODO: Show Error */ }
                    //
                    try {
                        lines_temp = result.split("MTD=");
                        lines = lines_temp[1].split(System.getProperty("line.separator"));
                        mMTDRet = lines[0];
                    } catch (Exception e) {
                    }
                    //
                    try {
                        lines_temp = result.split("AWB=");
                        lines = lines_temp[1].split(System.getProperty("line.separator"));
                        mAWBRet = lines[0];
                    } catch (Exception e) { /* TODO: Show Error */ }
                    //
                    try {
                        lines_temp = result.split("Flicker=");
                        lines = lines_temp[1].split(System.getProperty("line.separator"));
                        mFlickerRet = lines[0];
                    } catch (Exception e) { /* TODO: Show Error */ }
                    //
                    try {
                        lines_temp = result.split("EV=");
                        lines = lines_temp[1].split(System.getProperty("line.separator"));
                        mEVRet = lines[0];
                    } catch (Exception e) { /* TODO: Show Error */ }
                    //
                    try {
                        lines_temp = result.split("FWversion=");
                        lines = lines_temp[1].split(System.getProperty("line.separator"));
                        mFWVersionRet = lines[0];
                        SpUtils.putString(UIUtils.getContext(), CameraConstant.CAMERA_FIRMWARE_VERSION, mFWVersionRet);
                        SpUtils.putString(UIUtils.getContext(), CameraConstant.CAMERA_FACTORY, "");
                        SpUtils.putString(UIUtils.getContext(), CameraConstant.CAMERA_VERSION, "");
                        SpUtils.putString(UIUtils.getContext(), CameraConstant.CAMERA_VERSION_CURRENT, "");
                    } catch (Exception e) { /* TODO: Show Error */ }
                    try {
                        lines_temp = result.split("GSensor=");
                        lines = lines_temp[1].split(System.getProperty("line.separator"));
                        mGsensorRet = lines[0];
                    } catch (Exception e) { /* TODO: Show Error */ }
                    dataSourceSimpleCallBack.success();
                } else {
                    dataSourceSimpleCallBack.error("没有获取到状态");
                }
            }

            @Override
            public void onErrorResponse(String message) {
                dataSourceSimpleCallBack.error(message);
            }
        });
    }

    //    public enum MENU_ID {
//        menuVIDEO_RES, menuIMAGE_RES,
//        menuLOOPING_VIDEO,
//        menuBURST, menuTIME_LAPSE,
//        menuWHITE_BALANCE,
//        menuHDR,
//        menuMTD, menuFLICKER, menuEV, menuVersion
//    }
    public static class MENU_ID {
        public static final int menuVIDEO_RES = 0;
        public static final int menuIMAGE_RES = 1;
        public static final int menuLOOPING_VIDEO = 2;
        public static final int menuBURST = 3;
        public static final int menuTIME_LAPSE = 4;
        public static final int menuWHITE_BALANCE = 5;
        public static final int menuHDR = 6;
        public static final int menuMTD = 7;
        public static final int menuFLICKER = 8;
        public static final int menuEV = 9;
        public static final int menuVersion = 10;
        public static final int menuGST = 11;
    }

    public Menu GetAutoMenu(int menuid) {
        if (cammenu == null || cammenu.isEmpty()) {
            return null;
        }
        String menu_title;
        switch (menuid) {
            case MENU_ID.menuVIDEO_RES:
                menu_title = "VIDEO RESOLUTION";
                break;
            case MENU_ID.menuIMAGE_RES:
                menu_title = "CAPTURE RESOLUTION";
                break;
            case MENU_ID.menuLOOPING_VIDEO:
                menu_title = "LOOPING VIDEO";
                break;
            case MENU_ID.menuWHITE_BALANCE:
                menu_title = "WHITE BALANCE";
                break;
            case MENU_ID.menuBURST:
                menu_title = "BURST";
                break;
            case MENU_ID.menuTIME_LAPSE:
                menu_title = "TIME LAPSE";
                break;
            case MENU_ID.menuHDR:
                menu_title = "HDR";
                break;
            case MENU_ID.menuMTD:
                menu_title = "MTD";
                break;
            case MENU_ID.menuGST:
                menu_title = "GSENSOR SENSITIVITY";
                break;
            default:
                return null;
        }
        int num = cammenu.size();
        for (int i = 0; i < num; i++) {
            Menu menu = cammenu.get(i);
            if (menu.title.equalsIgnoreCase(menu_title)) {
                return menu;
            }
        }
        return null;
    }

    public static String[] EV_val = {"EVN200", "EVN167", "EVN133", "EVN100", "EVN067", "EVN033",
            "EV0",
            "EVP033", "EVP067", "EVP100", "EVP133", "EVP167", "EVP200"};
    public static String[] FLICKER_val = {"50Hz", "60Hz"};
    public static String[] MTD_val = {"Off", "Low", "Middle", "High"};

    public int AutoMenuCheck(int menuid, String sz) {
        if (sz == null) return -1;
        Menu menu = GetAutoMenu(menuid);
        if (menu == null) {
            int id;
            switch (menuid) {
                case MENU_ID.menuFLICKER:
                    id = FindItem(FLICKER_val, sz);
                    if (id != -1) return id;
                    return 0;    // default
                case MENU_ID.menuEV:
                    id = FindItem(EV_val, sz);
                    if (id != -1) return id;
                    return 0;    // default
                case MENU_ID.menuMTD:
                    id = FindItem(MTD_val, sz);
                    if (id != -1) return id;
                    return 0;    // default
            }
            return Integer.valueOf(sz);
        }
        return menu.GetItemId(sz);
    }

    private int FindItem(String[] valdb, String val) {
        for (int i = 0; i < valdb.length; i++)
            if (valdb[i].equalsIgnoreCase(val))
                return i;
        return -1;
    }

    @Override
    public void getFileList(@NonNull GetFileListCallback getFileListCallback) {

    }

    private String ns = null;

    private List<Menu> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readCamera(parser);
        } finally {
            in.close();
        }
    }

    private List<Menu> readCamera(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Menu> entries = new ArrayList<Menu>();

        parser.require(XmlPullParser.START_TAG, ns, "camera");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals("menu")) {
                entries.add(readMenu(parser));
            } else if (name.equals("version")) {
                version = readVersion(parser);
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    // Parses the contents of an menu. If it encounters a item tag, hands them
    // off
    // to their respective &quot;read&quot; methods for processing. Otherwise, skips the tag.
    private Menu readMenu(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, "menu");
        Menu menu;

        String title = parser.getAttributeValue(null, "title");
        menu = new Menu(title);
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("item")) {
                menu.AddItme(readMenuItem(parser));
            } else {
                skip(parser);
            }
        }
        return menu;
    }

    // Processes item tags in the menu.
    private MenuItem readMenuItem(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "item");
        String id = parser.getAttributeValue(null, "id");
        String title = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "item");
        return new MenuItem(title, id, 0);
    }

    // Processes version tag in the Camera.
    private String readVersion(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(XmlPullParser.START_TAG, ns, "version");
        String ver = readText(parser);
        parser.require(XmlPullParser.END_TAG, ns, "version");
        return ver;
    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    // Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
    // if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
    // finds the matching END_TAG (as indicated by the value of "depth" being 0).
    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private class MenuItem {
        public final String title;
        public final String id;
        public final int type;

        private MenuItem(String title, String id, int type) {
            this.title = title;
            this.id = id;
            this.type = type;
        }
    }

    public class Menu {
        public final String title;
        public List<MenuItem> items;

        private Menu(String title) {
            this.title = title;
            items = new ArrayList<MenuItem>();
        }

        public void AddItme(MenuItem item) {
            items.add(item);
        }

        public int GetNumberItem() {
            return items.size();
        }

        public String GetMenuItemID(int pos) {
            if (pos > GetNumberItem() || pos < 0)
                return null;
            return items.get(pos).id;
        }

        public List<String> GetMenuItemTitleList() {
            int num = GetNumberItem();
            List<String> list = new ArrayList<>();

            for (int i = 0; i < num; i++) {
                list.add(items.get(i).title);
            }
            return list;
        }

        public List<String> GetMenuItemIdList() {
            if (items == null) {
                return null;
            }
            List<String> list = new ArrayList<>();
            for (MenuItem menuItem : items) {
                list.add(menuItem.id);
            }
            return list;
        }

        public int GetItemId(String sz) {
            int num = GetNumberItem();
            for (int i = 0; i < num; i++) {
                if (items.get(i).id.equalsIgnoreCase(sz)) {
                    return i;
                }
            }
            // Support older version FW to convert sz to integer
            try {
                int val = Integer.valueOf(sz);
                if (val >= 0 && val < num) {
                    return val;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

            return -1;
        }
    }
}
