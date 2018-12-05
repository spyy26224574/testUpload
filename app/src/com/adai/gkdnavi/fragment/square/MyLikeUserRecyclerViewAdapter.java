package com.adai.gkdnavi.fragment.square;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.bean.square.LikeUserBean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkd.contacts.RequestMethods_square;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.BaseFragmentActivity;
import com.adai.gkdnavi.PersonalPageActivity;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.fragment.square.LikeuserListFragment.OnListFragmentInteractionListener;
import com.adai.gkdnavi.utils.imageloader.ImageLoaderUtil;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link LikeUserBean} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class MyLikeUserRecyclerViewAdapter extends RecyclerView.Adapter<MyLikeUserRecyclerViewAdapter.ViewHolder> {

    private final List<LikeUserBean> mValues;
    private final OnListFragmentInteractionListener mListener;
    private Activity mContext;

    public MyLikeUserRecyclerViewAdapter(Activity context, List<LikeUserBean> items, OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
        mContext = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_likeuser, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);

        holder.nickname.setText(holder.mItem.nickname);
        holder.sign.setText(holder.mItem.signature);
//        Glide.with(mContext).load(holder.mItem.portrait).into(holder.head_img);
        ImageLoaderUtil.getInstance().loadRoundImage(mContext, holder.mItem.portrait, R.drawable.icon_friend_normal, holder.head_img);
        if (holder.mItem.userId == CurrentUserInfo.id) {
            holder.add_attention.setVisibility(View.GONE);
        } else {
            holder.add_attention.setVisibility(View.VISIBLE);
        }
        if ("Y".equals(holder.mItem.isFocusOn)) {
            holder.add_attention.setText(mContext.getString(R.string.already_attention));
            holder.add_attention.setSelected(true);
        } else {
            holder.add_attention.setText(mContext.getString(R.string.add_attention));
            holder.add_attention.setSelected(false);
        }
        holder.add_attention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!((BaseFragmentActivity) mContext).checkLogin()) {
                    return;
                }
                if ("Y".equals(holder.mItem.isFocusOn)) {
                    RequestMethods_square.deleteAttention(holder.mItem.userId, new HttpUtil.Callback<BasePageBean>() {
                        @Override
                        public void onCallback(BasePageBean result) {
                            if (result != null) {
                                switch (result.ret) {
                                    case 0:
                                        holder.mItem.isFocusOn = "N";
                                        notifyItemChanged(position);
                                        break;
                                    default:
                                        Toast.makeText(mContext, result.message, Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        }
                    });
                } else {
                    RequestMethods_square.addAttention(holder.mItem.userId, new HttpUtil.Callback<BasePageBean>() {
                        @Override
                        public void onCallback(BasePageBean result) {
                            if (result != null) {
                                switch (result.ret) {
                                    case 0:
                                        holder.mItem.isFocusOn = "Y";
                                        notifyItemChanged(position);
                                        break;
                                    default:
                                        Toast.makeText(mContext, result.message, Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        }
                    });
                }
            }
        });

        holder.head_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent personal = new Intent(mContext, PersonalPageActivity.class);
                personal.putExtra("userid", holder.mItem.userId);
                mContext.startActivity(personal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView head_img;
        public final TextView nickname;
        public final TextView sign;
        public final TextView add_attention;
        public LikeUserBean mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            head_img = (ImageView) view.findViewById(R.id.head_img);
            nickname = (TextView) view.findViewById(R.id.nickname);
            sign = (TextView) view.findViewById(R.id.sign);
            add_attention = (TextView) view.findViewById(R.id.add_attention);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + nickname.getText() + "'";
        }
    }
}
