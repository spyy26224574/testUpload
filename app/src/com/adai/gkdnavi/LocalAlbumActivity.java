package com.adai.gkdnavi;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.adai.gkdnavi.utils.ImageLoadHelper;
import com.adai.gkdnavi.utils.VideoThumailLoadUtil;
import com.example.ipcamera.application.VLCApplication;

import java.io.File;
import java.util.ArrayList;

public class LocalAlbumActivity extends BaseActivity implements View.OnClickListener {

    private TextView photos_num, videos_num, montage_videos_num, phone_photos_num;
    private ImageView photos, videos, montage_videos, phone_photos;
    private ArrayList<String> photoList = new ArrayList<>();
    private ArrayList<String> videoList = new ArrayList<>();
    private ArrayList<String> montageVideoList = new ArrayList<>();
    private ArrayList<String> phonePhotoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_album);
        initView();
        init();

    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @Override
    protected void initView() {
        super.initView();
        photos_num = (TextView) findViewById(R.id.photos_num);
//        videos_num = (TextView) findViewById(R.id.videos_num);
//        montage_videos_num = (TextView) findViewById(R.id.montage_videos_num);
//        phone_photos_num = (TextView) findViewById(R.id.phone_photos_num);
//        photos = (ImageView) findViewById(R.id.photos);
//        videos = (ImageView) findViewById(R.id.videos);
//        montage_videos = (ImageView) findViewById(R.id.montage_videos);
//        phone_photos = (ImageView) findViewById(R.id.phone_photos);
    }

    @Override
    protected void init() {
        super.init();
        photoList.clear();
        videoList.clear();
        montageVideoList.clear();
        phonePhotoList.clear();

        String rootpath = VLCApplication.DOWNLOADPATH;
        File root = new File(rootpath);
        File[] files = root.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                String filepath = file.getAbsolutePath();
                if (filepath.endsWith("JPG") || filepath.endsWith("jpg") || filepath.endsWith("PNG") || filepath.endsWith("png")) {
                    photoList.add(filepath);
                } else if (filepath.endsWith("MOV") || filepath.endsWith("mov") || filepath.endsWith("mp4") || filepath.endsWith("MP4")) {
                    videoList.add(filepath);
                }
            }
        }


        String temppath = VLCApplication.CUT_VIDEO_PATH;
        File temp = new File(temppath);
        File[] temps = temp.listFiles();
        if (temps != null && temps.length > 0) {
            for (File file : temps) {
                String filepath = file.getAbsolutePath();
                if (filepath.endsWith("JPG") || filepath.endsWith("jpg") || filepath.endsWith("PNG") || filepath.endsWith("png")) {
                    //photoList.add(filepath);
                } else if (filepath.endsWith("MOV") || filepath.endsWith("mov") || filepath.endsWith("mp4") || filepath.endsWith("MP4")) {
                    montageVideoList.add(filepath);
                }
            }
        }

        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
        //遍历相册
        while (cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
            //将图片路径添加到集合
            phonePhotoList.add(path);


        }
        cursor.close();


        photos_num.setText(String.valueOf(photoList.size()));
        videos_num.setText(String.valueOf(videoList.size()));
        montage_videos_num.setText(String.valueOf(montageVideoList.size()));
        phone_photos_num.setText(String.valueOf(phonePhotoList.size()));

        if (photoList.size() > 0) {
            String photo = photoList.get(0);
            ImageLoadHelper.getInstance().displayImageFromSD(photo, photos);
        } else {
            photos.setImageResource(R.drawable.default_image_holder);
        }
        if (videoList.size() > 0) {
            VideoThumailLoadUtil.getInstance().displayVideoThumail(videoList.get(0), videos);
        } else {
            videos.setImageResource(R.drawable.video_default);
        }

        if (montageVideoList.size() > 0) {
            VideoThumailLoadUtil.getInstance().displayVideoThumail(montageVideoList.get(0), montage_videos);
        } else {
            montage_videos.setImageResource(R.drawable.video_default);
        }

        if (phonePhotoList.size() > 0) {
            String photo = phonePhotoList.get(0);
            ImageLoadHelper.getInstance().displayImageFromSD(photo, phone_photos);
        } else {
            phone_photos.setImageResource(R.drawable.default_image_holder);
        }

        photos.setOnClickListener(this);
        videos.setOnClickListener(this);
        montage_videos.setOnClickListener(this);
        phone_photos.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent grid = new Intent(mContext, FileGridActivity.class);
        int type = 0;
        String title = "";
        switch (v.getId()) {
//            case R.id.photos:
//                grid.putExtra("title", getString(R.string.recorder_photo));
//                grid.putExtra("fileType", 0);
//                grid.putStringArrayListExtra("fileList", photoList);
//                break;
//            case R.id.videos:
//                grid.putExtra("title", getString(R.string.recorder_video));
//                grid.putExtra("fileType", 1);
//                grid.putStringArrayListExtra("fileList", videoList);
//                break;
//            case R.id.montage_videos:
//                grid.putExtra("title", getString(R.string.clip_video));
//                grid.putExtra("fileType", 1);
//                grid.putStringArrayListExtra("fileList", montageVideoList);
//                break;
//            case R.id.phone_photos:
//                grid.putExtra("title", getString(R.string.local_photo));
//                grid.putExtra("fileType", 0);
//                grid.putStringArrayListExtra("fileList", phonePhotoList);
//                break;
        }
        mContext.startActivity(grid);
    }
}
