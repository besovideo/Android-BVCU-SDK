package com.smarteye.sdk.trans.cu.demo;

import static com.smarteye.adapter.BVCU_MediaDir.BVCU_MEDIADIR_AUDIORECV;
import static com.smarteye.adapter.BVCU_MediaDir.BVCU_MEDIADIR_VIDEORECV;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.smarteye.adapter.BVCU_DialogControlParam;
import com.smarteye.adapter.BVCU_DialogControlParam_Render;
import com.smarteye.adapter.BVCU_DialogInfo;
import com.smarteye.adapter.BVCU_DialogParam;
import com.smarteye.adapter.BVCU_DialogTarget;
import com.smarteye.adapter.BVCU_Display_Param;
import com.smarteye.adapter.BVCU_EVENT_DIALOG;
import com.smarteye.adapter.BVCU_Event_DialogCmd;
import com.smarteye.adapter.BVCU_Result;
import com.smarteye.mcp.sdk.smarteye.BVSmarteye;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class PreviewActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "PreviewTAG";
    private SurfaceView surfaceView;
    private TextView videoAudioBtn, videoBtn, audioBtn, rotateBtn, closeBtn, waitingText;
    private String deviceID;
    private int channelIndex;
    private int hDialog;
    private int currentRotate = 0; // 旋转角度
    private int currentAVStreamDir; // 媒体流方向
    private boolean isOpening; // 是否正在等待打开
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        EventBus.getDefault().register(this);
        initView();
        initData();
        initAction();
    }

    private void initView() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("预览");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        surfaceView = findViewById(R.id.activity_preview_surface);
        videoAudioBtn = findViewById(R.id.activity_preview_video_audio_btn);
        videoBtn = findViewById(R.id.activity_preview_video_btn);
        audioBtn = findViewById(R.id.activity_preview_audio_btn);
        rotateBtn = findViewById(R.id.activity_preview_rotate_btn);
        closeBtn = findViewById(R.id.activity_preview_close_btn);
        waitingText = findViewById(R.id.activity_preview_waiting_text);
    }

    private void initAction() {
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (!TextUtils.isEmpty(deviceID)) {
                    currentAVStreamDir = BVCU_MEDIADIR_VIDEORECV | BVCU_MEDIADIR_AUDIORECV;
                    openPreview(0);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
//                Log.d(TAG, "close dialog");
//                BVSmarteye.getBVCUManger(PreviewActivity.this).closeDialog(hDialog);
            }
        });
        videoAudioBtn.setOnClickListener(this);
        videoBtn.setOnClickListener(this);
        audioBtn.setOnClickListener(this);
        rotateBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        deviceID = intent.getStringExtra("deviceID");
        channelIndex = intent.getIntExtra("channelIndex", 0);
        if (TextUtils.isEmpty(deviceID)) {
            Toast.makeText(this, "错误：设备ID为空", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 延迟打开通道
     * @param delayMs 延迟毫秒数
     */
    private void openPreview(int delayMs) {
        isOpening = true;
        updateBtnStatus();
        if (delayMs > 0) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    openPreview();
                }
            }, delayMs);
        } else {
            openPreview();
        }
    }

    /**
     * 打开通道
     */
    private void openPreview() {
        BVCU_DialogInfo dialogInfo = new BVCU_DialogInfo();
        dialogInfo.stParam = new BVCU_DialogParam();
        dialogInfo.stParam.iTargetCount = 1;
        dialogInfo.stParam.pTarget = new BVCU_DialogTarget[1];
        dialogInfo.stParam.pTarget[0] = new BVCU_DialogTarget();
        dialogInfo.stParam.pTarget[0].iIndexMajor = channelIndex;
        dialogInfo.stParam.pTarget[0].iIndexMinor = -1;
        dialogInfo.stParam.pTarget[0].szID = deviceID;

        dialogInfo.stParam.iAVStreamDir = currentAVStreamDir;
        dialogInfo.stControlParam = new BVCU_DialogControlParam();
        dialogInfo.stControlParam.stRender = new BVCU_DialogControlParam_Render();
        dialogInfo.stControlParam.stRender.hWnd = surfaceView.getHolder().getSurface();

        BVCU_Display_Param display_param = new BVCU_Display_Param();
        display_param.fMulZoom = 1;
        display_param.iAngle = 0;
        dialogInfo.stControlParam.stRender.stDisplayParam = display_param;
        waitingText.setVisibility(View.VISIBLE);
        hDialog = BVSmarteye.getBVCUManger(this).invite(dialogInfo);
        Log.d(TAG, "hDialog: " + hDialog);
    }

    /**
     * 关闭通道
     */
    private void closePreview() {
        Log.d(TAG, "close dialog");
        BVSmarteye.getBVCUManger(PreviewActivity.this).closeDialog(hDialog);
        hDialog = 0;
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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
        if (message.getMessageType() == EventBusMessage.EVENTBUS_ON_DIALOG_EVENT) {
            int hDialog = (int) message.getObjectArray()[0];
            int iEventCode = (int) message.getObjectArray()[1];
            Log.d(TAG, "hDialog: " + hDialog + ", iEventCode: " + iEventCode);
            if (hDialog != this.hDialog)
                return;

            waitingText.setVisibility(View.INVISIBLE);

            if (iEventCode == BVCU_EVENT_DIALOG.BVCU_EVENT_DIALOG_OPEN) {
                isOpening = false;
                BVCU_Event_DialogCmd pParam = (BVCU_Event_DialogCmd) message.getObjectArray()[2];
                if (pParam != null && pParam.iResult == BVCU_Result.BVCU_RESULT_S_OK) {
                    Log.d(TAG, "打开成功");
                } else {
                    Log.d(TAG, "打开失败");
                }
            }
        }
    }

    /**
     * 旋转预览界面
     */
    private void rotate() {
        currentRotate -= 90;
        if (currentRotate < 0)
            currentRotate += 360;
        BVCU_DialogControlParam controlParam = new BVCU_DialogControlParam();
        controlParam.stRender = new BVCU_DialogControlParam_Render();
        BVCU_Display_Param displayParam = new BVCU_Display_Param();
        displayParam.fMulZoom = 1;
        displayParam.iAngle = currentRotate;
        controlParam.stRender.stDisplayParam = displayParam;
        controlParam.stRender.hWnd = surfaceView.getHolder().getSurface();
        BVSmarteye.getBVCUManger(this).controlDialog(hDialog, controlParam);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == videoAudioBtn.getId()) {
            if (isOpening || currentAVStreamDir == (BVCU_MEDIADIR_VIDEORECV | BVCU_MEDIADIR_AUDIORECV))
                return;
            currentAVStreamDir = BVCU_MEDIADIR_VIDEORECV | BVCU_MEDIADIR_AUDIORECV;
            closePreview();
            openPreview(500);
        } else if (view.getId() == videoBtn.getId()) {
            if (isOpening || currentAVStreamDir == BVCU_MEDIADIR_VIDEORECV)
                return;
            currentAVStreamDir = BVCU_MEDIADIR_VIDEORECV;
            closePreview();
            openPreview(500);
        } else if (view.getId() == audioBtn.getId()) {
            if (isOpening || currentAVStreamDir == BVCU_MEDIADIR_AUDIORECV)
                return;
            currentAVStreamDir = BVCU_MEDIADIR_AUDIORECV;
            closePreview();
            openPreview(500);
        } else if (view.getId() == rotateBtn.getId()) {
            if (hDialog != 0) {
                rotate();
            }
        } else if (view.getId() == closeBtn.getId()) {
            closePreview();
            finish();
        }
    }

    /**
     * 更新通道按钮显示状态
     */
    private void updateBtnStatus() {
        int selectedColor = getResources().getColor(R.color.selected);
        int defaultColor = getResources().getColor(R.color.default_text_color);
        if (currentAVStreamDir == (BVCU_MEDIADIR_VIDEORECV | BVCU_MEDIADIR_AUDIORECV)) {
            videoAudioBtn.setTextColor(selectedColor);
            videoBtn.setTextColor(defaultColor);
            audioBtn.setTextColor(defaultColor);
        } else if (currentAVStreamDir == BVCU_MEDIADIR_VIDEORECV) {
            videoAudioBtn.setTextColor(defaultColor);
            videoBtn.setTextColor(selectedColor);
            audioBtn.setTextColor(defaultColor);
        } else if (currentAVStreamDir == BVCU_MEDIADIR_AUDIORECV) {
            videoAudioBtn.setTextColor(defaultColor);
            videoBtn.setTextColor(defaultColor);
            audioBtn.setTextColor(selectedColor);
        }
    }
}