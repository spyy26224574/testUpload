/*
 * Copyright (C) 2015 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ijk.media.activity;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TextView;

import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.EditVideoActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.fragment.AlbumFragment;
import com.adai.gkdnavi.utils.NetworkDownloadUtils;
import com.adai.gkdnavi.utils.ShareUtils;
import com.adai.gkdnavi.utils.ToastUtil;
import com.example.ipcamera.application.VLCApplication;
import com.ijk.media.application.Settings;
import com.ijk.media.content.RecentMediaStorage;
import com.ijk.media.fragments.TracksFragment;
import com.ijk.media.widget.media.AndroidMediaController;
import com.ijk.media.widget.media.IjkVideoView;

import org.apache.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;


public class VideoActivity extends AppCompatActivity implements TracksFragment.ITrackHolder {
    private static final String TAG = "VideoActivity";

    private String mLargePath;
    private String mVideoPath;
    private Uri mVideoUri;

    private AndroidMediaController mMediaController;
    private IjkVideoView mVideoView;
    private TextView mToastTextView;
    private TableLayout mHudView;
    private DrawerLayout mDrawerLayout;
    private ViewGroup mRightDrawer;

    private Settings mSettings;
    private boolean mBackPressed;
    private ProgressBar loadingprogress;
    private long postion = 0;
    /**
     * 视频类型,本地还是摄像头，0为默认为摄像头，1为本地,2为网络
     */
    private int type = 0;

    private List<Appinfo> appinfos;

//    public static Intent newIntent(Context context, String videoPath, String videoTitle) {
//        Intent intent = new Intent(context, VideoActivity.class);
//        intent.putExtra("videoPath", videoPath);
//        intent.putExtra("videoTitle", videoTitle);
//        return intent;
//    }

//    public static void intentTo(Context context, String videoPath, String videoTitle) {
//        context.startActivity(newIntent(context, videoPath, videoTitle));
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        hideNavigation();
        setContentView(R.layout.activity_player);
        loadingprogress = (ProgressBar) findViewById(R.id.loadingprogress);
        mSettings = new Settings(this);

        // handle arguments
        mVideoPath = getIntent().getStringExtra("videoPath");
        mLargePath = getIntent().getStringExtra("largePath");
        type = getIntent().getIntExtra("type", 0);
        if (type == 2) {
            String local = checkDownload();
//            checkDownloadFile();
//            if(local!=null){
//                mVideoPath=local;
//            }
        }
        if (getIntent().hasExtra("position")) {
            postion = getIntent().getIntExtra("position", 0);
        }
//        mVideoPath = "rtsp://192.168.1.254/hello.mov";
//        mVideoUri=Uri.parse("rtsp://192.168.1.254/hello.mov");

        Intent intent = getIntent();
        String intentAction = intent.getAction();
        if (!TextUtils.isEmpty(intentAction)) {
            if (intentAction.equals(Intent.ACTION_VIEW)) {
                mVideoPath = intent.getDataString();
            } else if (intentAction.equals(Intent.ACTION_SEND)) {
                mVideoUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                    String scheme = mVideoUri.getScheme();
                    if (TextUtils.isEmpty(scheme)) {
                        Log.e(TAG, "Null unknown scheme\n");
                        finish();
                        return;
                    }
                    if (scheme.equals(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                        mVideoPath = mVideoUri.getPath();
                    } else if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
                        Log.e(TAG, "Can not resolve content below Android-ICS\n");
                        finish();
                        return;
                    } else {
                        Log.e(TAG, "Unknown scheme " + scheme + "\n");
                        finish();
                        return;
                    }
                }
            }
        }

        if (!TextUtils.isEmpty(mVideoPath)) {
            new RecentMediaStorage(this).saveUrlAsync(mVideoPath);
        }

        // updateSize UI
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        mMediaController = new AndroidMediaController(this, false);
        mMediaController.setSupportActionBar(actionBar);

        mToastTextView = (TextView) findViewById(R.id.toast_text_view);
        mHudView = (TableLayout) findViewById(R.id.hud_view);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mRightDrawer = (ViewGroup) findViewById(R.id.right_drawer);

        mDrawerLayout.setScrimColor(Color.TRANSPARENT);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // updateSize player
        IjkMediaPlayer.loadLibrariesOnce(null);
        IjkMediaPlayer.native_profileBegin("libijkplayer.so");

        mVideoView = (IjkVideoView) findViewById(R.id.video_view);
        mVideoView.setMediaController(mMediaController);
        mVideoView.setHudView(mHudView);
        mVideoView.setOnErrorListener(errorListener);
        mVideoView.setOnInfoListener(infoListener);
        mVideoView.setOnCompletionListener(completionListener);
        mVideoView.setOnMediaControlerChange(new IjkVideoView.onMediaControlerChange() {
            @Override
            public void onHide() {
                hideNavigation();
            }
        });
        mMediaController.setOnMediaControlerChange(new AndroidMediaController.onMediaControlerChange() {
            @Override
            public void onHide() {
                hideNavigation();
            }
        });
        // prefer mVideoPath
        if (mVideoPath != null) {
            mVideoView.setVideoPath(mVideoPath);
            File file = new File(mVideoPath);
            if (type != 2)
                setTitle(file.getName());
        } else if (mVideoUri != null)
            mVideoView.setVideoURI(mVideoUri);
        else {
            Log.e(TAG, "Null Data Source\n");
            finish();
            return;
        }
        if (postion > 0) {
            mVideoView.seekTo((int) postion);
        }
        mVideoView.start();
    }

    private String checkDownload() {
        String local = NetworkDownloadUtils.getLocalPath(mVideoPath);
        File file = new File(local);
        if (file.exists()) {
            isDownload = true;
            return local;
        } else {
            isDownload = false;
            return null;
        }
    }

    private void checkDownloadFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String local = NetworkDownloadUtils.getLocalPath(mVideoPath);
                    File file = new File(local);
                    if (!file.exists()) {
                        return;
                    }
                    URL mUrl = new URL(mVideoPath);
                    HttpURLConnection conn = (HttpURLConnection) mUrl.openConnection();
                    conn.setConnectTimeout(5 * 1000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept-Encoding", "identity");
                    conn.setRequestProperty("Referer", mVideoPath);
                    //conn.setRequestProperty("Referer", urlString);
                    conn.setRequestProperty("Charset", "UTF-8");
                    conn.setRequestProperty("Connection", "Keep-Alive");
                    conn.connect();
                    int responseCode = conn.getResponseCode();
                    // 判断请求是否成功处理
                    if (responseCode == HttpStatus.SC_OK) {
                        int length = conn.getContentLength();
                        if (length == file.length()) {
                            isDownload = true;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (downloadMenu != null) {
                                        downloadMenu.setIcon(R.drawable.bg_download_complete);
                                    }
                                }
                            });
                        }
                    }
                    conn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void hideNavigation() {
//        if (Build.VERSION.SDK_INT >= 14) {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        }
    }

    private IMediaPlayer.OnInfoListener infoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
            switch (i) {
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                    loadingprogress.setVisibility(View.VISIBLE);
                    break;
                //                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
//                    Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
//                    loadingprogress.setVisibility(View.GONE);
//                    break;
                case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                    loadingprogress.setVisibility(View.GONE);
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    loadingprogress.setVisibility(View.GONE);
                    break;
            }
            return false;
        }
    };

    private IMediaPlayer.OnErrorListener errorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            return false;
        }
    };

    private IMediaPlayer.OnCompletionListener completionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
//            finish();
        }
    };

    @Override
    public void onBackPressed() {
        mBackPressed = true;

        super.onBackPressed();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (mVideoView != null)
            mVideoView.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVideoView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mVideoView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mBackPressed || !mVideoView.isBackgroundPlayEnabled()) {
            mVideoView.stopPlayback();
            mVideoView.release(true);
            mVideoView.stopBackgroundPlay();
        } else {
            mVideoView.enterBackground();
        }
        IjkMediaPlayer.native_profileEnd();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private MenuItem progressMenu, downloadMenu;
    private boolean isDownload = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_player, menu);
        if (type == 1 || type == 0) {
            getMenuInflater().inflate(R.menu.menu_share, menu);
        } else if (type == 2) {
            getMenuInflater().inflate(R.menu.menu_download, menu);
            downloadMenu = menu.findItem(R.id.action_download);
            if (isDownload) {
                downloadMenu.setIcon(R.drawable.bg_download_complete);
            }
            progressMenu = menu.findItem(R.id.action_progress);
            progressMenu.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_share) {
            Intent share = new Intent(Intent.ACTION_SEND);
            if (type == 1) {
                new ShareUtils().shareVideo(this, mVideoPath);
            } else if (type == 0) {
                String path = mVideoPath;
                if (!TextUtils.isEmpty(mLargePath)) {
                    path = mLargePath;
                }
                share.setType("video/*");
                share.setComponent(new ComponentName(getPackageName(), "com.adai.gkdnavi.EditVideoActivity"));
                share.putExtra("videoType", type);
                share.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
                Log.e(TAG, "onOptionsItemSelected: mVideoPath:" + mVideoPath);
                share.putExtra("smallUri", Uri.parse(mVideoPath));
                startActivity(share);
                finish();
            }
        } else if (id == R.id.action_download) {
            downLoad(mVideoPath);
        }

        return super.onOptionsItemSelected(item);
    }

    private void downLoad(String path) {
        if (isDownload) return;
        String[] strs = path.split("/");
        if (progressMenu != null) {
//            int width= (int) getResources().getDimension(R.dimen.dimen_32);
//            ProgressBar progressBar=new ProgressBar(VideoActivity.this);
            View progressBar = View.inflate(VideoActivity.this, R.layout.layout_progress_menu, null);
            progressMenu.setActionView(progressBar);
            progressMenu.setVisible(true);
        }
        NetworkDownloadUtils.downloadFile(path, new HttpUtil.DownloadCallback() {
            @Override
            public void onDownloadComplete(String path) {
                Intent intent = new Intent(AlbumFragment.ACTION_FRESH);
                intent.putExtra("isVideo", true);
                VLCApplication.getAppContext().sendBroadcast(intent);
                if (!isFinishing()) {
                    ToastUtil.showShortToast(VideoActivity.this, getString(R.string.successfully_saved_to) + path);
                    ToastUtil.showShortToast(VideoActivity.this, getString(R.string.successfully_saved_to) + path);
                    if (progressMenu != null) {
                        progressMenu.setVisible(false);
                    }
                    if (downloadMenu != null) {
                        downloadMenu.setIcon(R.drawable.bg_download_complete);
                    }
                    isDownload = true;
                }
            }

            @Override
            public void onDownloading(int progress) {
                if (progressMenu != null && !progressMenu.isVisible()) {
                    progressMenu.setVisible(true);
                }
            }

            @Override
            public void onDownladFail() {
                if (!isFinishing()) {
                    ToastUtil.showShortToast(VideoActivity.this, getString(R.string.download_error));
                    if (progressMenu != null) {
                        progressMenu.setVisible(false);
                    }
                }
            }
        });
    }

    private void showShareLayout() {
        if (appinfos == null) {
            appinfos = getShareApps(this);
        }
        ListView content = new ListView(this);
        DialogListAdapter adapter = new DialogListAdapter(appinfos);
        content.setAdapter(adapter);
        content.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Appinfo info = (Appinfo) parent.getItemAtPosition(position);
                Uri uri = Uri.parse("file:///" + mVideoPath);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setComponent(new ComponentName(info.pkgName, info.laucherClassname));
                shareIntent.setType("video/*");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(shareIntent);
            }
        });
        new AlertDialog.Builder(this).setView(content).create().show();
    }

    @Override
    public ITrackInfo[] getTrackInfo() {
        if (mVideoView == null)
            return null;

        return mVideoView.getTrackInfo();
    }

    @Override
    public void selectTrack(int stream) {
        mVideoView.selectTrack(stream);
    }

    @Override
    public void deselectTrack(int stream) {
        mVideoView.deselectTrack(stream);
    }

    @Override
    public int getSelectedTrack(int trackType) {
        if (mVideoView == null)
            return -1;

        return mVideoView.getSelectedTrack(trackType);
    }


    private List<Appinfo> getShareApps(Context context) {
        List<Appinfo> appinfos = new ArrayList<Appinfo>();
        PackageManager pManager = context.getPackageManager();
        Appinfo info = new Appinfo();
        info.icon = getApplicationInfo().loadIcon(pManager);
        info.laucherClassname = EditVideoActivity.class.getName();
        info.pkgName = getApplication().getPackageName();
        info.title = getString(R.string.share_title);
        appinfos.add(info);
        List<ResolveInfo> mApps = new ArrayList<ResolveInfo>();
        Intent intent = new Intent(Intent.ACTION_SEND, null);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setType("video/*");
        //      intent.setType("*/*");

        mApps = pManager.queryIntentActivities(intent,
                PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
        if (mApps != null && mApps.size() > 0) {
            for (ResolveInfo resolveInfo : mApps) {
                Appinfo appinfo = new Appinfo();
                appinfo.pkgName = resolveInfo.activityInfo.packageName;
//              showLog_I(TAG, "pkg>" + resolveInfo.activityInfo.packageName + ";name>" + resolveInfo.activityInfo.name);
                appinfo.laucherClassname = resolveInfo.activityInfo.name;
                appinfo.title = resolveInfo.loadLabel(pManager).toString();
                appinfo.icon = resolveInfo.loadIcon(pManager);
                appinfos.add(appinfo);
            }
        }
        return appinfos;
    }

    public class Appinfo {
        public Drawable icon;
        public String pkgName;
        public String title;
        public String laucherClassname;
    }

    class DialogListAdapter extends BaseAdapter {

        List<Appinfo> infos;

        public DialogListAdapter(List<Appinfo> infos) {
            this.infos = infos;
        }

        @Override
        public int getCount() {
            return infos == null ? 0 : infos.size();
        }

        @Override
        public Object getItem(int position) {
            return infos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_list, null);
            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            icon.setBackgroundDrawable(infos.get(position).icon);
            title.setText(infos.get(position).title);
            return convertView;
        }
    }
}
