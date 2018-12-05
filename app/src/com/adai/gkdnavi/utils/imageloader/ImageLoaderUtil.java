package com.adai.gkdnavi.utils.imageloader;


import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class ImageLoaderUtil {
    // 图片类型
    public static final int PIC_LARGE = 0;
    public static final int PIC_MEDIUM = 1;
    public static final int PIC_SMALL = 2;

    private static volatile ImageLoaderUtil mInstance;
    private BaseImageLoaderProvider mProvider;

    private ImageLoaderUtil() {
        mProvider = new GlideImageLoaderProvider();
    }

    public static ImageLoaderUtil getInstance() {
        if (mInstance == null) {
            synchronized (ImageLoaderUtil.class) {
                if (mInstance == null) {
                    mInstance = new ImageLoaderUtil();
                    return mInstance;
                }
            }
        }
        return mInstance;
    }

    public void loadImage(Context context, ImageLoaderParameter parameter) {
        mProvider.loadImage(context, parameter);
    }

    public void loadImage(Context context, String url, ImageView imageView) {
        ImageLoaderParameter parameter = new ImageLoaderParameter.Builder().url(url).imgView(imageView).build();
        mProvider.loadImage(context, parameter);
    }

    public void loadImage(Context context, String url, int placeHolder,
                          ImageView imageView) {
        ImageLoaderParameter img = new ImageLoaderParameter.Builder().url(url)
                .placeHolder(placeHolder).imgView(imageView).build();
        mProvider.loadImage(context, img);
    }

    public void loadImageWithoutCache(Context context, String url, ImageView imageView) {
        if (checkContext(context)) return;
        Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(imageView);
    }

    private boolean checkContext(Context context) {
        if (context == null) {
            return true;
        }
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing()) {
                return true;
            }
        }
        return false;
    }

    public void loadImageWithoutCache(Context context, String url, int placeHolder, ImageView imageView) {
        if (checkContext(context)) return;
        Glide.with(context).load(url).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).placeholder(placeHolder).into(imageView);
    }

    public void loadRoundImage(Context context, String url, int placeHolder,
                               ImageView imageView) {
        ImageLoaderParameter img = new ImageLoaderParameter.Builder().url(url)
                .placeHolder(placeHolder).imgView(imageView).build();
        mProvider.loadRoundImage(context, img);
    }

    public void loadRoundImage(Context context, String url, ImageView imageView) {
        ImageLoaderParameter img = new ImageLoaderParameter.Builder().url(url).imgView(imageView).build();
        mProvider.loadRoundImage(context, img);
    }
}
