package com.adai.camera.sunplus.tool;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.LruCache;
import android.widget.ImageView;

import com.adai.camera.sunplus.SDKAPI.FileOperation;
import com.adai.gkdnavi.utils.ThreadPoolManager;
import com.adai.gkdnavi.utils.ThreadPoolProxy;
import com.adai.gkdnavi.utils.UIUtils;
import com.icatch.wificam.customer.type.ICatchFile;
import com.icatch.wificam.customer.type.ICatchFrameBuffer;

import java.io.File;

/**
 * Created by huangxy on 2017/4/11 17:43.
 */

public class SunplusImageLoadManager {
    private ThreadPoolProxy mPool;
    private LruCache<String, Bitmap> mMemoryCache;
    private DiskLruCache mDiskLruCache;
    private static SunplusImageLoadManager mInstance;
    private static final String DISK_CACHE_SUBDIR = "thumbnails";
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 20; // 20MB
    private static final int MSG_LOAD_COMPLETE = 1;

    public static SunplusImageLoadManager getInstance() {
        if (mInstance == null) {
            synchronized (SunplusImageLoadManager.class) {
                if (mInstance == null) {
                    mInstance = new SunplusImageLoadManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 存放图片信息
     */
    private class ImageRef {

        /**
         * 图片对应ImageView控件
         */
        ImageView mImageView;
        /**
         *
         */
        ICatchFile mICatchFile;
        /**
         * 默认图资源ID
         */
        int mPlaceHolder;

        /**
         * 图片数据
         */
        Bitmap mBitmap;

        /**
         * 是否加载的缩略图
         */
        boolean isThumbnail;

        ImageRef(ImageView imageView, @NonNull ICatchFile iCatchFile, @DrawableRes int placeHolder, boolean isThumbnail) {
            mImageView = imageView;
            mICatchFile = iCatchFile;
            mPlaceHolder = placeHolder;
            this.isThumbnail = isThumbnail;
        }

    }

    private SunplusImageLoadManager() {
        // FIXME: 2017/4/11 可以单独提取出一个配置类
        mPool = ThreadPoolManager.getInstance().getDownloadPool();
        int memClass = ((ActivityManager) UIUtils.getContext().getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();
        memClass = memClass > 32 ? 32 : memClass;

        final int cacheSize = 1024 * 1024 * memClass / 8;
        //使用可用内存的1/8作为图片缓存
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getRowBytes() * bitmap.getHeight();
            }
        };
        File cacheDir = DiskLruCache.getDiskCacheDir(UIUtils.getContext(), DISK_CACHE_SUBDIR);
        mDiskLruCache = DiskLruCache.openCache(UIUtils.getContext(), cacheDir, DISK_CACHE_SIZE);
    }

    public void loadImage(ICatchFile iCatchFile, @DrawableRes int placeHolder, @NonNull ImageView imageView, boolean isThumbnail) {
        //防止listView等异步加载导致的复用问题
        if (imageView.getTag() != null && imageView.getTag().toString().equals(iCatchFile.getFileName())) {
            return;
        }
        if (placeHolder > 0) {
            if (imageView.getBackground() == null) {
                imageView.setBackgroundResource(placeHolder);
            }
            imageView.setImageDrawable(null);
        }
        if (iCatchFile == null) {
            return;
        }
        //添加tag,将文件名作为tag
        imageView.setTag(iCatchFile.getFileName());

        //先从缓存加载
        Bitmap bitmap = mMemoryCache.get(iCatchFile.getFileName() + (isThumbnail ? "thumbnail" : ""));
        if (bitmap != null) {
            setImageBitmap(imageView, bitmap, false);
            return;
        }

        //生成文件名
        String tempPath = fileNameToTempPath(iCatchFile.getFileName());
        if (tempPath == null) {
            return;
        }
        LoadImageTask loadImageTask = new LoadImageTask(new ImageRef(imageView, iCatchFile, placeHolder, isThumbnail));
        mPool.execute(loadImageTask);
    }

    private class LoadImageTask implements Runnable {
        ImageRef mImageRef;

        LoadImageTask(ImageRef imageRef) {
            mImageRef = imageRef;
        }

        @Override
        public void run() {
            ICatchFile iCatchFile = mImageRef.mICatchFile;
            Bitmap bitmap = mDiskLruCache.get(iCatchFile.getFileName() + (mImageRef.isThumbnail ? "thumbnail" : ""));
            //从本地缓存读取出来并存入内存缓存
            if (bitmap != null) {
                if (mMemoryCache.get(iCatchFile.getFileName() + (mImageRef.isThumbnail ? "thumbnail" : "")) == null) {
                    mMemoryCache.put(iCatchFile.getFileName() + (mImageRef.isThumbnail ? "thumbnail" : ""), bitmap);
                }
            } else {
                //本地没有缓存，下载图片并进行双缓存
                ICatchFrameBuffer thumbnail;
                if (mImageRef.isThumbnail) {
                    thumbnail = FileOperation.getInstance().getThumbnail(iCatchFile);
                } else {
                    thumbnail = FileOperation.getInstance().getQuickview(iCatchFile);
                }
                if (thumbnail != null) {
                    int frameSize = thumbnail.getFrameSize();
                    if (frameSize > 0) {
                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inSampleSize = 1;
                        opt.inJustDecodeBounds = true;
                        byte[] data = thumbnail.getBuffer();
                        BitmapFactory.decodeByteArray(data, 0, data.length, opt);
                        int bitmapSize = opt.outHeight * opt.outWidth * 4;
                        if (bitmapSize > 1000 * 1200)
                            opt.inSampleSize = 2;
                        opt.inJustDecodeBounds = false;
                        bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
                        if (bitmap != null) {
                            //写入SD卡和内存
                            mDiskLruCache.put(iCatchFile.getFileName() + (mImageRef.isThumbnail ? "thumbnail" : ""), bitmap);
                            mMemoryCache.put(iCatchFile.getFileName() + (mImageRef.isThumbnail ? "thumbnail" : ""), bitmap);
                        }
                    }
                }
            }
            mImageRef.mBitmap = bitmap;
            Message message = mImageManagerHandler.obtainMessage(MSG_LOAD_COMPLETE, mImageRef);
            mImageManagerHandler.sendMessage(message);
        }
    }

    private Handler mImageManagerHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_LOAD_COMPLETE:
                    ImageRef imageRef = (ImageRef) (msg.obj);
                    if (imageRef == null) {
                        break;
                    }
                    if (imageRef.mImageView == null || imageRef.mImageView.getTag() == null || imageRef.mICatchFile == null) {
                        break;
                    }
                    //不是同一个imageView
                    if (!(imageRef.mICatchFile.getFileName()).equals(imageRef.mImageView.getTag())) {
                        break;
                    }
                    setImageBitmap(imageRef.mImageView, imageRef.mBitmap, true);
                    break;
            }
        }
    };

    /**
     * 添加图片显示渐现动画
     */
    private void setImageBitmap(ImageView imageView, Bitmap bitmap,
                                boolean isTran) {
        if (isTran) {
            final TransitionDrawable td = new TransitionDrawable(
                    new Drawable[]{
                            new ColorDrawable(UIUtils.getContext().getResources().getColor(android.R.color.transparent)),
                            new BitmapDrawable(bitmap)});
            td.setCrossFadeEnabled(true);
            imageView.setImageDrawable(td);
            td.startTransition(300);
        } else {
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * 根据文件名生成缓存文件完整路径名
     *
     * @param fileName
     */
    private String fileNameToTempPath(String fileName) {

        // 扩展名位置
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            return null;
        }
        return UIUtils.getContext().getCacheDir().toString() + '/' +
                MD5.Md5(fileName) + fileName.substring(index);
    }

    public void stop() {
        mPool.killPool();
    }
}
