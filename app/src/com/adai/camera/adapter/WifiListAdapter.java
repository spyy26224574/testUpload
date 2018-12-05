package com.adai.camera.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.adai.camera.bean.WifiBean;
import com.adai.gkdnavi.R;

import java.util.List;

/**
 * @author ryujin
 * @version $Rev$
 * @time 2016/11/10 14:33
 * @updateAuthor $Author$
 * @updateDate $Date$
 */

public class WifiListAdapter extends BaseAdapter {
    private List<WifiBean> mWifiList;
    private Context mContext;

    public WifiListAdapter(Context context, List<WifiBean> wifiList) {
        mContext = context;
        mWifiList = wifiList;
    }

    @Override
    public int getCount() {
        return mWifiList == null ? 0 : mWifiList.size();
    }

    @Override
    public Object getItem(int position) {
        return mWifiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(mContext, R.layout.item_wifi_list, null);
            holder.wifiName = (TextView) convertView.findViewById(R.id.wifi_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String ssid = mWifiList.get(position).SSID;
        if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
            ssid = ssid.substring(1, ssid.length() - 1);
        }
        holder.wifiName.setText(ssid);
        return convertView;
    }

    private static class ViewHolder {
        TextView wifiName;
    }
}
