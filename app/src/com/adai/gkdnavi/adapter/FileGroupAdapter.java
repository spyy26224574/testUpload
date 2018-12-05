package com.adai.gkdnavi.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adai.gkdnavi.R;
import com.adai.gkdnavi.fragment.square.AlbumNewFragment;
import com.adai.gkdnavi.view.NoScrollGridView;
import com.example.ipcamera.domain.HourFile;
import com.example.ipcamera.domain.MinuteFile;

import java.util.List;

/**
 * Created by huangxy on 2016/12/5.
 */

public class FileGroupAdapter extends RecyclerView.Adapter<FileGroupAdapter.ViewHolder> {
    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean editMode) {
        isEditMode = editMode;
        notifyDataSetChanged();
    }

    private boolean isEditMode;
    private Activity mContext;
    private List<HourFile> mHourFiles;
    private int mType;

    public FileGroupAdapter(Activity context, List<HourFile> hourFiles, int type) {
        mContext = context;
        mHourFiles = hourFiles;
        mType = type;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.item_file_grid_new, null);
        return new ViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mTvSelect.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        holder.mTvSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHourFiles.get(holder.getLayoutPosition()).isChecked) {
                    //选择当前小时下的所有文件
                    mHourFiles.get(holder.getLayoutPosition()).isChecked = false;
                    for (MinuteFile minuteFile : mHourFiles.get(holder.getLayoutPosition()).minuteFiles) {
                        minuteFile.isChecked = false;
                    }
                } else {
                    //取消当前小时下的所有文件的选中
                    mHourFiles.get(holder.getLayoutPosition()).isChecked = true;
                    for (MinuteFile minuteFile : mHourFiles.get(holder.getLayoutPosition()).minuteFiles) {
                        minuteFile.isChecked = true;
                    }
                }
                notifyItemChanged(holder.getLayoutPosition());
            }
        });
        holder.mTvSelect.setText(mHourFiles.get(holder.getLayoutPosition()).isChecked ? mContext.getString(R.string.cancel_select) : mContext.getString(R.string.select));
        if (mType == AlbumNewFragment.ALBUM_RECORDER) {
            holder.mTime.setText(mHourFiles.get(position).time + ":00");
        } else {
            holder.mTime.setText(mHourFiles.get(position).time);
        }
        holder.mImageGridNewAdapter = new ImageGridNewAdapter(mContext, mHourFiles.get(position), mType, new ImageGridNewAdapter.AllFileSelectedListener() {
            @Override
            public void selected(boolean isSelected) {
                mHourFiles.get(holder.getLayoutPosition()).isChecked = isSelected;
                notifyItemChanged(holder.getLayoutPosition());
            }
        });
        holder.mNoScrollGridView.setAdapter(holder.mImageGridNewAdapter);
        holder.mImageGridNewAdapter.setEditMode(isEditMode);
    }

    @Override
    public int getItemCount() {
        return mHourFiles == null ? 0 : mHourFiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvSelect;
        private TextView mTime;
        private NoScrollGridView mNoScrollGridView;
        private ImageGridNewAdapter mImageGridNewAdapter;

        public ViewHolder(View itemView) {
            super(itemView);
            mTvSelect = (TextView) itemView.findViewById(R.id.tv_select);
            mTime = (TextView) itemView.findViewById(R.id.tv_time);
            mNoScrollGridView = (NoScrollGridView) itemView.findViewById(R.id.nsgv);
        }
    }
}
