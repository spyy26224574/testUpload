package com.adai.gkdnavi.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.adai.gkd.bean.AdvertisementInfoBean;
import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.WebviewActivity;
import com.adai.gkdnavi.utils.VoiceManager;
import com.bumptech.glide.Glide;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/10/27 10:02
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class ImagePageAdapter extends PagerAdapter {
    private int images[] = new int[]{R.drawable.news1, R.drawable.news2, R.drawable.news3};
    private AdvertisementInfoBean[] data = new AdvertisementInfoBean[3];
    private Context mContext;

    public ImagePageAdapter(Context context) {
        mContext = context;
    }

    public void addAdv(AdvertisementInfoBean adv, int position) {
        if (position < data.length) {
            data[position] = adv;
            notifyDataSetChanged();
        }
    }

    public int[] getDrawables() {
        return images;
    }

    public Context getContext() {
        return mContext;
    }

    @Override
    public int getCount() {

        return images.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView = new ImageView(mContext);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
//        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        AdvertisementInfoBean adv = data[position];
        if (adv != null) {
            Glide.with(mContext).load(adv.cover).placeholder(R.drawable.default_image_holder).into(imageView);
        } else {
            Glide.with(mContext).load(images[position]).into(imageView);
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object tagobj = v.getTag();
                if (tagobj != null && tagobj instanceof AdvertisementInfoBean) {
                    gotoWeb(((AdvertisementInfoBean) tagobj).link, ((AdvertisementInfoBean) tagobj).title);
                    uploadClick((AdvertisementInfoBean) tagobj);
                }
            }
        });
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    private void gotoWeb(String url, String title) {
        if (TextUtils.isEmpty(url)) return;
        Intent web = new Intent(mContext, WebviewActivity.class);
        web.putExtra(WebviewActivity.KEY_TITLE, title);
        web.putExtra(WebviewActivity.KEY_URL, url);
        mContext.startActivity(web);
    }

    private void uploadClick(AdvertisementInfoBean adv) {
        RequestMethods.advertisementRecord(adv.id, CurrentUserInfo.id, VoiceManager.xuliehao, new HttpUtil.Callback<BasePageBean>() {
            @Override
            public void onCallback(BasePageBean result) {
                if (result != null)
                    Log.e(this.getClass().getSimpleName(), "result.ret=" + result.ret);
            }
        });
    }
}
