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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.adai.camera.novatek.contacts.Contacts;
import com.adai.camera.novatek.util.CameraUtils;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.adapter.FileGroupAdapter;
import com.adai.gkdnavi.fragment.square.AlbumNewFragment;
import com.adai.gkdnavi.fragment.square.LineDecoration;
import com.adai.gkdnavi.utils.MinuteFileDownloadManager;
import com.adai.gkdnavi.utils.VoiceManager;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.HourFile;
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
 * Created by huangxy on 2016/12/3.
 */

public class FileGridNewFragment extends BaseFragment implements View.OnClickListener {
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
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ARG_TYPE = "type";
    private static final String ARG_FILE_PATH = "file_path";
    private int mColumnCount;
    private int mType;
    private String mFilePath;
    private ArrayList<HourFile> mHourFiles;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private static final int LOCALFILESTART = 1;
    private FileGroupAdapter mFileGroupAdapter;
    private List<FileDomain> mCameraFiles;
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
                        mFileGroupAdapter.notifyDataSetChanged();
                    }
                    Toast.makeText(mContext, getString(R.string.no_file), Toast.LENGTH_SHORT).show();
                    break;
                case START:
//                    CameraUtils.getSDCardStatus(new CameraUtils.SDCardStatusListener() {
//                        @OverrideT
//                        public void success(boolean hasSDCard) {
//                            if (hasSDCard) {
                    CameraUtils.changeMode(CameraUtils.MODE_PLAYBACK, new CameraUtils.ModeChangeListener() {
                        @Override
                        public void success() {
                            getFileList();
                        }

                        @Override
                        public void failure(Throwable throwable) {
                            showErrorDialog(ERROR_GET_FILELIST);
                        }
                    });
//                            } else {
//                                showErrorDialog(ERROR_GET_SDCARD);
//                            }
//                        }

//                        @Override
//                        public void error(String error) {
//                            showErrorDialog(ERROR_GET_SDCARD);
//                        }
//                    });
                    break;
                case LOCALFILESTART:
                    mSwipeRefreshLayout.setRefreshing(false);
                    mFileGroupAdapter = new FileGroupAdapter(getActivity(), mHourFiles, mType);
                    mRecyclerView.setAdapter(mFileGroupAdapter);
                    break;
                case FREASH:
                    mSwipeRefreshLayout.setRefreshing(false);
                    hidepDialog();
                    initPictures();
                    break;
            }
        }

    };
    private View mIvDelete;

    public interface OnFileStateChangeListener {
        void deleted();

        void download();
    }

    private void getFileList() {
        CameraUtils.getFileList(new CameraUtils.GetFileListListener() {
            @Override
            public void success(List<FileDomain> fileDomains) {
                mCameraFiles = fileDomains;
                if (mCameraFiles == null || mCameraFiles.size() == 0) {
                    Toast.makeText(mContext, getString(R.string.no_file), Toast.LENGTH_SHORT).show();
                    mSwipeRefreshLayout.setRefreshing(false);
                } else if (mCameraFiles.size() >= 600) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    showFileSizeDialog();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mHourFiles = handlerRecorderData(mCameraFiles);
                            sendMessage(LOCALFILESTART);
                        }
                    }).start();

                }
            }

            @Override
            public void error(String error) {
                showErrorDialog(ERROR_GET_FILELIST);
            }
        });
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setOnFileStateChangeListener(OnFileStateChangeListener onFileStateChangeListener) {
        mOnFileStateChangeListener = onFileStateChangeListener;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        VoiceManager.selectedMinuteFile.clear();
        toggleIcon(false);
        mBottomLayout.setVisibility(editMode ? View.VISIBLE : View.GONE);
        if (!isEditMode) {
            //退出编辑模式的时候全部变成非选中状态
            if (mHourFiles != null && mHourFiles.size() > 0) {
                for (HourFile hourFile : mHourFiles) {
                    hourFile.isChecked = false;
                    for (MinuteFile minuteFile : hourFile.minuteFiles) {
                        minuteFile.isChecked = false;
                    }
                }
            }
        }
        if (mFileGroupAdapter != null) {
            mFileGroupAdapter.setEditMode(editMode);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_delete:
                if (VoiceManager.selectedMinuteFile.size() > 0) {
                    deleteFile();
                } else {
                    Toast.makeText(mContext, getString(R.string.please_select_file), Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.iv_download:
                if (VoiceManager.selectedMinuteFile.size() > 0) {
                    downloadFile();
                } else {
                    Toast.makeText(mContext, getString(R.string.please_select_file), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
//    private void deleteOneFile(String filePath) {
//        if (mDeleteList.size() > 0) {
//            CameraUtils.sendCmd(Contacts.URL_DELETE_ONE_FILE + filePath, new CameraUtils.CmdListener() {
//                @Override
//                public void onResponse(String response) {
//                    InputStream is;
//                    try {
//                        is = new ByteArrayInputStream(response.getBytes("utf-8"));
//                        DomParseUtils domParseUtils = new DomParseUtils();
//                        MovieRecord record = domParseUtils.getParserXml(is);
//                        if (record != null && record.getStatus().equals("0")) {
//                            String nextDeleteFile = getNextDeleteFile(true);
//                            deleteOneFile(nextDeleteFile);
//                        }
//                    } catch (UnsupportedEncodingException e) {
//                        String nextDeleteFile = getNextDeleteFile(false);
//                        deleteOneFile(nextDeleteFile);
//                        e.printStackTrace();
//                    }
//                }
//
//                @Override
//                public void onErrorResponse(VolleyError volleyError) {
//                    String nextDeleteFile = getNextDeleteFile(false);
//                    deleteOneFile(nextDeleteFile);
//                }
//            });
//        } else {
//            sendMessage(FREASH);
//        }
//    }
//
//    private String getNextDeleteFile(boolean isDeleteSuccess) {
//        String filePath = null;
//        if (mDeleteList.size() > 0) {
//            String smallPath = mDeleteList.get(mDeleteList.size() - 1).getSmallpath();
//            if (isDeleteSuccess && !TextUtils.isEmpty(smallPath)) {
//                filePath = smallPath;
//            } else {
//                mDeleteList.remove(mDeleteList.size() - 1);
//                filePath = mDeleteList.get(mDeleteList.size() - 1).fpath;
//            }
//        }
//        return filePath;
//    }

    private void deleteRecordFile(@NonNull final ArrayList<FileDomain> deleteList) {
        if (deleteList.size() > 0) {
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
                                        deleteList.remove(deleteList.size() - 1);
                                        deleteRecordFile(deleteList);
                                    }

                                    @Override
                                    public void onErrorResponse(Exception volleyError) {
                                        //删除失败了直接删除下一个
                                        deleteList.remove(deleteList.size() - 1);
                                        deleteRecordFile(deleteList);
                                    }
                                });
                            } else {
                                //没有小档直接删除下一个t
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
                    //删除失败了直接删除下一个
                    deleteList.remove(deleteList.size() - 1);
                    deleteRecordFile(deleteList);
                }
            });
        } else {
            mSwipeRefreshLayout.setRefreshing(true);
            hidepDialog();
            getFileList();
        }
    }


    @SuppressLint("SimpleDateFormat")
    private ArrayList<HourFile> handlerRecorderData(@NonNull List<FileDomain> cameraFiles) {
        ArrayList<FileDomain> imgFiles = new ArrayList<>();
        ArrayList<FileDomain> movFiles = new ArrayList<>();
        for (FileDomain fileDomain : cameraFiles) {
            if (fileDomain.getFpath().toLowerCase().contains("\\movie\\")) {
                fileDomain.isPicture = false;
                movFiles.add(fileDomain);
            } else if (fileDomain.getFpath().toLowerCase().contains("\\photo\\") && fileDomain.getFpath().toLowerCase().contains(".jpg")) {
                imgFiles.add(fileDomain);
            }
        }
//        //先将Ro和非RO进行分组，以便之后区分大小档
//        Collections.sort(movFiles, new Comparator<FileDomain>() {
//            @Override
//            public int compare(FileDomain lhs, FileDomain rhs) {
//                return lhs.fpath.compareTo(rhs.fpath);
//            }
//        });
//        ArrayList<FileDomain> newMovFiles = new ArrayList<>();
//        int size = movFiles.size();
//        for (int i = 0; i < size; i++) {
//            FileDomain file1 = movFiles.get(i);
//            FileDomain file2 = null;
//            if ((i + 1) < size)
//                file2 = movFiles.get(i + 1);
//            if (file2 != null && file1.fpath.endsWith("A.MOV") && file2.fpath.endsWith("B.MOV") &&
//                    Math.abs(getAbsTime(file1.fpath) - getAbsTime(file2.fpath)) < 3000 &&
//                    (file1.fpath.substring(0, 17)).equals(file2.fpath.substring(0, 17))) {
//                file1.setSmallpath(file2.fpath);
//                file1.setSmallname(file2.name);
//                newMovFiles.add(file1);
//                i++;
//            } else if (file2 != null && file2.fpath.endsWith("A.MOV") && file1.fpath.endsWith("B.MOV")
//                    && Math.abs(getAbsTime(file1.fpath) - getAbsTime(file2.fpath)) < 3000 &&
//                    (file1.fpath.substring(0, 17)).equals(file2.fpath.substring(0, 17))) {
//                file2.setSmallpath(file1.fpath);
//                file2.setSmallname(file1.name);
//                newMovFiles.add(file2);
//                i++;
//            } else {
//                newMovFiles.add(file1);
//            }
//        }
        cameraFiles.clear();
        cameraFiles.addAll(imgFiles);
        cameraFiles.addAll(movFiles);
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
        ArrayList<HourFile> hourFiles = new ArrayList<>();
        HourFile hourFile = new HourFile();
        MinuteFile minuteFile = new MinuteFile();
        for (int i = 0; i < cameraFiles.size(); i++) {
            FileDomain fileDomain = cameraFiles.get(i);
            long lastModified = getAbsTime(CameraUtils.currentProduct == CameraUtils.PRODUCT.SJ ? fileDomain.upTime : fileDomain.time);
            String time = simpleDateFormat.format(new Date(lastModified));
            String minuteTime = time.substring(0, 16);
            String hourTime = time.substring(0, 13);
            if (hourFile.minuteFiles.size() == 0) {
                minuteFile.time = minuteTime;
                minuteFile.fileDomains.add(cameraFiles.get(i));
                hourFile.minuteFiles.add(minuteFile);
                hourFile.time = hourTime;
                hourFiles.add(hourFile);
            } else if (minuteTime.equals(hourFile.minuteFiles.get(hourFile.minuteFiles.size() - 1).time)) {
                //如果这分钟已经添加过并且是图片，那么直接添加
                if (cameraFiles.get(i).isPicture) {
                    if (minuteFile.fileDomains.get(0).isPicture) {
                        minuteFile.fileDomains.add(cameraFiles.get(i));
                    } else {
                        minuteFile = new MinuteFile();
                        minuteFile.time = minuteTime;
                        minuteFile.fileDomains.add(cameraFiles.get(i));
                        hourFile.minuteFiles.add(minuteFile);
                    }
                } else {
                    minuteFile = new MinuteFile();
                    minuteFile.time = minuteTime;
                    minuteFile.fileDomains.add(cameraFiles.get(i));
                    hourFile.minuteFiles.add(minuteFile);
                }
            } else {
                //如果这分钟没有添加过，那么将上次的minuteFile添加到hourFiles里
                if (i != cameraFiles.size() - 1) {
                    if (hourTime.equals(hourFiles.get(hourFiles.size() - 1).time)) {
                        minuteFile = new MinuteFile();
                        minuteFile.time = minuteTime;
                        minuteFile.fileDomains.add(cameraFiles.get(i));
                        hourFile.minuteFiles.add(minuteFile);
                    } else {
                        hourFile = new HourFile();
                        minuteFile = new MinuteFile();
                        hourFile.time = hourTime;
                        minuteFile.time = minuteTime;
                        minuteFile.fileDomains.add(cameraFiles.get(i));
                        hourFile.minuteFiles.add(minuteFile);
                        hourFiles.add(hourFile);
                    }
                }
            }
            if ((i == cameraFiles.size() - 1) && i != 0) {
                //如果是最后一条且不是第一条
                if (!hourTime.equals(hourFiles.get(hourFiles.size() - 1).time)) {
                    minuteFile = new MinuteFile();
                    minuteFile.time = minuteTime;
                    minuteFile.fileDomains.add(cameraFiles.get(i));
                    hourFile = new HourFile();
                    hourFile.time = hourTime;
                    hourFile.minuteFiles.add(minuteFile);
                    hourFiles.add(hourFile);
                }
            }
        }
        return hourFiles;
    }

    @SuppressLint("SimpleDateFormat")
    private ArrayList<HourFile> sortLocalFiles(ArrayList<FileDomain> fileList) {
        ArrayList<HourFile> hourFiles = null;
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
            hourFiles = new ArrayList<>();
            HourFile hourFile = new HourFile();
            MinuteFile minuteFile = new MinuteFile();
            for (int i = 0; i < fileList.size(); i++) {
                File file = new File(fileList.get(i).fpath);
                long lastModified = file.lastModified();//最后修改的时间戳
                String time = simpleDateFormat.format(new Date(lastModified));
                String minuteTime = time.substring(0, 13);
                String hourTime = time.substring(0, 10);
                if (hourFile.minuteFiles.size() == 0) {
                    minuteFile.time = minuteTime;
                    minuteFile.fileDomains.add(fileList.get(i));
                    //第一次往分钟的集合添加
                    hourFile.minuteFiles.add(minuteFile);
                    hourFile.time = hourTime;
                    hourFiles.add(hourFile);
                } else if (minuteTime.equals(hourFile.minuteFiles.get(hourFile.minuteFiles.size() - 1).time)) {
                    //如果这分钟已经添加过并且是图片，那么直接添加
                    if (fileList.get(i).isPicture) {
                        if (minuteFile.fileDomains.get(0).isPicture) {
                            minuteFile.fileDomains.add(fileList.get(i));
                        } else {
                            minuteFile = new MinuteFile();
                            minuteFile.time = minuteTime;
                            minuteFile.fileDomains.add(fileList.get(i));
                            hourFile.minuteFiles.add(minuteFile);
                        }
                    } else {
                        minuteFile = new MinuteFile();
                        minuteFile.time = minuteTime;
                        minuteFile.fileDomains.add(fileList.get(i));
                        hourFile.minuteFiles.add(minuteFile);
                    }
                } else {
                    //如果这分钟没有添加过，那么将上次的minuteFile添加到hourFiles里
                    if (i != fileList.size() - 1) {
                        if (hourTime.equals(hourFiles.get(hourFiles.size() - 1).time)) {
                            minuteFile = new MinuteFile();
                            minuteFile.time = minuteTime;
                            minuteFile.fileDomains.add(fileList.get(i));
                            hourFile.minuteFiles.add(minuteFile);
                        } else {
                            hourFile = new HourFile();
                            minuteFile = new MinuteFile();
                            hourFile.time = hourTime;
                            minuteFile.time = minuteTime;
                            minuteFile.fileDomains.add(fileList.get(i));
                            hourFile.minuteFiles.add(minuteFile);
                            hourFiles.add(hourFile);
                        }
                    }
                }
                if ((i == fileList.size() - 1) && i != 0) {
                    //如果是最后一条且不是第一条
                    if (!hourTime.equals(hourFiles.get(hourFiles.size() - 1).time)) {
                        minuteFile = new MinuteFile();
                        minuteFile.time = minuteTime;
                        minuteFile.fileDomains.add(fileList.get(i));
                        hourFile = new HourFile();
                        hourFile.time = hourTime;
                        hourFile.minuteFiles.add(minuteFile);
                        hourFiles.add(hourFile);
                    }
                }
            }

        }
        return hourFiles;
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

    public static FileGridNewFragment newInstance(int columnCount, int type, String filePath) {
        FileGridNewFragment fragment = new FileGridNewFragment();
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
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new LineDecoration(3));
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
        if (mHourFiles != null) {
            mHourFiles.clear();
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
                        toggleRecordStatus(false);
                        break;
                    case ERROR_GET_SDCARD:
                        toggleRecordStatus(false);
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
                ArrayList<FileDomain> localFileList = new ArrayList<>();
                if (files != null && files.length > 0) {
                    for (File file : files) {
                        String filepath = file.getAbsolutePath();
                        if (filepath.endsWith("JPG") || filepath.endsWith("jpg") || filepath.endsWith("PNG") || filepath.endsWith("png")) {
                            FileDomain fileDomain = new FileDomain();
                            fileDomain.isPicture = true;
                            fileDomain.fpath = filepath;
                            fileDomain.time = simpleDateFormat.format(new Date(file.lastModified()));
                            fileDomain.setName(file.getName());
                            localFileList.add(fileDomain);
                        } else if (filepath.endsWith("MOV") || filepath.endsWith("mov") || filepath.endsWith("mp4") || filepath.endsWith("MP4")|| filepath.endsWith("ts")|| filepath.endsWith("TS")) {
                            FileDomain fileDomain = new FileDomain();
                            fileDomain.isPicture = false;
                            fileDomain.fpath = filepath;
                            fileDomain.time = simpleDateFormat.format(new Date(file.lastModified()));
                            fileDomain.setName(file.getName());
                            localFileList.add(fileDomain);
                        }
                    }
                    if (localFileList.size() == 0) {
                        sendMessage(EMPTY);
                        return;
                    }
                    mHourFiles = sortLocalFiles(localFileList);
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
                ArrayList<FileDomain> phonePhotoList = new ArrayList<>();
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
                        phonePhotoList.add(fileDomain);
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
                        phonePhotoList.add(fileDomain);
                    }
                    videoCursor.close();
                }
                if (phonePhotoList.size() == 0) {
                    sendMessage(EMPTY);
                    return;
                }
                mHourFiles = sortLocalFiles(phonePhotoList);
                sendMessage(LOCALFILESTART);
            }
        }).start();

    }

    private ArrayList<FileDomain> mDeleteList;

    public void downloadFile() {
        for (HourFile hourFile : mHourFiles) {
            for (MinuteFile minuteFile : hourFile.minuteFiles) {
                if (minuteFile.isChecked) {
                    MinuteFileDownloadManager.getInstance().download(minuteFile);
                }
            }
        }
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
                                    for (HourFile hourFile : mHourFiles) {
                                        for (MinuteFile minuteFile : hourFile.minuteFiles) {
                                            if (minuteFile.isChecked) {
                                                for (FileDomain fileDomain : minuteFile.fileDomains) {
                                                    mDeleteList.add(fileDomain);
                                                }
                                            }
                                        }
                                    }
                                    sendMessage(DELETE_RECORD_FILE);
                                } else {
                                    ContentResolver contentResolver = getActivity().getContentResolver();
                                    for (HourFile hourFile : mHourFiles) {
                                        for (MinuteFile minuteFile : hourFile.minuteFiles) {
                                            if (minuteFile.isChecked) {
                                                for (FileDomain fileDomain : minuteFile.fileDomains) {
                                                    File file = new File(fileDomain.fpath);
                                                    if (file.isFile() && file.exists()) {
                                                        if (file.delete()) {
                                                            String where = (fileDomain.isPicture ? MediaStore.Images.Media.DATA :
                                                                    MediaStore.Video.Media.DATA) + "='" + fileDomain.fpath + "'";
                                                            contentResolver.delete(fileDomain.isPicture ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                                                    : MediaStore.Video.Media.EXTERNAL_CONTENT_URI, where, null);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    sendMessage(FREASH);
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
