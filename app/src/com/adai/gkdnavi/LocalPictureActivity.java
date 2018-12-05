package com.adai.gkdnavi;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.adai.gkdnavi.utils.StringUtils;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;
import com.example.ipcamera.application.VLCApplication;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class LocalPictureActivity extends BaseActivity implements View.OnClickListener {
    private TextView mTvNoPicture;
    private TextView mTvNoVideo;
    private ListView mListView;
    private RelativeLayout mRelative;
    private Button mDelete;
    private List<LocalFile> mFiles = new ArrayList<>();
    private boolean isMulChoice = false; // 是否多选
    private ViewHolder viewHolder;
    private List<LocalFile> selectid = new ArrayList<>();
    private ListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.local_picture_activity);
        createFolderDispList();
        initView();
    }

    private void createFolderDispList() {
        File filePath;
        mFiles.clear();
        String filefirstPathText = VLCApplication.LOCAL_PICTURE;
        filePath = new File(filefirstPathText);
        File[] fileList = filePath.listFiles();
        if (fileList != null) {
            for (File currenFile : fileList) {
                String fileName = currenFile.getName();
                int indexPoint = fileName.lastIndexOf('.');
                if (indexPoint > 0 && currenFile.isFile()) {
                    String path = currenFile.getPath();
                    long length = currenFile.length();
                    String size = StringUtils.formatFileSize(length, false);
                    Long lastmodifiedtime = currenFile.lastModified();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String time = sdf.format(lastmodifiedtime);
                    mFiles.add(new LocalFile(fileName, path, time, size));
                    Collections.sort(mFiles);
                }
            }
        }
    }

    @Override
    protected void initView() {
        super.initView();
        setTitle(R.string.takephoto_file);
        findViewById(R.id.right_img).setVisibility(View.GONE);
        mTvNoPicture = (TextView) findViewById(R.id.tv_no_picture);
        mTvNoVideo = (TextView) findViewById(R.id.tv_no_video);
        mListView = (ListView) findViewById(R.id.listView);
        mRelative = (RelativeLayout) findViewById(R.id.relative);
        mDelete = (Button) findViewById(R.id.delete);
        mDelete.setClickable(false);
        mRelative.setOnClickListener(this);
        adapter = new ListAdapter(this);
        mListView.setAdapter(adapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.relative:
                askForDelete();
                break;
        }
    }

    private void askForDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.notice)).setMessage(getString(R.string.wheter_delete_file))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isMulChoice = false;
                        for (int i = 0; i < selectid.size(); i++) {
                            for (int j = 0; j < mFiles.size(); j++) {
                                if (selectid.get(i).equals(mFiles.get(j))) {

                                    String path = mFiles.get(j).getPath();
                                    File file = new File(path);
                                    file.delete();
                                    mFiles.remove(j);
                                    String where = MediaStore.Images.Media.DATA + "='" + path + "'";
                                    LocalPictureActivity.this.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
                                }
                            }
                        }
                        selectid.clear();
                        adapter = new ListAdapter(LocalPictureActivity.this);
                        mListView.setAdapter(adapter);
                        mRelative.setVisibility(View.GONE);
                        if (mFiles.size() == 0) {
                            mTvNoPicture.setVisibility(View.VISIBLE);
                        } else {
                            mTvNoPicture.setVisibility(View.GONE);
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

    public class ListAdapter extends BaseAdapter {
        private HashMap<Integer, View> mView;
        public HashMap<Integer, Integer> visiblecheck;// 用来记录是否显示checkBox
        public HashMap<Integer, Boolean> ischeck;

        public ListAdapter(Context context) {
            mView = new HashMap<>();
            visiblecheck = new HashMap<>();
            ischeck = new HashMap<Integer, Boolean>();
            if (isMulChoice) {
                for (int i = 0; i < mFiles.size(); i++) {
                    ischeck.put(i, false);
                    visiblecheck.put(i, CheckBox.VISIBLE);
                }
            } else {
                for (int i = 0; i < mFiles.size(); i++) {
                    ischeck.put(i, false);
                    visiblecheck.put(i, CheckBox.INVISIBLE);
                }
            }
        }

        @Override
        public int getCount() {
            return mFiles.size();
        }

        @Override
        public Object getItem(int position) {
            return mFiles.get(position);
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
                convertView = View.inflate(LocalPictureActivity.this, R.layout.file_item, null);
                name = (TextView) convertView.findViewById(R.id.name);
                time = (TextView) convertView.findViewById(R.id.time);
                size = (TextView) convertView.findViewById(R.id.file_size);
                imgView = (ImageView) convertView.findViewById(R.id.imgView);
                checkbox = (CheckBox) convertView.findViewById(R.id.file_checkbox);
                convertView.setTag(new ViewHolder(imgView, name, time, size, checkbox));
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
                name = viewHolder.name;
                time = viewHolder.time;
                size = viewHolder.size;
                imgView = viewHolder.imgView;
                checkbox = viewHolder.checkbox;
            }

            LocalFile item = mFiles.get(position);
            name.setText(mFiles.get(position).getName());
            //Log.e("info", "hellow = " + list.get(position).getName());
            time.setText(mFiles.get(position).getTime());
            size.setText(mFiles.get(position).getSize());
            // viewHolder.imgView.setImageBitmap(list.get(position).getBitmap());
//            LoadImage(imgView, list.get(position).getPath());
            ImageLoaderUtil.getInstance().loadImage(LocalPictureActivity.this, mFiles.get(position).getPath(), imgView);
            checkbox.setVisibility(visiblecheck.get(position));
            convertView.setOnLongClickListener(new ListAdapter.Onlongclick());
            if (selectid.contains(item)) {
                checkbox.setChecked(true);
            } else {
                checkbox.setChecked(false);
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (isMulChoice) {
                        if (checkbox.isChecked()) {
                            checkbox.setChecked(false);
                            selectid.remove(mFiles.get(position));
                        } else {
                            checkbox.setChecked(true);
                            selectid.add(mFiles.get(position));
                        }
                    } else {
                        // 点击效果
                        Intent mIntent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString("path", mFiles.get(position).getPath());
                        bundle.putSerializable("photos", (Serializable) mFiles);
                        bundle.putInt("position", position);
                        mIntent.putExtras(bundle);
                        mIntent.setClass(LocalPictureActivity.this, LocalPhotoPreviewActivity.class);
                        startActivity(mIntent);
                    }
                }
            });
            mView.put(position, convertView);
            return convertView;
        }

        class Onlongclick implements View.OnLongClickListener {

            public boolean onLongClick(View v) {
                isMulChoice = true;
                selectid.clear();
                mRelative.setVisibility(View.VISIBLE);
                for (int i = 0; i < mFiles.size(); i++) {
                    adapter.visiblecheck.put(i, CheckBox.VISIBLE);
                }
                adapter = new ListAdapter(LocalPictureActivity.this);
                mListView.setAdapter(adapter);
                return true;
            }
        }

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

}
