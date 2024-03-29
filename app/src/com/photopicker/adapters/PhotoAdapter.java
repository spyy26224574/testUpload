package com.photopicker.adapters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.adai.gkdnavi.R;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;
import com.photopicker.PhotoPickerActivity;
import com.photopicker.beans.Photo;
import com.photopicker.preview.PhotoPreviewActivity;
import com.photopicker.utils.OtherUtils;

import java.util.ArrayList;

/**
 * @Class: PhotoAdapter
 * @Description: 图片适配器
 * @author: lling(www.liuling123.com)
 * @Date: 2015/11/4
 */
public class PhotoAdapter extends BaseAdapter {

    private static final int TYPE_CAMERA = 0;
    private static final int TYPE_PHOTO = 1;

    private ArrayList<Photo> mDatas;
    //存放已选中的Photo数据
    private ArrayList<String> mSelectedPhotos;
    private Activity mContext;
    private int mWidth;
    //是否显示相机，默认不显示
    private boolean mIsShowCamera = false;
    //照片选择模式，默认单选
    private int mSelectMode = PhotoPickerActivity.MODE_SINGLE;
    //图片选择数量
    private int mMaxNum = PhotoPickerActivity.DEFAULT_NUM;

    private View.OnClickListener mOnPhotoClick;
    private PhotoClickCallBack mCallBack;

    public PhotoAdapter(Activity context, ArrayList<Photo> mDatas) {
        this.mDatas = mDatas;
        this.mContext = context;
        int screenWidth = OtherUtils.getWidthInPx(mContext);
        mWidth = (screenWidth - OtherUtils.dip2px(mContext, 4)) / 3;
    }

    public PhotoAdapter(Activity context, ArrayList<Photo> mDatas, int maxNum) {
        this.mDatas = mDatas;
        this.mContext = context;
        int screenWidth = OtherUtils.getWidthInPx(mContext);
        mWidth = (screenWidth - OtherUtils.dip2px(mContext, 4)) / 3;
        mMaxNum = maxNum;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mIsShowCamera) {
            return TYPE_CAMERA;
        } else {
            return TYPE_PHOTO;
        }
    }

    @Override
    public int getCount() {
        if (mIsShowCamera) {
            return mDatas.size() + 1;
        } else {
            return mDatas.size();
        }
    }

    @Override
    public Photo getItem(int position) {
        if (mIsShowCamera) {
            if (position == 0) {
                return null;
            }
            return mDatas.get(position - 1);
        } else {
            return mDatas.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        if (mIsShowCamera) {
            if (position == 0) {
                return 0;
            } else {
                return mDatas.get(position - 1).getId();
            }
        }
        return mDatas.get(position).getId();
    }

    public void setDatas(ArrayList<Photo> mDatas) {
        this.mDatas = mDatas;
    }

    public void setIsShowCamera(boolean isShowCamera) {
        this.mIsShowCamera = isShowCamera;
    }

    public boolean isShowCamera() {
        return mIsShowCamera;
    }

    public void setMaxNum(int maxNum) {
        this.mMaxNum = maxNum;
    }

    public void setPhotoClickCallBack(PhotoClickCallBack callback) {
        mCallBack = callback;
    }


    /**
     * 获取已选中相片
     *
     * @return
     */
    public ArrayList<String> getmSelectedPhotos() {
        return mSelectedPhotos;
    }

    public void setSelectMode(int selectMode) {
        this.mSelectMode = selectMode;
        if (mSelectMode == PhotoPickerActivity.MODE_MULTI) {
            initMultiMode();
        }
    }

    public void addSelectedPhotos(ArrayList<String> selected) {
        if (mSelectedPhotos != null) {
            mSelectedPhotos.clear();
            mSelectedPhotos.addAll(selected);
        }
    }

    /**
     * 初始化多选模式所需要的参数
     */
    private void initMultiMode() {
        mSelectedPhotos = new ArrayList<String>();
        mOnPhotoClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.checkmark) {
                    FrameLayout parent = (FrameLayout) v.getParent();
                    String path = v.getTag(R.id.search_key).toString();
                    if (mSelectedPhotos.contains(path)) {
                        parent.findViewById(R.id.mask).setVisibility(View.GONE);
                        parent.findViewById(R.id.checkmark).setSelected(false);
                        mSelectedPhotos.remove(path);
                    } else {
                        if (mSelectedPhotos.size() >= mMaxNum) {
                            Toast.makeText(mContext, R.string.msg_maxi_capacity,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        mSelectedPhotos.add(path);
                        parent.findViewById(R.id.mask).setVisibility(View.VISIBLE);
                        parent.findViewById(R.id.checkmark).setSelected(true);
                    }
                    if (mCallBack != null) {
                        mCallBack.onPhotoClick();
                    }
                } else if (v.getId() == R.id.wrap_layout) {
                    Intent preview = new Intent(mContext, PhotoPreviewActivity.class);
                    preview.putExtra(PhotoPickerActivity.EXTRA_MAX_MUN, mMaxNum);
                    preview.putExtra(PhotoPreviewActivity.KEY_MODE, PhotoPreviewActivity.MODE_SELECT);
                    preview.putExtra(PhotoPreviewActivity.KEY_SELECT_LIST, mSelectedPhotos);
                    ArrayList<String> allPhotos = new ArrayList<>();
                    String path = v.findViewById(R.id.checkmark).getTag(R.id.search_key).toString();
                    for (Photo photo : mDatas) {
                        allPhotos.add(photo.getPath());
                    }
                    preview.putExtra(PhotoPreviewActivity.KEY_TOTAL_LIST, allPhotos);
                    preview.putExtra(PhotoPreviewActivity.KEY_POSTION, allPhotos.indexOf(path));
                    mContext.startActivityForResult(preview, PhotoPickerActivity.REQUEST_CODE_PREVIEW);
                }
            }
        };
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getItemViewType(position) == TYPE_CAMERA) {
            convertView = LayoutInflater.from(mContext).inflate(
                    R.layout.item_camera_layout, null);
            convertView.setTag(null);
            //设置高度等于宽度
            GridView.LayoutParams lp = new GridView.LayoutParams(mWidth, mWidth);
            convertView.setLayoutParams(lp);
        } else {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.item_photo_layout, null);
                holder.photoImageView = (ImageView) convertView.findViewById(R.id.imageview_photo);
                holder.selectView = (ImageView) convertView.findViewById(R.id.checkmark);
                holder.maskView = convertView.findViewById(R.id.mask);
                holder.wrapLayout = (FrameLayout) convertView.findViewById(R.id.wrap_layout);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.photoImageView.setImageResource(R.drawable.ic_photo_loading);
            Photo photo = getItem(position);
            if (mSelectMode == PhotoPickerActivity.MODE_MULTI) {
                holder.wrapLayout.setOnClickListener(mOnPhotoClick);
                holder.selectView.setTag(R.id.search_key, photo.getPath());
                holder.selectView.setVisibility(View.VISIBLE);
                holder.selectView.setOnClickListener(mOnPhotoClick);
                if (mSelectedPhotos != null && mSelectedPhotos.contains(photo.getPath())) {
                    holder.selectView.setSelected(true);
                    holder.maskView.setVisibility(View.VISIBLE);
                } else {
                    holder.selectView.setSelected(false);
                    holder.maskView.setVisibility(View.GONE);
                }
            } else {
                holder.selectView.setVisibility(View.GONE);
            }
            if (photo.getPath().endsWith("head.jpg")) {
                ImageLoaderUtil.getInstance().loadImageWithoutCache(mContext, photo.getPath(), holder.photoImageView);
            }else{
                ImageLoaderUtil.getInstance().loadImage(mContext,photo.getPath(),holder.photoImageView);
            }
        }
        return convertView;
    }

    private class ViewHolder {
        private ImageView photoImageView;
        private ImageView selectView;
        private View maskView;
        private FrameLayout wrapLayout;
    }

    /**
     * 多选时，点击相片的回调接口
     */
    public interface PhotoClickCallBack {
        void onPhotoClick();
    }
}
