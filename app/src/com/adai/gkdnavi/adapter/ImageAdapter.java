package com.adai.gkdnavi.adapter;


import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.adai.gkd.bean.AdvertisementInfoBean;
import com.adai.gkd.bean.BasePageBean;
import com.adai.gkd.contacts.CurrentUserInfo;
import com.adai.gkd.contacts.RequestMethods;
import com.adai.gkd.httputils.HttpUtil;
import com.adai.gkdnavi.R;
import com.adai.gkdnavi.WebviewActivity;
import com.adai.gkdnavi.utils.VoiceManager;
import com.bumptech.glide.Glide;

public class ImageAdapter extends BaseAdapter {

	Context context;
	int images[] = new int[]{R.drawable.news1,R.drawable.news2,R.drawable.news3};
	AdvertisementInfoBean[] data=new AdvertisementInfoBean[3];
	//ImageLoader
	public ImageAdapter(final Context context) {
		this.context = context;
		
	}

	public void addAdv(AdvertisementInfoBean adv,int position){
		if(position<data.length){
			data[position]=adv;
			notifyDataSetChanged();
		}
	}

	public Context getContext(){
		return context;
	}

    public int[] getDrawables(){
        return images;
    }
	
	@Override
	public int getCount() {
		return images.length;
	}

	@Override
	public Object getItem(int position) {
		return images[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup viewgroup) {
		if(convertView==null)
        convertView = new ImageView(context);
        ((ImageView) convertView).setScaleType(ScaleType.FIT_XY);
		((ImageView) convertView).setLayoutParams(new Gallery.LayoutParams(Gallery.LayoutParams.MATCH_PARENT,Gallery.LayoutParams.MATCH_PARENT));
//        convertView.setBackgroundResource(images[position]);
		AdvertisementInfoBean adv=data[position];
		if(adv!=null){
			Glide.with(context).load(adv.cover).placeholder(R.drawable.default_image_holder).into((ImageView) convertView);
			convertView.setTag(adv);
		}else {
			Glide.with(context).load(images[position]).into((ImageView) convertView);
		}
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Object tagobj = v.getTag();
				if(tagobj!=null&&tagobj instanceof AdvertisementInfoBean){
					gotoWeb(((AdvertisementInfoBean) tagobj).link,((AdvertisementInfoBean) tagobj).title);
					uploadClick((AdvertisementInfoBean) tagobj);
				}
			}
		});
		return convertView;
	}

	private void gotoWeb(String url,String title){
		if(TextUtils.isEmpty(url))return;
		Intent web=new Intent(context, WebviewActivity.class);
		web.putExtra(WebviewActivity.KEY_TITLE,title);
		web.putExtra(WebviewActivity.KEY_URL,url);
		context.startActivity(web);
	}

	private void uploadClick(AdvertisementInfoBean adv){
		RequestMethods.advertisementRecord(adv.id, CurrentUserInfo.id, VoiceManager.xuliehao, new HttpUtil.Callback<BasePageBean>() {
			@Override
			public void onCallback(BasePageBean result) {
				if(result!=null)
				Log.e(this.getClass().getSimpleName(),"result.ret="+result.ret);
			}
		});
	}
}
