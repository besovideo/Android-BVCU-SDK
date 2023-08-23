package com.smarteye.sdk.trans.cu.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.smarteye.adapter.BVCU_PUOneChannelInfo;
import com.smarteye.sdk.trans.cu.demo.R;

import java.util.ArrayList;
import java.util.List;

public class ChannelItemAdapter extends BaseAdapter {
    private final Context context;
    private List<BVCU_PUOneChannelInfo> channelInfoList;
    public ChannelItemAdapter(Context context, List<BVCU_PUOneChannelInfo> channelInfoList) {
        this.context = context;
        this.channelInfoList = channelInfoList;
        if (channelInfoList == null) {
            this.channelInfoList = new ArrayList<>();
        }
    }

    public void setChannelInfoList(@NonNull List<BVCU_PUOneChannelInfo> channelInfoList) {
        this.channelInfoList = channelInfoList;
    }

    @Override
    public int getCount() {
        return channelInfoList.size();
    }

    @Override
    public Object getItem(int i) {
        return channelInfoList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_channel, null);
            holder = new ViewHolder();
            holder.channelName = view.findViewById(R.id.item_channel_name);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        BVCU_PUOneChannelInfo channelInfo = channelInfoList.get(i);
        holder.index = i;
        holder.channelIndex = channelInfo.iChannelIndex;
        holder.channelName.setText(channelInfo.szName);
        return view;
    }

    /**
     * 获取通道索引，因为实际上的通道索引不一定和position相同
     * @param position 通道所在列表的位置
     * @return 通道索引
     */
    public int getChannelIndex(int position) {
        if (position < 0 || position > channelInfoList.size())
            return -1;
        return channelInfoList.get(position).iChannelIndex;
    }

    private static class ViewHolder {
        int index;
        int channelIndex;
        TextView channelName;
    }
}
