package com.adai.gkdnavi.utils;

import android.support.annotation.DrawableRes;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by admin on 2016/8/18.
 */
public class ImageLoadHelper {
    private static ImageLoadHelper _instance;

    //    private BitmapUtils utils;
    private ImageLoadHelper() {

    }

    public static ImageLoadHelper getInstance() {
        if (_instance == null) {
            _instance = new ImageLoadHelper();
        }
        return _instance;
    }

    public void displayImageFromSD(String path, ImageView imageView) {
        ImageLoader.getInstance().displayImage("file://" + path, imageView);
    }

    public void displayImage(String uri, ImageView imageView) {
        ImageLoader.getInstance().displayImage(uri, imageView);
    }

    public void displayImage(String uri, ImageView imageView, @DrawableRes int defaultRes) {
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisk(true).showImageOnFail(defaultRes).showImageForEmptyUri(defaultRes).build();
        ImageLoader.getInstance().displayImage(uri, imageView, options);
    }

    public void displayImageWithoutCatch(String uri, ImageView imageView, @DrawableRes int defaultRes) {
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheOnDisk(false).cacheInMemory(false).showImageOnFail(defaultRes).showImageForEmptyUri(defaultRes).build();
        ImageLoader.getInstance().displayImage(uri, imageView, options);
    }
}
