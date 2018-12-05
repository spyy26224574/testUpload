
package com.adai.gkdnavi;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.gkd.contacts.Contacts;
import com.adai.gkdnavi.utils.CameraUpdateUtil;
import com.adai.gkdnavi.utils.LogUtils;
import com.example.ipcamera.application.VLCApplication;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckVersionTask implements Runnable {
    protected static final String TAG = "CheckVersionTask";
    private static final int GET_UNDATAINFO_ERROR = 0;
    private static final int DOWN_UPDATE = 1;
    private static final int DOWN_OVER = 2;
    // 添加的代码
    private static final int DOWN_ERROR = 3;
    private static final int UPDATA_CLIENT = 4;
    private static final int SHOW_UPDATE_DIALOG = 5;
    // 对比版本号
    private String ota_apk_Ver;
    private ProgressBar Progress;
    private Context context;
    private Dialog noticeDialog;
    private Dialog downloadDialog;
    /* 下载包安装路径 */

    private int progress;
    private Thread downLoadThread;
    private boolean interceptFlag = false;
    //SharedPreferences pref = null;
    SharedPreferences spf_otacontentcheck = null;
    private String ota_apk_url = null;
//	private static TextView textnfumber;


    public CheckVersionTask(Context context) {
        this.context = context;
    }


    // ***********************找到服务器的版本号*****************************************************************
    public static List<UpdataInfo> getUpdataInfo(InputStream is) throws Exception {// 传入输入流
        // 这个流就是包含服务器信息的一段xml字符串
        XmlPullParser parser = Xml.newPullParser(); // 创建一个PULL解析器
        parser.setInput(is, "utf-8");// 设置解析的数据源
        List<UpdataInfo> infos = null;
        int type = parser.getEventType(); // 开始解析时调用
        UpdataInfo info = new UpdataInfo();// 实体
        while (type != XmlPullParser.END_DOCUMENT) { // 当文档没有结束的时候，调用这个方法
            switch (type) {
                case XmlPullParser.START_DOCUMENT:
                    infos = new ArrayList<>();
                    break;
                case XmlPullParser.START_TAG: // 解析到xml标签的时候
                    if ("updata".equals(parser.getName())) {
                        info = new UpdataInfo();
                        info.setType(parser.getAttributeValue(0));
                    } else if ("version".equals(parser.getName())) {
                        Log.v(parser.getName() + "Version number", "Versionnumber=" + parser.getName());
                        info.setVersion(parser.nextText()); // 获取版本号
                    } else if ("url".equals(parser.getName())) {
                        info.setUrl(parser.nextText()); // 获取要升级的APK文件
                    } else if ("md5".equals(parser.getName())) {
                        info.setMd5(parser.nextText()); // 获取该文件加密信息
                    } else if ("contentcn".equals(parser.getName()))//简体
                    {
                        info.setContentcn(parser.nextText());
                    } else if ("contenttw".equals(parser.getName()))//繁体
                    {
                        info.setContenttw(parser.nextText());
                    } else if ("contentdefault".equals(parser.getName()))//默认
                    {
                        info.setContentdefault(parser.nextText());
                    } else if ("url".equals(parser.getName())) {
                        info.setUrl(parser.nextText());
                    } else if ("applicationname".equals(parser.getName())) {
                        info.setApplicationname(parser.nextText());
                    } else if ("description".equals(parser.getName()) && info != null) {
                        UpdateDescriptioninfo description = new UpdateDescriptioninfo();
                        description.setCountry(parser.getAttributeValue(0));
                        description.setContent(parser.nextText());
                        info.addDescription(description);
                    }

                    break;
                case XmlPullParser.END_TAG:
                    if ("updata".equals(parser.getName()) && infos != null && info != null)
                        infos.add(info);
                    break;
            }
            type = parser.next();
        }

        return infos;
    }

    // *************************************安装apk************************************************
    private void installApk() {
        File apkfile = new File(VLCApplication.OTA_PATH, DownloadService.LOCALAPPNAME);
        if (!apkfile.exists()) {
            return;
        }

        Intent tent = new Intent(Intent.ACTION_VIEW);
        tent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        tent.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
        context.startActivity(tent);

    }

    // ***********************************加密的**********************************加密的************************
    // 下面这个函数用于将字节数组换成成16进制的字符串
    public static String byteArrayToHex(byte[] byteArray) {
        // 首先初始化一个字符数组，用来存放每个16进制字符
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F'};
        // new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符（2的8次方等于16的2次方））
        char[] resultCharArray = new char[byteArray.length * 2];
        // 遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
        int index = 0;
        for (byte b : byteArray) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        // 字符数组组合成字符串返回
        return new String(resultCharArray);
    }

    // *********************************加密的代码***********
    public static String fileMD5(String inputFile) throws IOException {
        if (TextUtils.isEmpty(inputFile)) {
            return "";
        }
        // 缓冲区大小（这个可以抽出一个参数）
        int bufferSize = 256 * 1024;
        FileInputStream fileInputStream = null;
        DigestInputStream digestInputStream = null;
        try {
            // 拿到一个MD5转换器（同样，这里可以换成SHA1）
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            // 使用DigestInputStream
            fileInputStream = new FileInputStream(inputFile);
            digestInputStream = new DigestInputStream(fileInputStream, messageDigest);
            // read的过程中进行MD5处理，直到读完文件
            byte[] buffer = new byte[bufferSize];
            while (digestInputStream.read(buffer) > 0) ;
            // 获取最终的MessageDigest
            messageDigest = digestInputStream.getMessageDigest();
            // 拿到结果，也是字节数组，包含16个元素
            byte[] resultByteArray = messageDigest.digest();
            // 同样，把字节数组转换成字符串
            return byteArrayToHex(resultByteArray);
        } catch (NoSuchAlgorithmException e) {
            return null;
        } finally {
            try {
                digestInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                fileInputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ***************************弹出的下载框*******************************************
    public void showNoticeDialog() {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        AlertDialog.Builder bulider = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.chekversion_dialog, null);
        Button btnUpdate = (Button) view.findViewById(R.id.btn_update);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        bulider.setView(view);

        //版本号
        String versionnumberget = spf_otacontentcheck.getString("gspOtaAPKVer", "");

        //内容
        Locale localbase = Locale.getDefault();
        String language = localbase.getLanguage();
        String country = localbase.getCountry().toLowerCase();

        if ("zh".equals(language)) {
            if ("cn".equals(country)) {

                String contentcnget = spf_otacontentcheck.getString("content_cn", "");

                TextView contentone = (TextView) view.findViewById(R.id.contentone);
                contentone.setText(contentcnget);
                LogUtils.e("update cn msg=" + contentcnget);

            } else if ("tw".equals(country)) {
                //SharedPreferences contenttwPreferences = Context.getSharedPreferences("gspOta", Context.MODE_PRIVATE);
                String contenttwget = spf_otacontentcheck.getString("content_tw", "");
                TextView contentone = (TextView) view.findViewById(R.id.contentone);
                contentone.setText(contenttwget);
                LogUtils.e("update tw msg=" + contenttwget);
            }
        } else if ("contentdefault".equals(language)) {
            if ("ELSE".equals(country)) {
                //SharedPreferences contentdefalutPreferences = Context.getSharedPreferences("gspOta", Context.MODE_PRIVATE);
                String contentdefalutget = spf_otacontentcheck.getString("content_def", "");
                TextView contentone = (TextView) view.findViewById(R.id.contentone);
                contentone.setText(contentdefalutget);
                LogUtils.e("update default msg=" + contentdefalutget);
            }
        }
        TextView Versionnumber = (TextView) view.findViewById(R.id.banbennumber);
        Versionnumber.setText(context.getResources().getString(R.string.newcheckversion) + ":" + versionnumberget);

        final AlertDialog dialog = bulider.create();
        btnUpdate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                int type = networkInfo.getType();
                if (type == ConnectivityManager.TYPE_WIFI) {//WIFI
                    LogUtils.e("连接wifi网络");
                    //GuanyuCheckOTA();
                    if (!VLCApplication.mIsDownloadedAPK) {
                        Intent intent = new Intent(context, DownloadService.class);
                        SharedPreferences apkurl1Preferences = context.getSharedPreferences("gspOta", Context.MODE_PRIVATE);
                        String apkurl1get = apkurl1Preferences.getString("gspOtaApkUrl", "");
                        LogUtils.e("CheckVersion onclick apkurl = " + apkurl1get);
                        intent.putExtra("otapakurl", apkurl1get);
                        //由intent启动service，后台运行下载进程，在服务中调用notifycation状态栏显示进度条
                        //mContext是个上下文对象，由activity传递过来的，就相当于activity
                        context.startService(intent);
                        VLCApplication.mIsDownloadedAPK = true;
                    }
                } else if (type == ConnectivityManager.TYPE_MOBILE) {//MOBILE
                    LogUtils.e("连接移动网络");
                    showDetermineDownloadDialog();
                }
//	             WifiManager mWifiManager = (WifiManager) Context.getSystemService(Context.WIFI_SERVICE);  
//		         WifiInfo wifiInfo = mWifiManager.getConnectionInfo();  
//		         int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();  
//		         if (mWifiManager.isWifiEnabled() && ipAddress != 0) {  
//		        	 dialog.dismiss();
//		        	 if (!VLCApplication.mIsDownloadedAPK) {
//		        		 Intent intent=new Intent(Context, DownloadService.class); 
//		        		 String   apkurl1get  = spf_otacontentcheck.getString("gspOtaApkUrl", "");
//		        		 LogUtils.e( "CheckVersion showNoticeDialog()|| otaurl= " + apkurl1get);
//		        		 intent.putExtra("otapakurl", apkurl1get);
//		        		 //由intent启动service，后台运行下载进程，在服务中调用notifycation状态栏显示进度条
//		        		 //mContext是个上下文对象，由activity传递过来的，就相当于activity
//		        		 Context.startService(intent);
//		        		 VLCApplication.mIsDownloadedAPK = true;
//		        	 }
//		          } else {  
//		        	 dialog.dismiss();
//		        	 showDetermineDownloadDialog();
//		             //System.out.println("**** WIFI is off"); 
//		        }  
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                // 点击取消按钮响应事件：
                dialog.dismiss();
            }
        });
        dialog.show();
    }


    public void showDetermineDownloadDialog() {
        AlertDialog.Builder bulider = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.network_state, null);
        Button btnUpdate = (Button) view.findViewById(R.id.btn_network_update);
        Button btnCancel = (Button) view.findViewById(R.id.btn_network_cancel);
        bulider.setView(view);
        final AlertDialog dialog = bulider.create();
        btnUpdate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                if (!VLCApplication.mIsDownloadedAPK) {
                    Intent intent = new Intent(context, DownloadService.class);
                    SharedPreferences apkurl1Preferences = context.getSharedPreferences("gspOta", Context.MODE_PRIVATE);
                    String apkurl1get = apkurl1Preferences.getString("gspOtaApkUrl", "");
                    LogUtils.e("CheckVersion onclick apkurl = " + apkurl1get);
                    intent.putExtra("otapakurl", apkurl1get);
                    //由intent启动service，后台运行下载进程，在服务中调用notifycation状态栏显示进度条
                    //mContext是个上下文对象，由activity传递过来的，就相当于activity
                    context.startService(intent);
                    VLCApplication.mIsDownloadedAPK = true;
                }
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // 点击取消按钮响应事件：
                dialog.dismiss();
            }
        });
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    // ****************************************更新中**********************
    public void showDownloadDialog() {

        //downloadApk();
        AlertDialog.Builder bulider = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.check_jindutiao, null);
        Progress = (ProgressBar) view.findViewById(R.id.progress);
        Button btnUpdate = (Button) view.findViewById(R.id.btn_update);
        Button btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        bulider.setView(view);
        final AlertDialog dialog = bulider.create();
        btnUpdate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                dialog.dismiss();
                // 点击立即更新按钮响应事件 后台下载：
                dialog.dismiss();
                Intent intent = new Intent(context, DownloadService.class);
                SharedPreferences apkurl1Preferences = context.getSharedPreferences("gspOta", Context.MODE_PRIVATE);
                String apkurl1get = apkurl1Preferences.getString("gspOtaApkUrl", "");
                LogUtils.e("otapakurl = " + apkurl1get);
                intent.putExtra("otapakurl", apkurl1get);
                //由intent启动service，后台运行下载进程，在服务中调用notifycation状态栏显示进度条
                //mContext是个上下文对象，由activity传递过来的，就相当于activity
                context.startService(intent);
            }
        });
        btnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                // 点击取消按钮响应事件：
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    // **********************************外部接口让主Activity调用********************************
    public void checkUpdateInfo() {
        showNoticeDialog();
    }

    public void run() {
        new Thread() {
            @Override
            public void run() { // 需要在线程执行的方法  192.168.21.106
                try {
//                	String httpUrl = "http://120.27.194.224/upload/appota/gh801l.xml";

                    String httpUrl = Contacts.VERSION_UPDATE;
                    HttpClient httpClient = new DefaultHttpClient();
                    httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
                    HttpGet get = new HttpGet(httpUrl);
                    HttpResponse response;
                    InputStream input;
                    //Log.e(otaTag, "httpUrl = " + httpUrl);
                    response = httpClient.execute(get);
                    Message msg = Message.obtain();
                    if (response.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = response.getEntity();
                        input = entity.getContent();
                        List<UpdataInfo> infos = getUpdataInfo(input);
                        if (infos == null || infos.size() <= 0) return;
                        spf_otacontentcheck = context.getSharedPreferences("gspOta", Context.MODE_PRIVATE);
                        SharedPreferences.Editor spf_otacontenteditor = spf_otacontentcheck.edit();
                        UpdataInfo info = infos.get(0); // 调用解析方法
                        for (UpdataInfo updataInfo : infos) {
                            if ("app".equals(updataInfo.getType())) {
                                info = updataInfo;
                            } else if ("mcu".equals(updataInfo.getType())) {
                                spf_otacontenteditor.putString("mcu_md5", updataInfo.getMd5());
                                spf_otacontenteditor.putString("mcu_version", updataInfo.getVersion());
                                spf_otacontenteditor.putString("mcu_url", updataInfo.getUrl());
                            } else if ("obd".equals(info.getType())) {
//								spf_otacontenteditor.putString("obd_md5", info.getMd5());
//								spf_otacontenteditor.putString("obd_version", info.getVersion());
//								spf_otacontenteditor.putString("obd_url", info.getUrl());
                            } else if ("camera".equals(info.getType())) {
                                spf_otacontenteditor.putString("camera_md5", info.getMd5());
                                spf_otacontenteditor.putString("camera_version", info.getVersion());
                                spf_otacontenteditor.putString("camera_url", info.getUrl());
                                spf_otacontenteditor.putString("camera_defaultcontent", info.getContentdefault());

                            }
                        }
                        ota_apk_Ver = info.getVersion(); // 获得服务器版本

                        String servermd5 = info.getMd5();//得到服务器MD5
                        ota_apk_url = info.getUrl();//获得服务器版本路径
                        //Log.e(otaTag, "httpUrl = " + httpUrl+",Version="+ota_apk_Ver);
                        LogUtils.e("Version=" + ota_apk_Ver + ",ota_apk_url = " + ota_apk_url);
                        String applicationname = info.getApplicationname();//得到应用的名字
                        String contentcn = info.getContentcn();//得到内容简体
                        String contenttw = info.getContenttw();//得到内容繁体
                        String contentdefalut = info.getContentdefault();//得到默认的内容
                        Locale local = Locale.getDefault();
                        String language = local.getLanguage();
                        String country = local.getCountry().toLowerCase();

                        if ("zh".equals(language)) {
                            if ("cn".equals(country)) {
                                String cncontent = contentcn.replace(",", "\n");
                                spf_otacontenteditor.putString("content_cn", cncontent);
                            } else if ("tw".equals(country)) {
                                String wrapcontent = contenttw.replace(",", "\n");
                                spf_otacontenteditor.putString("content_tw", wrapcontent);
                                //spf_otacontenteditor.commit();
                            }
                        } else if ("contentdefault".equals(language)) {
                            if ("ELSE".equals(country)) {
                                String defalutcontent = contentdefalut.replace(",", "\n");
                                spf_otacontenteditor.putString("content_def", defalutcontent);
                            }
                        }

                        //得到服务器的url
                        // ota_apk_url=info.getUrl();//获得服务器版本
                        //存储应用名字
                        spf_otacontenteditor.putString("gspOTAAppName", applicationname);
                        //存储服务器的url
                        spf_otacontenteditor.putString("gspOtaApkUrl", ota_apk_url);
                        //存储版本号
                        spf_otacontenteditor.putString("gspOtaAPKVer", ota_apk_Ver);
                        spf_otacontenteditor.putString("gspOtaAPKMD5", servermd5);
                        spf_otacontenteditor.commit();
                        //本地版本号
                        String versionnumberget = spf_otacontentcheck.getString("gspLocalVerNo", "");

                        //if (!ota_apk_Ver.equals(versionnumberget)) {
                        //if (ota_apk_Ver.compareTo(versionnumberget) > 0) {
                        if (ota_apk_Ver.compareTo(versionnumberget) > 0) {
                            String localApkVersion = discoverVersion();
                            if (ota_apk_Ver != null && ota_apk_Ver.equals(localApkVersion)) {//判断下载成功的版本号
                                Uri findUri = findUri();
                                if (findUri != null) {
                                    openfile(findUri);
                                    return;
                                }
                            }
                            msg.what = SHOW_UPDATE_DIALOG;
                            mHandler.sendMessage(msg);
                        }

                    } else {
                        msg.what = GET_UNDATAINFO_ERROR;
                        mHandler.sendMessage(msg);
                    }

                    CameraUpdateUtil update = new CameraUpdateUtil(context);
                    if (!update.checkFile(spf_otacontentcheck.getString("camera_md5", ""))) {
                        Looper.prepare();
                        update.downloadFile(spf_otacontentcheck.getString("camera_url", ""), spf_otacontentcheck.getString("camera_md5", ""));
                        Looper.loop();
                    }
                } catch (IOException e) {
                    Message msg = Message.obtain();
                    msg.what = GET_UNDATAINFO_ERROR;
                    mHandler.sendMessage(msg);
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            private SharedPreferences getSharedPreferences(String string, int i) {
                return null;
            }
        }.start();
    }

    // Handler消息接收机制
    public Handler mHandler = new Handler() {
        // //Handler接收到相应消息进行刷新ui等操作
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DOWN_OVER:
                    VLCApplication.mIsDownloadedAPK = false;
                    installApk();
                    break;
                case GET_UNDATAINFO_ERROR:
//                    if (!showUpdateDialog) {
//                        Toast.makeText(context.getApplicationContext(), context.getString(R.string.ota_connetfail), Toast.LENGTH_SHORT).show(); //
//                    }
                    break;
                case DOWN_ERROR:
                    System.out.println("down fial  md5  is not ok");
                    VLCApplication.mIsDownloadedAPK = false;
                    Toast.makeText(context.getApplicationContext(), context.getString(R.string.download_newversion_failed), Toast.LENGTH_SHORT).show();
                    break;
                case UPDATA_CLIENT:
                    checkUpdateInfo();
                    break;
                case SHOW_UPDATE_DIALOG:
                    showNoticeDialog();
                default:
                    break;
            }
        }
    };

    private String discoverVersion() {
        String version = null;
        File file = new File(VLCApplication.OTA_PATH);
        File[] listFiles = file.listFiles();
        PackageManager pm = context.getPackageManager();
        for (File currenFile : listFiles) {
            if (currenFile.isFile() && currenFile.getName().equals(DownloadService.LOCALAPPNAME)) {
                File file2 = new File(file, DownloadService.LOCALAPPNAME);
                String absolutePath = file2.getAbsolutePath();
                PackageInfo info = pm.getPackageArchiveInfo(absolutePath, PackageManager.GET_ACTIVITIES);
                if (info != null) {
                    version = info.versionName;       //得到版本信息
                    LogUtils.e(version);
                }
            }
        }
        return version;
    }

    private Uri findUri() {
        Uri localFilePath = null;
        File file = new File(VLCApplication.OTA_PATH);
        File[] listFiles = file.listFiles();
        for (File currenFile : listFiles) {
            if (currenFile.isFile() && currenFile.getName().equals(DownloadService.LOCALAPPNAME)) {
                File file2 = new File(file, DownloadService.LOCALAPPNAME);
                localFilePath = Uri.fromFile(file2);
            }
        }
        return localFilePath;
    }

    private void openfile(Uri url) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(url, "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
}
 


