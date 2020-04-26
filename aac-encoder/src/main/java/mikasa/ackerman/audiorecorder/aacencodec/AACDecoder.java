package mikasa.ackerman.audiorecorder.aacencodec;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodec.Callback;
import android.media.MediaCodec.CodecException;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
 * 2020-04-24 19:47
 */
public class AACDecoder {
    private static final String AAC_FILE_PATH
        = "/sdcard/Android/data/mikasa.ackerman.audiorecorder.demo/files/aac/1587728777037.aac";
    private static final String TAG = "AACDecoder";

    private final MediaFormat mMediaFormat;
    private final MediaCodec mMediaCodec;

    private final MediaExtractor mExtractor;

    private static final int MSG_END_OUT = 10;
    private static final int MSG_DATA_OUT = 11;

    private final Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_DATA_OUT:
                    byte[] data = (byte[])msg.obj;
                    if (mPCMCallback != null) {
                        mPCMCallback.onCallback(data);
                    }
                    break;
                case MSG_END_OUT:
                    if (mPCMCallback != null) {
                        mPCMCallback.onDataEnd();
                    }
                    break;
                default:
                    break;
            }
        }
    };
    private PCMCallback mPCMCallback;

    public AACDecoder() throws IOException {
        mExtractor = new MediaExtractor();
        mExtractor.setDataSource(AAC_FILE_PATH);
        MediaFormat mediaFormat = null;
        for (int i = 0; i < mExtractor.getTrackCount(); i++) {
            MediaFormat format = mExtractor.getTrackFormat(i);
            String mime = format.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith("audio/")) {
                mExtractor.selectTrack(i);
                mediaFormat = format;
                break;
            }
        }

        if (mediaFormat == null) {
            L.i("extract aac file", "no audio trace ?");
            mExtractor.release();
        }

        mMediaFormat = mediaFormat;

        if (mMediaFormat != null) {
            mMediaCodec = createCodec(mMediaFormat);
        } else {
            mMediaCodec = null;
        }
    }

    public void start() {
        if (mMediaCodec == null) {
            return;
        }
        mMediaCodec.start();
    }

    protected MediaCodec createCodec(MediaFormat format) throws IOException {
        MediaCodec codec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
        codec.configure(format, null, null, 0);
        codec.setCallback(new DecodeCallback());
        return codec;
    }

    public void setPCMCallback(PCMCallback pcmCallback) {
        mPCMCallback = pcmCallback;
    }

    private class DecodeCallback extends Callback {
        @Override
        public void onInputBufferAvailable(MediaCodec codec, int index) {
            //获取输入buffer
            ByteBuffer inputBuffer = codec.getInputBuffer(index);
            if (inputBuffer == null) {
                return;
            }
            //清空buffer，准备写入
            inputBuffer.clear();
            //从文件中抽取数据，写入codec
            int size = mExtractor.readSampleData(inputBuffer, 0);
            if (size < 0) {
                //size小于0表示文件到了结尾，没有数据了，通过flag告知codec
                codec.queueInputBuffer(index, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                //告知数据长度，让codec知道什么时候写满了
                inputBuffer.limit(size);
                //正常写入数据，待硬解
                codec.queueInputBuffer(index, 0, size, mExtractor.getSampleTime(), 0);
                //将抽取器指针挪到下一个采样点
                mExtractor.advance();
            }
        }

        @Override
        public void onOutputBufferAvailable(MediaCodec codec, int index, BufferInfo info) {
            if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                //Codec的控制信息，而非音频数据，忽略
                L.i(TAG, "Codec控制信息，将忽略");
            } else if((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) !=0){
                //输入结束
                L.i(TAG, "解码结束");
                mHandler.sendEmptyMessage(MSG_END_OUT);
            } else {
                if (info.size > 0){
                    ByteBuffer outBuf = codec.getOutputBuffer(index);
                    if (outBuf != null) {
                        outBuf.position(info.offset);
                        outBuf.limit(info.size);
                        byte[] pcmData = new byte[info.size];
                        outBuf.get(pcmData, info.offset, info.size);
                        mHandler.sendMessage(Message.obtain(mHandler, MSG_DATA_OUT, pcmData));
                    }
                }
            }
            codec.releaseOutputBuffer(index, false);
        }

        @Override
        public void onError(MediaCodec codec, CodecException e) {
            e.printStackTrace();
        }

        @Override
        public void onOutputFormatChanged(MediaCodec codec, MediaFormat format) {

        }
    }

    public interface PCMCallback{
        void onCallback(byte[] pcmData);

        void onDataEnd();
    }
}
