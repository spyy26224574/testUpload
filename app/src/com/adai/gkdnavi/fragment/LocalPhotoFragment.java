package com.adai.gkdnavi.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.adai.gkdnavi.utils.StringUtils;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;
import com.example.ipcamera.application.VLCApplication;
import com.filepicker.imagebrowse.PictureBrowseActivity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class LocalPhotoFragment extends Fragment implements OnClickListener {
    private ListView listView;
    private RelativeLayout layout;
    private Button delete;
    private FragmentActivity activityLocalPhoto;
    private LinearLayout ll_button;
    // ##############
    protected static final String TAG = "LocalPhotoActivity";
    private List<LocalFile> list = new ArrayList<>();
    private ListAdapter adapter;
    private LayoutInflater mInflater;
    private ViewHolder viewHolder;
    final String fileEndingPhoto = "JPG";
    private boolean isMulChoice = false; // 是否多选
    private TextView mTextViewNoPhoto;
    private List<LocalFile> selectid = new ArrayList<LocalFile>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activityLocalPhoto = this.getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.local_photo_activity, container,
                false);
        mTextViewNoPhoto = (TextView) view.findViewById(R.id.tv_no_picture);
        listView = (ListView) view.findViewById(R.id.listView);
        layout = (RelativeLayout) view.findViewById(R.id.relative);
        delete = (Button) view.findViewById(R.id.delete);
//		ll_button = (RadioGroup) activityLocalPhoto
//				.findViewById(R.id.ll_button);
        ll_button = (LinearLayout) activityLocalPhoto
                .findViewById(R.id.ll_button);
        delete.setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        IntentFilter filter = new IntentFilter("MESSAGE");
        activityLocalPhoto.registerReceiver(myPhoto, filter);
        // ###############
        createFolderDispList();
        adapter = new ListAdapter(activityLocalPhoto);
        listView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        createFolderDispList();
        adapter = new ListAdapter(activityLocalPhoto);
        listView.setAdapter(adapter);
        if (list.size() == 0) {
            mTextViewNoPhoto.setVisibility(View.VISIBLE);
        } else {
            mTextViewNoPhoto.setVisibility(View.GONE);
        }
        System.out.println(TAG + "onResume###################");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        activityLocalPhoto.unregisterReceiver(myPhoto);
    }

    private void createFolderDispList() {
        File filePath;
        list.clear();
        String filefirstPathText = VLCApplication.DOWNLOADPATH;
        filePath = new File(filefirstPathText);
        File[] fileList = filePath.listFiles();
        Log.e("mark", "fileList != null  " + String.valueOf(fileList != null) + "filePath.isDirectory()" + String.valueOf(filePath.isDirectory()));
        if (fileList != null) {
            for (File currenFile : fileList) {
                String fileName = currenFile.getName();
                int indexPoint = fileName.lastIndexOf('.');
                if (indexPoint > 0 && currenFile.isFile()) {
                    String fileEnd = fileName.substring(indexPoint + 1);
                    if (fileEnd.equalsIgnoreCase(fileEndingPhoto)) {
                        String name = fileName;
                        String path = currenFile.getPath();
                        long length = currenFile.length();
                        String size = StringUtils.formatFileSize(length, false);
                        Long lastmodifiedtime = currenFile.lastModified();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        String time = sdf.format(lastmodifiedtime);
                        list.add(new LocalFile(name, path, time, size));
                        Collections.sort(list);
                    }
                }
            }
        }
    }

    public class ListAdapter extends BaseAdapter {
        private HashMap<Integer, View> mView;
        public HashMap<Integer, Integer> visiblecheck;// 用来记录是否显示checkBox
        public HashMap<Integer, Boolean> ischeck;

        public ListAdapter(Context context) {
            mInflater = LayoutInflater.from(context);
            mView = new HashMap<Integer, View>();
            visiblecheck = new HashMap<Integer, Integer>();
            ischeck = new HashMap<Integer, Boolean>();
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
            // TODO Auto-generated method stub
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
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

            LocalFile item = list.get(position);
            name.setText(list.get(position).getName());
            //Log.e("info", "hellow = " + list.get(position).getName());
            time.setText(list.get(position).getTime());
            size.setText(list.get(position).getSize());
            // viewHolder.imgView.setImageBitmap(list.get(position).getBitmap());
//            LoadImage(imgView, list.get(position).getPath());
            ImageLoaderUtil.getInstance().loadImage(getActivity(), list.get(position).getPath(), imgView);
            checkbox.setVisibility(visiblecheck.get(position));
            convertView.setOnLongClickListener(new Onlongclick());
            if (selectid.contains(item)) {
                checkbox.setChecked(true);
            } else {
                checkbox.setChecked(false);
            }
            convertView.setOnClickListener(new OnClickListener() {
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
//                        Bundle bundle = new Bundle();
//                        bundle.putString("path", list.get(position).getPath());
//                        bundle.putSerializable("photos", (Serializable) list);
//                        bundle.putInt("position", position);
//                        mIntent.putExtras(bundle);
//                        mIntent.setClass(activityLocalPhoto, LocalPhotoPreviewActivity.class);

                        mIntent.putExtra(PictureBrowseActivity.KEY_MODE, PictureBrowseActivity.MODE_LOCAL);
                        ArrayList<String> values = new ArrayList<>();
                        for (LocalFile localPhotoFile : list) {
                            values.add(localPhotoFile.getPath());
                        }
                        mIntent.putStringArrayListExtra(PictureBrowseActivity.KEY_TOTAL_LIST, values);
                        mIntent.putExtra(PictureBrowseActivity.KEY_POSTION, position);
                        mIntent.setClass(activityLocalPhoto, PictureBrowseActivity.class);
                        startActivity(mIntent);
                    }
                }
            });
            mView.put(position, convertView);
            return convertView;
        }

        class Onlongclick implements OnLongClickListener {

            public boolean onLongClick(View v) {
                if (isMulChoice == false) {
                    isMulChoice = true;
                    selectid.clear();
                    layout.setVisibility(View.VISIBLE);
                    // 隐藏图片和视频的按钮
                    ll_button.setVisibility(View.GONE);
                    //Log.e("9527", "layout.setVisibility(View.VISIBLE)");
                    for (int i = 0; i < list.size(); i++) {
                        adapter.visiblecheck.put(i, CheckBox.VISIBLE);
                    }
//                    adapter = new ListAdapter(activityLocalPhoto);
//                    listView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                }
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
//						list.remove(j);
//					}
//				}
//			}
//			selectid.clear();
//			adapter = new ListAdapter(activityLocalPhoto);
//			listView.setAdapter(adapter);
//			layout.setVisibility(View.GONE);
//			ll_button.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

    BroadcastReceiver myPhoto = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isMulChoice = false;
            selectid.clear();
            layout.setVisibility(View.GONE);
            ll_button.setVisibility(View.VISIBLE);
            for (int i = 0; i < list.size(); i++) {
                adapter.visiblecheck.put(i, CheckBox.GONE);
            }
//            adapter = new ListAdapter(activityLocalPhoto);
//            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

        }
    };

    private void askForDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activityLocalPhoto);
        builder.setTitle(getString(R.string.notice)).setMessage(getString(R.string.wheter_delete_file))
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isMulChoice = false;
                        for (int i = 0; i < selectid.size(); i++) {
                            for (int j = 0; j < list.size(); j++) {
                                if (selectid.get(i).equals(list.get(j))) {

                                    String path = list.get(j).getPath();
                                    //Log.e("9527", "Path = " + path);
                                    File file = new File(path);
                                    file.delete();
                                    list.remove(j);
                                    String where = MediaStore.Images.Media.DATA + "='" + path + "'";
                                    getActivity().getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);
                                }
                            }
                        }
                        selectid.clear();
//                        adapter = new ListAdapter(activityLocalPhoto);
//                        listView.setAdapter(adapter);
                        for (int i = 0; i < list.size(); i++) {
                            adapter.visiblecheck.put(i, CheckBox.GONE);
                        }
                        adapter.notifyDataSetChanged();

                        layout.setVisibility(View.GONE);
                        ll_button.setVisibility(View.VISIBLE);
                        if (list.size() == 0) {
                            mTextViewNoPhoto.setVisibility(View.VISIBLE);
                        } else {
                            mTextViewNoPhoto.setVisibility(View.GONE);
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
