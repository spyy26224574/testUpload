package com.adai.gkd.httputils;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.UIUtils;
import com.android.internal.http.multipart.FilePart;
import com.android.internal.http.multipart.MultipartEntity;
import com.android.internal.http.multipart.Part;
import com.android.internal.http.multipart.StringPart;
import com.google.gson.Gson;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.params.CoreConnectionPNames;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;


public class HttpUtil {

    public interface Callback<T extends BasePageBean> {
        public void onCallback(T result);
    }

    private static HttpUtil _instance;
    private ThreadPoolExecutor mExecutors = new ThreadPoolExecutor(5, 25, 200, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(20));

    private HttpUtil() {

    }

    public static HttpUtil getInstance() {
        if (_instance == null) {
            _instance = new HttpUtil();
        }
        return _instance;
    }

    public <T extends BasePageBean> void requestPost(String url, Object params, Class<T> classz, Callback<T> callback) {
        MyRequst<T> postreqest = new MyRequst<T>(url, params, callback, classz, "POST", true);
        try {
            mExecutors.execute(postreqest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T extends BasePageBean> void requestGet(String url, Object params, Class<T> classz, Callback<T> callback) {
        MyRequst<T> postreqest = new MyRequst<T>(url, params, callback, classz, "GET", true);
        try {
            mExecutors.execute(postreqest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T extends BasePageBean> void requestDelete(String url, Object params, Class<T> classz, Callback<T> callback) {
        MyDeleteRequest<T> postreqest = new MyDeleteRequest<T>(url, params, callback, classz);
        try {
            mExecutors.execute(postreqest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public <T extends BasePageBean> void requestPut(String url, Object params, Class<T> classz, Callback<T> callback) {
        MyRequst<T> postreqest = new MyRequst<T>(url, params, callback, classz, "PUT", true);
        try {
            mExecutors.execute(postreqest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T extends BasePageBean> void requestPostWithoutContenttype(String url, Object params, Class<T> classz, Callback<T> callback) {
        MyRequst<T> postreqest = new MyRequst<T>(url, params, callback, classz, "POST", false);
        try {
            mExecutors.execute(postreqest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T extends BasePageBean> void requestGetWithoutContenttype(String url, Object params, Class<T> classz, Callback<T> callback) {
        MyRequst<T> postreqest = new MyRequst<T>(url, params, callback, classz, "GET", false);
        try {
            mExecutors.execute(postreqest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T extends BasePageBean> void requestDeleteWithoutContenttype(String url, Object params, Class<T> classz, Callback<T> callback) {
        MyRequst<T> postreqest = new MyRequst<T>(url, params, callback, classz, "DELETE", false);
        try {
            mExecutors.execute(postreqest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public <T extends BasePageBean> void requestPostWithFile(String url, Object params, Class<T> calssz, Callback<T> callback) {
//		MyFileReqeust<T> postFilerequest=new MyFileReqeust<T>(url, params, callback, calssz);
//		mExecutors.execute(postFilerequest);
        requestPostWithFile(url, params, calssz, callback, null);
    }

    public <T extends BasePageBean> void requestPostWithFile(String url, Object params, Class<T> calssz, Callback<T> callback, UploadCallback uploadCallback) {
        MyFileReqeust<T> postFilerequest = new MyFileReqeust<T>(url, params, callback, calssz);
        postFilerequest.setUploadCallback(uploadCallback);
        try {
            mExecutors.execute(postFilerequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadFile(String url, String localpath, DownloadCallback callback) {
        DownloadFile down = new DownloadFile(url, localpath, callback);
        try {
            mExecutors.execute(down);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean testDNS(String hostname) {
        try {
            GetDnsTask getDnsTask = new GetDnsTask(hostname);
            Thread thread = new Thread(getDnsTask);
            thread.start();
            thread.join(2000);
            return getDnsTask.getInetAddress() != null;
        } catch (Exception e) {
            return false;
        }
    }

    public static class GetDnsTask implements Runnable {
        private InetAddress mInetAddress;
        private String mHostname;

        public InetAddress getInetAddress() {
            return mInetAddress;
        }

        void setInetAddress(InetAddress inetAddress) {
            mInetAddress = inetAddress;
        }

        public GetDnsTask(String hostname) {
            mHostname = hostname;
        }

        @Override
        public void run() {
            try {
                InetAddress inetAddress = InetAddress.getByName(mHostname);
                setInetAddress(inetAddress);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }

    class MyRequst<T extends BasePageBean> implements Runnable {

        private Callback<T> _callback;
        private Class<T> _classz;
        private Object _params;
        private String _url;
        private String _requestType;
        /**
         * 是否需要指定content-type
         */
        private boolean _needContenttype = true;
        T ret = null;
        private Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 0:
                        if (_callback != null) {
                            _callback.onCallback(ret);
                        }
                        break;

                    default:
                        break;
                }
            }

            ;
        };

        public MyRequst(String url, Object params, Callback<T> callback, Class<T> classz, String requesttype, boolean needcontenttype) {
            this._callback = callback;
            this._url = url;
            this._classz = classz;
            this._params = params;
            this._requestType = requesttype;
            this._needContenttype = needcontenttype;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                ret = _classz.newInstance();
                StringBuffer content = new StringBuffer();
                if (_params != null) {
                    if ("GET".equals(_requestType)) {
                        Field[] fields = _params.getClass().getFields();
                        for (Field field : fields) {
                            content.append(field.getName()).append("=").append(field.get(_params)).append("&");
                        }
                        if (!TextUtils.isEmpty(CurrentUserInfo.access_token)) {
                            content.append("access_token=").append(CurrentUserInfo.access_token);
                        }
                    } else {
                        Map<String, Object> map = new HashMap<String, Object>();
                        Field[] fields = _params.getClass().getFields();
                        for (Field field : fields) {
                            map.put(field.getName(), field.get(_params));
                        }
                        map.put("access_token", CurrentUserInfo.access_token);
                        content.append(new Gson().toJson(map));
                    }
                }
//				if(CurrentUserInfo.access_token!=null)
//				content.append("access_token=").append(CurrentUserInfo.access_token);
                if ("GET".equals(_requestType) && !TextUtils.isEmpty(content)) {
                    _url = _url + "?" + content;
                }
                Log.e("HttpUtil", "run: requestUrl = " + _url);
                URL url = new URL(_url);
                boolean dnsActive = testDNS(url.getHost());
                if (!dnsActive) {
                    ret.ret = -1;
                    ret.message = UIUtils.getString(R.string.Abnormal_request);
                    handler.sendEmptyMessage(0);
                    return;
                }
                HttpURLConnection conn;
                if (_url.startsWith("https")) {
                    conn = (HttpsURLConnection) url.openConnection();
                } else {
                    conn = (HttpURLConnection) url.openConnection();
                }
                conn.setRequestMethod(_requestType);
                conn.setDoInput(true);
                if ("POST".equals(_requestType)) {
                    conn.setDoOutput(true);
                }
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setUseCaches(false);
//				conn.setRequestProperty("connection", "Keep-Alive");
                conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)");
                conn.setRequestProperty("Charsert", "UTF-8");
                if ("DELETE".equals(_requestType))
                    conn.setRequestProperty("Header", content.toString());
                if (_needContenttype)
                    conn.setRequestProperty("Content-Type", "application/json");
                conn.connect();
                if ("POST".equals(_requestType) || "PUT".equals(_requestType)) {
//					DataOutputStream out = new DataOutputStream(conn.getOutputStream());
//					String data=new String(content.toString().getBytes("UTF-8"));
                    OutputStream out = conn.getOutputStream();
                    byte[] buffer = content.toString().getBytes("UTF-8");
                    out.write(buffer);
                    out.flush();
                    out.close();
                }

                int responsecode = conn.getResponseCode();
                if (responsecode == 200) {
                    InputStream is = conn.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is, "UTF-8");
//				ret=new Gson().fromJson(isr, _classz);
                    BufferedReader br = new BufferedReader(isr);
                    StringBuffer result = new StringBuffer();
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }
                    ret = new Gson().fromJson(result.toString(), _classz);
                    conn.getInputStream().close();
                    conn.disconnect();
                } else {
                    ret.ret = responsecode;
                    ret.message = UIUtils.getString(R.string.network_timeout);
                    conn.disconnect();
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                if (ret == null) {
                    try {
                        ret = _classz.newInstance();
                    } catch (InstantiationException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (IllegalAccessException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                if (ret != null) {
                    ret.ret = -1;
                    ret.message = UIUtils.getString(R.string.network_timeout);
                }
            }
            handler.sendEmptyMessage(0);
        }

    }

    class MyDeleteRequest<T extends BasePageBean> implements Runnable {

        private Callback<T> _callback;
        private Class<T> _classz;
        private Object _params;
        private String _url;
        T ret = null;
        private Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 0:
                        if (_callback != null) {
                            _callback.onCallback(ret);
                        }
                        break;

                    default:
                        break;
                }
            }

            ;
        };

        public MyDeleteRequest(String url, Object params, Callback<T> callback, Class<T> classz) {
            // TODO Auto-generated constructor stub
            this._callback = callback;
            this._url = url;
            this._classz = classz;
            this._params = params;
        }

        @Override
        public void run() {
            try {
                ret = _classz.newInstance();
                HttpClient httpClient = HttpClientUtil.getNewHttpClient();
                HttpEntityEnclosingRequestBase httpDelete = new HttpEntityEnclosingRequestBase() {
                    @Override
                    public String getMethod() {
                        return "DELETE";
                    }
                };
                boolean dnsActive = testDNS(new URL(_url).getHost());
                if (!dnsActive) {
                    ret.ret = -1;
                    ret.message = UIUtils.getString(R.string.Abnormal_request);
                    handler.sendEmptyMessage(0);
                    return;
                }
                httpDelete.setURI(URI.create(_url));
                StringBuffer content = new StringBuffer();
                Map<String, Object> map = new HashMap<String, Object>();
                Field[] fields = _params.getClass().getFields();
                for (Field field : fields) {
                    map.put(field.getName(), field.get(_params));
                }
                map.put("access_token", CurrentUserInfo.access_token);
                content.append(new Gson().toJson(map));
                httpDelete.setEntity(new StringEntity(content.toString()));
                httpDelete.setHeader("Content-Type", "application/json; charset=UTF-8");
                httpDelete.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
                httpDelete.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
                HttpResponse response = httpClient.execute(httpDelete);
                int status = response.getStatusLine().getStatusCode();
                if (status == HttpStatus.SC_OK) {
                    InputStream is = response.getEntity().getContent();
                    ;
                    InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                    BufferedReader br = new BufferedReader(isr);
                    StringBuffer result = new StringBuffer();
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }
                    ret = new Gson().fromJson(result.toString(), _classz);
                } else {
                    ret.ret = status;
                    ret.message = response.getStatusLine().getReasonPhrase();
                }
            } catch (Exception e) {
                e.printStackTrace();
                if (ret == null) {
                    try {
                        ret = _classz.newInstance();
                    } catch (InstantiationException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (IllegalAccessException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                if (ret != null) {
                    ret.ret = -1;
                    ret.message = UIUtils.getString(R.string.Abnormal_request);
                }
            }
            handler.sendEmptyMessage(0);
        }
    }

    class MyFileReqeust<T extends BasePageBean> implements Runnable {

        private Callback<T> _callback;
        private Class<T> _classz;
        private Object _params;
        private String _url;
        private long totalSize = 0;
        private UploadCallback uploadCallback;
        T ret = null;
        private Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case 0:
                        if (_callback != null) {
                            _callback.onCallback(ret);
                        }
                        break;
                    case 1:
                        if (uploadCallback != null) {
                            uploadCallback.onUploading(msg.arg1);
                        }
                        break;

                    default:
                        break;
                }
            }
        };

        public void setUploadCallback(UploadCallback uploadCallback) {
            this.uploadCallback = uploadCallback;
        }

        public MyFileReqeust(String url, Object params, Callback<T> callback, Class<T> classz) {
            // TODO Auto-generated constructor stub
            this._callback = callback;
            this._url = url;
            this._classz = classz;
            this._params = params;
        }

        @Override
        public void run() {
            // TODO Auto-generated method stub
            try {
                ret = _classz.newInstance();
                boolean dnsActive = testDNS(new URL(_url).getHost());
                if (!dnsActive) {
                    ret.ret = -1;
                    ret.message = UIUtils.getString(R.string.Abnormal_request);
                    handler.sendEmptyMessage(0);
                    return;
                }
                HttpClient httpClient = HttpClientUtil.getNewHttpClient();
                HttpPost post = new HttpPost(_url);
                List<Part> parts = new ArrayList<>();

                if (_params != null) {
                    Field[] fields = _params.getClass().getFields();
                    for (Field field : fields) {
                        if (field.get(_params) != null) {
                            if (field.getType().equals(File.class)) {
                                parts.add(new FilePart(field.getName(), (File) field.get(_params)));
                            } else if (field.getType().equals(List.class)) {
                                List<Object> value = (List<Object>) field.get(_params);
                                for (int i = 0; i < value.size(); i++) {
                                    Object t = value.get(i);
                                    if (t instanceof File) {
                                        parts.add(new FilePart(field.getName() + i, (File) t));
                                    } else {
                                        parts.add(new StringPart(field.getName() + i, t.toString(), "UTF-8"));
                                    }
                                }
                            } else {
                                parts.add(new StringPart(field.getName(), field.get(_params).toString(), "UTF-8"));
                            }
                        }
                    }
                }
                if (CurrentUserInfo.access_token != null) {
                    parts.add(new StringPart("access_token", CurrentUserInfo.access_token, "UTF-8"));
                }
                MultipartEntity entity = new MultipartEntity(parts.toArray(new Part[parts.size()]));
                totalSize = entity.getContentLength();
                ProgressOutHttpEntity progressEntity = new ProgressOutHttpEntity(entity, new ProgressListener() {
                    @Override
                    public void transferred(long transferedBytes) {
                        int progress = (int) (100 * transferedBytes / totalSize);
                        Log.e(this.getClass().getName(), "progress=" + progress);
                        Message msg = handler.obtainMessage();
                        msg.what = 1;
                        msg.arg1 = progress;
                        handler.sendMessage(msg);
                    }
                });
                post.setEntity(progressEntity);
                HttpResponse respose = httpClient.execute(post);
                int status = respose.getStatusLine().getStatusCode();
                if (status == HttpStatus.SC_OK) {
                    InputStream is = respose.getEntity().getContent();
                    InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                    BufferedReader br = new BufferedReader(isr);
                    StringBuffer result = new StringBuffer();
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }
                    if (result.toString().startsWith("<?xml")) {
                        ret.ret = -1;
                        ret.message = UIUtils.getString(R.string.transmission_failure);
                        InputStream reader = new ByteArrayInputStream(result.toString().getBytes());
                        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                        XmlPullParser parser = factory.newPullParser();
                        parser.setInput(reader, "UTF-8");
                        int entype = parser.getEventType();
                        while (entype != XmlPullParser.END_DOCUMENT) {
                            switch (entype) {
                                case XmlPullParser.START_TAG:
                                    String name = parser.getName();
                                    if ("Status".equals(name)) {
                                        String text = parser.nextText();
                                        if ("0".equals(text)) {
                                            ret.ret = 0;
                                            ret.message = UIUtils.getString(R.string.file_transfer_success);
                                        }
                                    }
                                    break;

                                default:

                                    break;
                            }
                            entype = parser.next();
                        }
                    } else {
                        ret = new Gson().fromJson(result.toString(), _classz);
                    }
                } else if (status == 500) {
                    if (ret != null) {
                        ret.ret = -1;
                        ret.message = UIUtils.getString(R.string.server_exception);
                    }
                } else {
                    if (ret != null) {
                        ret.ret = -1;
                        ret.message = UIUtils.getString(R.string.network_timeout);
                    }
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                if (ret == null) {
                    try {
                        ret = _classz.newInstance();
                    } catch (InstantiationException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    } catch (IllegalAccessException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
                if (ret != null) {
                    ret.ret = -1;
                    ret.message = UIUtils.getString(R.string.network_timeout);
                }
            }
            handler.sendEmptyMessage(0);
        }

    }

    public interface DownloadCallback {

        void onDownloadComplete(String path);

        void onDownloading(int progress);

        void onDownladFail();
    }

    public interface UploadCallback {
        void onUploadComplete();

        void onUploading(int progress);

        void onUploadFail();
    }

    class DownloadFile implements Runnable {

        private String _url;
        private String _localPath;
        private DownloadCallback callback;

        public DownloadFile(String url, String localPath, DownloadCallback callback) {
            this._localPath = localPath;
            this._url = url;
            this.callback = callback;
        }

        private Handler handler = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (callback == null) return;
                switch (msg.what) {
                    case 0:
                        callback.onDownloadComplete(_localPath);
                        break;
                    case 1:
                        int progress = msg.arg1;
                        callback.onDownloading(progress);
                        break;
                    case 2:
                        callback.onDownladFail();
                        break;

                    default:
                        break;
                }
            }

            ;
        };

        @Override
        public void run() {
            // TODO Auto-generated method stub
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                URL url = new URL(_url);
                boolean dnsActive = testDNS(url.getHost());
                if (!dnsActive) {
                    handler.sendEmptyMessage(2);
                    return;
                }
                HttpURLConnection conn;
                if (_url.startsWith("https")) {
                    conn = (HttpsURLConnection) url.openConnection();
                } else {
                    conn = (HttpURLConnection) url.openConnection();
                }
                conn.setDoInput(true);
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setUseCaches(false);
                File local = new File(_localPath);
                if (local.exists()) {
                    if (local.length() == conn.getContentLength()) {
                        handler.sendEmptyMessage(0);
                        return;
                    }
                }
                is = conn.getInputStream();
                fos = new FileOutputStream(_localPath);
                int length = conn.getContentLength();
                byte[] buffer = new byte[2048];
                int len = 0;
                int download = 0;
                int percent = 0;
                while ((len = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                    download += len;
                    int progress = (download * 100) / length;
                    percent += len;
                    if (percent * 100 / length >= 1) {
                        Message msg = handler.obtainMessage();
                        msg.what = 1;
                        msg.arg1 = progress;
                        handler.sendMessage(msg);
                        percent = 0;
                    }
                }
                if (_localPath.endsWith(".temp")) {
                    File temp = new File(_localPath);
                    _localPath = _localPath.substring(0, _localPath.length() - 5);
                    temp.renameTo(new File(_localPath));
                }
                handler.sendEmptyMessage(0);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                File local = new File(_localPath);
                local.deleteOnExit();
                handler.sendEmptyMessage(2);
            } finally {
                try {
                    if (fos != null) {
                        fos.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }
}
