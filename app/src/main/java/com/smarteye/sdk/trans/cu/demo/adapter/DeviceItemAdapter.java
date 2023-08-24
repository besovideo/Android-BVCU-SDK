package com.smarteye.sdk.trans.cu.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.smarteye.adapter.BVCU_PUChannelInfo;
import com.smarteye.adapter.BVCU_PUOneChannelInfo;
import com.smarteye.adapter.BVCU_Search_UAInfo;
import com.smarteye.adapter.BVCU_SubDev;
import com.smarteye.sdk.trans.cu.demo.R;
import com.smarteye.sdk.trans.cu.demo.SmartEyeDataManager;

import java.util.ArrayList;
import java.util.List;

public class DeviceItemAdapter extends BaseAdapter {
    private final Context context;
    private final List<BVCU_PUChannelInfo> puDeviceList;
    private final OnChannelClickCallback callback;

    public DeviceItemAdapter(Context context, List<BVCU_PUChannelInfo> puDeviceList
            , OnChannelClickCallback callback) {
        this.context = context;
        this.puDeviceList = puDeviceList;
        this.callback = callback;
    }

    @Override
    public int getCount() {
        return puDeviceList.size();
    }

    @Override
    public Object getItem(int i) {
        if (puDeviceList.size() > i) {
            return puDeviceList.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        int devicePosition = i;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_device, null);
            holder = new ViewHolder();
            holder.layout = view.findViewById(R.id.item_device_layout);
            holder.deviceName = view.findViewById(R.id.item_device_name);
            holder.deviceID = view.findViewById(R.id.item_device_id);
            holder.lineView = view.findViewById(R.id.item_device_line_view);
            holder.channelList = view.findViewById(R.id.item_device_listview);
            holder.channelAdapter = new ChannelItemAdapter(context, new ArrayList<>());
            view.setTag(holder);
            // 点击设备名显示或隐藏通道列表
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.showList = !holder.showList;
                    int visibility;
                    if (holder.showList)
                        visibility = View.VISIBLE;
                    else
                        visibility = View.GONE;
                    holder.lineView.setVisibility(visibility);
                    holder.channelList.setVisibility(visibility);
                }
            });
            // 点击通道名，执行回调
            holder.channelList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    BVCU_PUChannelInfo puDevice = (BVCU_PUChannelInfo)getItem(devicePosition);
                    if (puDevice == null)
                        return;
                    String deviceID = puDevice.szPUID;
                    int channelIndex = holder.channelAdapter.getChannelIndex(i);
                    if (callback != null && channelIndex >= 0) {
                        callback.onChannelClick(deviceID, channelIndex);
                    }
                }
            });
            holder.channelList.setAdapter(holder.channelAdapter);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        BVCU_PUChannelInfo puDevice = (BVCU_PUChannelInfo)getItem(i);
        if (puDevice == null)
            return view;
        BVCU_Search_UAInfo uaDevice = SmartEyeDataManager.getInstance(context).getUaDevice(puDevice.szPUID);
        holder.index = i;
        // 显示设备名
        if (uaDevice == null) {
            holder.deviceName.setText(puDevice.szPUName);
        } else {
            holder.deviceName.setText(puDevice.szPUName + " (" + uaDevice.szUserName + ")");
        }
        holder.deviceID.setText(puDevice.szPUID);
        if (puDevice.pChannel != null) {
            List<BVCU_PUOneChannelInfo> channelInfoList = new ArrayList<>();
            // 通道列表仅显示音视频通道，其他通道如：GPS通道，串口通道等不显示
            for (BVCU_PUOneChannelInfo item : puDevice.pChannel) {
                if (item != null
                        && item.iChannelIndex >= BVCU_SubDev.BVCU_SUBDEV_INDEXMAJOR_MIN_CHANNEL
                        && item.iChannelIndex <= BVCU_SubDev.BVCU_SUBDEV_INDEXMAJOR_MAX_CHANNEL) {
                    channelInfoList.add(item);
                }
            }
            holder.channelAdapter.setChannelInfoList(channelInfoList);
            holder.channelAdapter.notifyDataSetChanged();
        }
        return view;
    }

    private static class ViewHolder {
        int index;
        boolean showList = false;
        LinearLayout layout;
        TextView deviceName;
        TextView deviceID;
        View lineView;
        ListView channelList;
        ChannelItemAdapter channelAdapter;
    }

    public interface OnChannelClickCallback {
        void onChannelClick(String deviceID, int channelIndex);
    }
}
