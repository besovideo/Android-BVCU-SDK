package com.smarteye.sdk.trans.cu.demo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.smarteye.sdk.trans.cu.demo.adapter.DeviceItemAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DevicesActivity extends AppCompatActivity implements DeviceItemAdapter.OnChannelClickCallback{
    private ListView deviceListView;
    private DeviceItemAdapter deviceAdapter;
    private SmartEyeDataManager dataManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        EventBus.getDefault().register(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("设备列表");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        dataManager = SmartEyeDataManager.getInstance(this);
        dataManager.requestDeviceList();
        deviceListView = findViewById(R.id.activity_device_listview);
        deviceAdapter = new DeviceItemAdapter(this, dataManager.getPuDeviceList(), this);
        deviceListView.setAdapter(deviceAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventBusMessage message) {
        switch (message.getMessageType()) {
            case EventBusMessage.EVENTBUS_GET_PU_LIST_RESPONSE:
            case EventBusMessage.EVENTBUS_GET_UA_LIST_RESPONSE:
                deviceAdapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onChannelClick(String deviceID, int channelIndex) {
        Log.d("DevicesTAG", "device id: " + deviceID + ", channel index: " + channelIndex);
        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra("deviceID", deviceID);
        intent.putExtra("channelIndex", channelIndex);
        startActivity(intent);
    }
}