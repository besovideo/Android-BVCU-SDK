package com.smarteye.sdk.trans.cu.demo;

/**
 * 该类表示需要在初始化时就设置好的一些值,调用者最好是持久化存储
 */
public class Constant {

    // 设备ID(8位16进制字符)
    // 需要进行持久化存储以免设备ID变化
    // TODO 自己实现逻辑来传入一个设备ID,如果两设备相同设备id,会互相影响登录。
    public static String deviceID = "9FFFFFFF";

    // 设备名 TODO 后台显示的设备名称
    public static String deviceName = "SDK_设备名2";

    // 版本号
    public static String versionName = "SDK_Trans_CU_Version202308";

    // 服务器
    public static String ip = "192.168.6.43";

    // 端口
    public static int port = 9702;

}
