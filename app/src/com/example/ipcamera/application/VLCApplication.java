package com.example.ipcamera.application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.LocaleList;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDex;
import android.text.TextUtils;
import android.util.Log;

import com.adai.gkd.contacts.Constants_weibo;
import com.adai.gkd.contacts.Constants_wx;
import com.adai.gkdnavi.BuildConfig;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.mob.MobSDK;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiskCache;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.umeng.analytics.MobclickAgent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//import com.jieli.lib.dv.control.DeviceClient;
//import com.jieli.stream.dv.running2.bean.DeviceDesc;
//import com.jieli.stream.dv.running2.bean.DeviceSettingInfo;
//import com.jieli.stream.dv.running2.ui.service.CommunicationService;
//import com.jieli.stream.dv.running2.ui.service.ScreenShotService;
//import com.jieli.stream.dv.running2.util.AppUtils;
//import com.jieli.stream.dv.running2.util.ClientManager;
//import com.jieli.stream.dv.running2.util.Dbug;
//import com.jieli.stream.dv.running2.util.IConstant;
//import com.jieli.stream.dv.running2.util.PreferencesHelper;
//import com.jieli.stream.dv.running2.util.WifiHelper;

//import static com.jieli.stream.dv.running2.util.IConstant.DIR_FRONT;
//import static com.jieli.stream.dv.running2.util.IConstant.DIR_REAR;
//import static com.jieli.stream.dv.running2.util.IConstant.KEY_APP_LANGUAGE_CODE;
//import static com.jieli.stream.dv.running2.util.IConstant.SERVICE_CMD_CLOSE_SCREEN_TASK;


/**
 * VLC和下载的初始化
 */
public class VLCApplication extends Application {
    public static Thread mMainThread;
    public static int mMainThreadId;
    public static Handler mMainHandler;
    private static VLCApplication sInstance;
    private boolean apisConnect;
    public static boolean allowDownloads;
    public static final String APP_ROOT = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/WFCARDV360/";//最好引用到配置文件中去
    public static final String LOCAL_PICTURE = APP_ROOT + "localpicture/";
    public static final String DOWNLOADPATH = APP_ROOT + "downloads";
    public static final String LOG_PATH = APP_ROOT + "log";
    public static final String OTA_PATH = APP_ROOT + "ota";
    public static final String TEMP_PATH = APP_ROOT + "temp";
    public static final String MUSIC_PATH = APP_ROOT + "music";
    public static final String CUT_VIDEO_PATH = APP_ROOT + "cutvideo";
    public static final String CALIBRATION_PATH = APP_ROOT + "calibration";
    public static final String CACHE = APP_ROOT + "cache/";
    public static final String TAG = "VolleyPatterns";
    public static int picture = 0;
    public static boolean mIsDownloadedAPK = false;
    /**
     * 分享最大图片数量
     */
    public static final int MAX_PHOTO_NUM = 9;

    /**
     * 当前用户nickname,为了苹果推送不是userid而是昵称
     */
    public static String currentUserNick = "";

    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;

    private ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(0, 2, 2, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>());

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized VLCApplication getInstance() {
        return sInstance;
    }

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be  
        // created when it is accessed for the first time  
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    public void resetRequestQueue() {
        if (mRequestQueue != null) {
            mRequestQueue.stop();
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }
    }

    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty  
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setShouldCache(false);
        req.setTag(TAG);

        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not updateSize your app in this process.
//            return;
//        }
//        LeakCanary.install(this);
        // Normal app updateSize code...
        MobclickAgent.setDebugMode(false);//用来在ide上查看日志，tag是MobclickAgent
        MobclickAgent.setCatchUncaughtExceptions(false);
        MobSDK.init(this, "1dba6a3ff9b26", "0e8a5ef55cdb38b634e67380a1097d04");
        if (!BuildConfig.DEBUG) {
            recordMessage();
        }
        sInstance = this;
//		currentMode=PreferenceManager.getDefaultSharedPreferences(this).getInt("appmode", 0);
//		EaseUI.getInstance().updateSize(this);
        regToWx();
        regToWeibo();
        mMainThread = Thread.currentThread();
        mMainThreadId = android.os.Process.myTid();
//		if(!VoiceManager.isConnectBaidu){
//			handler.postDelayed(runnable, 2000);
//		}
//        if (currentMode == 0 && !isBleServiceRuning()) {
//            startBleService();
//        }
        ///////////////
        mMainHandler = new Handler();

//        File destDir = new File(APP_ROOT);
//        if (!destDir.exists()) {
//            destDir.mkdirs();
//        }
//        File downloads = new File(DOWNLOADPATH);
//        if (!downloads.exists()) {
//            downloads.mkdirs();
//        }
//        File log = new File(LOG_PATH);
//        if (!log.exists()) {
//            log.mkdirs();
//        }
//        File localPicture = new File(LOCAL_PICTURE);
//        if (!localPicture.exists()) {
//            localPicture.mkdirs();
//        }
//        File ota = new File(OTA_PATH);
//        if (!ota.exists()) {
//            ota.mkdirs();
//        }
//        File temp = new File(TEMP_PATH);
//        if (!temp.exists()) {
//            temp.mkdirs();
//        }
//        File cutvideo = new File(CUT_VIDEO_PATH);
//        if (!cutvideo.exists()) {
//            cutvideo.mkdirs();
//        }
//        File cache = new File(CACHE);
//        if (!cache.exists()) {
//            cache.mkdirs();
//        }
//        File calibrationFolder = new File(CALIBRATION_PATH);
//        if (!calibrationFolder.exists()) {
//            calibrationFolder.mkdirs();
//        }
//		new Thread(new Runnable() {
//			
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				//getLog();
//			}
//		}).start();
        //创建默认的ImageLoader配置参数
//        ImageLoaderConfiguration configuration = ImageLoaderConfiguration  
//                .createDefault(this);  
        File cacheDir = StorageUtils.getCacheDirectory(sInstance);  //缓存文件夹路径
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(sInstance)
                .memoryCacheExtraOptions(240, 400) // default = device screen dimensions 内存缓存文件的最大长宽
//		        .diskCacheExtraOptions(480, 800, null)  // 本地缓存的详细信息(缓存的最大长宽)，最好不要设置这个 
                .threadPoolSize(1) // default  线程池内加载的数量
                .threadPriority(Thread.NORM_PRIORITY - 2) // default 设置当前线程的优先级
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024)) //可以通过自己的内存缓存实现
                .memoryCacheSize(2 * 1024 * 1024)  // 内存缓存的最大值
                .memoryCacheSizePercentage(13) // default
                .diskCache(new UnlimitedDiskCache(cacheDir)) // default 可以自定义缓存路径
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb sd卡(本地)缓存的最大值
                .diskCacheFileCount(100)  // 可以缓存的文件数量
                // default为使用HASHCODE对UIL进行加密命名， 还可以用MD5(new Md5FileNameGenerator())加密
//		        .diskCacheFileNameGenerator(new HashCodeFileNameGenerator()) 
//		        .imageDownloader(new BaseImageDownloader(sInstance)) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
//		        .writeDebugLogs() // 打印debug log
                .build(); //开始构建
        //Initialize ImageLoaderParameter with configuration.
        ImageLoader.getInstance().init(config);

//
//        sMyApplication = this;
////        SDKInitializer.initialize(sMyApplication);
//        appName = PreferencesHelper.getSharedPreferences(getApplicationContext()).getString(KEY_ROOT_PATH_NAME, null);
//        PackageManager pm = this.getPackageManager();
//        if (TextUtils.isEmpty(appName)) {
//            appName = getApplicationInfo().loadLabel(pm).toString();
//        }
//        try {
//            appVersion = pm.getPackageInfo(getPackageName(), 0).versionCode;
//            appVersionName = pm.getPackageInfo(getPackageName(), 0).versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        deviceDesc = new DeviceDesc();
//        deviceSettingInfo = new DeviceSettingInfo();
    }

    private void copyFile(String source, String path) {
        Log.e(TAG, "copyFile: ");
        try {
            InputStream inputStream = getAssets().open(source);
            FileOutputStream outputStream = new FileOutputStream(path);
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLanguage() {
        Locale locale;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            LocaleList locales = getResources().getConfiguration().getLocales();
            locale = locales.get(0);
        } else {
            locale = getResources().getConfiguration().locale;
        }
        return locale.getLanguage() + "-" + locale.getCountry();
    }

    public void regToWeibo() {
        WeiboShareSDK.createWeiboAPI(this, Constants_weibo.WEIBO_APPKEY).registerApp();
    }

    /**
     * 注册应用到微信
     */
    public void regToWx() {
        WXAPIFactory.createWXAPI(this, Constants_wx.WX_APP_ID, true).registerApp(Constants_wx.WX_APP_ID);
    }

    private static final String LOG_FILE_NAME = LOG_PATH + "/crash_log_file.log";

    private void saveThrowable(Throwable ex) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(LOG_FILE_NAME);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ex);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Throwable getLastLog() {
        File file = new File(LOG_FILE_NAME);
        if (!file.exists()) {
            return null;
        }
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(file);
            ois = new ObjectInputStream(fis);
            Throwable ex = (Throwable) ois.readObject();
            file.delete();
            return ex;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 获取与摄像头连接类型
     * 0为摄像头起AP，1为手机端起AP
     *
     * @return
     */
    public int getCurrentNetModel() {
        SharedPreferences shareprefrence = PreferenceManager.getDefaultSharedPreferences(this);
        return shareprefrence.getInt("netmode", 0);
    }

    private void recordMessage() {
        // TODO Auto-generated method stub
        Thread.setDefaultUncaughtExceptionHandler(
                new UncaughtExceptionHandler() {

                    @Override
                    public void uncaughtException(Thread thread, Throwable ex) {
//                        MobclickAgent.reportError(sInstance, ex);
                        saveThrowable(ex);
                        StringWriter out = new StringWriter();
                        File root = new File(LOG_PATH);
                        if (!root.exists()) {
                            root.mkdirs();
                        }
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                        String filename = "error" + format.format(new Date(System.currentTimeMillis())) + ".txt";
                        File file = new File(LOG_PATH, filename);
                        PrintWriter err;
                        try {
                            FileOutputStream fos = new FileOutputStream(file);
                            err = new PrintWriter(out);
                            Field[] declaredFields = Build.class
                                    .getDeclaredFields();
                            String devicesMessage = "";
                            for (Field field : declaredFields) {
                                try {
                                    devicesMessage = field.getName() + ":"
                                            + field.get(null);
                                    out.append(devicesMessage + "\n");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            ex.printStackTrace(err);
                            fos.write(out.toString().getBytes());
                            fos.close();
                            err.close();
                            out.close();
                        }
                        // TODO Auto-generated method stub
                        // 保存异常到文件中
                        catch (IllegalArgumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        // 增强用户体验
//						Intent intent = getPackageManager()
//								.getLaunchIntentForPackage(getPackageName());
//						startActivity(intent);// 复活
                        // android.os.Process.killProcess(Process.myPid());// 自杀
//						android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(0);
                        try {
                            throw (ex);
                        } catch (Throwable e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        // 未捕获的异常处理
                        //System.out.println("程序挂了吧。。。" + ex);
                    }
                });
    }

    private void getLog() {
        // TODO Auto-generated method stub
        //System.out.println("--------func start--------"); // 方法启动

        ArrayList<String> cmdLine = new ArrayList<String>(); // 设置命令
        // -d 读取日志
        cmdLine.add("logcat");
        cmdLine.add("-d");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-HH-mm-ss");
        String time = dateFormatter.format(Calendar.getInstance().getTime());

        String fileTrace = APP_ROOT + time + "-hudTrace.txt";
        Log.e("VLCApplication", "getlog start,filename =" + fileTrace);
        try {
            Process process = Runtime.getRuntime().exec("logcat -f " + fileTrace + " *:E ");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
        } // 捕获日志

		/*
        try {
			ArrayList<String> cmdLine = new ArrayList<String>(); // 设置命令
			// -d 读取日志
			cmdLine.add("logcat");
			cmdLine.add("-d");
		
			ArrayList<String> clearLog = new ArrayList<String>(); // 设置命令
			// -c 清除日志
			clearLog.add("logcat");
			clearLog.add("-c");
			Process process = Runtime.getRuntime().exec(
					cmdLine.toArray(new String[cmdLine.size()])); // 捕获日志
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(process.getInputStream())); // 将捕获内容转换为BufferedReader
			//File file = new File(APP_ROOT, "GKDTrace1.txt");
			SimpleDateFormat dateFormatter=new SimpleDateFormat("MM-dd-HH-mm-ss");
			String time = dateFormatter.format(Calendar.getInstance().getTime());
			File FTraceFile = new File(APP_ROOT, time+"-hudTrace.txt");			
			
			FileOutputStream fos = new FileOutputStream(FTraceFile);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter bw = new BufferedWriter(osw);
			// Runtime.runFinalizersOnExit(true);
			String str = null;
			while ((str = bufferedReader.readLine()) != null) {// 开始读取日志，每次读取一行
				Runtime.getRuntime().exec(
						clearLog.toArray(new String[clearLog.size()])); // 清理日志...
				bw.write(str + "\n");
			}
			bw.close();
			osw.close();
			fos.close();
			bufferedReader.close();
			if (str == null) {
				System.out.println("--   is null   --");
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		*/
        ///System.out.println("--------func end--------");
        Log.e("VLCApplication", "getlog end");
    }

    public boolean getApisConnect() {
        return this.apisConnect;
    }

    public void setApisConnect(boolean c) {
        this.apisConnect = c;
    }

    public boolean getAllowDownloads() {
        return this.allowDownloads;
    }

    public void setAllowDownloads(boolean allowDownloads) {
        this.allowDownloads = allowDownloads;
    }

    public static Context getAppContext() {
        return sInstance;
    }

    /**
     * @return the main resources from the Application
     */
    public static Resources getAppResources() {
        return sInstance.getResources();
    }

    public static void runBackground(Runnable runnable) {
        sInstance.mThreadPool.execute(runnable);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

//    //#################
//    private static VLCApplication sMyApplication = null;
//    private Toast mToastShort, mToastLong;
//    private DeviceDesc deviceDesc;
//
//    private int appVersion;
//    private int searchMode = 0;
//    private String appName;
//    private String appVersionName;
//    private String UUID;
//    private boolean sdcardExist;
//    private boolean isUpgrading;
//    private boolean isAbnormalExitThread;
////    private Stack<BaseActivity> activityStack;
//
//    private DeviceSettingInfo deviceSettingInfo;
//    public static boolean isFactoryMode = false;
//    String KEY_ROOT_PATH_NAME = "key_root_path_name";
//
//    public static synchronized VLCApplication getApplication() {
//        return sMyApplication;
//    }
//
//    @Override
//    public void onLowMemory() {
//        super.onLowMemory();
//    }
//
//    @SuppressLint("ShowToast")
//    public void showToastShort(String info) {
//        if (mToastShort != null) {
//            mToastShort.setText(info);
//        } else {
//            mToastShort = Toast.makeText(this, info, Toast.LENGTH_SHORT);
////            mToastShort.setGravity(Gravity.CENTER, 0, 0);
//        }
//        mToastShort.show();
//    }
//
//    public void showToastShort(int info) {
//        showToastShort(getResources().getString(info));
//    }
//
//    @SuppressLint("ShowToast")
//    public void showToastLong(String msg) {
//        if (mToastLong != null) {
//            mToastLong.setText(msg);
//        } else {
//            mToastLong = Toast.makeText(this, msg, Toast.LENGTH_LONG);
//            mToastLong.setGravity(Gravity.CENTER, 0, 0);
//        }
//        mToastLong.show();
//    }
//
//    public void showToastLong(int msg) {
//        showToastLong(getResources().getString(msg));
//    }
//
//    public String getUUID() {
//        return UUID;
//    }
//
//    public void setUUID(String UUID) {
//        this.UUID = UUID;
//    }
//
//    public boolean isSdcardExist() {
//        return sdcardExist;
//    }
//
//    public void setSdcardExist(boolean sdcardExist) {
//        this.sdcardExist = sdcardExist;
//    }
//
//    public DeviceSettingInfo getDeviceSettingInfo() {
//        return deviceSettingInfo;
//    }
//
//    public String getAppName() {
//        return appName;
//    }
//
//    public int getAppVersion() {
//        return appVersion;
//    }
//
//    public String getAppVersionName() {
//        return appVersionName;
//    }
//
//    public void setDeviceDesc(DeviceDesc deviceDesc) {
//        if (deviceDesc != null) {
//            this.deviceDesc = deviceDesc;
//        }
//    }
//
//    public DeviceDesc getDeviceDesc() {
//        return deviceDesc;
//    }
//
//    public boolean isUpgrading() {
//        return isUpgrading;
//    }
//
//    public void setUpgrading(boolean upgrading) {
//        isUpgrading = upgrading;
//    }
//
//    public void sendCommandToService(int cmd) {
//        sendCommandToService(cmd, null);
//    }
//
//    public void sendCommandToService(int cmd, String ip) {
//        Intent intent = new Intent(getApplicationContext(), CommunicationService.class);
//        intent.putExtra(IConstant.SERVICE_CMD, cmd);
//        if (!TextUtils.isEmpty(ip)) {
//            intent.putExtra(IConstant.KEY_CONNECT_IP, ip);
//        }
//        getApplicationContext().startService(intent);
//    }
//
//    public void sendScreenCmdToService(int cmd) {
//        Intent intent = new Intent(this, ScreenShotService.class);
//        intent.putExtra(IConstant.SERVICE_CMD, cmd);
//        getApplicationContext().startService(intent);
//    }

//    public void pushActivity(BaseActivity baseActivity) {
//        if (activityStack == null)
//            activityStack = new Stack<>();
//        activityStack.add(baseActivity);
//        //  Dbug.e("activityStack", "add activity = " + baseActivity.toString());
//    }
//
//    public void popActivity(BaseActivity baseActivity) {
//        activityStack.remove(baseActivity);
//        // Dbug.e("activityStack", "remove activity = " + baseActivity.toString());
//    }
//
//    public BaseActivity getTopActivity() {
//        return activityStack.lastElement();
//    }
//
//    public void popAllActivity() {
//        for (BaseActivity activity : activityStack) {
//            if (activity != null) {
//                activity.finish();
//            }
//        }
//    }
//
//    public void popActivityOnlyMain() {
//        for (BaseActivity activity : activityStack) {
//            if (activity != null && !(activity instanceof MainActivity)) {
//                activity.finish();
//            }
//        }
//    }

//    public void setAppName(String appName) {
//        this.appName = appName;
//    }
//
//    public String getCameraDir() {
//        String dir;
//        if (deviceSettingInfo.getCameraType() == DeviceClient.CAMERA_REAR_VIEW) {
//            dir = DIR_REAR;
//        } else {
//            dir = DIR_FRONT;
//        }
//        return dir;
//    }
//
//    public boolean isAbnormalExitThread() {
//        return isAbnormalExitThread;
//    }
//
//    public void setAbnormalExitThread(boolean abnormalExitThread) {
//        isAbnormalExitThread = abnormalExitThread;
//    }
//
//    public int getSearchMode() {
//        return searchMode;
//    }
//
//    public void setSearchMode(int searchMode) {
//        this.searchMode = searchMode;
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        Dbug.w(getClass().getSimpleName(), "onConfigurationChanged orientation : " + newConfig.orientation);
//        Intent intent = new Intent(this, ScreenShotService.class);
//        intent.putExtra(IConstant.SERVICE_CMD, IConstant.SERVICE_CMD_SCREEN_CHANGE);
//        intent.putExtra(IConstant.SCREEN_ORIENTATION, newConfig.orientation);
//        getApplicationContext().startService(intent);
//    }
//
//    public void switchWifi() {
//        ClientManager.getClient().close();
//        sendScreenCmdToService(SERVICE_CMD_CLOSE_SCREEN_TASK);
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                WifiHelper.getInstance(sMyApplication).removeCurrentNetwork(sMyApplication);
//            }
//        }, 1000);
//    }
}
