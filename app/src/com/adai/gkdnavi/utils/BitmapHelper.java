package com.adai.gkdnavi.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.lidroid.xutils.BitmapUtils;
import com.lidroid.xutils.bitmap.BitmapDisplayConfig;
import com.lidroid.xutils.bitmap.callback.BitmapLoadCallBack;
import com.lidroid.xutils.bitmap.callback.BitmapLoadFrom;

import java.util.HashMap;

public class BitmapHelper {
    private static final String TAG = BitmapHelper.class.getSimpleName();
    private static BitmapUtils utils;
    public static HashMap<String,View> cacheData=new HashMap<String,View>();
    public static <T extends View> void display(T container, String uri) {
        if (utils == null) {
            utils = new BitmapUtils(UIUtils.getContext());
        }
        cacheData.put(uri,container);
        if(cacheData.keySet().size()==1) {
            displayImage(container,uri);
        }
    }
    private static <T extends View> void displayImage(T container, String uri){
        utils.display(container, uri, new BitmapLoadCallBack<T>() {
            @Override
            public void onLoadCompleted(T container, String uri, Bitmap bitmap, BitmapDisplayConfig config, BitmapLoadFrom from) {
                Log.e(TAG, "onLoadCompleted: uri="+uri+" bitmap="+bitmap);
                if(container instanceof ImageView){
                    ((ImageView)container).setImageBitmap(bitmap);
                }
                cacheData.remove(uri);
                if(cacheData.keySet().size()>0){
                    String[] list=cacheData.keySet().toArray(new String[cacheData.keySet().size()]);
                    String key=list[0];
                    View value=cacheData.get(key);
                    displayImage(value,key);
                }
            }

            @Override
            public void onLoadFailed(T container, String uri, Drawable drawable) {
                Log.e(TAG, "onLoadFailed: uri="+uri);
//                cacheData.remove(uri);
                if(cacheData.keySet().size()>0){
                    String[] list=cacheData.keySet().toArray(new String[cacheData.keySet().size()]);
                    String key=list[0];
                    View value=cacheData.get(key);
                    displayImage(value,key);
                }
            }
        });
    }
}
