package mikasa.ackerman.audiorecorder.audiorecord;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

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
    private static final int AUDIO_SOURCE = AudioSource.MIC;
    /**
     * （MediaRecoder 的采样率通常是8000Hz AAC的通常是44100Hz。 设置采样率为48000，DVD音质）
     */
    public static final int SAMPLE_RATE_IN_HZ = 48000;
    /**
     * 输入声道,STEREO立体声双声道，MONO单声道
     */
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_STEREO;
    /**
     * 指定音频量化位数 ,在AudioFormaat类中指定了以下各种可能的常量。通常我们选择ENCODING_PCM_16BIT和ENCODING_PCM_8BIT PCM代表的是脉冲编码调制，它实际上是原始音频样本。
     * /* 因此可以设置每个样本的分辨率为16位或者8位，16位将占用更多的空间和处理能力,表示的音频也更加接近真实
     */
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    /**
     * 指定缓冲区大小,最小缓冲区的十倍
     */
    public static final int BUFFER_SIZE_IN_BYTES = 10 * AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, CHANNEL,
        AUDIO_FORMAT);

    private final AudioRecord mAudioRecord;

    AudioRecorder() {
        mAudioRecord = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE_IN_HZ, CHANNEL, AUDIO_FORMAT, BUFFER_SIZE_IN_BYTES);
    }

    public void startRecord(){
        sRecording = true;
        mAudioRecord.startRecording();
    }

    /**
     * 声道数
     * @return 将声道的值转换为声道的数量
     */
    public static int getChannelCount(){
        switch (CHANNEL){
            case AudioFormat.CHANNEL_IN_MONO:
                return 1;
            case AudioFormat.CHANNEL_IN_STEREO:
                return 2;
            default:
                break;
        }
        return 2;
    }

    public int readBuffer(byte[] buffer){
        return mAudioRecord.read(buffer, 0, buffer.length);
    }

    public static int getSuggestBufferSize(){
        return BUFFER_SIZE_IN_BYTES;
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
