package mikasa.ackerman.audiorecorder.audiorecord;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.os.HandlerThread;

/**
 * cloud-dragon-game-app-android的副本
 *
 * <p>Title: 定制好的录音机，预设好固定的参数</p>
 *
 * <p>Description: </p>
 * <p>
 *
 * <br>
 * 用法:
 * <pre>
 * </pre>
 * </p>
 *
 * <p>Copyright: Copyright (c) 2020</p>
 *
 * @author zhengxian.lzx@alibaba-inc.com
 * @version 1.0
 * 2020-03-05 09:51
 */
public class AudioRecorder {

    /**
     * 同时只能有一个录音机启动，所以这个是静态变量
     */
    private static boolean sRecording = true;

    //音频源 MIC指的是麦克风
    private static final int mAudioSource = AudioSource.MIC;
    /**
     * （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为44100，目前为常用的采样率，官方文档表示这个值可以兼容所有的设置）
     */
    public static final int mSampleRateInHz = 44100;
    /**
     * 输入声道,STEREO立体声双声道，MONO单声道
     */
    public static final int mChannel = AudioFormat.CHANNEL_IN_STEREO;
    /**
     * 指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
     * /* 因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实
     */
    private static final int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * 指定缓冲区大小,最小缓冲区的十倍
     */
    private static final int mBufferSizeInBytes = 10 * AudioRecord.getMinBufferSize(mSampleRateInHz, mChannel, mAudioFormat);

    private final AudioRecord mAudioRecord;

    AudioRecorder() {
        mAudioRecord = new AudioRecord(mAudioSource, mSampleRateInHz, mChannel, mAudioFormat, mBufferSizeInBytes);
    }

    public void startRecord(){
        sRecording = true;
        mAudioRecord.startRecording();
    }

    public int readBuffer(byte[] buffer){
        return mAudioRecord.read(buffer, 0, buffer.length);
    }

    public static int getSuggestBufferSize(){
        return mBufferSizeInBytes;
    }

    public void stopAndRelease(){
        mAudioRecord.stop();
        mAudioRecord.release();
        sRecording = false;
    }

    public static boolean isRecording() {
        return sRecording;
    }
}
