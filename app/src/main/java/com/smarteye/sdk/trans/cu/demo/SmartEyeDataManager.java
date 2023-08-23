package com.smarteye.sdk.trans.cu.demo;

import android.content.Context;

import com.smarteye.adapter.BVCU_PUChannelInfo;
import com.smarteye.adapter.BVCU_SEARCH_TYPE;
import com.smarteye.adapter.BVCU_Search_PUListFilter;
import com.smarteye.adapter.BVCU_Search_Response;
import com.smarteye.adapter.BVCU_Search_UAInfo;
import com.smarteye.mcp.sdk.smarteye.BVSmarteye;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

public class SmartEyeDataManager {
    private static SmartEyeDataManager instance;
    private final List<BVCU_PUChannelInfo> puDeviceList = new ArrayList<>();
    private final List<BVCU_Search_UAInfo> uaDeviceList = new ArrayList<>();
    private final Context context;

    public static SmartEyeDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new SmartEyeDataManager(context);
        }
        return instance;
    }

    private SmartEyeDataManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void dealSearchResponse(BVCU_Search_Response response) {
        if (response == null || response.stSearchInfo == null)
            return;
        switch (response.stSearchInfo.iType) {
            case BVCU_SEARCH_TYPE.BVCU_SEARCH_TYPE_PULIST: {
                if (response.pPUList == null)
                    return;
                if (response.stSearchInfo.iPostition == 0) {
                    puDeviceList.clear();// 从头开始接收数据，清空设备列表
                }
                for (BVCU_PUChannelInfo item : response.pPUList) {
                    if (item != null && item.iOnlineStatus == 1) {
                        puDeviceList.add(item);
                    }
                }

                int requestEndPosition = response.stSearchInfo.iCount + response.stSearchInfo.iPostition;
                if (requestEndPosition < response.stSearchInfo.iTotalCount) {
                    // 还有数据则继续请求
                    getPuList(requestEndPosition);
                } else {
                    // 数据已经全部接收完成，发送通知消息
                    EventBus.getDefault().post(new EventBusMessage(EventBusMessage.EVENTBUS_GET_PU_LIST_RESPONSE));
                    // 获取UA列表 PU列表中已经包含UA设备，但不包含UA设备的登录账户名等信息，需要额外获取
                    getUaList(0);
                }
                break;
            }
            case BVCU_SEARCH_TYPE.BVCU_SEARCH_TYPE_UALIST: {
                if (response.pUAList == null)
                    return;
                if (response.stSearchInfo.iPostition == 0) {
                    uaDeviceList.clear();// 从头开始接收数据，清空设备列表
                }
                for (BVCU_Search_UAInfo item : response.pUAList) {
                    if (item != null) {
                        uaDeviceList.add(item);
                    }
                }

                int requestEndPosition = response.stSearchInfo.iCount + response.stSearchInfo.iPostition;
                if (requestEndPosition < response.stSearchInfo.iTotalCount) {
                    // 还有数据则继续请求
                    getUaList(requestEndPosition);
                } else {
                    // 数据已经全部接收完成，发送通知消息
                    EventBus.getDefault().post(new EventBusMessage(EventBusMessage.EVENTBUS_GET_UA_LIST_RESPONSE));
                }
                break;
            }
        }
    }

    public List<BVCU_PUChannelInfo> getPuDeviceList() {
        return puDeviceList;
    }

    public List<BVCU_Search_UAInfo> getUaDeviceList() {
        return uaDeviceList;
    }

    /**
     * 根据设备ID获取UA信息
     * @param deviceID 设备ID
     * @return UA信息
     */
    public BVCU_Search_UAInfo getUaDevice(String deviceID) {
        for (BVCU_Search_UAInfo item : uaDeviceList) {
            if (item.szDevID.equals(deviceID)) {
                return item;
            }
        }
        return null;
    }

    /**
     * 请求设备列表
     */
    public void requestDeviceList() {
        getPuList(0);
    }

    /**
     * 获取PU设备列表
     * @param position 开始的位置
     */
    private void getPuList(int position) {
        BVCU_Search_PUListFilter puListFilter = new BVCU_Search_PUListFilter();
        puListFilter.iOnlineStatus = 1;// 0-全部 1-在线设备 2-不在线设备
        BVSmarteye.getBVCUManger(context).sendQueryPUList(puListFilter, position, 128);
    }

    /**
     * 获取UA设备列表
     * @param position 开始的位置
     */
    private void getUaList(int position) {
        // 获取UA设备列表不需要设置过滤登录状态，因为只会获取到在线设备
        BVSmarteye.getBVCUManger(context).sendQueryUAList(null, position, 128);
    }
}
