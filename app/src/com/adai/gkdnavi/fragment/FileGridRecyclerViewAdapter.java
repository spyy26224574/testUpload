package com.adai.gkdnavi.fragment;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.adai.gkdnavi.R;
import com.adai.gkdnavi.fragment.FileGridFragment.OnListFragmentInteractionListener;
import com.adai.gkdnavi.utils.ShareUtils;
import com.bumptech.glide.Glide;
import com.filepicker.imagebrowse.PictureBrowseActivity;
import com.ijk.media.activity.VideoActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * {@link RecyclerView.Adapter} that can display a {@link String} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class FileGridRecyclerViewAdapter extends RecyclerView.Adapter<FileGridRecyclerViewAdapter.ViewHolder> {

    private final ArrayList<String> mValues;

    private ArrayList<String> selectid = new ArrayList<String>();      //存储选中的ID
    //public HashMap<Integer, Boolean> ischeck = new HashMap<Integer, Boolean>();  //用来记录是否选中

    private final OnListFragmentInteractionListener mListener;
    private int fileType = 0;
    private Context context;

    private int coulums = 1;
    private int imageSize = 0;
    private boolean isMulChoice = false; // 是否多选

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    private void setColumnNumber(Context context, int columnNum) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        int widthPixels = metrics.widthPixels;
        imageSize = (widthPixels / columnNum) - 20;
    }

    public FileGridRecyclerViewAdapter(Context context, int coulums, ArrayList<String> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        this.coulums = coulums;
        this.context = context;
        setColumnNumber(context, coulums);

//        for (int i = 0; i < mValues.size(); i++) {
//            ischeck.put(i, false);
//        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_file_grid_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);

        if (isMulChoice) {
            holder.mCheckbox.setVisibility(View.VISIBLE);
        } else {
            holder.mCheckbox.setVisibility(View.GONE);
        }

//        switch (fileType) {
//            case 0:
//                ImageLoadHelper.getInstance().displayImageFromSD(mValues.get(position), holder.mLogo);
//                break;
//            case 1:
//                VideoThumailLoadUtil.getInstance().displayVideoThumail(mValues.get(position), holder.mLogo);
//                break;
//        }

        Glide.with(context).load(mValues.get(position)).placeholder(R.drawable.default_image_holder).into(holder.mLogo);
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                Log.e("9527", "FileGridRecyclerViewAdapter onLongClick");
                int layoutPos = holder.getLayoutPosition();
                onItemClickListener.onItemLongClick(holder.itemView, layoutPos);
                if (isMulChoice == false) {
                    isMulChoice = true;
                }
                holder.mCheckbox.setVisibility(View.VISIBLE);
                return true;
            }
        });
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("9527", "FileGridRecyclerViewAdapter OnClick");

                int layoutPos = holder.getLayoutPosition();
                onItemClickListener.onItemClick(holder.itemView, layoutPos);


                if (isMulChoice == false) {//单选进入预览
                    if (null != mListener) {
                        mListener.onListFragmentInteraction(holder.mItem);
                        if (fileType == 0) {
                            Intent picture = new Intent(context, PictureBrowseActivity.class);
                            picture.putExtra(PictureBrowseActivity.KEY_MODE, PictureBrowseActivity.MODE_LOCAL);
                            picture.putStringArrayListExtra(PictureBrowseActivity.KEY_TOTAL_LIST, mValues);
                            picture.putExtra(PictureBrowseActivity.KEY_POSTION, position);
                            context.startActivity(picture);
                        } else {
                            Intent video = new Intent(context, VideoActivity.class);
                            video.putExtra("videoPath", mValues.get(position));
                            video.putExtra("type", 1);
                            context.startActivity(video);
                        }
                    }
                } else if (isMulChoice == true) {//开始多选  删除或者分享
                    if (holder.mCheckbox.isChecked()) {
                        holder.mCheckbox.setChecked(false);
                        selectid.remove(mValues.get(position));
                    } else {
                        holder.mCheckbox.setChecked(true);
                        selectid.add(mValues.get(position));
                    }


                }
            }
        });
        holder.mCheckbox.setChecked(selectid.contains(mValues.get(position)));

        if (isMulChoice == false) {
            holder.mCheckbox.setChecked(false);
        }

    }


    public interface onItemClickListener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onButtonStatus(boolean display);
    }

    private onItemClickListener onItemClickListener;

    public void setOnItemClickListener(onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public void DeleteItem() {
        Log.e("9527", "DeleteItem");
        isMulChoice = false;
        if (selectid.size() != 0) {
            for (int i = 0; i < selectid.size(); i++) {
                for (int j = 0; j < mValues.size(); j++) {
                    if (selectid.get(i).equals(mValues.get(j))) {

                        String path = mValues.get(j);
                        Log.e("9527", "Path = " + path);


                        String where = MediaStore.Images.Media.DATA + "='" + path + "'";
                        context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, where, null);

                        File file = new File(path);
                        file.delete();

                        //context.getContentResolver().delete(Uri.fromFile(new File(path)), null, null);

                        mValues.remove(j);

                    }
                }
            }
        }

        selectid.clear();
        notifyDataSetChanged();
        onItemClickListener.onButtonStatus(false);

    }

    public void ShareItem() {
        Log.e("9527", "addItem position");

        if (selectid.size() == 0) {

            Toast.makeText(context, "请选择要分享的图片或视频", Toast.LENGTH_SHORT).show();
        } else {
            isMulChoice = false;

            if (selectid.size() != 0) {
                if (fileType == 0) {
//            Intent share_image = new Intent(Intent.ACTION_SEND);
//            share_image.setType("image/*");
//            share_image.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(selectid.get(0))));
//            share_image.putStringArrayListExtra("list", selectid);
//            share_image.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(share_image);


//                    ArrayList<Uri> photos = new ArrayList<>();
//
//                    for (int i = 0; i < selectid.size(); i++) {
//                        photos.add(Uri.fromFile(new File(selectid.get(i))));
//                    }

                    new ShareUtils().sharePhoto((Activity) context, selectid);


                } else {
                    if (selectid.size() > 1) {
                        Toast.makeText(context, context.getString(R.string.only_share_one_video), Toast.LENGTH_SHORT).show();
                    } else {

                        Intent share_video = new Intent(Intent.ACTION_SEND);
                        share_video.setType("video/*");
                        share_video.setComponent(new ComponentName(context.getPackageName(), "com.adai.gkdnavi.EditVideoActivity"));
                        share_video.putExtra("videoType", 0);
                        share_video.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(new File(selectid.get(0))));
                        context.startActivity(share_video);
                    }
                }
            }
            selectid.clear();
            notifyDataSetChanged();
            onItemClickListener.onButtonStatus(false);
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mLogo;
        public final CheckBox mCheckbox;
        public String mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mLogo = (ImageView) view.findViewById(R.id.logo);
            mCheckbox = (CheckBox) view.findViewById(R.id.checkbox);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mLogo.getLayoutParams();
            params.width = imageSize;
            params.height = imageSize;
            mLogo.setLayoutParams(params);
        }

        @Override
        public String toString() {
            return super.toString() + " '";
        }
    }

}
