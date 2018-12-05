package com.adai.gkdnavi.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.gkdnavi.LocalFile;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.fragment.LocalVideoFragment.ListAdapter.ViewHolder;
import com.adai.gkdnavi.gpsvideo.GpsVideoActivity;
import com.adai.gkdnavi.utils.StringUtils;
import com.bumptech.glide.Glide;
import com.example.ipcamera.application.VLCApplication;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LocalVideoFragment extends Fragment implements OnClickListener {
    private ListView listView;
    private RelativeLayout layout;
    private Button delete;
    private List<LocalFile> list = new ArrayList<>();
    public boolean isMulChoice = false; // 是否多选
    // ############
    protected static final String TAG = "LocalVideoActivity";
    private ListAdapter adapter;
    private LayoutInflater mInflater;
    private ViewHolder viewHolder;
    final String fileEndingVideo = "MOV";
    public List<LocalFile> selectid = new ArrayList<>();
    private FragmentActivity mActivity;
    private LinearLayout ll_button;
    private TextView mTextViewNoVideo;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.local_photo_activity, container, false);
        mTextViewNoVideo = (TextView) view.findViewById(R.id.tv_no_video);
        listView = (ListView) view.findViewById(R.id.listView);
        layout = (RelativeLayout) view.findViewById(R.id.relative);
        delete = (Button) view.findViewById(R.id.delete);
        ll_button = (LinearLayout) mActivity.findViewById(R.id.ll_button);
        delete.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        IntentFilter filter = new IntentFilter("MESSAGE");
        mActivity.registerReceiver(myLoaclVideo, filter);
        // ############
        createFolderDispList();
        adapter = new ListAdapter(mActivity);
        listView.setAdapter(adapter);
    }

    @Override
    public void onResume() {

        super.onResume();
        createFolderDispList();
        adapter = new ListAdapter(mActivity);
        listView.setAdapter(adapter);
        if (list.size() == 0) {
            mTextViewNoVideo.setVisibility(View.VISIBLE);
        } else {
            mTextViewNoVideo.setVisibility(View.GONE);
        }

        //System.out.println(TAG + "onResume###################");
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        mActivity.unregisterReceiver(myLoaclVideo);
    }

    private void createFolderDispList() {
        File filePath;
        // String filefirstPathText = null;
        // list = new ArrayList<LocalFile>();
        list.clear();
        String filefirstPathText = VLCApplication.DOWNLOADPATH;
        Log.e("mark", "filefirstPathText===   " + filefirstPathText);
        filePath = new File(filefirstPathText);
        // File[] fileList = getFile(filePath);
        File[] fileList = filePath.listFiles();
        /*
         * Log.e("mark", "fileList != null  " + String.valueOf(fileList != null)
		 * + "filePath.isDirectory()" + String.valueOf(filePath.isDirectory()));
		 */
        if (fileList != null && filePath.isDirectory()) {
            Log.e(TAG, "fileList.length = " + fileList.length);
            for (File currenFile : fileList) {
                String fileName = currenFile.getName();
                int indexPoint = fileName.lastIndexOf('.');
                if (indexPoint > 0 && currenFile.isFile()) {
                    String fileEnd = fileName.substring(indexPoint + 1);

                    if (fileEnd.equalsIgnoreCase(fileEndingVideo) || fileEnd.equalsIgnoreCase("mp4")) {
                        String name = fileName;
                        String path = currenFile.getPath();
                        long length = currenFile.length();
                        String size = StringUtils.formatFileSize(length, false);
                        Long lastmodifiedtime = currenFile.lastModified();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        String time = sdf.format(lastmodifiedtime);
                        list.add(new LocalFile(name, path, time, size));
                        //Collections.sort(list);
                    }
                }
            }
        }

    }

    public class ListAdapter extends BaseAdapter {

        private HashMap<Integer, View> mView;
        public HashMap<Integer, Integer> visiblecheck;// 用来记录是否显示checkBox
        public HashMap<Integer, Boolean> ischeck;
        private LayoutInflater mInflater;

        public ListAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mView = new HashMap<>();
            visiblecheck = new HashMap<>();
            ischeck = new HashMap<>();
            if (isMulChoice) {
                for (int i = 0; i < list.size(); i++) {
                    ischeck.put(i, false);
                    visiblecheck.put(i, CheckBox.VISIBLE);
                }
            } else {
                for (int i = 0; i < list.size(); i++) {
                    ischeck.put(i, false);
                    visiblecheck.put(i, CheckBox.INVISIBLE);
                }
            }
        }

        @Override
        public int getCount() {

            return list.size();
        }

        @Override
        public Object getItem(int position) {

            return list.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {

            TextView name;
            TextView time;
            TextView size;
            ImageView imgView;
            final CheckBox checkbox;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.file_item, null);
                name = (TextView) convertView.findViewById(R.id.name);
                time = (TextView) convertView.findViewById(R.id.time);
                size = (TextView) convertView.findViewById(R.id.file_size);
                imgView = (ImageView) convertView.findViewById(R.id.imgView);
                checkbox = (CheckBox) convertView
                        .findViewById(R.id.file_checkbox);
                convertView.setTag(new ViewHolder(imgView, name, time, size,
                        checkbox));
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
                name = viewHolder.name;
                time = viewHolder.time;
                size = viewHolder.size;
                imgView = viewHolder.imgView;
                checkbox = viewHolder.checkbox;
            }

            LocalFile item = list.get(position);
            name.setText(list.get(position).getName());
            Log.e("info", "hellow = " + list.get(position).getName());
            time.setText(list.get(position).getTime());
            size.setText(list.get(position).getSize());
            // viewHolder.imgView.setImageBitmap(list.get(position).getBitmap());
//            LoadImage(imgView, list.get(position).getPath());
            Glide.with(getActivity()).load(list.get(position).getPath()).dontAnimate().into(imgView);
            checkbox.setVisibility(visiblecheck.get(position));
            convertView.setOnLongClickListener(new Onlongclick());
            if (selectid.contains(item)) {
                checkbox.setChecked(true);
            } else {
                checkbox.setChecked(false);
            }
            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    // image1.getDrawable().getCurrent().getConstantState().equals(getResources().getDrawable(R.drawable.A).getConstantState())
                    if (isMulChoice) {
                        // if (checkbox.isChecked()) {
                        if (checkbox.isChecked()) {
                            checkbox.setChecked(false);
                            selectid.remove(list.get(position));
                        } else {
                            checkbox.setChecked(true);
                            selectid.add(list.get(position));
                        }
                    } else {
                        // 点击效果
                        Log.e(TAG, "You clicked the " + position);
                        Intent mIntent = new Intent();
                        Bundle bundle = new Bundle();
//						bundle.putString("path", list.get(position).getPath());
//						mIntent.putExtras(bundle);
//						mIntent.setClass(mActivity, VideoViewActivity.class);
//						startActivity(mIntent);
                        bundle.putString("videoPath", list.get(position).getPath());
                        bundle.putInt("type", 1);
                        mIntent.putExtras(bundle);
                        mIntent.setClass(getActivity(), GpsVideoActivity.class);
                        startActivity(mIntent);
                    }
                }
            });
            mView.put(position, convertView);
            return convertView;
        }

        public final class ViewHolder {
            public ImageView imgView;
            public TextView name;
            public TextView time;
            public TextView size;
            public CheckBox checkbox;

            public ViewHolder() {
            }

            public ViewHolder(ImageView imgView, TextView name, TextView time,
                              TextView size, CheckBox checkbox) {
                this.imgView = imgView;
                this.name = name;
                this.time = time;
                this.size = size;
                this.checkbox = checkbox;
            }
        }

        class Onlongclick implements OnLongClickListener {
            @Override
            public boolean onLongClick(View v) {
                // TODO Auto-generated method stub
                // ################
                // Intent intent = new Intent("COM.MESSAGE");
                // intent.putExtra("message", "haha");
                // sendOrderedBroadcast(intent, "xvtian.gai.receiver");
                // sendBroadcast(intent);
                // #######################

                if (isMulChoice == false) {
                    isMulChoice = true;
                    selectid.clear();
                    ll_button.setVisibility(View.GONE);
                    layout.setVisibility(View.VISIBLE);
                    Log.e("9527", "layout.setVisibility(View.VISIBLE)");
                    for (int i = 0; i < list.size(); i++) {
                        adapter.visiblecheck.put(i, CheckBox.VISIBLE);
                    }
//					adapter = new ListAdapter(mActivity);
//					listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
                return true;
            }
        }
    }

    public void LoadImage(ImageView imgView, String path) {
        AsyncTaskImageLoad async = new AsyncTaskImageLoad(imgView);
        async.execute(path);
    }

    public class AsyncTaskImageLoad extends AsyncTask<String, Integer, Bitmap> {
        private ImageView Image = null;

        public AsyncTaskImageLoad(ImageView img) {
            Image = img;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                Log.e("info", "doInBackground params[0] = " + params[0]);
//				String pngpath = getFileNameNoEx(params[0]) + ".PNG";
//				BitmapFactory.Options options = new Options();
//				options.inSampleSize = 4;
//				Bitmap bitmap = BitmapFactory.decodeFile(pngpath, options);
//				Bitmap comp = PhotoUtils.comp(bitmap);
                Bitmap comp = getVideoThumbnail(params[0]);
                return comp;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (Image != null && result != null) {
                Image.setImageBitmap(result);
            }
            super.onPostExecute(result);
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options op, int reqWidth,
                                     int reqheight) {
        int originalWidth = op.outWidth;
        int originalHeight = op.outHeight;
        int inSampleSize = 1;
        if (originalWidth > reqWidth || originalHeight > reqheight) {
            int halfWidth = originalWidth / 2;
            int halfHeight = originalHeight / 2;
            while ((halfWidth / inSampleSize > reqWidth)
                    && (halfHeight / inSampleSize > reqheight)) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public Bitmap getVideoThumbnailNew(String filepath) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filepath, MediaStore.Images.Thumbnails.MINI_KIND);
        return bitmap;
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
            if (seconds >= 1) {
                bitmap = retriever.getFrameAtTime(1, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            } else {
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        /*
		 * case R.id.cancle: 撤销选中 isMulChoice = false; selectid.clear(); adapter
		 * = new Adapter(context); listview.setAdapter(adapter);
		 * layout.setVisibility(View.INVISIBLE); break;
		 */
            case R.id.delete:
                askForDelete();
//			isMulChoice = false;
//			for (int i = 0; i < selectid.size(); i++) {
//				for (int j = 0; j < list.size(); j++) {
//					if (selectid.get(i).equals(list.get(j))) {
//
//						String path = list.get(j).getPath();
//						Log.e("9527", "Path = " + path);
//						File file = new File(path);
//						file.delete();
//						path = getFileNameNoEx(path) + ".PNG";
//						file = new File(path);
//						file.delete();
//						list.remove(j);
//					}
//				}
//			}
//			selectid.clear();
//			layout.setVisibility(View.GONE);
//			ll_button.setVisibility(View.VISIBLE);
//			adapter = new ListAdapter(mActivity);
//			listView.setAdapter(adapter);

                break;
            default:
                break;
        }

    }

    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    BroadcastReceiver myLoaclVideo = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isMulChoice = false;
            selectid.clear();
//			adapter = new ListAdapter(mActivity);
//			listView.setAdapter(adapter);
            for (int i = 0; i < list.size(); i++) {
                adapter.visiblecheck.put(i, CheckBox.GONE);
            }
            adapter.notifyDataSetChanged();
            layout.setVisibility(View.GONE);
            ll_button.setVisibility(View.VISIBLE);
        }
    };

    private void askForDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle(getString(R.string.notice)).setMessage(getString(R.string.wheter_delete_file))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isMulChoice = false;
                        for (int i = 0; i < selectid.size(); i++) {
                            for (int j = 0; j < list.size(); j++) {
                                if (selectid.get(i).equals(list.get(j))) {

                                    String path = list.get(j).getPath();
                                    Log.e("9527", "Path = " + path);
                                    File file = new File(path);
                                    file.delete();
                                    path = getFileNameNoEx(path) + ".PNG";
                                    file = new File(path);
                                    file.delete();
                                    list.remove(j);
                                }
                            }
                        }
                        selectid.clear();
                        layout.setVisibility(View.GONE);
                        ll_button.setVisibility(View.VISIBLE);
//						adapter = new ListAdapter(mActivity);
//						listView.setAdapter(adapter);
                        for (int i = 0; i < list.size(); i++) {
                            adapter.visiblecheck.put(i, CheckBox.GONE);
                        }
                        adapter.notifyDataSetChanged();
                        if (list.size() == 0) {
                            mTextViewNoVideo.setVisibility(View.VISIBLE);
                        } else {
                            mTextViewNoVideo.setVisibility(View.GONE);
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).setCancelable(false).show();
    }
}
