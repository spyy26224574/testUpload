package com.oss.utils;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.adai.gkdnavi.utils.SpUtils;
import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.example.ipcamera.application.VLCApplication;
import com.oss.bean.OssUploadParam;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by huangxy on 2017/6/5 10:57.
 */

public class OSSRequestUtil {
    private OSSClient oss;
    private String mBucketName;
    private String preObjectKey;
    private static final String accessKeyId = "LTAI8detyn4qC5Ls";
    private static final String accessKeySecret = "ktP1zCgqzNfyJ3c7XbgWS19sD8mCsQ ";
    private final OSSCredentialProvider mCredentialProvider;
    private final ClientConfiguration mConf;
    private ThreadPoolExecutor mExecutors = new ThreadPoolExecutor(5, 25, 200, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(20));
    //    private static final String ossUrl = "http://ligohd-test.oss-cn-shanghai.aliyuncs.com/cardv/pub/";//测试
    private static final String ossUrl = "http://ligohd2.oss-cn-shanghai.aliyuncs.com/cardv/pub/";//正式

    private OSSRequestUtil() {
        mCredentialProvider = new OSSPlainTextAKSKCredentialProvider(accessKeyId, accessKeySecret);
        mConf = new ClientConfiguration();
        mConf.setConnectionTimeout(10 * 1000); // 连接超时，默认15秒
        mConf.setSocketTimeout(10 * 1000); // socket超时，默认15秒
        mConf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        mConf.setMaxErrorRetry(3); // 失败后最大重试次数，默认2次
        String oss_address = SpUtils.getString(VLCApplication.getAppContext(), "oss_address", ossUrl);
        OSSLog.enableLog();
        if (TextUtils.isEmpty(oss_address)) {
            return;
        }
        String endpoint = "";
        try {
            String tempString = oss_address.replace("//", "/");
            String[] strings = tempString.split("/");
            mBucketName = strings[1].substring(0, strings[1].indexOf("."));
            endpoint = strings[1].substring(strings[1].indexOf(".") + 1);
            preObjectKey = strings[2] + "/" + strings[3];
        } catch (Exception ignore) {
        }
        oss = new OSSClient(VLCApplication.getAppContext(), endpoint, mCredentialProvider, mConf);
    }

    private static OSSRequestUtil INSTANCE;

    public static OSSRequestUtil getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new OSSRequestUtil();
        }
        return INSTANCE;
    }

    public interface MultiUploadCallBack {
        void onUploadComplete(List<String> objectKey);

        void onUploading(int progress);

        void onUploadFail();
    }

    /**
     * 只调用一次！
     *
     * @param ossAddress
     */
    public void setOssAddress(String ossAddress) {
        String endpoint = "";
        try {
            String tempString = ossAddress.replace("//", "/");
            String[] strings = tempString.split("/");
            mBucketName = strings[1].substring(0, strings[1].indexOf("."));
            endpoint = strings[1].substring(strings[1].indexOf(".") + 1);
            preObjectKey = strings[2] + "/" + strings[3];
        } catch (Exception ignore) {

        }
        oss = new OSSClient(VLCApplication.getAppContext(), endpoint, mCredentialProvider, mConf);
    }

    public void postFile2OSS(@NonNull List<OssUploadParam> ossUploadParams, MultiUploadCallBack multiUploadCallBack) {
        FileUploadRequest fileUploadRequest = new FileUploadRequest(ossUploadParams, multiUploadCallBack);
        try {
            mExecutors.execute(fileUploadRequest);
        } catch (Exception ignore) {
        }
    }

    private class FileUploadRequest implements Runnable {
        private static final int UPLOAD_SUCCESS = 1;
        private static final int UPLOAD_FAILED = 2;
        private static final int UPLOAD_PROGRESS = 3;
        private Handler mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case UPLOAD_FAILED:
                        mMultiUploadCallBack.onUploadFail();
                        break;
                    case UPLOAD_PROGRESS:
                        int progress = (int) msg.obj;
                        mMultiUploadCallBack.onUploading(progress);
                        break;
                    case UPLOAD_SUCCESS:
                        ArrayList<String> paths = (ArrayList<String>) msg.obj;
                        mMultiUploadCallBack.onUploadComplete(paths);
                        break;
                }
            }
        };
        private final List<OssUploadParam> mOssUploadParams;
        private final MultiUploadCallBack mMultiUploadCallBack;
        private long mTotalSize;
        private long mCurrentSize;
        private long mLastFileUploadSize = 0;
        private int mCurrentCount;
        private int mTotalCount;
        private int mCurrentProgress;
        private List<String> mPartObjects = new ArrayList<>();

        FileUploadRequest(@NonNull List<OssUploadParam> ossUploadParams, MultiUploadCallBack multiUploadCallBack) {
            mOssUploadParams = ossUploadParams;
            mMultiUploadCallBack = multiUploadCallBack;
            mTotalCount = ossUploadParams.size();
            for (OssUploadParam ossUploadParam : ossUploadParams) {
                mTotalSize += ossUploadParam.fileSize;
            }
        }

        @Override
        public void run() {
            if (oss == null) {
                mHandler.sendEmptyMessage(UPLOAD_FAILED);
            } else {
                for (OssUploadParam ossUploadParam : mOssUploadParams) {
                    String objectKey = preObjectKey + "/" + ossUploadParam.partObject;
                    PutObjectRequest request = new PutObjectRequest(mBucketName, objectKey, ossUploadParam.uploadFilePath);
                    Log.e("9527", "mBucketName = " + mBucketName);
                    Log.e("9527", "objectKey = " + objectKey);
                    Log.e("9527", " ossUploadParam.uploadFilePath = " + ossUploadParam.uploadFilePath);
                    request.setProgressCallback(new OSSProgressCallback<PutObjectRequest>() {
                        @Override
                        public void onProgress(PutObjectRequest request, long currentSize, long totalSize) {
//                            Log.e("9527", "onProgress totalSize = " + totalSize);
//                            Log.e("9527", "onProgress currentSize = " + currentSize);
                            mCurrentSize = mLastFileUploadSize + currentSize;
                            int progress = (int) (100 * mCurrentSize / mTotalSize);
                            if (progress >= mCurrentProgress + 1) {
                                mCurrentProgress = progress;
                                Message message = mHandler.obtainMessage(UPLOAD_PROGRESS);
                                message.obj = progress;
                                mHandler.sendMessage(message);
                            }
                        }
                    });
                    try {
                        Log.e("9527", "mLastFileUploadSize = " + mLastFileUploadSize);
                        Log.e("9527", "mCurrentSize = " + mCurrentSize);
                        Log.e("9527", "mTotalSize = " + mTotalSize);
                        oss.putObject(request);
                        mLastFileUploadSize += new File(request.getUploadFilePath()).length();
                        mCurrentCount++;
                        mPartObjects.add(ossUploadParam.partObject);
                        Message message = mHandler.obtainMessage(UPLOAD_PROGRESS);
                        message.obj = (int) (100 * mCurrentSize / mTotalSize);
                        mHandler.sendMessage(message);
                        if (mCurrentCount == mTotalCount) {
                            Message message1 = mHandler.obtainMessage(UPLOAD_SUCCESS);
                            message1.obj = mPartObjects;
                            mHandler.sendMessage(message1);
                        }
                    } catch (ClientException | ServiceException e) {
                        e.printStackTrace();
                        mHandler.sendEmptyMessage(UPLOAD_FAILED);
                        break;
                    }
                }
            }
        }
    }
}
