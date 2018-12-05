package com.adai.gkdnavi.fragment.square;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.gkdnavi.FileGridNewActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.fragment.BaseFragment;
import com.adai.gkdnavi.utils.VoiceManager;
import com.adai.gkdnavi.utils.WifiUtil;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;
import com.example.ipcamera.application.VLCApplication;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by huangxy on 2016/12/3.
 */

public class AlbumNewFragment extends BaseFragment implements View.OnClickListener {
    private TextView recorder_num, downloaded_num, montage_videos_num, phone_photos_num;
    private ImageView recorder_album, downloaded, montage_videos, phone_ablum;
    private ArrayList<String> downloadedList = new ArrayList<>();
    private ArrayList<String> montageVideoList = new ArrayList<>();
    private ArrayList<String> phonePhotoList = new ArrayList<>();
    //    private ArrayList<HourFile> phoneAblumList = new ArrayList<>();
//    private ArrayList<HourFile> montageVideoList = new ArrayList<>();
//    private ArrayList<HourFile> downloadedList = new ArrayList<>();
    public static final int ALBUM_RECORDER = 0;
    public static final int ALBUM_LOCAL_FILE = 1;
    public static final int ALBUM_PHONE = 2;
    private static final int MSG_ALBUM_PHONE = 3;
    private static final int MSG_ALBUM_RECORDER = 4;
    private static final int MSG_ALBUM_DOWNLOADED = 5;
    private static final int MSG_ALBUM_MONTAGE = 6;
    private static final int MSG_CAMERA_CONNECTED = 7;
    private static final int MSG_CAMERA_UNCONNECT = 8;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ALBUM_PHONE:
                    phone_photos_num.setText("" + phonePhotoList.size());
                    if (phonePhotoList != null && phonePhotoList.size() > 0) {
                        ImageLoaderUtil.getInstance().loadImage(mContext, phonePhotoList.get(0), R.drawable.no_picture, phone_ablum);
                    } else {
                        ImageLoaderUtil.getInstance().loadImage(mContext, "", R.drawable.no_picture, phone_ablum);
                    }
                    break;
                case MSG_ALBUM_DOWNLOADED:
                    downloaded_num.setText("" + downloadedList.size());
                    if (downloadedList.size() > 0) {
                        ImageLoaderUtil.getInstance().loadImage(mContext, downloadedList.get(0), R.drawable.no_picture, downloaded);
                    } else {
                        ImageLoaderUtil.getInstance().loadImage(mContext, "", R.drawable.no_picture, downloaded);
                    }
                    break;
                case MSG_ALBUM_MONTAGE:
                    montage_videos_num.setText("" + montageVideoList.size());
                    if (montageVideoList.size() > 0) {
                        ImageLoaderUtil.getInstance().loadImage(mContext, montageVideoList.get(0), R.drawable.no_video, montage_videos);
                    } else {
                        ImageLoaderUtil.getInstance().loadImage(mContext, "", R.drawable.no_video, montage_videos);
                    }
                    break;
                case MSG_ALBUM_RECORDER:
                    break;
                case MSG_CAMERA_CONNECTED:
                    hidepDialog();
                    Intent grid = new Intent(mContext, FileGridNewActivity.class);
                    grid.putExtra("title", getString(R.string.recorder_album));
                    grid.putExtra("fileType", ALBUM_RECORDER);
                    mContext.startActivity(grid);
                    break;
                case MSG_CAMERA_UNCONNECT:
                    hidepDialog();
//                    Toast.makeText(mContext, getString(R.string.please_connect_camera), Toast.LENGTH_SHORT).show();
                    showConnectCameraDialog();
                    break;
            }
        }
    };

    private void showConnectCameraDialog() {
        //        new android.support.v7.app.AlertDialog.Builder(mContext).setTitle(R.string.notice).setMessage(R.string.wifi_checkmessage);
//                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        WifiUtil.getInstance().gotoWifiSetting(mContext);
//                    }
//                }).setNegativeButton(R.string.cancel, null).create().show();
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getContext());
        builder.setTitle(getString(R.string.notice));
        if (getCurrentNetModel() == 0) {
            builder.setMessage(getString(R.string.wifi_checkmessage));
        } else {
            builder.setMessage(getString(R.string.ap_checkmessage));
        }
        // builder.setIcon(R.drawable.ic_launcher);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (getCurrentNetModel() == 0) {
                    WifiUtil.getInstance().gotoWifiSetting(mContext);
                } else {

                    WifiUtil.getInstance().startAP(getContext());
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setCancelable(false).create().show();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.activity_local_album_new, container, false);
        recorder_num = (TextView) rootView.findViewById(R.id.recorder_num);
        downloaded_num = (TextView) rootView.findViewById(R.id.downloaded_num);
        montage_videos_num = (TextView) rootView.findViewById(R.id.montage_videos_num);
        phone_photos_num = (TextView) rootView.findViewById(R.id.phone_photos_num);
        recorder_album = (ImageView) rootView.findViewById(R.id.recorder_album);
        downloaded = (ImageView) rootView.findViewById(R.id.downloaded);
        montage_videos = (ImageView) rootView.findViewById(R.id.montage_videos);
        phone_ablum = (ImageView) rootView.findViewById(R.id.phone_ablum);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    protected void init() {
        super.init();
        initFile();
        recorder_album.setOnClickListener(this);
        downloaded.setOnClickListener(this);
        montage_videos.setOnClickListener(this);
        phone_ablum.setOnClickListener(this);
    }

    private void initFile() {
        downloadedList.clear();
        phonePhotoList.clear();
        montageVideoList.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor photoCursor = mContext.getContentResolver().query(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or " +
                                MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED + " desc");
                //遍历相册
                if (photoCursor != null) {
                    while (photoCursor.moveToNext()) {
                        String path = photoCursor.getString(photoCursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                        //将图片路径添加到集合
                        phonePhotoList.add(path);
                    }
                    photoCursor.close();
                }
                Cursor videoCursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                if (videoCursor != null) {
                    while (videoCursor.moveToNext()) {
                        String path = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                        phonePhotoList.add(path);
                    }
                    videoCursor.close();
                }
                sendMessage(MSG_ALBUM_PHONE);
                String rootpath = VLCApplication.DOWNLOADPATH;
                File root = new File(rootpath);
                File[] files = root.listFiles();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        String filepath = file.getAbsolutePath();
                        String lowcasePath = filepath.toLowerCase();
                        if (lowcasePath.endsWith("jpg") || lowcasePath.endsWith("png") || lowcasePath.endsWith("mov") || lowcasePath.endsWith("mp4")) {
                            //只添加图片和视频
                            downloadedList.add(filepath);
                        }
                    }
                }
                sendMessage(MSG_ALBUM_DOWNLOADED);
                String temppath = VLCApplication.CUT_VIDEO_PATH;
                File temp = new File(temppath);
                File[] temps = temp.listFiles();
                if (temps != null && temps.length > 0) {
                    for (File file : temps) {
                        String filepath = file.getAbsolutePath();
                        if (filepath.toLowerCase().endsWith("mov") || filepath.toLowerCase().endsWith("mp4")) {
                            montageVideoList.add(filepath);
                        }
                    }
                }
                sendMessage(MSG_ALBUM_MONTAGE);
            }
        }).start();
    }

    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
    }

    @Override
    public void onStart() {
        super.onStart();
        //避免用户自己在手机文件管理中删除了照片而不能及时更新
        Log.e(_TAG_, "onStart: ");
        initFile();
    }

    @Override
    public void onClick(View v) {
        Intent grid = new Intent(mContext, FileGridNewActivity.class);
        switch (v.getId()) {
            case R.id.recorder_album:
                if (VoiceManager.isCameraBusy) {
                    showToast(R.string.camera_isbusy);
                    return;
                }
                showpDialog(R.string.are_surveillance_cameras);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (WifiUtil.checkNetwork(getContext(), getCurrentNetModel())) {
                            sendMessage(MSG_CAMERA_CONNECTED);
                        } else {
                            sendMessage(MSG_CAMERA_UNCONNECT);
                        }
                    }
                }).start();
                break;
            case R.id.downloaded:
                if (downloadedList.size() == 0) {
                    Toast.makeText(mContext, getString(R.string.no_file), Toast.LENGTH_SHORT).show();
                } else {
                    grid.putExtra("title", getString(R.string.downloaded));
                    grid.putExtra("fileType", ALBUM_LOCAL_FILE);
                    grid.putExtra("filePath", VLCApplication.DOWNLOADPATH);
                    mContext.startActivity(grid);
                }
                break;
            case R.id.montage_videos:
                if (montageVideoList.size() == 0) {
                    Toast.makeText(mContext, getString(R.string.no_file), Toast.LENGTH_SHORT).show();
                } else {
                    grid.putExtra("title", getString(R.string.clip_video));
                    grid.putExtra("fileType", ALBUM_LOCAL_FILE);
                    grid.putExtra("filePath", VLCApplication.CUT_VIDEO_PATH);
                    mContext.startActivity(grid);
                }
                break;
            case R.id.phone_ablum:
                if (phonePhotoList.size() == 0) {
                    Toast.makeText(mContext, getString(R.string.no_file), Toast.LENGTH_SHORT).show();
                } else {
                    grid.putExtra("title", getString(R.string.phone_ablum));
                    grid.putExtra("fileType", ALBUM_PHONE);
//                grid.putStringArrayListExtra("filePath", phonePhotoList);
                    mContext.startActivity(grid);
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext.unregisterReceiver(mFreshReceiver);
    }

    private BroadcastReceiver mFreshReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };
}
