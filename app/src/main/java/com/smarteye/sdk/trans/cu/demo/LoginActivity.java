package com.smarteye.sdk.trans.cu.demo;

import static com.smarteye.adapter.BVCU_PU_ONLINE_THROUGH.BVCU_PU_ONLINE_THROUGH_ETHERNET;
import static com.smarteye.adapter.BVCU_PU_ONLINE_THROUGH.BVCU_PU_ONLINE_THROUGH_INVALID;
import static com.smarteye.adapter.BVCU_PU_ONLINE_THROUGH.BVCU_PU_ONLINE_THROUGH_RADIO;
import static com.smarteye.adapter.BVCU_PU_ONLINE_THROUGH.BVCU_PU_ONLINE_THROUGH_WIFI;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.smarteye.adapter.BVCU_PROTOTYPE;
import com.smarteye.adapter.BVCU_ServerParam;
import com.smarteye.adapter.BVPU_MediaDir;
import com.smarteye.adapter.BVPU_OSD_CONFIG;
import com.smarteye.adapter.BVPU_ServerParam;
import com.smarteye.mcp.sdk.auth.IAuthCallback;
import com.smarteye.mcp.sdk.base.BVBase;
import com.smarteye.mcp.sdk.smarteye.BVSmarteye;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = this.getClass().getSimpleName();
    private static final int RC_PERMISSION = 1;
    private boolean hasDenied = false;
    private static Button loginBtn, deviceListBtn;
    private EditText ipEdit, portEdit, userNameEdit, passwordEdit;
    private final static Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 申请必要权限
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_FINE_LOCATION}, RC_PERMISSION);

        {
            // 此代码块必须执行
            System.loadLibrary("bvrtc");
            BVSmarteye.getSmarteyeSDK(this).init();// 必须在BVBase之前调用
            BVSmarteye.getBVCUManger(this).init();
            BVSmarteye.getBVCUManger(this).setBVCUCallback(new SmartEyeEventCallBack(this));
            BVBase.getSDK(this).init();
        }

        ipEdit = findViewById(R.id.ip);
        portEdit = findViewById(R.id.port);
        userNameEdit = findViewById(R.id.username);
        passwordEdit = findViewById(R.id.password);
        loginBtn = findViewById(R.id.login);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**
                 * 登录
                 */
                if (loginBtn.getText().toString().equals("注销")) {
                    BVSmarteye.getBVCUManger(LoginActivity.this).logout();// 注销登录
                    return;
                }

                BVCU_ServerParam bvcuServerParam = new BVCU_ServerParam();
                bvcuServerParam.iCmdProtoType = BVCU_PROTOTYPE.BVCU_PROTOTYPE_UDP;// 信令协议类型
                bvcuServerParam.iServerPort = Integer.parseInt(portEdit.getText().toString());// 端口
                bvcuServerParam.szServerAddr = ipEdit.getText().toString();// ip
                bvcuServerParam.szClientID = "CU_" + Constant.deviceID;// 设备ID
                bvcuServerParam.szUserName = userNameEdit.getText().toString();// 用户名
                bvcuServerParam.szPassword = passwordEdit.getText().toString();// 密码
                Constant.ip = bvcuServerParam.szServerAddr;
                Constant.port = bvcuServerParam.iServerPort;

                BVPU_ServerParam bvpuServerParam = new BVPU_ServerParam();
                bvpuServerParam.iOnlineThrough = getConnectedType(LoginActivity.this);
                bvpuServerParam.iMediaDir = BVPU_MediaDir.BVPU_MEDIADIR_VIDEOSEND | BVPU_MediaDir.BVPU_MEDIADIR_TALKONLY;// 控制媒体方向
                bvpuServerParam.szDeviceName = Constant.deviceName;// 设备名
                bvpuServerParam.szVersionName = Constant.versionName;// 软件版本

                int token = BVSmarteye.getBVCUManger(LoginActivity.this).login(bvcuServerParam, bvpuServerParam);
                Log.i(TAG, "login token = " + token);// 小于0为接口调用失败
                if (token < 0) {
                    Toast.makeText(LoginActivity.this, "登录接口失败 = " + token, Toast.LENGTH_LONG).show();
                }
            }
        });

        // 跳转设备列表
        deviceListBtn = findViewById(R.id.jump_to_device_list);
        deviceListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, DevicesActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (loginBtn.getText().toString().equals("注销")) {
            BVSmarteye.getBVCUManger(this).logout();// 退出登录
        }
        BVBase.getSDK(this).deinit();
        BVSmarteye.getSmarteyeSDK(this).deinit();
        BVSmarteye.getBVCUManger(this).deinit();
    }

    public static void loginResult(boolean b) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (b) {
                    loginBtn.setText("注销");
                } else {
                    loginBtn.setText("登录");
                }
            }
        });
    }

    private static int getConnectedType(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null && mNetworkInfo.isAvailable()) {
                switch (mNetworkInfo.getType()) {
                    case ConnectivityManager.TYPE_WIFI:
                        return BVCU_PU_ONLINE_THROUGH_WIFI;
                    case ConnectivityManager.TYPE_MOBILE:
                        return BVCU_PU_ONLINE_THROUGH_RADIO;
                    case ConnectivityManager.TYPE_ETHERNET:
                        return BVCU_PU_ONLINE_THROUGH_ETHERNET;
                }
            }
        }
        return BVCU_PU_ONLINE_THROUGH_INVALID;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 权限申请结果
        if (requestCode == RC_PERMISSION) {
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        hasDenied = true;
                    }
                }
            }
            if (hasDenied) {
                Toast.makeText(LoginActivity.this, "权限未获取完全，无法使用", Toast.LENGTH_LONG).show();
            }
        }
    }

}