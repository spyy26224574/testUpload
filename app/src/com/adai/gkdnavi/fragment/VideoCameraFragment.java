package com.adai.gkdnavi.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.camera.novatek.contacts.Contacts;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.RemoteActivity;
import com.adai.gkdnavi.gpsvideo.GpsVideoActivity;
import com.adai.gkdnavi.utils.AppListAdapter;
import com.adai.gkdnavi.utils.BitmapHelper;
import com.adai.gkdnavi.utils.DownloadManager;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MovieRecord;

import org.videolan.vlc.util.DomParseUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class VideoCameraFragment extends Fragment {
    private FragmentActivity mVideoCamerafragmentActivity;
    private ListView listView;
    private LinearLayout ll_button;
    private LinearLayout mLinearLayoutLoading;
    private AppListAdapter adapter;
    private String type;
    private static final int START = 0;
    private static final int END = 1;
    private static final int NOTIFY = 2;
    private static final int NOTIFYQ = 3;
    private VLCApplication app;
    protected static final String TAG = "VideoCameraFragment";
    private static final int VOLLEYTIMEOUT = 5000;
    //private List<FileDomain> list = new ArrayList<FileDomain>();
    // private List<FileDomain> movlist = new ArrayList<FileDomain>();
    // private List<FileDomain> jpglist = new ArrayList<FileDomain>();
    private List<FileDomain> listget = new ArrayList<FileDomain>();
    private TextView mTextViewNoCameraVideo;

    private String camera_version;

    private static final String CHECK_CAMERA_VERSION = "V0.3.3";

    List<FileDomain> list = new ArrayList<FileDomain>();
    List<FileDomain> newlist = new ArrayList<FileDomain>();
    private String deletefilename;
    private String deletefilesmallname;
    private FileDomain deletefile;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case START:
                    mLinearLayoutLoading.setVisibility(View.VISIBLE);
                    break;
                case END:
                    mLinearLayoutLoading.setVisibility(View.GONE);
                    break;
                case NOTIFY:
                    listView.setVisibility(View.GONE);
                    mTextViewNoCameraVideo.setVisibility(View.VISIBLE);
                    break;
                case NOTIFYQ:
                    mTextViewNoCameraVideo.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                default:
                    break;
            }
        }

        ;

    };

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        mVideoCamerafragmentActivity = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.video_camer_fragment, container,
                false);
        BitmapHelper.cacheData.clear();
        app = (VLCApplication) getActivity().getApplicationContext();
        app.setAllowDownloads(true);
        SharedPreferences pref = app.getSharedPreferences("syllabus", Context.MODE_PRIVATE);
        camera_version = pref.getString("camera_version", "");
        listView = (ListView) view.findViewById(R.id.lv_view);
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(final AdapterView<?> parent, View view, int position, long id) {

//                Intent mIntent = new Intent();
//                Bundle bundle = new Bundle();
                final Object item = parent.getAdapter().getItem(position);
                if (item instanceof FileDomain) {
                    String path;
                    String cachePath = DownloadManager.getInstance().getCachePath(((FileDomain) item).getName());
                    File cacheFile = new File(cachePath);
                    if (cacheFile.exists()) {
                        path = cachePath;
                    } else {
                        String filePath = ((FileDomain) item).getSmallpath();
                        if (TextUtils.isEmpty(filePath)) {
                            new AlertDialog.Builder(mVideoCamerafragmentActivity)
                                    .setMessage(R.string.Online_preview_may_not_be_smooth)
                                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            String path = ((FileDomain) item).getFpath();
                                            path = getPath(path, (FileDomain) item);
                                            toVideoActivity(path);
                                        }
                                    }).setNegativeButton(R.string.no, null)
                                    .show();

                            return;
                        }
                        path = getPath(filePath, (FileDomain) item);
                    }
                    toVideoActivity(path);
                }
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (!(adapter.getItem(i) instanceof FileDomain)) {
                    return false;
                }
                deletefile = (FileDomain) adapter.getItem(i);
                deletefilename = deletefile.getFpath();
                deletefilesmallname = deletefile.getSmallpath();
                Log.e("9527", "getFpath = " + deletefilename);
                Log.e("9527", "getSmallpath = " + deletefilesmallname);

                int indexPoint = deletefilename.lastIndexOf('\\');
                String fileStart = deletefilename.substring(0,
                        indexPoint);
                Log.e("9527", "fileStart = " + fileStart);
                //if (!fileStart.equalsIgnoreCase("A:\\CARDV\\MOVIE\\RO\\")) {
                if (deletefile.attr == 32) {
                    showLongClickDialog(i);
//                    AlertDialog.Builder builder = new AlertDialog.Builder(mVideoCamerafragmentActivity);
//                    builder.setTitle(R.string.notice);
//                    builder.setMessage("是否删除文件");
//
//                    builder.setNegativeButton(getContext().getString(R.string.delete), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface arg0, int arg1) {
////                        Toast toast = Toast.makeText(mVideoCamerafragmentActivity, "你选择了删除\n" + deletefilename, Toast.LENGTH_SHORT);
////                        toast.show();
//                            deleteonefile(deletefilename);
//                        }
//                    });
//
//                    builder.setNeutralButton(getContext().getString(R.string.all_delete), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface arg0, int arg1) {
////                        Toast toast = Toast.makeText(mVideoCamerafragmentActivity, "你选择了全部删除", Toast.LENGTH_SHORT);
////                        toast.show();
//                            deleteallfile();
//                        }
//                    });
//                    builder.setPositiveButton(getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface arg0, int arg1) {
////                        Toast toast = Toast.makeText(mVideoCamerafragmentActivity, "你选择了取消", Toast.LENGTH_SHORT);
////                        toast.show();
//                        }
//                    });
//
//                    builder.create().show();//	Diglog的显示
                } else {
                    showLongClickDialogLock(i);
                }
                return true;
            }
        });


        mLinearLayoutLoading = (LinearLayout) view
                .findViewById(R.id.ll_loading);
        mTextViewNoCameraVideo = (TextView) view.findViewById(R.id.tv_no_camera_video);
        ll_button = (LinearLayout) mVideoCamerafragmentActivity
                .findViewById(R.id.ll_button);
        return view;
    }

    private void showLongClickDialog(int i) {
        deletefilename = newlist.get(i).getFpath();
        deletefile = newlist.get(i);
        List<String> items = new ArrayList<>();
        items.add(getContext().getString(R.string.action_delete));
        items.add(getContext().getString(R.string.all_delete));
        items.add(getContext().getString(R.string.share));
        items.add(getContext().getString(R.string.cancel));

        new AlertDialog.Builder(mVideoCamerafragmentActivity).setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0://删除
                        deleteonefile(deletefilename);
                        break;
                    case 1://全部删除
                        deleteallfile();
                        break;
                    case 2://分享视频
                        sharefile(deletefilename);
                        break;
                    case 3://取消
                        break;
                }
            }
        }).create().show();
    }

    private void showLongClickDialogLock(int i) {
        deletefilename = newlist.get(i).getFpath();
        deletefile = newlist.get(i);
        List<String> items = new ArrayList<>();
        items.add(getContext().getString(R.string.share));
        items.add(getContext().getString(R.string.cancel));

        new AlertDialog.Builder(mVideoCamerafragmentActivity).setItems(items.toArray(new String[items.size()]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0://分享视频
                        sharefile(deletefilename, true);
                        break;
                    case 1://取消
                        break;
                }
            }
        }).create().show();
    }

    private void toVideoActivity(String path) {
        Intent intent = new Intent(mVideoCamerafragmentActivity, GpsVideoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("videoPath", path);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @NonNull
    private String getPath(String path, FileDomain item) {
        String name = null;
        int type = PreferenceManager.getDefaultSharedPreferences(mVideoCamerafragmentActivity).getInt("video_type", 1);
        if (type == 1) {
            name = item.getSmallname();
        }
        if (TextUtils.isEmpty(name)) {
            name = item.getName();
        }
        //// FIXME: 2016/10/28 暂时更改过滤规则
//        if (path.contains("RO")) {
//            path = Contacts.URL_GET_THUMBNAIL_HEAD_RO + name;
//        } else {
//            path = Contacts.URL_GET_THUMBNAIL_HEAD_MOVIE + name;
//        }
        path = Contacts.BASE_HTTP_IP + path.substring(path.indexOf(":") + 1);
        path = path.replace("\\", "/");
        Log.e(TAG, "getPath: " + path);
        return path;
    }

    private void deleteonefile(final String deletefilename) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mVideoCamerafragmentActivity);
        builder.setTitle(R.string.notice);
        builder.setMessage(R.string.wheter_delete_file);

        builder.setNegativeButton(R.string.action_delete, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
//                Toast toast = Toast.makeText(mVideoCamerafragmentActivity, "你选择了确认删除" + deletefilename, Toast.LENGTH_SHORT);
//                toast.show();
                sendCommand(Contacts.URL_DELETE_ONE_FILE + deletefilename);
            }
        });
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
//                Toast toast = Toast.makeText(mVideoCamerafragmentActivity, "你选择了取消", Toast.LENGTH_SHORT);
//                toast.show();
            }
        });

        builder.create().show(); //	Diglog的显示
    }

    private void deleteallfile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mVideoCamerafragmentActivity);
        builder.setTitle(R.string.notice);
        builder.setMessage(R.string.delete_all_file_notice);

        builder.setNegativeButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
//                Toast toast = Toast.makeText(mVideoCamerafragmentActivity, "你选择了删除所有文件", Toast.LENGTH_SHORT);
//                toast.show();
                sendCommand(Contacts.URL_MOVIE_RECORD);
            }
        });
        builder.setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
//                Toast toast = Toast.makeText(mVideoCamerafragmentActivity, "你选择了取消", Toast.LENGTH_SHORT);
//                toast.show();
            }
        });

        builder.create().show(); //	Diglog的显示
    }

    private void sharefile(String name) {
        sharefile(name, false);
    }

    private void sharefile(String name, boolean islock) {
        Intent share = new Intent(Intent.ACTION_SEND);
//        String path = (islock ? Contacts.URL_GET_THUMBNAIL_HEAD_RO : Contacts.URL_GET_THUMBNAIL_HEAD_MOVIE) + name.substring(name.lastIndexOf("\\")+1);
        String path = Contacts.BASE_HTTP_IP + name.substring(name.indexOf(":") + 1);
        path = path.replace("\\", "/");
        Log.e("9527", " path = " + path);
        share.setType("video/*");
        share.setComponent(new ComponentName(getActivity().getPackageName(), "com.adai.gkdnavi.EditVideoActivity"));
        share.putExtra("videoType", 0);
        Log.e("9527", " Uri.parse(path) = " + Uri.parse(path));
        if (!TextUtils.isEmpty(deletefilesmallname)) {
//            String smallPath = (islock?Contacts.URL_GET_THUMBNAIL_HEAD_RO:Contacts.URL_GET_THUMBNAIL_HEAD_MOVIE) + deletefilesmallname.substring(deletefilesmallname.length() - 25);
            String smallPath = Contacts.BASE_HTTP_IP + deletefilesmallname.substring(deletefilesmallname.indexOf(":") + 1);
            smallPath = smallPath.replace("\\", "/");
            share.putExtra("smallUri", Uri.parse(smallPath));
            Log.e(TAG, "sharefile: smallPath"+smallPath);
        }
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
        startActivity(share);

    }


    private void sendCommand(final String url) {
        StringRequest req = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e(TAG, response);
                try {
                    InputStream is = new ByteArrayInputStream(response.getBytes("utf-8"));
                    DomParseUtils domParseUtils = new DomParseUtils();
                    MovieRecord record = domParseUtils.getParserXml(is);
                    if (record != null && record.getStatus().equals("0")) {
                        if (url.contains("4003")) {
                            //删除成功
                            Toast toast = Toast.makeText(mVideoCamerafragmentActivity, R.string.deleted_success, Toast.LENGTH_SHORT);
                            toast.show();

                            //list.remove(deletefile);
                            newlist.remove(deletefile);


                            for (int i = 0; i < listget.size(); i++) {
                                FileDomain file = listget.get(i);
                                if (file.getFpath().equalsIgnoreCase(deletefilename) || file.getFpath().equalsIgnoreCase(deletefilesmallname)) {
                                    listget.remove(file);
                                }
                            }

                            RemoteActivity activity = (RemoteActivity) getActivity();
                            activity.setFiles(listget);
                            adapter.notifyDataSetChanged();
                            if (url.contains(deletefilename)) {
                                sendCommand(Contacts.URL_DELETE_ONE_FILE + deletefilesmallname);
                            }


                        } else if (url.contains("2001")) {
                            sendCommand(Contacts.URL_DELETE_ALL);
                        } else if (url.contains("4004")) {
                            listdeleteall(newlist);
                            adapter.notifyDataSetChanged();
                            listdeleteall(listget);
                            RemoteActivity activity = (RemoteActivity) getActivity();
                            activity.setFiles(listget);

                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
                Log.e(TAG, "Error: " + error.getMessage());
            }
        });
        req.setRetryPolicy(new DefaultRetryPolicy(VOLLEYTIMEOUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VLCApplication.getInstance().addToRequestQueue(req);
    }

    private void listdeleteall(List<FileDomain> listget) {
        List<FileDomain> listdelete = new ArrayList<FileDomain>();
        for (int i = 0; i < listget.size(); i++) {
            FileDomain file = listget.get(i);
            String fpath = file.getFpath();

            int indexPoint = fpath.lastIndexOf('\\');
            String fileStart = fpath.substring(0,
                    indexPoint);
//// FIXME: 2016/10/28 暂时修改规则
            if (!fileStart
                    .equalsIgnoreCase("\\RO\\")) {

                listdelete.add(file);

            }
        }
        listget.removeAll(listdelete);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {

        super.onResume();
        if (adapter != null) {
            adapter.startObserver();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.e("9527", "V hidden = " + hidden);
        if (!hidden) {
            list.clear();
            newlist.clear();
            initdatawithactivity();
        }
    }

//    private void initData() {
//        Message msg = Message.obtain();
//        msg.what = START;
//        mHandler.sendMessage(msg);
//        ConnectivityManager manager = (ConnectivityManager) mVideoCamerafragmentActivity
//                .getSystemService(Context.CONNECTIVITY_SERVICE);
//        State wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
//        if (wifi == State.CONNECTED) {
//            Log.e(TAG, "CONNECTED");
//            //list.clear();
//            accessInternet(Contacts.URL_FILE_LIST);
//        } else {
//            // Toast.makeText(DownActivity.this,"", 0).show();
//        }
//    }

    public void initdatawithactivity() {
        RemoteActivity activity = (RemoteActivity) getActivity();
        listget = activity.getFiles();
        if (listget != null && listget.size() > 0) {
            handleData(listget);
        }
    }

    private void handleData(List<FileDomain> listget) {
//        for (int i = 0; i < listget.size(); i++) {
//            FileDomain file = new FileDomain();
//            file = listget.get(i);
//            String fpath = file.getFpath();
//
//            int indexPoint = fpath.lastIndexOf('\\');
//            String fileStart = fpath.substring(0,
//                    indexPoint);
//
//            if (fileStart
//                    .equalsIgnoreCase("A:\\CARDV\\MOVIE")
//                    || fileStart
//                    .equalsIgnoreCase("A:\\CARDV\\MOVIE\\RO")) {
//                list.add(file);
//
//            }
//        }
        for (FileDomain fileDomain : listget) {
            if (fileDomain.getFpath().toLowerCase().contains("\\movie\\")) {
                list.add(fileDomain);
            }
        }
        Collections.sort(list, new Comparator<FileDomain>() {

            @Override
            public int compare(FileDomain lhs, FileDomain rhs) {


                return lhs.fpath.compareTo(rhs.fpath);
            }

        });

        int size = list.size();
        for (int i = 0; i < size; i++) {
            FileDomain file1 = list.get(i);
            FileDomain file2 = null;
            if ((i + 1) < size) {
                file2 = list.get(i + 1);
            }
//            Log.e("file", "filename=" + file1.fpath + ",\n" + (file2 != null ? file2.fpath : ""));
            if (file2 != null &&file1.fpath.endsWith("A.MOV") && file2.fpath.endsWith("B.MOV") &&
                    Math.abs(getAbsTime(file1.fpath) - getAbsTime(file2.fpath)) < 3000 &&
                    (file1.fpath.substring(0, 17)).equals(file2.fpath.substring(0, 17))) {
                file1.setSmallpath(file2.fpath);
                file1.setSmallname(file2.name);
                newlist.add(file1);
                i++;
            } else if (file2 != null && file2.fpath.endsWith("A.MOV") && file1.fpath.endsWith("B.MOV")
                    && Math.abs(getAbsTime(file1.fpath) - getAbsTime(file2.fpath)) < 3000 &&
                    (file1.fpath.substring(0, 17)).equals(file2.fpath.substring(0, 17))) {
                file2.setSmallpath(file1.fpath);
                file2.setSmallname(file1.name);
                newlist.add(file2);
                i++;
            } else {
                newlist.add(file1);
            }
        }
        Collections.sort(newlist, new Comparator<FileDomain>() {

            @Override
            public int compare(FileDomain lhs, FileDomain rhs) {

                long timeCode = lhs.timeCode;
                long timeCode2 = rhs.timeCode;
                if (timeCode <= timeCode2) {
                    return 1;
                }
                return -1;
            }

        });
        adapter = new AppListAdapter(newlist);
        //}
        listView.setAdapter(adapter);
        adapter.startObserver();
        Message msg = Message.obtain();
        msg.what = END;
        mHandler.sendMessage(msg);
        if (list.size() == 0) {
            mHandler.removeMessages(NOTIFYQ);
            mHandler.sendEmptyMessage(NOTIFY);
        } else {
            mHandler.removeMessages(NOTIFY);
            mHandler.sendEmptyMessage(NOTIFYQ);
        }
    }

    public static long getAbsTime(String user_time) {
        long re_time = 0;

        int indexPoint = user_time.lastIndexOf('\\');
        String time = user_time.substring(indexPoint + 1,
                indexPoint + 17);
        Log.e("9527", "time = " + time);


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MMdd_HHmmss");
        Date d;
        try {
            d = sdf.parse(time);
            re_time = d.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return re_time;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        Log.e("9527", "onDestroy");
        app.setAllowDownloads(false);
    }

}
