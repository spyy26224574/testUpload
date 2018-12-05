package com.adai.camera.sunplus.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.adai.camera.CameraFactory;
import com.adai.gkdnavi.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by huangxy on 2017/11/13 16:31.
 */

public class SunplusResolutionAdapter extends RecyclerView.Adapter<SunplusResolutionAdapter.ViewHolder> {
    private List<String> mValueList;
    private int mType;
    static final public int VIDEO_RESOLUTION = 0;
    static final public int PHOTO_RESOLUTION = 5;
    private ItemClickListener mItemClickListener;

    public interface ItemClickListener {
        /**
         * item点击事件的回调
         *
         * @param type     指令类型
         * @param position 点击的条目位置
         */
        void onItemClick(int type, int position, String value);
    }

    public void setOnItemClickListener(ItemClickListener onItemClickListener) {
        mItemClickListener = onItemClickListener;
    }

    public SunplusResolutionAdapter(int type) {
        mType = type;
        switch (mType) {
            case VIDEO_RESOLUTION:
                mValueList = CameraFactory.getInstance().getSunplusCamera().getVideoSize().getValueList();
                break;
            case PHOTO_RESOLUTION:
                String[] valueArrayString = CameraFactory.getInstance().getSunplusCamera().getImageSize().getValueArrayString();
                mValueList = Arrays.asList(valueArrayString);
                break;
            default:
                break;
        }

    }

    @Override
    public SunplusResolutionAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_resolution, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SunplusResolutionAdapter.ViewHolder holder, final int position) {
        holder.mTvResolution.setText(mValueList.get(position));
        holder.mItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(mType, holder.getAdapterPosition(),mValueList.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValueList == null ? 0 : mValueList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTvResolution;
        private View mItemView;

        public ViewHolder(View itemView) {
            super(itemView);
            mTvResolution = (TextView) itemView.findViewById(R.id.tv_resolution);
            mItemView = itemView;
        }
    }
}
