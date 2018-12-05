package com.adai.gkdnavi.utils;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;

import com.adai.gkdnavi.R;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by admin on 2016/8/18.
 */
public class VideoThumailLoadUtil {
    private static VideoThumailLoadUtil _instance;

    private ThreadPoolExecutor executor;
    private VideoThumailLoadUtil(){
        executor=new ThreadPoolExecutor(5, 20, 200, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(5));
    }
    public static VideoThumailLoadUtil getInstance(){
        if(_instance==null){
            _instance=new VideoThumailLoadUtil();
        }
        return _instance;
    }

    public void displayVideoThumail(String uri,ImageView image){
        ThumailLoadThread request=new ThumailLoadThread(image,uri);
        executor.execute(request);
    }

    class ThumailLoadThread implements Runnable{

        ImageView image;
        String path;
        Handler handler=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 0:
                        Bitmap bitmap=(Bitmap)msg.obj;
                        image.setImageBitmap(bitmap);
                        break;
                    case 1:
                        image.setImageResource(R.drawable.video_default);
                        break;
                }
            }
        };
        public ThumailLoadThread(ImageView image,String path){
            this.image=image;
            this.path=path;
        }
        @Override
        public void run() {
            Bitmap bitmap=getVideoThumbnail(path);
            Message msg=new Message();
            if(bitmap!=null){
                msg.what=0;
                msg.obj=bitmap;
            }else{
                msg.what=1;
            }
            handler.sendMessage(msg);
        }
    }

    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            // 取得视频的长度(单位为毫秒)
            String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            // 取得视频的长度(单位为秒)
            int seconds = Integer.valueOf(time) / 1000;
            if(seconds>=1){
                bitmap=retriever.getFrameAtTime(1,MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            }else {
                bitmap = retriever.getFrameAtTime();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }
}
