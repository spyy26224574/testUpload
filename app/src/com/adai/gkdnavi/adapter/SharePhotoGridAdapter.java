package com.adai.gkdnavi.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.adai.gkdnavi.R;
import com.adai.gkdnavi.ShareActivity;
import com.example.ipcamera.application.VLCApplication;
import com.filepicker.imagebrowse.PictureBrowseActivity;
import com.lidroid.xutils.BitmapUtils;
import com.photopicker.PhotoPickerActivity;
import com.photopicker.preview.PhotoPreviewActivity;

import java.util.ArrayList;

/**
 * Created by admin on 2016/8/17.
 */
public class SharePhotoGridAdapter extends BaseAdapter {

    private ArrayList<String> photos = new ArrayList<>();

    private Activity mContext;

    private int imageSize;
    BitmapUtils bitmapUtils;
    private int mMaxNum = VLCApplication.MAX_PHOTO_NUM;

    public SharePhotoGridAdapter(Activity context, int maxNum) {
        this.mContext = context;
        bitmapUtils = new BitmapUtils(mContext);
        setColumnNumber(context, 5);
        mMaxNum = maxNum;
    }

    public SharePhotoGridAdapter(Activity context) {
        this.mContext = context;
        bitmapUtils = new BitmapUtils(mContext);
        setColumnNumber(context, 5);
    }

    public void addPhotos(ArrayList<String> photo) {
        photos.clear();
        photos.addAll(photo);
        notifyDataSetChanged();
    }

    public ArrayList<String> getPhotos() {
        return photos;
    }

    @Override
    public int getCount() {
        int num = photos == null ? 0 : photos.size();
        if (num < mMaxNum) {
            num += 1;
        }
        return num;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    private void setColumnNumber(Context context, int columnNum) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        imageSize = widthPixels / columnNum;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(mContext,R.layout.item_share_photo_grid, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.mIvItem = (ImageView) convertView.findViewById(R.id.image);
//        ImageView image = (ImageView) convertView.findViewById(R.id.image);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(imageSize, imageSize);
//        params.width=imageSize;
//        params.height=imageSize;
        convertView.setLayoutParams(params);
//        ImageView delete = (ImageView) convertView.findViewById(R.id.delete);
        holder.mIvDelete = (ImageView) convertView.findViewById(R.id.delete);
        if (position >= photos.size()) {
            holder.mIvItem.setImageResource(R.drawable.icon_add_image);
            holder.mIvDelete.setVisibility(View.GONE);
        } else {
            bitmapUtils.display(holder.mIvItem, photos.get(position));
            holder.mIvDelete.setVisibility(View.VISIBLE);
        }
        holder.mIvItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("PhotoPickerActivity", "onClick: ");
                if (position >= photos.size()) {
//                    FilePickerBuilder.getInstance().setMaxCount(VLCApplication.MAX_PHOTO_NUM)
//                            .setSelectedFiles(photos)
//                            //make your own Dark action bar theme according to your app design and set it here
//                            .setActivityTheme(R.style.FilePickerTheme)
//                            .pickPhoto(mContext);
                    Intent intent = new Intent(mContext, PhotoPickerActivity.class);
                    intent.putExtra(PhotoPickerActivity.EXTRA_SHOW_CAMERA, true);
                    intent.putExtra(PhotoPickerActivity.EXTRA_SELECT_MODE, PhotoPickerActivity.MODE_MULTI);
                    Log.e("SharePhotoGridAdapter", "开启PhotoPickerActivity");
//                    if (mContext instanceof FeedBackActivity) {
                    intent.putExtra(PhotoPickerActivity.EXTRA_MAX_MUN, mMaxNum);
//                    } else {
//                        intent.putExtra(PhotoPickerActivity.EXTRA_MAX_MUN, VLCApplication.MAX_PHOTO_NUM);
//                    }
                    intent.putExtra(PhotoPickerActivity.EXTRA_SELECTED_PHOTOS, photos);
                    mContext.startActivityForResult(intent, ShareActivity.REQUEST_PICKPHOTO_CODE);
                } else {
                    Intent select = new Intent(mContext, PhotoPreviewActivity.class);
                    select.putExtra(PhotoPreviewActivity.KEY_MODE, PictureBrowseActivity.MODE_SELECT);
                    select.putExtra(PhotoPreviewActivity.KEY_POSTION, position);
                    select.putExtra(PhotoPickerActivity.EXTRA_MAX_MUN, mMaxNum);
                    select.putStringArrayListExtra(PhotoPreviewActivity.KEY_SELECT_LIST, getPhotos());
                    select.putStringArrayListExtra(PhotoPreviewActivity.KEY_TOTAL_LIST, getPhotos());
                    mContext.startActivityForResult(select, ShareActivity.REQUESE_SELECT_PHOTO_CODE);
                }
            }
        });
        holder.mIvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photos.remove(position);
                notifyDataSetChanged();
            }
        });
        return convertView;
    }

    static class ViewHolder {
        private ImageView mIvItem, mIvDelete;
    }
}
