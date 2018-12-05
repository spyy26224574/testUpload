package com.adai.gkdnavi.adapter;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.adai.gkdnavi.R;

import java.util.ArrayList;
import java.util.List;


public class AutoComleteAdapter extends BaseAdapter implements Filterable {
    private ArrayFilter mFilter;
    private List<Addinfo> mList;
    private Context context;
    private ArrayList<Addinfo> mUnfilteredData;

    public static final int ADDRESS = 0;
    public static final int MORE_ADDRESS = 1;

    public AutoComleteAdapter(List<Addinfo> mList, Context context) {
        this.mList = mList;
        this.context = context;
    }

    @Override
    public int getCount() {

        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mList.get(position);
    }

    @Override
    public int getItemViewType(int position) {
        // TODO Auto-generated method stub
        return mList.get(position).addresstype;
    }

    //两个样式 返回2
    @Override
    public int getViewTypeCount() {
        // TODO Auto-generated method stub
        return 2;
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        ViewHolder holder = null;
        if (convertView == null) {

            switch (type) {
                case ADDRESS:
                    convertView = View.inflate(context, R.layout.route_inputs, null);
                    break;
                case MORE_ADDRESS:
                    convertView = View.inflate(context, R.layout.route_inputs_more, null);
                    break;
            }
            holder = new ViewHolder();
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.tv_address = (TextView) convertView.findViewById(R.id.tv_address);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();

        }

        Addinfo pc = mList.get(position);
        holder.tv_name.setText(pc.name);
        if (type == ADDRESS) {
            holder.tv_address.setText(pc.address);
        }
        return convertView;
    }

    static class ViewHolder {
        public TextView tv_name;
        public TextView tv_address;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    private class ArrayFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            if (mUnfilteredData == null) {
                mUnfilteredData = new ArrayList<Addinfo>(mList);
            }

            if (prefix == null || prefix.length() == 0) {
                ArrayList<Addinfo> list = mUnfilteredData;
                results.values = list;
                results.count = list.size();
            } else {
                String prefixString = prefix.toString().toLowerCase();

                ArrayList<Addinfo> unfilteredValues = mUnfilteredData;
                int count = unfilteredValues.size();

                ArrayList<Addinfo> newValues = new ArrayList<Addinfo>(count);

                for (int i = 0; i < count; i++) {
                    Addinfo pc = unfilteredValues.get(i);
                    if (pc != null) {

                        if (pc.name != null && pc.name.startsWith(prefixString)) {

                            newValues.add(pc);
                        }
                    }
                }

                results.values = newValues;
                results.count = newValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            //noinspection unchecked
            mList = (List<Addinfo>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

    }
}
