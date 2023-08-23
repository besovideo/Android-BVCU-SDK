package com.smarteye.sdk.trans.cu.demo;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.smarteye.adapter.BVCU_Command;
import com.smarteye.adapter.BVCU_DialogInfo;
import com.smarteye.adapter.BVCU_DialogParam;
import com.smarteye.adapter.BVCU_EventCode;
import com.smarteye.adapter.BVCU_Event_DialogCmd;
import com.smarteye.adapter.BVCU_File_TransferInfos;
import com.smarteye.adapter.BVCU_Packet;
import com.smarteye.adapter.BVCU_Result;
import com.smarteye.adapter.BVCU_Search_Response;
import com.smarteye.adapter.BVCU_SessionInfo;
import com.smarteye.adapter.BVCU_SubMethod;
import com.smarteye.bean.JNIMessage;
import com.smarteye.mcp.sdk.smarteye.bvcu.IMyBVCUCallback;

import org.greenrobot.eventbus.EventBus;

public class SmartEyeEventCallBack implements IMyBVCUCallback {

    private final String TAG = "SmartEyeEventCallBack";
    private Context context;
    private final Gson gson = new Gson();

    private SmartEyeEventCallBack() {
    }

    public SmartEyeEventCallBack(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public void OnSessionEvent(int hSession, int iEventCode, int iResult, BVCU_SessionInfo sessionInfo) {
        // 登录结果回调
        Log.i(TAG, "iEventCode = " + iEventCode + ", iResult = " + iResult);
        switch (iEventCode) {
            case BVCU_EventCode.BVCU_EVENT_SESSION_OPEN:
                if (iResult == BVCU_Result.BVCU_RESULT_S_OK) {
                    Toast.makeText(context, "登录成功", Toast.LENGTH_LONG).show();
                    LoginActivity.loginResult(true);// 只是为了此demo的UI显示
                } else {
                    Toast.makeText(context, "登录失败 = " + iResult, Toast.LENGTH_LONG).show();
                    LoginActivity.loginResult(false);// 只是为了此demo的UI显示
                }
                break;
            case BVCU_EventCode.BVCU_EVENT_SESSION_CLOSE:
                Toast.makeText(context, "注销登录", Toast.LENGTH_LONG).show();
                LoginActivity.loginResult(false);// 只是为了此demo的UI显示
                break;
            default:
                break;
        }
    }

    @Override
    public int OnSessionCommand(int i, BVCU_Command bvcu_command) {
        return BVCU_Result.BVCU_RESULT_E_UNSUPPORTED;
    }

    @Override
    public int OnPasvDialogCmd(int hDialog, int iEventCode, BVCU_DialogParam pParam) {
        return BVCU_Result.BVCU_RESULT_E_UNSUPPORTED;
    }

    @Override
    public void OnPasvDialogEvent(int hDialog, int iEventCode, BVCU_Event_DialogCmd pParam) {

    }

    @Override
    public void OnDialogEvent(int hDialog, int iEventCode, BVCU_Event_DialogCmd pParam) {
        Log.d(TAG, "hDialog: " + hDialog + ", iEventCode: " + iEventCode + ", result: " + pParam.iResult);
        EventBus.getDefault().post(new EventBusMessage(EventBusMessage.EVENTBUS_ON_DIALOG_EVENT,
                new Object[]{hDialog, iEventCode, pParam}));
    }

    @Override
    public void OnGetDialogInfo(int iToken, BVCU_DialogInfo bvcu_dialogInfo) {

    }

    @Override
    public int OnCmdEvent(int hCmd, BVCU_Command bvcu_command, int iResult) {
        switch (bvcu_command.iSubMethod) {
            case BVCU_SubMethod.BVCU_SUBMETHOD_SEARCH_LIST:
                BVCU_Search_Response rsp = (BVCU_Search_Response) bvcu_command.stMsgContent.pData;
                Log.i(TAG, "-----[ cmd query search list response ]-----");
                Log.i(TAG, "----rsp: " + gson.toJson(rsp));
                Log.i(TAG, "----iResult: " + iResult);
                Log.i(TAG, "---------------------------------------------");
                SmartEyeDataManager.getInstance(context).dealSearchResponse(rsp);
                break;
        }
        return 0;
    }

    @Override
    public int DialogAfterRecv(int i, BVCU_Packet bvcu_packet) {
        return 0;
    }

    @Override
    public void OnFileTransferInfo(BVCU_File_TransferInfos[] bvcu_file_transferInfos) {

    }

    @Override
    public void OnElecMapAlarm(int i, String s) {

    }

    @Override
    public void OnElecMapConfigUpdate(String s, String s1) {

    }

    @Override
    public void OnNotifyMessage(JNIMessage jniMessage) {

    }

}
