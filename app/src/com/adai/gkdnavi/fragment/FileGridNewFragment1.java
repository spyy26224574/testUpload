package com.adai.gkdnavi.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.adai.camera.novatek.contacts.Contacts;
import com.adai.camera.novatek.util.CameraUtils;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.adapter.FileGroupNewAdapter;
import com.adai.gkdnavi.fragment.square.AlbumNewFragment;
import com.adai.gkdnavi.utils.MinuteFileDownloadManager;
import com.adai.gkdnavi.utils.ToastUtil;
import com.adai.gkdnavi.utils.VoiceManager;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MinuteFile;
import com.example.ipcamera.domain.MovieRecord;

import org.videolan.vlc.util.DomParseUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by huangxy on 2017/1/3.
 */

public class FileGridNewFragment1 extends BaseFragment implements View.OnClickListener {
    protected static final int START = 0;
    protected static final int END = 1;
    private static final int EMPTY = 2;
    public static final String ACTION_EDIT_MODE_CHANGE = "com.adai.gkdnavi.fragment.FileGridNewFragment.editModeChange";
    public static final String ACTION_SELECTED_FILE = "com.adai.gkdnavi.fragment.FileGridNewFragment.selected_file";
    public static final int REQUEST_FILE_DELETE = 11;
    private static final int DELETE_RECORD_FILE = 3;
    private static final int ERROR_GET_RECORD_STATE = 6;
    private static final int ERROR_STOP_RECORD = 7;
    private static final int ERROR_GET_FILELIST = 8;
    private static final int ERROR_GET_SDCARD = 9;
    private static final int FREASH = 10;
    private static final int GET_FILELIST = 12;
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_TYPE = "type";
    private static final String ARG_FILE_PATH = "file_path";
    private int mColumnCount;
    private int mType;
    private View mIvDelete;
    private String mFilePath;
    private ArrayList<MinuteFile> mMinuteFiles = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private static final int LOCALFILESTART = 1;
    //    private FileGroupAdapter mFileGroupAdapter;
    private FileGroupNewAdapter mFileGroupAdapter;
    private List<FileDomain> mCameraFiles = new ArrayList<>();
    private OnFileStateChangeListener mOnFileStateChangeListener;
    private RelativeLayout mBottomLayout;
    private ImageView mIvDownload;
    private boolean isEditMode;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DELETE_RECORD_FILE:
                    mSwipeRefreshLayout.setRefreshing(false);
                    if (mDeleteList != null && mDeleteList.size() > 0) {
                        deleteRecordFile(mDeleteList);
                    } else {
                        hidepDialog();
                    }
                    break;
                case EMPTY:
                    mSwipeRefreshLayout.setRefreshing(false);
                    if (mFileGroupAdapter != null) {
                        mMinuteFiles.clear();
                        mFileGroupAdapter.notifyDataSetChanged();
                    }
                    ToastUtil.showShortToast(mContext, getString(R.string.no_file));
                    break;
                case START:
                    CameraUtils.changeMode(CameraUtils.MODE_PLAYBACK, new CameraUtils.ModeChangeListener() {
                        @Override
                        public void success() {
                            CameraUtils.getSDCardStatus(new CameraUtils.SDCardStatusListener() {
                                @Override
                                public void success(int status) {
                                    if (status > 0) {
                                        sendEmptyMessageDelayed(GET_FILELIST, 300);
                                    } else {
                                        showErrorDialog(ERROR_GET_SDCARD);
                                    }
                                }

                                @Override
                                public void error(String error) {
                                    showErrorDialog(ERROR_GET_SDCARD);
                                }
                            });
                        }

                        @Override
                        public void failure(Throwable throwable) {
                            showErrorDialog(ERROR_GET_FILELIST);
                        }
                    });
                    break;
                case GET_FILELIST:
                    getFileList();
                    break;
                case LOCALFILESTART:
                    hidepDialog();
                    mSwipeRefreshLayout.setRefreshing(false);
                    if (mFileGroupAdapter == null) {
                        mFileGroupAdapter = new FileGroupNewAdapter(getActivity(), mMinuteFiles, mType);
                        mRecyclerView.setAdapter(mFileGroupAdapter);
                    } else {
                        mFileGroupAdapter.notifyDataSetChanged();
                    }
                    break;
                case FREASH:
                    mSwipeRefreshLayout.setRefreshing(false);
                    hidepDialog();
                    initPictures();
                    break;
                default:
                    break;
            }
        }

    };

    public ArrayList<MinuteFile> getMinuteFiles() {
        return mMinuteFiles;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        toggleIcon(false);
        VoiceManager.selectedMinuteFile.clear();
        mBottomLayout.setVisibility(editMode ? View.VISIBLE : View.GONE);
        if (!isEditMode) {
            //退出编辑模式的时候全部变成非选中状态
            if (mMinuteFiles != null && mMinuteFiles.size() > 0) {
                for (MinuteFile minuteFile : mMinuteFiles) {
                    minuteFile.isChecked = false;
                    minuteFile.isTitleSelected = false;
                }
            }
        }
        if (mFileGroupAdapter != null) {
            mFileGroupAdapter.setEditMode(editMode);
        }
    }

    public interface OnFileStateChangeListener {
        void deleted();

        void download();
    }

    public void getFileList() {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
        switch (mType) {
            case AlbumNewFragment.ALBUM_PHONE://手机相册
                getPhoneAlbum();
                break;
            case AlbumNewFragment.ALBUM_RECORDER://摄像头
                CameraUtils.getFileList(new CameraUtils.GetFileListListener() {
                    @Override
                    public void success(List<FileDomain> fileDomains) {
                        mCameraFiles = fileDomains;
                        if (mCameraFiles == null || mCameraFiles.size() == 0) {
                            ToastUtil.showShortToast(mContext, getString(R.string.no_file));
                            mSwipeRefreshLayout.setRefreshing(false);
                            sendMessage(EMPTY);
                        } else if (mCameraFiles.size() >= 600) {
                            showFileSizeDialog();
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    mMinuteFiles = handlerRecorderData(mCameraFiles);
                                    sendMessage(LOCALFILESTART);
                                }
                            }).start();
                        } else {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    mMinuteFiles = handlerRecorderData(mCameraFiles);
                                    sendMessage(LOCALFILESTART);
                                }
                            }).start();

                        }
                    }

                    @Override
                    public void error(String error) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        showErrorDialog(ERROR_GET_FILELIST);
                    }
                });
                break;
            default://本地文件
                if (mFilePath != null) {
                    getLocalFile();
                }
                break;
        }
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setOnFileStateChangeListener(OnFileStateChangeListener onFileStateChangeListener) {
        mOnFileStateChangeListener = onFileStateChangeListener;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_delete:
                if (VoiceManager.selectedMinuteFile.size() > 0) {
                    deleteFile();
                } else {
                    ToastUtil.showShortToast(mContext, getString(R.string.please_select_file));
                }
                break;
            case R.id.iv_download:
                if (VoiceManager.selectedMinuteFile.size() > 0) {
                    downloadFile();
                } else {
                    ToastUtil.showShortToast(mContext, getString(R.string.please_select_file));
                }
                break;
        }
    }

    private void deleteRecordFile(@NonNull final ArrayList<FileDomain> deleteList) {
        if (deleteList.size() > 0) {
            if (deleteList.get(deleteList.size() - 1).attr == 33) {
                deleteList.remove(deleteList.size() - 1);
                deleteRecordFile(deleteList);
            } else {
                CameraUtils.sendCmd(Contacts.URL_DELETE_ONE_FILE + deleteList.get(deleteList.size() - 1).fpath, new CameraUtils.CmdListener() {
                    @Override
                    public void onResponse(String response) {
                        InputStream is;
                        try {
                            is = new ByteArrayInputStream(response.getBytes("utf-8"));
                            DomParseUtils domParseUtils = new DomParseUtils();
                            MovieRecord record = domParseUtils.getParserXml(is);
                            if (record != null && record.getStatus().equals("0")) {
                                if (deleteList.get(deleteList.size() - 1).getSmallpath() != null) {
                                    //删除小档
                                    CameraUtils.sendCmd(Contacts.URL_DELETE_ONE_FILE + deleteList.get(deleteList.size() - 1).getSmallpath(), new CameraUtils.CmdListener() {
                                        @Override
                                        public void onResponse(String response) {
                                            //小档也删除了，删除下一个
                                            mCameraFiles.remove(deleteList.get(deleteList.size() - 1));
                                            deleteList.remove(deleteList.size() - 1);
                                            deleteRecordFile(deleteList);
                                        }

                                        @Override
                                        public void onErrorResponse(Exception volleyError) {
                                            hidepDialog();
                                            showToast(R.string.deleted_failure);
                                        }
                                    });
                                } else {
                                    //没有小档直接删除下一个t
                                    mCameraFiles.remove(deleteList.get(deleteList.size() - 1));
                                    deleteList.remove(deleteList.size() - 1);
                                    deleteRecordFile(deleteList);
                                }
                            } else {
                                //删除失败了直接删除下一个
                                deleteList.remove(deleteList.size() - 1);
                                deleteRecordFile(deleteList);
                            }
                        } catch (UnsupportedEncodingException e) {
                            //删除失败了直接删除下一个
                            deleteList.remove(deleteList.size() - 1);
                            deleteRecordFile(deleteList);
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onErrorResponse(Exception volleyError) {
                        hidepDialog();
                        showToast(R.string.deleted_failure);
                    }
                });
            }

        } else {
            mSwipeRefreshLayout.setRefreshing(true);
            hidepDialog();
//            getFileList();
            handlerRecorderData(mCameraFiles);
            sendMessage(LOCALFILESTART);
        }
    }


    @SuppressLint("SimpleDateFormat")
    private ArrayList<MinuteFile> handlerRecorderData(@NonNull List<FileDomain> cameraFiles) {
        for (FileDomain fileDomain : cameraFiles) {
            if (fileDomain.getFpath().toLowerCase().endsWith(".mov") || fileDomain.getFpath().toLowerCase().endsWith(".mp4") || fileDomain.getFpath().toLowerCase().endsWith(".ts")) {
                fileDomain.isPicture = false;
            }
        }
        Collections.sort(cameraFiles, new Comparator<FileDomain>() {
            @Override
            public int compare(FileDomain o1, FileDomain o2) {
              if (o2.timeCode > o1.timeCode) {
                    return 1;
                } else if (o2.timeCode < o1.timeCode) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        ArrayList<MinuteFile> minuteFiles = new ArrayList<>();
        mMinuteFiles.clear();
        MinuteFile minuteFile = new MinuteFile();
        for (int i = 0; i < cameraFiles.size(); i++) {
            FileDomain fileDomain = cameraFiles.get(i);
            long lastModified = getAbsTime(CameraUtils.currentProduct == CameraUtils.PRODUCT.SJ ? fileDomain.upTime : fileDomain.time);
            String time = simpleDateFormat.format(new Date(lastModified));
            String minuteTime = time.substring(0, 16);
            String hourTime = time.substring(0, 13);
            if (mMinuteFiles.size() == 0) {
                minuteFile.hourTime = hourTime;
                minuteFile.minuteTime = minuteTime;
                minuteFile.time = time;
                //第一次直接添加
                minuteFile.isTitle = true;//第一个是title
                mMinuteFiles.add(minuteFile);
                //加完title后加真正的内容
                minuteFile = new MinuteFile();
                minuteFile.hourTime = hourTime;
                minuteFile.minuteTime = minuteTime;
                minuteFile.time = time;
                minuteFile.fileDomains.add(cameraFiles.get(i));
                mMinuteFiles.add(minuteFile);
            } else if (minuteTime.equals(mMinuteFiles.get(mMinuteFiles.size() - 1).minuteTime)) {
                //说明是同一分钟的文件
                if (cameraFiles.get(i).isPicture) {
                    //如果是图片就放到同一分钟的图片下面
                    if (minuteFile.fileDomains.get(0).isPicture) {
                        minuteFile.fileDomains.add(cameraFiles.get(i));
                    } else {
                        minuteFile = new MinuteFile();
                        minuteFile.hourTime = hourTime;
                        minuteFile.minuteTime = minuteTime;
                        minuteFile.time = time;
                        minuteFile.fileDomains.add(cameraFiles.get(i));
                        mMinuteFiles.add(minuteFile);
                    }
                } else {
                    //视频不需要合并到通一分钟下
                    minuteFile = new MinuteFile();
                    minuteFile.hourTime = hourTime;
                    minuteFile.minuteTime = minuteTime;
                    minuteFile.time = time;
                    minuteFile.fileDomains.add(cameraFiles.get(i));
                    mMinuteFiles.add(minuteFile);
                }
            } else {
                //不是通一分钟的文件,需要判断是不是同一个小时的文件
//                if (i != cameraFiles.size() - 1) {
                if (hourTime.equals(mMinuteFiles.get(mMinuteFiles.size() - 1).hourTime)) {
                    //是同一个小时下的文件
                    minuteFile = new MinuteFile();
                    minuteFile.hourTime = hourTime;
                    minuteFile.minuteTime = minuteTime;
                    minuteFile.time = time;
                    minuteFile.fileDomains.add(cameraFiles.get(i));
                    mMinuteFiles.add(minuteFile);
                } else {
                    //不是一个小时下的文件
                    minuteFile = new MinuteFile();
                    minuteFile.isTitle = true;
                    minuteFile.hourTime = hourTime;
                    minuteFile.minuteTime = minuteTime;
                    minuteFile.time = time;
                    mMinuteFiles.add(minuteFile);
                    minuteFile = new MinuteFile();
                    minuteFile.hourTime = hourTime;
                    minuteFile.minuteTime = minuteTime;
                    minuteFile.time = time;
                    minuteFile.fileDomains.add(cameraFiles.get(i));
                    mMinuteFiles.add(minuteFile);
                }
//                }
            }
//            if ((i == cameraFiles.size() - 1) && i != 0) {
//                if (!hourTime.equals(minuteFiles.get(minuteFiles.size() - 1).hourTime)) {
//                    minuteFile = new MinuteFile();
//                    minuteFile.hourTime = hourTime;
//                    minuteFile.minuteTime = minuteTime;
//                    minuteFile.time = time;
//                    minuteFile.isTitle = true;
//                    minuteFiles.add(minuteFile);
//                    minuteFile = new MinuteFile();
//                    minuteFile.hourTime = hourTime;
//                    minuteFile.minuteTime = minuteTime;
//                    minuteFile.time = time;
//                    minuteFile.fileDomains.add(cameraFiles.get(i));
//                    minuteFiles.add(minuteFile);
//                }
//            }
        }
        return mMinuteFiles;
    }

    @SuppressLint("SimpleDateFormat")
    private ArrayList<MinuteFile> sortLocalFiles(List<FileDomain> fileList) {
//        ArrayList<MinuteFile> minuteFiles = null;
        mMinuteFiles.clear();
        if (fileList != null && fileList.size() > 0) {
            //先对文件列表排序，方便之后的分组
            Collections.sort(fileList, new Comparator<FileDomain>() {
                @Override
                public int compare(FileDomain o1, FileDomain o2) {
                    long l = new File(o1.fpath).lastModified();
                    long l1 = new File(o2.fpath).lastModified();
                    int ret;
                    if (l > l1) {
                        ret = -1;
                    } else if (l < l1) {
                        ret = 1;
                    } else {
                        ret = 0;
                    }
                    return ret;
                }
            });
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            minuteFiles = new ArrayList<>();
            MinuteFile minuteFile = new MinuteFile();
            for (int i = 0; i < fileList.size(); i++) {
                File file = new File(fileList.get(i).fpath);
                long lastModified = file.lastModified();//最后修改的时间戳
                String time = simpleDateFormat.format(new Date(lastModified));
                String minuteTime = time.substring(0, 16);
                String hourTime = time.substring(0, 10);//实际上是按照天来分的（hourTime指的是天）
                if (mMinuteFiles.size() == 0) {
                    minuteFile.hourTime = hourTime;
                    minuteFile.minuteTime = minuteTime;
                    minuteFile.time = minuteTime;
                    minuteFile.isTitle = true;
                    mMinuteFiles.add(minuteFile);
                    minuteFile = new MinuteFile();
                    minuteFile.hourTime = hourTime;
                    minuteFile.minuteTime = minuteTime;
                    minuteFile.time = time;
                    minuteFile.fileDomains.add(fileList.get(i));
                    mMinuteFiles.add(minuteFile);
                } else if (minuteTime.equals(mMinuteFiles.get(mMinuteFiles.size() - 1).minuteTime)) {
                    //如果这分钟已经添加过并且是图片，那么直接添加
                    if (fileList.get(i).isPicture) {
                        if (minuteFile.fileDomains.get(0).isPicture) {
                            minuteFile.fileDomains.add(fileList.get(i));
                        } else {
                            minuteFile = new MinuteFile();
                            minuteFile.hourTime = hourTime;
                            minuteFile.minuteTime = minuteTime;
                            minuteFile.time = minuteTime;
                            minuteFile.fileDomains.add(fileList.get(i));
                            mMinuteFiles.add(minuteFile);
                        }
                    } else {
                        minuteFile = new MinuteFile();
                        minuteFile.hourTime = hourTime;
                        minuteFile.minuteTime = minuteTime;
                        minuteFile.time = minuteTime;
                        minuteFile.fileDomains.add(fileList.get(i));
                        mMinuteFiles.add(minuteFile);
                    }
                } else {
//                    if (i != fileList.size() - 1) {
                    if (hourTime.equals(mMinuteFiles.get(mMinuteFiles.size() - 1).hourTime)) {
                        minuteFile = new MinuteFile();
                        minuteFile.hourTime = hourTime;
                        minuteFile.minuteTime = minuteTime;
                        minuteFile.time = minuteTime;
                        minuteFile.fileDomains.add(fileList.get(i));
                        mMinuteFiles.add(minuteFile);
                    } else {
                        minuteFile = new MinuteFile();
                        minuteFile.hourTime = hourTime;
                        minuteFile.minuteTime = minuteTime;
                        minuteFile.time = minuteTime;
                        minuteFile.isTitle = true;
                        mMinuteFiles.add(minuteFile);
                        minuteFile = new MinuteFile();
                        minuteFile.hourTime = hourTime;
                        minuteFile.minuteTime = minuteTime;
                        minuteFile.time = time;
                        minuteFile.fileDomains.add(fileList.get(i));
                        mMinuteFiles.add(minuteFile);
                    }
//                    }
                }
//                if ((i == fileList.size() - 1) && i != 0) {
//                    //如果是最后一条且不是第一条
//                    if (!hourTime.equals(minuteFiles.get(minuteFiles.size() - 1).hourTime)) {
//                        minuteFile = new MinuteFile();
//                        minuteFile.hourTime = hourTime;
//                        minuteFile.minuteTime = minuteTime;
//                        minuteFile.time = minuteTime;
//                        minuteFile.isTitle = true;
//                        minuteFiles.add(minuteFile);
//                        minuteFile = new MinuteFile();
//                        minuteFile.hourTime = hourTime;
//                        minuteFile.minuteTime = minuteTime;
//                        minuteFile.time = time;
//                        minuteFile.fileDomains.add(fileList.get(i));
//                        minuteFiles.add(minuteFile);
//                    }
//                }
            }

        }
        return mMinuteFiles;
    }

    public static long getAbsTime(String user_time) {
        long re_time = 0;
        try {
            SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getInstance();
            dateFormat.applyPattern("yyyy/MM/dd HH:mm:ss");
            Date d;
            d = dateFormat.parse(user_time);
            re_time = d.getTime();
        } catch (Exception ignored) {
        }
        return re_time;
    }

    public static FileGridNewFragment1 newInstance(int columnCount, int type, String filePath) {
        FileGridNewFragment1 fragment = new FileGridNewFragment1();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        args.putInt(ARG_TYPE, type);
        args.putString(ARG_FILE_PATH, filePath);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            mType = getArguments().getInt(ARG_TYPE);
            mFilePath = getArguments().getString(ARG_FILE_PATH);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        LinearLayout rootView = (LinearLayout) View.inflate(getContext(), R.layout.fragment_file_grid_new, null);
        VoiceManager.selectedMinuteFile.clear();
        mBottomLayout = (RelativeLayout) rootView.findViewById(R.id.bottom_layout);
        mIvDownload = (ImageView) rootView.findViewById(R.id.iv_download);
        mIvDownload.setVisibility(mType == AlbumNewFragment.ALBUM_RECORDER ? View.VISIBLE : View.GONE);
        mIvDelete = rootView.findViewById(R.id.iv_delete);
        mIvDelete.setOnClickListener(this);
        mIvDownload.setOnClickListener(this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.srl);
        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.orange);
        mRecyclerView = (RecyclerView) mSwipeRefreshLayout.findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(new GridLayoutManager(mContext, mColumnCount));
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
        initPictures();
        initReceiver();
    }

    private void initReceiver() {
        IntentFilter filter = new IntentFilter(ACTION_SELECTED_FILE);
        mContext.registerReceiver(mFileSelectedReceiver, filter);
    }

    private BroadcastReceiver mFileSelectedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_SELECTED_FILE.endsWith(action)) {
                boolean hasFileSelected = intent.getBooleanExtra("hasFile", false);
                toggleIcon(hasFileSelected);
            }
        }
    };

    private void toggleIcon(boolean hasFileSelected) {
        mIvDownload.setBackgroundResource(hasFileSelected ? R.drawable.download : R.drawable.down);
        mIvDelete.setBackgroundResource(hasFileSelected ? R.drawable.btn_delete_selector : R.drawable.delete_normal);
    }

    public void initPictures() {
        if (!mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
        if (mMinuteFiles != null) {
            mMinuteFiles.clear();
        }
        switch (mType) {
            case AlbumNewFragment.ALBUM_PHONE://手机相册
                getPhoneAlbum();
                break;
            case AlbumNewFragment.ALBUM_RECORDER://摄像头
                getCameraAlbum();
                break;
            default://本地文件
                if (mFilePath != null) {
                    getLocalFile();
                }
                break;
        }

    }


    private void getCameraAlbum() {
        CameraUtils.getRecordStatus(new CameraUtils.RecordStatusListener() {
            @Override
            public void success(boolean isRecording) {
                if (isRecording) {
                    toggleRecordStatus(false);
                } else {
                    sendMessage(START);
                }
            }

            @Override
            public void error(String error) {
                mSwipeRefreshLayout.setRefreshing(false);
                showErrorDialog(ERROR_GET_RECORD_STATE);
            }
        });
    }

    private void sendMessage(int what) {
        Message message = mHandler.obtainMessage();
        message.what = what;
        mHandler.sendMessage(message);
    }

    private void toggleRecordStatus(boolean startRecord) {
        CameraUtils.toggleRecordStatus(startRecord, new CameraUtils.ToggleStatusListener() {
            @Override
            public void success() {
                sendMessage(START);
            }

            @Override
            public void error(String error) {
                showErrorDialog(ERROR_STOP_RECORD);
            }
        });
    }


    private int currentErrorCode = -1;

    private void showErrorDialog(int errorCode) {
        currentErrorCode = errorCode;
        String message = "";
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        switch (errorCode) {
            case ERROR_GET_FILELIST:
                message = getString(R.string.failed_get_filelist);
                break;
            case ERROR_GET_RECORD_STATE:
                message = getString(R.string.failed_get_recording_status);
                break;
            case ERROR_STOP_RECORD:
                message = getString(R.string.failed_stop_recording);
                break;
            case ERROR_GET_SDCARD:
                message = getString(R.string.failed_sdcard);
            default:
                break;
        }

        new AlertDialog.Builder(getContext()).setTitle(R.string.notice).setMessage(message).setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (currentErrorCode) {
                    case ERROR_GET_RECORD_STATE:
                        getCameraAlbum();
                        break;
                    case ERROR_STOP_RECORD:
                        toggleRecordStatus(false);
                        break;
                    case ERROR_GET_FILELIST:
//                        toggleRecordStatus(false);
                        getFileList();
                        break;
                    case ERROR_GET_SDCARD:
                        toggleRecordStatus(false);
                        break;
                    default:
                        break;
                }
            }
        }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getActivity().finish();
            }
        }).create().show();
    }

    private void showFileSizeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.notice);
        builder.setMessage(R.string.please_clear_files);
        builder.setPositiveButton(getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                });
        builder.create().show();
    }

    private void getLocalFile() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                File root = new File(mFilePath);
                File[] files = root.listFiles();
//                ArrayList<FileDomain> localFileList = new ArrayList<>();
                mCameraFiles.clear();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        String filepath = file.getAbsolutePath();
                        if (filepath.endsWith("JPG") || filepath.endsWith("jpg") || filepath.endsWith("PNG") || filepath.endsWith("png")) {
                            FileDomain fileDomain = new FileDomain();
                            fileDomain.isPicture = true;
                            fileDomain.fpath = filepath;
                            fileDomain.time = simpleDateFormat.format(new Date(file.lastModified()));
                            fileDomain.setName(file.getName());
                            mCameraFiles.add(fileDomain);
                        } else if (filepath.endsWith("MOV") || filepath.endsWith("mov") || filepath.endsWith("mp4") || filepath.endsWith("MP4") || filepath.endsWith("TS")||filepath.endsWith("ts")) {
                            FileDomain fileDomain = new FileDomain();
                            fileDomain.isPicture = false;
                            fileDomain.fpath = filepath;
                            fileDomain.time = simpleDateFormat.format(new Date(file.lastModified()));
                            fileDomain.setName(file.getName());
                            mCameraFiles.add(fileDomain);
                        }
                    }
                    if (mCameraFiles.size() == 0) {
                        sendMessage(EMPTY);
                        return;
                    }
                    sortLocalFiles(mCameraFiles);
                    sendMessage(LOCALFILESTART);
                } else {
                    sendMessage(EMPTY);
                }
            }
        }).start();
    }

    @SuppressLint("SimpleDateFormat")
    private void getPhoneAlbum() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Cursor photoCursor = mContext.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                        MediaStore.Images.Media.MIME_TYPE + "=? or " +
                                MediaStore.Images.Media.MIME_TYPE + "=?",
                        new String[]{"image/jpeg", "image/png"},
                        MediaStore.Images.Media.DATE_MODIFIED);
                //遍历相册
//                ArrayList<FileDomain> phonePhotoList = new ArrayList<>();
                mCameraFiles.clear();
                if (photoCursor != null) {
                    while (photoCursor.moveToNext()) {
                        String path = photoCursor.getString(photoCursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                        File file = new File(path);

                        //将图片路径添加到集合
                        FileDomain fileDomain = new FileDomain();
                        fileDomain.isPicture = true;
                        fileDomain.fpath = path;
                        fileDomain.setName(file.getName());
                        if (file.exists()) {
                            fileDomain.time = simpleDateFormat.format(new Date(file.lastModified()));
                        }
                        mCameraFiles.add(fileDomain);
                    }
                    photoCursor.close();
                }
                Cursor videoCursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, null, null, null);
                if (videoCursor != null) {
                    while (videoCursor.moveToNext()) {
                        String path = videoCursor.getString(videoCursor.getColumnIndex(MediaStore.MediaColumns.DATA));
                        File file = new File(path);
                        FileDomain fileDomain = new FileDomain();
                        fileDomain.setName(file.getName());
                        fileDomain.isPicture = false;
                        fileDomain.fpath = path;
                        if (file.exists()) {
                            fileDomain.time = simpleDateFormat.format(new Date(file.lastModified()));
                        }
                        mCameraFiles.add(fileDomain);
                    }
                    videoCursor.close();
                }
                if (mCameraFiles.size() == 0) {
                    sendMessage(EMPTY);
                    return;
                }
                sortLocalFiles(mCameraFiles);
                sendMessage(LOCALFILESTART);
            }
        }).start();

    }

    private ArrayList<FileDomain> mDeleteList;

    public void downloadFile() {
        for (MinuteFile minuteFile : mMinuteFiles) {
            if (!minuteFile.isTitle && minuteFile.isChecked) {
                MinuteFileDownloadManager.getInstance().download(minuteFile);
            }
        }
        VoiceManager.selectedMinuteFile.clear();
        if (mOnFileStateChangeListener != null) {
            mOnFileStateChangeListener.download();
        }
    }

    public void deleteFile() {
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.notice)
                .setMessage(R.string.navi_confDel)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showpDialog(R.string.deleting);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if (mType == AlbumNewFragment.ALBUM_RECORDER) {
                                    mDeleteList = new ArrayList<>();
                                    for (MinuteFile minuteFile : VoiceManager.selectedMinuteFile) {
                                        for (FileDomain fileDomain : minuteFile.fileDomains) {
                                            mDeleteList.add(fileDomain);
                                        }
                                    }
                                    VoiceManager.selectedMinuteFile.clear();
                                    sendMessage(DELETE_RECORD_FILE);
                                } else {
                                    ContentResolver contentResolver = getActivity().getContentResolver();
                                    for (MinuteFile minuteFile : VoiceManager.selectedMinuteFile) {
                                        for (FileDomain fileDomain : minuteFile.fileDomains) {
                                            File file = new File(fileDomain.fpath);
                                            if (file.isFile() && file.exists()) {
                                                if (file.delete()) {
                                                    String where = (fileDomain.isPicture ? MediaStore.Images.Media.DATA :
                                                            MediaStore.Video.Media.DATA) + "='" + fileDomain.fpath + "'";
                                                    contentResolver.delete(fileDomain.isPicture ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                                            : MediaStore.Video.Media.EXTERNAL_CONTENT_URI, where, null);
                                                    mCameraFiles.remove(fileDomain);
                                                }
                                            }
                                        }
                                    }
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            showToast(R.string.deleted_success);
                                        }
                                    });
                                    VoiceManager.selectedMinuteFile.clear();
                                    sortLocalFiles(mCameraFiles);
                                    sendMessage(LOCALFILESTART);
                                }
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mOnFileStateChangeListener != null) {
                                            mOnFileStateChangeListener.deleted();
                                        }
                                    }
                                });
                            }
                        }).start();

                    }
                }).setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext.unregisterReceiver(mFileSelectedReceiver);
    }
}
