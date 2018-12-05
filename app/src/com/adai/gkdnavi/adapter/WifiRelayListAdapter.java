package com.adai.gkdnavi.adapter;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.adai.gkdnavi.R;

import java.util.List;

public class WifiRelayListAdapter extends BaseAdapter {

    private Context context;
    private List<ScanResult> wifiList;
    //private Handler setWifiHandler = null;
    int mSelect = -1; //

    public void changeSelected(int positon) { //
        if (positon != mSelect) {
            mSelect = positon;
            notifyDataSetChanged();
        }
    }

    public WifiRelayListAdapter(Context context, List<ScanResult> wifiList) {
        this.context = context;
        this.wifiList = wifiList;
    }

    @Override
    public int getCount() {
        return wifiList.size();
    }

    @Override
    public Object getItem(int position) {
        return wifiList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.wifi_list, null);
        }
        final ScanResult childData = wifiList.get(position);

        TextView wifi_info_text = (TextView) convertView
                .findViewById(R.id.wifiname);
        wifi_info_text.setText(childData.SSID);
        // ##############
        View wifiSelect = convertView.findViewById(R.id.view);
        // ###############
//		LinkWifi linkWifi = new LinkWifi(context);
//		WifiManager wifiManager = (WifiManager) context.getApplicationContext()
//				.getSystemService(Service.WIFI_SERVICE);

//		if (linkWifi.IsExsits(childData.SSID) != null
//				&& linkWifi.IsExsits(childData.SSID).networkId == wifiManager
//						.getConnectionInfo().getNetworkId()) {
//		}
        //##############
//		convertView.setOnClickListener(new OnClickListener() {
//			public void onClick(View arg0) {
//				if (setWifiHandler != null) {
//					Message msg = new Message();
//					msg.what = 0;
//					msg.obj = childData;
//					setWifiHandler.sendMessage(msg);
//				}
//			}
//		});
        //##################
        if (position == mSelect) {
            wifiSelect.setBackgroundDrawable(context.getResources().getDrawable(
                    R.drawable.wifi_selected));
        } else {
            wifiSelect.setBackgroundDrawable(context.getResources().getDrawable(
                    R.drawable.wifi_unselected));
        }
        //######################
        convertView.setTag("wifi_" + childData.BSSID);

        return convertView;
    }

}
