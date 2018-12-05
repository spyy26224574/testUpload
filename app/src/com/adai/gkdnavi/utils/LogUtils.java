package com.adai.gkdnavi.utils;

import android.text.TextUtils;
import android.util.Log;

import com.adai.gkdnavi.BuildConfig;

import java.util.List;
import java.util.Locale;

public class LogUtils {

    /**
     * 关闭日志
     */
    public static final int LEVEL_NONE = 6;
    /**
     * 日志输出级别V
     */
    public static final int LEVEL_VERBOSE = 1;
    /**
     * 日志输出级别D
     */
    public static final int LEVEL_DEBUG = 2;
    /**
     * 日志输出级别I
     */
    public static final int LEVEL_INFO = 3;
    /**
     * 日志输出级别W
     */
    public static final int LEVEL_WARN = 4;
    /**
     * 日志输出级别E
     */
    public static final int LEVEL_ERROR = 5;

    /**
     * 日志输出时的TAG,用来过滤log用
     */
    private static String mTag = "LogUtils:";
    /**
     * 是否允许输出log
     */
    private static int mDebuggable = BuildConfig.DEBUG ? LEVEL_INFO : LEVEL_NONE;

    /**
     * 用于记时的变量
     */
    private static long mTimestamp = 0;
    /**
     * 写文件的锁对象
     */
    private static final Object mLogLock = new Object();

    public static String customTagPrefix = "";

    private LogUtils() {
    }

    private static String generateTag(StackTraceElement caller) {
        String tag = "%s.%s(L:%d)";
//        String callerClazzName = caller.getClassName();
//        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        String fileName = caller.getFileName();
        fileName = fileName.substring(0, fileName.lastIndexOf("."));
        tag = String.format(Locale.ENGLISH, tag, fileName, caller.getMethodName(), caller.getLineNumber());
        tag = TextUtils.isEmpty(customTagPrefix) ? tag : customTagPrefix + ":" + tag;
        return tag;
    }

    public static CustomLogger customLogger;


    public interface CustomLogger {
        void d(String tag, String content);

        void d(String tag, String content, Throwable tr);

        void e(String tag, String content);

        void e(String tag, String content, Throwable tr);

        void i(String tag, String content);

        void i(String tag, String content, Throwable tr);

        void v(String tag, String content);

        void v(String tag, String content, Throwable tr);

        void w(String tag, String content);

        void w(String tag, String content, Throwable tr);

        void w(String tag, Throwable tr);

    }

    public static void d(String content) {
        if (mDebuggable > LEVEL_DEBUG) return;
        StackTraceElement caller = getStackTraceElement();
        String tag = generateTag(caller);

        if (customLogger != null) {
            customLogger.d(tag, content);
        } else {
            Log.d(tag, content);
        }
    }

    public static void d(String content, Throwable tr) {

        if (mDebuggable > LEVEL_DEBUG) return;
        StackTraceElement caller = getStackTraceElement();
        String tag = mTag + generateTag(caller);

        if (customLogger != null) {
            customLogger.d(tag, content, tr);
        } else {
            Log.d(tag, content, tr);
        }

    }

    public static void e(String content) {
        if (mDebuggable > LEVEL_ERROR) return;
        StackTraceElement caller = getStackTraceElement();
        String tag = mTag + generateTag(caller);

        if (customLogger != null) {
            customLogger.e(tag, content);
        } else {
            Log.e(tag, content);
        }
    }

    public static void e(Throwable tr) {
        if (mDebuggable > LEVEL_ERROR) return;
        StackTraceElement caller = getStackTraceElement();
        String tag = mTag + generateTag(caller);
        if (customLogger != null) {
            customLogger.e(tag, "", tr);
        } else {
            Log.e(tag, "", tr);
        }
    }

    public static void e(String content, Throwable tr) {
        if (mDebuggable > LEVEL_ERROR) return;
        StackTraceElement caller = getStackTraceElement();
        String tag = mTag + generateTag(caller);

        if (customLogger != null) {
            customLogger.e(tag, content, tr);
        } else {
            Log.e(tag, content, tr);
        }
    }

    public static void i(String content) {
        if (mDebuggable > LEVEL_INFO) return;
        StackTraceElement caller = getStackTraceElement();
        String tag = mTag + generateTag(caller);

        if (customLogger != null) {
            customLogger.i(tag, content);
        } else {
            Log.i(tag, content);
        }
    }

    public static void i(String content, Throwable tr) {
        if (mDebuggable > LEVEL_INFO) return;
        StackTraceElement caller = getStackTraceElement();
        String tag = mTag + generateTag(caller);

        if (customLogger != null) {
            customLogger.i(tag, content, tr);
        } else {
            Log.i(tag, content, tr);
        }
    }

    public static void v(String content) {
        if (mDebuggable > LEVEL_VERBOSE) return;
        StackTraceElement caller = getStackTraceElement();
        String tag = mTag + generateTag(caller);

        if (customLogger != null) {
            customLogger.v(tag, content);
        } else {
            Log.v(tag, content);
        }
    }

    public static void v(String content, Throwable tr) {
        if (mDebuggable > LEVEL_VERBOSE) return;
        StackTraceElement caller = getStackTraceElement();
        String tag = mTag + generateTag(caller);

        if (customLogger != null) {
            customLogger.v(tag, content, tr);
        } else {
            Log.v(tag, content, tr);
        }
    }

    public static void w(String content) {
        if (mDebuggable > LEVEL_WARN) return;
        StackTraceElement caller = getStackTraceElement();
        String tag = mTag + generateTag(caller);

        if (customLogger != null) {
            customLogger.w(tag, content);
        } else {
            Log.w(tag, content);
        }
    }

    public static void w(String content, Throwable tr) {
        if (mDebuggable > LEVEL_WARN) return;
        StackTraceElement caller = getStackTraceElement();
        String tag = mTag + generateTag(caller);

        if (customLogger != null) {
            customLogger.w(tag, content, tr);
        } else {
            Log.w(tag, content, tr);
        }
    }

    public static void w(Throwable tr) {
        if (mDebuggable > LEVEL_WARN) return;
        StackTraceElement caller = getStackTraceElement();
        String tag = mTag + generateTag(caller);

        if (customLogger != null) {
            customLogger.w(tag, tr);
        } else {
            Log.w(tag, tr);
        }
    }

    /**
     * 把Log存储到文件中
     *
     * @param log  需要存储的日志
     * @param path 存储路径
     */
    public static void log2File(String log, String path) {
        log2File(log, path, true);
    }

    public static void log2File(String log, String path, boolean append) {
        synchronized (mLogLock) {
            FileUtils.writeFile(log + "\r\n", path, append);
        }
    }

    /**
     * 以级别为 e 的形式输出msg信息,附带时间戳，用于输出一个时间段起始点
     *
     * @param msg 需要输出的msg
     */
    public static void msgStartTime(String msg) {
        mTimestamp = System.currentTimeMillis();
        if (!TextUtils.isEmpty(msg)) {
            e("[Started：" + mTimestamp + "]" + msg);
        }
    }

    /**
     * 以级别为 e 的形式输出msg信息,附带时间戳，用于输出一个时间段结束点* @param msg 需要输出的msg
     */
    public static void elapsed(String msg) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - mTimestamp;
        mTimestamp = currentTime;
        e("[Elapsed：" + elapsedTime + "]" + msg);
    }

    public static <T> void printList(List<T> list) {
        if (list == null || list.size() < 1) {
            return;
        }
        int size = list.size();
        i("---begin---");
        for (int i = 0; i < size; i++) {
            i(i + ":" + list.get(i).toString());
        }
        i("---end---");
    }

    public static <T> void printArray(T[] array) {
        if (array == null || array.length < 1) {
            return;
        }
        int length = array.length;
        i("---begin---");
        for (int i = 0; i < length; i++) {
            i(i + ":" + array[i].toString());
        }
        i("---end---");
    }

    private static StackTraceElement getStackTraceElement() {
        StackTraceElement targetStackTrace = null;
        boolean shouldTrace = false;
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            boolean isLogMethod = stackTraceElement.getClassName().equals(LogUtils.class.getName());
            if (shouldTrace && !isLogMethod) {
                targetStackTrace = stackTraceElement;
                break;
            }
            shouldTrace = isLogMethod;
        }
        return targetStackTrace;
    }

}
