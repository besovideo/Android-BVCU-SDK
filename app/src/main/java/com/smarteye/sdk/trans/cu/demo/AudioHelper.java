package com.smarteye.sdk.trans.cu.demo;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.smarteye.mcp.sdk.smarteye.BVSmarteye;

public class AudioHelper {

    private Context mContext;
    private AudioRecord mAudioRecord = null;
    private boolean mRecordThreadExitFlag = false;

    private AudioHelper() {

    }

    public AudioHelper(Context context) {
        this.mContext = context;
    }

    public void start() {
        if (mRecordThreadExitFlag) {
            return;
        }
        /**
         * 打开Audio
         */
        // AudioRecorder
        // 音频 inputAudioData(byte[] data, int size, long stamp) 暂时只支持8000采样率16位单通道pcm
        int channel = AudioFormat.CHANNEL_CONFIGURATION_MONO, sampleRate = 8000, sampleBit = AudioFormat.ENCODING_PCM_16BIT;
        int audioSource = MediaRecorder.AudioSource.DEFAULT;
        int mMinRecordBufSize = AudioRecord.getMinBufferSize(sampleRate, channel, sampleBit);
        // 如果有的设备音频有问题,把mMinRecordBufSize写死640
        mAudioRecord = new AudioRecord(audioSource, sampleRate, channel, sampleBit, mMinRecordBufSize);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mAudioRecord == null)
                        return;
                    mAudioRecord.startRecording();
                    byte[] buf = new byte[mMinRecordBufSize];
                    while (!mRecordThreadExitFlag) {
                        int ret = mAudioRecord.read(buf, 0, buf.length);
//                        Log.i("AudioHelper", "ret = " + ret);
                        // pcm数据, 大小, 时间戳
                        BVSmarteye.getBVCUManger(mContext).inputAudioData(buf, ret, System.currentTimeMillis() * 1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void end() {
        mRecordThreadExitFlag = true;
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
            mAudioRecord = null;
        }
    }

}
