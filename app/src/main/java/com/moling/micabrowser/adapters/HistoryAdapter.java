package com.moling.micabrowser.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.moling.micabrowser.R;
import com.moling.micabrowser.models.HistoryModel;

import java.util.List;

public class HistoryAdapter extends BaseAdapter {
    private List<HistoryModel> mData;
    private LayoutInflater mInflater;
    public HistoryAdapter(LayoutInflater inflater, List<HistoryModel> data) {
        this.mInflater = inflater;
        this.mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View viewHistory = mInflater.inflate(R.layout.widget_history, null);
        // 获取 history 对象
        HistoryModel history = mData.get(position);
        // 获得自定义布局中每一个控件的对象
        TextView text_title = (TextView) viewHistory.findViewById(R.id.text_title);
        TextView text_url = (TextView) viewHistory.findViewById(R.id.text_url);
        // 将数据添加到布局
        text_title.setText(history.getTitle());
        text_url.setText(history.getUrl());
        return viewHistory;
    }
}
