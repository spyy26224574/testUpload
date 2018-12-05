package org.videolan.vlc.util;

import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;

import com.adai.camera.mstar.MstarCamera;
import com.adai.camera.novatek.contacts.Contacts;
import com.adai.gkdnavi.utils.LogUtils;
import com.example.ipcamera.domain.CameraVersionResponse;
import com.example.ipcamera.domain.CaptureResponse;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MenuItem;
import com.example.ipcamera.domain.MenuOption;
import com.example.ipcamera.domain.MovieRecord;
import com.example.ipcamera.domain.MovieRecordValue;
import com.example.ipcamera.domain.MovieSizeItem;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.videolan.vlc.util.VLCInstance.TAG;

public class DomParseUtils {

    private String strFiletag = "AndroidFile";
    private String strCmdPre = "<Cmd>"; //cmd begain
    private String strCmdBac = "</Cmd>";  //cmd end
    private String strStatusPre = "<Status>";  //status begain
    private String strStatusBac = "</Status>";  //status end
    public int iMainPosPre = 0;  //the pre cmd position
    public int iMainPosBac = 0;  //the next cmd position
    public int iSubPosPre = 0;   //the substring begain postion (cmd&status)
    public int iSubPosBac = 0;   //the substring end postion (cmd&status)
    public int iCmdIgnore = 5;   //cmd str ignore number
    public int iStatusIgnore = 8; //status str ignore number
    public int iBacStatusIgn = 9;
    public HashMap<String, String> hMap = new HashMap<String, String>();

    class cmdstatus {
        String cmd;
        String status;

    }

    public List<Integer> querySupportCmd(InputStream in) throws XmlPullParserException, IOException {
        List<Integer> supportCmds = null;
        XmlPullParser xmlPullParser = Xml.newPullParser();
        xmlPullParser.setInput(in, "UTF-8");
        int eventType = xmlPullParser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    supportCmds = new ArrayList<>();
                    break;
                case XmlPullParser.START_TAG:
                    if (xmlPullParser.getName().equals("Cmd")) {
                        xmlPullParser.next();
                        supportCmds.add(Integer.valueOf(xmlPullParser.getText()));
                    }
                    break;
                case XmlPullParser.END_TAG:
                    break;
                default:
                    break;
            }
            eventType = xmlPullParser.next();
        }
        return supportCmds;
    }

    public List<MenuItem> queryMenuItemXml(InputStream in) throws Exception {
        List<MenuItem> list = null;
        MenuItem item = null;
        MenuOption option = null;
        List<MenuOption> menuList = new ArrayList<MenuOption>();

        // 由android.util.Xml创建一个XmlPullParser实例
        XmlPullParser parser = Xml.newPullParser();
        // 设置输入流 并指明编码方式
        parser.setInput(in, "UTF-8");
        // 产生第一个事件
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                // 判断当前事件是否为文档开始事件
                case XmlPullParser.START_DOCUMENT:
                    list = new ArrayList<MenuItem>();// 初始化list集合
                    break;
                // 判断当前事件是否为标签元素开始事件
                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("Item")) {
                        item = new MenuItem();
                    } else if (parser.getName().equals("Cmd")) {
                        eventType = parser.next();
                        item.setCmd(Integer.parseInt(parser.getText()));
                    } else if (parser.getName().equals("Name")) {
                        eventType = parser.next();
                        item.setName(parser.getText());
                    } else if (parser.getName().equals("MenuList")) {
                        menuList.clear();
                    } else if (parser.getName().equals("Option")) {
                        option = new MenuOption();
                    } else if (parser.getName().equals("Index")) {
                        eventType = parser.next();
                        option.setIndex(Integer.parseInt(parser.getText()));
                    } else if (parser.getName().equals("Id")) {
                        eventType = parser.next();
                        option.setId(parser.getText());
                    }
                    break;
                // 判断当前事件是否为标签元素结束事件
                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("Item")) {
                        list.add(item);
                        item = null;
                    } else if (parser.getName().equals("Option")) {
                        menuList.add(option);
                        option = null;
                    } else if (parser.getName().equals("MenuList")) {
                        item.setOption(menuList);
                        menuList.clear();
                    }
                    break;
            }
            // 进入下一个元素并触发相应事件
            eventType = parser.next();
        }
        return list;
    }

    public List<MovieSizeItem> queryMovieSizeXml(InputStream in) throws Exception {
        List<MovieSizeItem> list = null;
        MovieSizeItem item = null;

        // 由android.util.Xml创建一个XmlPullParser实例
        XmlPullParser parser = Xml.newPullParser();
        // 设置输入流 并指明编码方式
        parser.setInput(in, "UTF-8");
        // 产生第一个事件
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                // 判断当前事件是否为文档开始事件
                case XmlPullParser.START_DOCUMENT:
                    list = new ArrayList<MovieSizeItem>();// 初始化list集合
                    break;
                // 判断当前事件是否为标签元素开始事件
                case XmlPullParser.START_TAG:
                    if (parser.getName().equals("Item")) {
                        item = new MovieSizeItem();
                    } else if (parser.getName().equals("Name")) {
                        eventType = parser.next();
                        item.setName(parser.getText());
                    } else if (parser.getName().equals("Index")) {
                        eventType = parser.next();
                        item.setIndex(Integer.parseInt(parser.getText()));
                    } else if (parser.getName().equals("Size")) {
                        eventType = parser.next();
                        item.setSize(parser.getText());
                    } else if (parser.getName().equals("FrameRate")) {
                        eventType = parser.next();
                        item.setFramerate(Integer.parseInt(parser.getText()));
                    } else if (parser.getName().equals("Type")) {
                        eventType = parser.next();
                        item.setType(Integer.parseInt(parser.getText()));
                    }
                    break;
                // 判断当前事件是否为标签元素结束事件
                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("Item")) {
                        list.add(item);
                        item = null;
                    }
                    break;
            }
            // 进入下一个元素并触发相应事件
            eventType = parser.next();
        }
        return list;
    }

    public long getUnsignedInt(int data) {
        return data & 0x0FFFFFFFFL;
    }

    public CameraVersionResponse getCameraVersionResponse(InputStream inputStream) {
        XmlPullParser parser = Xml.newPullParser(); // 创建一个PULL解析器
        CameraVersionResponse info = null;
        try {
            parser.setInput(inputStream, "utf-8");
            info = new CameraVersionResponse();
            int type = parser.getEventType();
            while (type != XmlPullParser.END_DOCUMENT) {
                switch (type) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if ("Cmd".equals(parser.getName())) {
                            info.cmd = parser.nextText();
                        } else if ("Status".equals(parser.getName())) {
                            info.status = parser.nextText();
                        } else if ("String".equals(parser.getName())) {
                            info.string = parser.nextText();
                        } else if ("Chip".equals(parser.getName())) {
                            info.chip = parser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                type = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
        return info;
    }

    public CaptureResponse getCaputreResponse(InputStream inputStream) throws IOException {
        XmlPullParser parser = Xml.newPullParser(); // 创建一个PULL解析器
        try {
            parser.setInput(inputStream, "utf-8");// 设置解析的数据源
            CaptureResponse info = new CaptureResponse();
            int type = parser.getEventType(); // 开始解析时调用
            while (type != XmlPullParser.END_DOCUMENT) { // 当文档没有结束的时候，调用这个方法
                switch (type) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG: // 解析到xml标签的时候
                        if ("Cmd".equals(parser.getName())) {
                            info.cmd = parser.nextText();
                        } else if ("Status".equals(parser.getName())) {
                            info.status = parser.nextText();
                        } else if ("NAME".equals(parser.getName())) {
                            info.name = parser.nextText();
                        } else if ("FPATH".equals(parser.getName())) {
                            info.path = parser.nextText();
                        } else if ("FREEPICNUM".equals(parser.getName())) {
                            info.freepicnum = parser.nextText();
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                type = parser.next();
                Log.e(TAG, "getCaputreResponse: type:" + type);
            }
            return info;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T parseSimpleXml(InputStream inputStream, Class<T> clazz) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(inputStream);
            Element root = document.getDocumentElement();
            T t = clazz.newInstance();
            Field[] fields = clazz.getFields();
            for (Field field : fields) {
                NodeList elementsByTagName = root.getElementsByTagName(field.getName());
                if (elementsByTagName != null && elementsByTagName.getLength() > 0) {
                    field.set(t, elementsByTagName.item(0).getFirstChild().getNodeValue());
                }
            }
            return t;
        } catch (Exception ignored) {

        }
        return null;
    }

    public <T> T parseSimpleXml(String response, Class<T> clazz) {
        try {
            InputStream is = new ByteArrayInputStream(response
                    .getBytes("utf-8"));
            return parseSimpleXml(is, clazz);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public MovieRecord getParserXml(InputStream inputStream) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(inputStream);
            Element root = document.getDocumentElement(); // 先取跟元素Function
            MovieRecord mMovieRecord = new MovieRecord();
            NodeList stuNo = root.getElementsByTagName("Cmd");
            if (stuNo != null && stuNo.getLength() > 0) {
                mMovieRecord.setCmd(stuNo.item(0).getFirstChild().getNodeValue());
            }
            NodeList stuStatus = root.getElementsByTagName("Status");
            if (stuStatus != null && stuStatus.getLength() > 0) {
                mMovieRecord.setStatus(stuStatus.item(0).getFirstChild().getNodeValue());
            }
            NodeList stuValue = root.getElementsByTagName("Value");
            if (stuValue != null && stuValue.getLength() > 0) {
                mMovieRecord.setValue(stuValue.item(0).getFirstChild().getNodeValue());
            }
            NodeList stuString = root.getElementsByTagName("String");
            if (stuString != null && stuString.getLength() > 0) {
                mMovieRecord.setString(stuString.item(0).getFirstChild().getNodeValue());
            }
            return mMovieRecord;
        } catch (DOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO: handle exception
        }
        return null;

    }

    public List<FileDomain> parseMstarFileXml(InputStream is) throws Exception {
        List<FileDomain> booksList = null;
        FileDomain fileDomain = null;
        // 由android.util.Xml创建一个XmlPullParser实例
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, "UTF-8");
        // 产生第一个事件
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                // 判断当前事件是否为文档开始事件
                case XmlPullParser.START_DOCUMENT:
                    booksList = new ArrayList<FileDomain>(); // 初始化books集合
                    break;

                // 判断当前事件是否为标签元素开始事件
                case XmlPullParser.START_TAG:

                    if (parser.getName().equals("file")) { // 判断开始标签元素是否是book
                        fileDomain = new FileDomain();
                        fileDomain.baseUrl = "http://" + MstarCamera.CAM_IP;
                    } else if (parser.getName().equals("name")) { // 判断开始标签元素是否是book
                        eventType = parser.next();
                        String fpath = parser.getText().replace("\\", "/");
                        fileDomain.setName(fpath.substring(fpath.lastIndexOf("/") + 1));
                        fileDomain.setFpath(fpath);
                        fileDomain.setDownloadPath(fileDomain.baseUrl + fileDomain.getFpath());
                        try {
                            String lowerCasePath = fileDomain.getFpath().toLowerCase();
                            if (lowerCasePath.endsWith(".mov") || lowerCasePath.endsWith(".mp4") || lowerCasePath.endsWith(".avi") || lowerCasePath.endsWith(".ts")) {
                                fileDomain.isPicture = false;
                            }
                        } catch (NullPointerException ignore) {

                        }
                    } else if (parser.getName().equals("size")) { // 判断开始标签元素是否是price
                        eventType = parser.next();
                        fileDomain.setSize(getUnsignedInt(Integer.parseInt(parser.getText())));
                    } else if (parser.getName().equals("time")) { // 判断开始标签元素是否是book
                        eventType = parser.next();
                        fileDomain.setTime(parser.getText());
                    } else if (parser.getName().equals("attr")) { // 判断开始标签元素是否是book
                        eventType = parser.next();
                        String text = parser.getText();
                        if (!TextUtils.isEmpty(text) && text.equals("RW")) {
                            fileDomain.setAttr(32);
                        } else {
                            fileDomain.setAttr(33);
                        }
                    }
                    break;

                // 判断当前事件是否为标签元素结束事件
                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("file")) { // 判断结束标签元素是否是book
                        booksList.add(fileDomain); // 将book添加到books集合
                        fileDomain = null;
                    }
                    break;
                default:
                    break;
            }
            // 进入下一个元素并触发相应事件
            eventType = parser.next();
        }
        return booksList;
    }

    public List<FileDomain> parsePullXml(InputStream is) throws Exception {

        List<FileDomain> booksList = null;
        FileDomain fileDomain = null;

        // 由android.util.Xml创建一个XmlPullParser实例
        XmlPullParser parser = Xml.newPullParser();
        // 设置输入流 并指明编码方式
        parser.setInput(is, "UTF-8");
        // 产生第一个事件
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                // 判断当前事件是否为文档开始事件
                case XmlPullParser.START_DOCUMENT:
                    booksList = new ArrayList<FileDomain>(); // 初始化books集合
                    break;

                // 判断当前事件是否为标签元素开始事件
                case XmlPullParser.START_TAG:

                    if (parser.getName().equals("File")) { // 判断开始标签元素是否是book
                        fileDomain = new FileDomain();
                    } else if (parser.getName().equals("NAME")) {
                        eventType = parser.next();
                        // 得到book标签的属性值，并设置book的id
                        fileDomain.setName(parser.getText());
                    } else if (parser.getName().equals("FPATH")) { // 判断开始标签元素是否是book
                        eventType = parser.next();
                        fileDomain.setFpath(parser.getText());
                        fileDomain.setDownloadPath(Contacts.BASE_HTTP_IP + fileDomain.getFpath().substring(fileDomain.getFpath().indexOf(":") + 1).replace("\\", "/"));
                        try {
                            String lowerCasePath = fileDomain.getFpath().toLowerCase();
                            if (lowerCasePath.endsWith(".mov") || lowerCasePath.endsWith(".mp4") || lowerCasePath.endsWith(".avi") || lowerCasePath.endsWith(".ts")) {
                                fileDomain.isPicture = false;
                            }
                        } catch (NullPointerException ignore) {

                        }
                    } else if (parser.getName().equals("SIZE")) { // 判断开始标签元素是否是price
                        eventType = parser.next();
                        fileDomain.setSize(getUnsignedInt(Integer.parseInt(parser.getText())));
                    } else if (parser.getName().equals("TIMECODE")) { // 判断开始标签元素是否是book
                        eventType = parser.next();
                        fileDomain.setTimeCode(Long.parseLong(parser.getText()));
                    } else if (parser.getName().equals("TIME")) { // 判断开始标签元素是否是book
                        eventType = parser.next();
                        fileDomain.setTime(parser.getText());
                    } else if (parser.getName().equals("ATTR")) { // 判断开始标签元素是否是book
                        eventType = parser.next();
                        fileDomain.setAttr(Integer.parseInt(parser.getText()));
                    } else if (parser.getName().equals("UPTIME")) {
                        eventType = parser.next();
                        fileDomain.upTime = parser.getText();
                    }
                    break;

                // 判断当前事件是否为标签元素结束事件
                case XmlPullParser.END_TAG:
                    if (parser.getName().equals("File")) { // 判断结束标签元素是否是book
                        booksList.add(fileDomain); // 将book添加到books集合
                        fileDomain = null;
                    }
                    break;
                default:
                    break;
            }
            // 进入下一个元素并触发相应事件
            eventType = parser.next();
        }
        return booksList;
    }

    public int parastr(String inStr) {
        String strMainTemp;
        String strSubTemp;
        cmdstatus[] cmdstat;

        if (hMap != null) {
            hMap.clear();
        }
        int icmdcount = substrcount(strCmdPre, inStr);
        cmdstat = new cmdstatus[icmdcount];
        for (int i =
             0; i < icmdcount; i++) {
            cmdstat[i] = new cmdstatus();
            //get cmd
            //iMainPosPre = inStr.indexOf(strCmdPre, i);
            //iMainPosBac = inStr.indexOf(strCmdBac,i);
            iMainPosPre = inStr.indexOf(strCmdPre);
            iMainPosBac = inStr.indexOf(strCmdBac);
            // FIXME: 2016/8/26 (-1 == iMainPosPre) && (-1 == iMainPosPre)
            if ((-1 == iMainPosPre) || (-1 == iMainPosBac)) {
                //return -1;
                break;
            }
            strSubTemp = inStr.substring(iMainPosPre + iCmdIgnore, iMainPosBac);
            cmdstat[i].cmd = strSubTemp;

            //get status
            //iMainPosPre = inStr.indexOf(strStatusPre, i);
            //iMainPosBac = inStr.indexOf(strStatusBac,i);
            iMainPosPre = inStr.indexOf(strStatusPre);
            iMainPosBac = inStr.indexOf(strStatusBac);
            strSubTemp = inStr.substring(iMainPosPre + iStatusIgnore, iMainPosBac);
            cmdstat[i].status = strSubTemp;

            //Log.e(strFiletag, "iMainPosPre="+ iMainPosPre + "iMainPosBac ="+ iMainPosBac );
            //Log.e(strFiletag, "strSubTemp = "+ strSubTemp);

            strMainTemp = inStr.substring(iMainPosBac + iBacStatusIgn); //

            //Log.e(strFiletag,  " ,strMainTemp = "+ strMainTemp);
            inStr = strMainTemp;
        }
        for (int i = 0; i < icmdcount; i++) {
            Log.e(strFiletag, "cmd = " + cmdstat[i].cmd + ",status = " + cmdstat[i].status);
            hMap.put(cmdstat[i].cmd, cmdstat[i].status);
        }
        return icmdcount;
    }

    private int substrcount(String sub, String str) {
        int index = 0;
        int count = 0;

        while ((index = str.indexOf(sub)) != -1) {
            str = str.substring(index + sub.length());
            count++;
        }
        return count;
    }

    public MovieRecordValue getParserXmls(InputStream inputStream) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(inputStream);
            Element root = document.getDocumentElement(); // 先取跟元素Function
            MovieRecordValue mMovieRecordValue = new MovieRecordValue();
            NodeList stuNo = root.getElementsByTagName("Cmd");
            if (stuNo != null && stuNo.getLength() > 0) {
                mMovieRecordValue.setCmd(stuNo.item(0).getFirstChild().getNodeValue());
            }
            NodeList stuName = root.getElementsByTagName("Status");
            if (stuName != null && stuNo.getLength() > 0) {
                mMovieRecordValue.setStatus(stuName.item(0).getFirstChild()
                        .getNodeValue());
            }
            NodeList value = root.getElementsByTagName("Value");
            if (value != null && value.getLength() > 0) {
                LogUtils.i(value.item(0).getFirstChild().getNodeValue());
                mMovieRecordValue.setValue(value.item(0).getFirstChild().getNodeValue());
            }
            NodeList string = root.getElementsByTagName("String");
            if (string != null && string.getLength() > 0) {
                LogUtils.i(string.item(0).getFirstChild().getNodeValue());
                mMovieRecordValue.setString(string.item(0).getFirstChild().getNodeValue());
            }
            return mMovieRecordValue;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
