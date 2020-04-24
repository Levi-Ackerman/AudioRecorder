package mikasa.ackerman.audiorecorder.aacencoder;

import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodec.CodecException;
import android.media.MediaCodecInfo.CodecProfileLevel;
import android.media.MediaFormat;
import android.os.Handler;
import android.os.HandlerThread;
import mikasa.ackerman.audiorecorder.util.L;

/**
 * cloud-dragon-game-app-android的副本
 *
 * <p>Title: PCM转AAC</p>
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
 * 2020-03-25 15:43
 */
public abstract class BaseAACCodec {

    private static final int CODE_SUCCESS = 10;
    private static final int CODE_INPUT_ERROR_CODEC = 11;
    private static final int CODE_INPUT_ERROR_OTHER = 12;
    private static final int CODE_OUTPUT_ERROR_CODEC = 13;
    private static final int CODE_OUTPUT_ERROR_OTHER = 14;
    private static final int CODE_START_ERROR_CODEC = 15;

    private static final String TAG = "BaseAACCodec-%s ";

    private int mChannelCount;
    private int mKeyBitRate;
    private int mSampleRate;
    private int mOneFrameSize;
    private MediaCodec mMediaCodec;

    private static final long DEFAULT_WAIT_INTERVAL = 5000L;

    private Handler mInputHandler, mOutputHandler;
    private boolean mIsRunning;
    private Callback mAacCallback;

    public interface Callback{
        void callback(byte[] data);
    }

    public BaseAACCodec(int sampleRate,int channelCount, int oneFrameSize, int keyBitRate){
        mSampleRate = sampleRate;
        mOneFrameSize = oneFrameSize;
        mKeyBitRate = keyBitRate;
        mChannelCount = channelCount;
    }

    public void start(){
        L.i(TAG, "编码器开始运转");
        if (!initAACMediaEncode()) {
            return;
        }

        HandlerThread inputThread = new HandlerThread("PCM-AAC-Input");
        HandlerThread outputThread = new HandlerThread("PCM-AAC-Output");
        inputThread.start();
        outputThread.start();
        mInputHandler = new Handler(inputThread.getLooper());
        mOutputHandler = new Handler(outputThread.getLooper());

        mIsRunning = true;

        //输出线程启动循环
        mOutputHandler.post(mOutputRunnable);
    }

    /**
     * 只能调用一次destroy，这个标记位限制
     */
    private boolean mDestroyCalled = false;

    /**
     * 退出编码器并释放销毁
     */
    public void stopAndRelease(){
        if (mDestroyCalled){
            return;
        }
        mDestroyCalled = true;

        L.i(TAG, "编码器停止运转");
        if (mIsRunning){
            mInputHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mIsRunning) {
                        mIsRunning = false;
                        if (mOutputHandler != null) {
                            mOutputHandler.removeCallbacks(mOutputRunnable);
                            if (mOutputHandler.getLooper() != null) {
                                mOutputHandler.getLooper().quitSafely();
                            }
                        }

                        if (mInputHandler != null) {
                            if (mInputHandler.getLooper() != null) {
                                mInputHandler.getLooper().quitSafely();
                            }
                        }
                    }
                }
            });
        }
    }

    /**
     * 初始化AAC编码器
     */
    private boolean initAACMediaEncode() {
        try {
            L.d("aac encode" , mKeyBitRate + " " + mChannelCount + " " + mSampleRate);
            MediaFormat encodeFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                mSampleRate, mChannelCount);
            encodeFormat.setInteger(MediaFormat.KEY_BIT_RATE, mKeyBitRate);
            encodeFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, CodecProfileLevel.AACObjectLC);
            encodeFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mOneFrameSize);
            mMediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
            mMediaCodec.configure(encodeFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
        }catch (Exception e){
            L.e(e);
            return false;
        }
        return true;
    }

    /**
     * 编码PCM->aac
     */
    public void input(final byte[] chunkPCM) {
        if (mIsRunning) {
            mInputHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mIsRunning) {
                        try {
                            int inputIndex;
                            ByteBuffer inputBuffer;
                            inputIndex = mMediaCodec.dequeueInputBuffer(-1);
                            if (inputIndex < 0) {
                                return;
                            }
                            inputBuffer = mMediaCodec.getInputBuffer(inputIndex);
                            if (inputBuffer == null) {
                                return;
                            }
                            inputBuffer.clear();
                            inputBuffer.limit(chunkPCM.length);
                            inputBuffer.put(chunkPCM);
                            mMediaCodec.queueInputBuffer(inputIndex, 0, chunkPCM.length, 0, 0);
                        }catch (CodecException e){
                            if (!e.isTransient()){
                                L.e(e);
                                stopAndRelease();
                            }
                        }catch (Exception e){
                            L.e(e);
                            stopAndRelease();
                        }
                    }
                }
            });
        }
    }

    private Runnable mOutputRunnable = new Runnable() {
        @Override
        public void run() {
            startOutput();
        }
    };

    private void startOutput() {
        L.i(TAG, "启动编码器的输出线程");
        int outputIndex;
        int outBitSize;
        int outPacketSize;
        ByteBuffer outputBuffer;
        byte[] chunkAudio;
        BufferInfo bufferInfo = new BufferInfo();

        while(mIsRunning) {
            try{
                final long start = System.currentTimeMillis();
            outputIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, DEFAULT_WAIT_INTERVAL);
            if (outputIndex < 0){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    L.e(e);
                }
            }else {
                outputBuffer = mMediaCodec.getOutputBuffer(outputIndex);//拿到输出Buffer
                outBitSize = bufferInfo.size;
                outPacketSize = outBitSize + 7;//7为ADTS头部的大小
                outputBuffer.position(bufferInfo.offset);
                outputBuffer.limit(bufferInfo.offset + outBitSize);
                chunkAudio = new byte[outPacketSize];
                addADTStoPacket(chunkAudio, outPacketSize);//添加ADTS 代码后面会贴上
                outputBuffer.get(chunkAudio, 7, outBitSize);//将编码得到的AAC数据 取出到byte[]中 偏移量offset=7 你懂得
                //                showLog("outPacketSize:" + outPacketSize + " encodeOutBufferRemain:" + outputBuffer.remaining());
                if (mAacCallback != null) {
                    mAacCallback.callback(chunkAudio);
                }

                mMediaCodec.releaseOutputBuffer(outputIndex, false);
                try {
                    long sleepMillis = 20 - (System.currentTimeMillis() - start);
                    if (sleepMillis > 0) {
                        Thread.sleep(sleepMillis);
                    }
                } catch (InterruptedException e) {
                    L.e(e);
                }
            }}catch (CodecException e){
                L.e(e);

                if (!e.isTransient()){
                    stopAndRelease();
                }
            }catch (Exception e){
                stopAndRelease();
                L.e(e);
            }
        }

        //退出循环时就是停止录音了， 这个时候停掉mediacodec
        try {
            mMediaCodec.stop();
            mMediaCodec.release();
        }catch (Exception e){
            L.e(e);
        }
    }


    /**
     * 添加ADTS头
     * @param packet
     * @param packetLen
     */
    private void addADTStoPacket(byte[] packet, int packetLen) {
        int profile = 2; // AAC LC
        int freqIdx = 3; // 采样率用一个索引号表示，3表示48k，4表示44.1k
        int chanCfg = 2; // channel_configuration，声道数

        //ADTS头是7个字节，也就是56比特（56bit）
        //syncword：0-11个比特（3个16进制数）恒为0xFFF，便于解码器找到开始的位置
        //ID：第12个位，0 for MPEG-4， 1 for MPEG-2
        //Layer：第13，14，固定为00
        //protection_absent：第15，是否有CRC校验， 1没有，0有 （和常规的1有0无是反过来的，如果有CRC校验，头部会加入两个字节校验码，变成9字节的头）
        //profile：第16，17位，Audio Object Types的索引-1，如01 表示 AAC LC(但在索引表里是2)
        //sampleRate：

        // fill in ADTS data
        packet[0] = (byte) 0xFF;
        packet[1] = (byte) 0xF9;
        packet[2] = (byte) (((profile - 1) << 6) + (freqIdx << 2) + (chanCfg >> 2));
        packet[3] = (byte) (((chanCfg & 3) << 6) + (packetLen >> 11));
        packet[4] = (byte) ((packetLen & 0x7FF) >> 3);
        packet[5] = (byte) (((packetLen & 7) << 5) + 0x1F);
        packet[6] = (byte) 0xFC;
    }

    public void setAacCallback(Callback aacCallback) {
        mAacCallback = aacCallback;
    }
}
