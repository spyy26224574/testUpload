package com.adai.gkdnavi.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.camera.novatek.contacts.Contacts;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.RemoteActivity;
import com.adai.gkdnavi.utils.AppListAdapter;
import com.adai.gkdnavi.utils.BitmapHelper;
import com.adai.gkdnavi.utils.NetworkDownloadUtils;
import com.adai.gkdnavi.utils.ShareUtils;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.example.ipcamera.application.VLCApplication;
import com.example.ipcamera.domain.FileDomain;
import com.example.ipcamera.domain.MovieRecord;
import com.filepicker.imagebrowse.RemotePictureBrowseActivity;

import org.videolan.vlc.util.DomParseUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class PhotoCameraFragment extends Fragment {
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
    protected static final String TAG = "PhotoCameraFragment";
    private static final int VOLLEYTIMEOUT = 5000;
    // private List<FileDomain> list = new ArrayList<FileDomain>();
    // private List<FileDomain> movlist = new ArrayList<FileDomain>();
    // private List<FileDomain> jpglist = new ArrayList<FileDomain>();

    private List<FileDomain> listget = new ArrayList<>();
    private TextView mTextViewNoCameraPicture;
    private List<FileDomain> list = new ArrayList<>();
    private String deletefilename;
    private FileDomain deletefile;

    private Handler mHandler = new Handler() {
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
                    mTextViewNoCameraPicture.setVisibility(View.VISIBLE);
                    break;
                case NOTIFYQ:
                    mTextViewNoCameraPicture.setVisibility(View.GONE);
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
        listView = (ListView) view.findViewById(R.id.lv_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent intent = new Intent(mVideoCamerafragmentActivity, RemotePhotoPreviewActivity.class);
//                intent.putExtra("photos", (Serializable) list);
//                intent.putExtra("position", position);
//                startActivity(intent);
                ArrayList<String> pictureList = new ArrayList<String>();
                for (int i = 0; i < list.size(); i++) {
                    String fpath = list.get(i).fpath;
                    String path = Contacts.BASE_HTTP_IP + fpath.substring(2, fpath.length());
                    //String path = "http://192.168.1.254/CARDV/PHOTO/2016_0926_183805_006A.JPG";
                    String newpath = path.replace("\\", "/");
                    pictureList.add(newpath);
                }

                Intent picture = new Intent(getActivity(), RemotePictureBrowseActivity.class);
                picture.putExtra(RemotePictureBrowseActivity.KEY_MODE, RemotePictureBrowseActivity.MODE_NETWORK);
                picture.putStringArrayListExtra(RemotePictureBrowseActivity.KEY_TOTAL_LIST, pictureList);
                picture.putExtra(RemotePictureBrowseActivity.KEY_POSTION, position);
                getActivity().startActivity(picture);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                //长按事件
                showLongClickDialog(i);

//                deletefilename = list.get(i).getFpath();
//                deletefile = list.get(i);
//
//                AlertDialog.Builder builder = new AlertDialog.Builder(mVideoCamerafragmentActivity);
//                builder.setTitle(R.string.notice);
//                builder.setMessage("是否删除文件");
//
//                builder.setNegativeButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface arg0, int arg1) {
////                        Toast toast = Toast.makeText(mVideoCamerafragmentActivity, "你选择了删除\n" + deletefilename, Toast.LENGTH_SHORT);
////                        toast.show();
//                        deleteonefile(deletefilename);
//                    }
//                });
//
//                builder.setNeutralButton(getString(R.string.all_delete), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface arg0, int arg1) {
////                        Toast toast = Toast.makeText(mVideoCamerafragmentActivity, "你选择了全部删除", Toast.LENGTH_SHORT);
////                        toast.show();
//                        deleteallfile();
//                    }
//                });
//                builder.setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface arg0, int arg1) {
////                        Toast toast = Toast.makeText(mVideoCamerafragmentActivity, "你选择了取消", Toast.LENGTH_SHORT);
////                        toast.show();
//                    }
//                });
//
//                builder.create().show();//	Diglog的显示
                return true;
            }
        });
        mLinearLayoutLoading = (LinearLayout) view.findViewById(R.id.ll_loading);
        mTextViewNoCameraPicture = (TextView) view.findViewById(R.id.tv_no_camera_picture);
        ll_button = (LinearLayout) mVideoCamerafragmentActivity.findViewById(R.id.ll_button);
        return view;
    }

    private String downloadingPath = null;

    private void downloadFile(String path) {
        downloadingPath = Contacts.URL_GET_THUMBNAIL_HEAD_PHOTO + path.substring(path.lastIndexOf("\\")+1);
        NetworkDownloadUtils.downloadFile(downloadingPath, new HttpUtil.DownloadCallback() {
            @Override
            public void onDownloadComplete(String path) {
//                Toast.makeText(mVideoCamerafragmentActivity,path , Toast.LENGTH_SHORT).show();
                downloadingPath = null;
                sharefile();
            }

            @Override
            public void onDownloading(int progress) {

            }

            @Override
            public void onDownladFail() {
                Toast.makeText(mVideoCamerafragmentActivity, getString(R.string.download_error), Toast.LENGTH_SHORT).show();
                downloadingPath = null;
            }
        });
    }


    private void showLongClickDialog(int i) {
        deletefilename = list.get(i).getFpath();
        deletefile = list.get(i);
        List<String> items = new ArrayList<>();
        items.add(getString(R.string.action_delete));
        items.add(getString(R.string.all_delete));
        items.add(getString(R.string.share));
        items.add(getString(R.string.cancel));

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
                    case 2://分享
                        downloadFile(deletefilename);
                        break;
                    case 3://取消
                        break;
                    default:
                        break;
                }
            }
        }).create().show();
    }

    private void sharefile() {
//        ArrayList<Uri> photos = new ArrayList<>();
        String localpath = VLCApplication.DOWNLOADPATH + "/" + deletefilename.substring(deletefilename.lastIndexOf("\\")+1);
//        photos.add(Uri.fromFile(new File(localpath)));
        ArrayList<String> paths = new ArrayList<>();
        paths.add(localpath);
        new ShareUtils().sharePhoto(mVideoCamerafragmentActivity, paths);
    }

    private void deleteonefile(final String deletefilename) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mVideoCamerafragmentActivity);
        builder.setTitle(R.string.notice);
        builder.setMessage(R.string.wheter_delete_file);

        builder.setNegativeButton(R.string.action_delete, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                sendCommand(Contacts.URL_DELETE_ONE_FILE + deletefilename);
            }
        });
        builder.setPositiveButton(R.string.cancel, null);

        builder.create().show(); //	Diglog的显示
    }

    private void deleteallfile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mVideoCamerafragmentActivity);
        builder.setTitle(R.string.notice);
        builder.setMessage(R.string.delete_all_file_notice);

        builder.setNegativeButton(R.string.action_delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                // TODO: 缺少参数
                sendCommand(Contacts.URL_MOVIE_RECORD);
            }
        });
        builder.setPositiveButton(R.string.cancel, null);

        builder.create().show(); //	Diglog的显示
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
                            list.remove(deletefile);
                            listget.remove(deletefile);
                            RemoteActivity activity = (RemoteActivity) getActivity();
                            activity.setFiles(listget);

                            adapter.notifyDataSetChanged();
                        } else if (url.contains("2001")) {
                            sendCommand(Contacts.URL_DELETE_ALL);
                        } else if (url.contains("4004")) {
                            list.clear();
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
// FIXME: 2016/10/28 暂时修改规则
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
        // TODO Auto-generated method stub
        super.onResume();
        if (adapter != null) {
            adapter.startObserver();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.e("9527", "P hidden = " + hidden);
        if (!hidden) {
            list.clear();
            initDataWithActivity();
        }
    }

    public void initDataWithActivity() {
        RemoteActivity activity = (RemoteActivity) getActivity();
        listget = activity.getFiles();
        if (listget != null && listget.size() > 0) {
            handleData(listget);
        }
    }

    private void handleData(List<FileDomain> listget) {
        list.clear();
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
//                    .equalsIgnoreCase("A:\\CARDV\\PHOTO")) {
//                list.add(file);
//
//            }
//        }
        for (FileDomain fileDomain : listget) {
            if (fileDomain.getFpath().toLowerCase().contains("\\photo\\")) {
                list.add(fileDomain);
            }
        }
        Iterator<FileDomain> iterator = list.iterator();
        while (iterator.hasNext()) {
            FileDomain fileDomain = (FileDomain) iterator
                    .next();
            if (!fileDomain.getName().contains("JPG")) {
                iterator.remove();
            }
        }
        Collections.sort(list, new Comparator<FileDomain>() {

            @Override
            public int compare(FileDomain lhs,
                               FileDomain rhs) {
                long timeCode = lhs.timeCode;
                long timeCode2 = rhs.timeCode;
                if (timeCode <= timeCode2) {
                    return 1;
                }
                return -1;
            }
        });
        adapter = new AppListAdapter(list);
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("9527", "onDestroy");
        app.setAllowDownloads(false);
    }

}
