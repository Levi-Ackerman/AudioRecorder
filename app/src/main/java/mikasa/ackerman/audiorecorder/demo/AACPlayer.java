package mikasa.ackerman.audiorecorder.demo;

import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import mikasa.ackerman.audiorecorder.aacencodec.AACDecoder;
import mikasa.ackerman.audiorecorder.aacencodec.AACDecoder.PCMCallback;
import mikasa.ackerman.audiorecorder.audiorecord.AudioRecorder;
import mikasa.ackerman.audiorecorder.util.L;

/**
 * AudioRecorder
 *
 * <p>Title: </p>
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
 * 2020-04-26 09:44
 */
public class AACPlayer implements PCMCallback {
    /**
     * 解码器，将AAC数据解码成PCM数据
     */
    private AACDecoder mAACDecoder;

    /**
     * 播放器，将PCM数据播放出来
     */
    private AudioTrack mAudioTrack;

    public AACPlayer() {
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, AudioRecorder.SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_OUT_STEREO,
            AudioRecorder.AUDIO_FORMAT,AudioRecorder.BUFFER_SIZE_IN_BYTES, AudioTrack.MODE_STREAM);
        try {
            mAACDecoder = new AACDecoder();
            mAACDecoder.setPCMCallback(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void playDemo() {
        mAudioTrack.play();
        mAACDecoder.start();
    }

    @Override
    public void onDataEnd() {

    }

    @Override
    public void onCallback(byte[] pcmData) {
        L.i("AACPlayer", "数据解码成功："+pcmData.length);
        mAudioTrack.write(pcmData,0 , pcmData.length);
    }
}
