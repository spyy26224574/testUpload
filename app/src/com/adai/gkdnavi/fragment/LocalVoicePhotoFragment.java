package com.adai.gkdnavi.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.adai.gkd.bean.MessageBean;
import com.adai.gkdnavi.LocalPhotoFile;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.PhotoUtils;
import com.adai.gkdnavi.utils.StringUtils;
import com.example.ipcamera.application.VLCApplication;
import com.filepicker.imagebrowse.PictureBrowseActivity;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class LocalVoicePhotoFragment extends Fragment implements OnClickListener {
	
	protected static final String TAG = "LocalVoicePhotoFragment";
	private static final String KEYVALUE = "LocalVoicePhotoFragment";
	private ListView listView;
	private RelativeLayout layout;
	private Button delete;
	private FragmentActivity activityLocalPhoto;
	private LinearLayout ll_button;
	private List<LocalPhotoFile> list = new ArrayList<LocalPhotoFile>();
	private ListAdapter adapter;
	private LayoutInflater mInflater;
	private ViewHolder viewHolder;
	private final String fileEndingPhoto = "JPG";
	private TextView mTextViewNoPhoto;
	private List<LocalPhotoFile> selectid = new ArrayList<LocalPhotoFile>();
	private boolean isMulChoice = false; // 是否多选
	private MessageBean message;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		activityLocalPhoto = this.getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.local_photo_activity, container, false);
		mTextViewNoPhoto = (TextView) view.findViewById(R.id.tv_no_picture);
		listView = (ListView) view.findViewById(R.id.listView);
		layout = (RelativeLayout) view.findViewById(R.id.relative);
		delete = (Button) view.findViewById(R.id.delete);
		ll_button = (LinearLayout) activityLocalPhoto.findViewById(R.id.activity_photo_ll_button);
		delete.setOnClickListener(this);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		IntentFilter filter = new IntentFilter(KEYVALUE);
		activityLocalPhoto.registerReceiver(myPhoto, filter);
		// ###############
		createFolderDispList();
//		adapter = new ListAdapter(activityLocalPhoto);
//		listView.setAdapter(adapter);
		
	}

	@Override
	public void onResume() {
		super.onResume();
		adapter = new ListAdapter(activityLocalPhoto);
		listView.setAdapter(adapter);
		//发送广播
		if (list.size() == 0) {
			mTextViewNoPhoto.setVisibility(View.VISIBLE);
		}else {
			mTextViewNoPhoto.setVisibility(View.GONE);
		}		
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		activityLocalPhoto.unregisterReceiver(myPhoto);
	}

	@SuppressLint("SimpleDateFormat") 
	private void createFolderDispList() {
		try {
			File filePath;
			list.clear();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
//			String stringExtra = activityLocalPhoto.getIntent().getStringExtra("TIME");
//			String endtimeExtra = activityLocalPhoto.getIntent().getStringExtra("endTime");
            message = (MessageBean) activityLocalPhoto.getIntent().getSerializableExtra("message");
            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            long startTime = 0;
            long endtime = 0;
            try {
                startTime = format.parse(message.starttime).getTime();
                endtime = format.parse(message.endtime).getTime();
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                // TODO: handle exception
            }
            String filefirstPathText = VLCApplication.DOWNLOADPATH;
            filePath = new File(filefirstPathText);
            File[] fileList = filePath.listFiles();
            if (fileList != null) {
                for (File currenFile : fileList) {
                    String fileName = currenFile.getName();
                    int indexPoint = fileName.lastIndexOf('.');
                    if (indexPoint > 0 && currenFile.isFile()) {
                        String fileEnd = fileName.substring(indexPoint + 1);
                        if (fileEnd.equalsIgnoreCase(fileEndingPhoto)) {
                            long filetTime = 0;
                            try {
                                int lastindex=fileName.lastIndexOf("_");
                                if(lastindex<1) {
                                    continue;
                                }
                                String substring = fileName.substring(0, lastindex).replace("_", "");
                                filetTime = simpleDateFormat.parse(substring).getTime();
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if(filetTime==0) {
                                continue;
                            }
                            String name = fileName;
                            String path = currenFile.getAbsolutePath();
                            long length = currenFile.length();
                            Long lastmodifiedtime = currenFile.lastModified();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String time = sdf.format(lastmodifiedtime);
                            if (length < 1024 * 1024) {
                                if (filetTime <= endtime + 1000 && filetTime >= startTime && list.size() < 5) {
                                    String size = StringUtils.formatFileSize(length, false);
                                    list.add(new LocalPhotoFile(name, path, time, size));
                                    Collections.sort(list);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	class ListAdapter extends BaseAdapter {
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

            LocalPhotoFile item = list.get(position);
            name.setText(list.get(position).getName());
            time.setText(list.get(position).getTime());
            size.setText(list.get(position).getSize());
            LoadImage(imgView, list.get(position).getPath());
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
                    if (isMulChoice) {
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
                        mIntent.putExtra(PictureBrowseActivity.KEY_MODE, PictureBrowseActivity.MODE_LOCAL);
                        ArrayList<String> values = new ArrayList<>();
                        for (LocalPhotoFile localPhotoFile: list) {
                            values.add(localPhotoFile.getPath());
                        }
                        mIntent.putStringArrayListExtra(PictureBrowseActivity.KEY_TOTAL_LIST, values);
                        mIntent.putExtra(PictureBrowseActivity.KEY_POSTION, position);
//                        bundle.putString("path", list.get(position).getPath());
//                        bundle.putSerializable("photos", (Serializable) list);
//                        bundle.putInt("position", position);
//                        mIntent.putExtras(bundle);
                        mIntent.setClass(activityLocalPhoto, PictureBrowseActivity.class);
                        startActivity(mIntent);
                    }
                }
            });
            mView.put(position, convertView);
            return convertView;
        }

		class Onlongclick implements OnLongClickListener {

			@Override
            public boolean onLongClick(View v) {
				isMulChoice = true;
				selectid.clear();
				layout.setVisibility(View.VISIBLE);
				// 隐藏图片和视频的按钮
				ll_button.setVisibility(View.GONE);
				for (int i = 0; i < list.size(); i++) {
					adapter.visiblecheck.put(i, CheckBox.VISIBLE);
				}
				adapter = new ListAdapter(activityLocalPhoto);
				listView.setAdapter(adapter);
				return true;
			}
		}

	}

    class ViewHolder {
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

	private void LoadImage(ImageView imgView, String path) {
		AsyncTaskImageLoad async = new AsyncTaskImageLoad(imgView);
		async.execute(path);
	}

	private class AsyncTaskImageLoad extends AsyncTask<String, Integer, Bitmap> {
		private ImageView Image = null;

		public AsyncTaskImageLoad(ImageView img) {
			Image = img;
		}

		@Override
        protected Bitmap doInBackground(String... params) {
			try {
				//Log.e("info", "doInBackground params[0] = " + params[0]);
				BitmapFactory.Options options = new Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(params[0], options);
				options.inPreferredConfig = Bitmap.Config.ARGB_4444;
				options.inSampleSize = calculateInSampleSize(options, 100, 66);
				options.inJustDecodeBounds = false;
				Bitmap bitmap = BitmapFactory.decodeFile(params[0], options);
				Bitmap comp = PhotoUtils.comp(bitmap);
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

	private int calculateInSampleSize(BitmapFactory.Options op, int reqWidth,
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.delete:
                Log.e("9527", "delete");
                askForDelete();
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
			adapter = new ListAdapter(activityLocalPhoto);
			listView.setAdapter(adapter);

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
                            Log.e("9527", "selectid" + i + " = " + selectid.get(i));
                        }

                        for (int j = 0; j < list.size(); j++) {
                            Log.e("9527", "list" + j + " = " + list.get(j));
                        }

                        for (int i = 0; i < selectid.size(); i++) {
                            for (int j = 0; j < list.size(); j++) {
                                if (selectid.get(i).equals(list.get(j))) {

                                    String path = list.get(j).getPath();
                                    Log.e("9527", "Path = " + path);
                                    File file = new File(path);
                                    file.delete();
                                    list.remove(j);
                                }
                            }
                        }
                        selectid.clear();
                        adapter = new ListAdapter(activityLocalPhoto);
                        listView.setAdapter(adapter);
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
