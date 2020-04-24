package mikasa.ackerman.audiorecorder.aacencodec;

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

    public interface Callback {
        void callback(byte[] data);
    }

    public BaseAACCodec(int sampleRate, int channelCount, int oneFrameSize, int keyBitRate) {
        mSampleRate = sampleRate;
        mOneFrameSize = oneFrameSize;
        mKeyBitRate = keyBitRate;
        mChannelCount = channelCount;
    }

    public void start() {
        L.i(TAG, "编码器开始运转");
        if (!initAACMediaCodec()) {
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
    public void stopAndRelease() {
        if (mDestroyCalled) {
            return;
        }
        mDestroyCalled = true;

        L.i(TAG, "编码器停止运转");
        if (mIsRunning) {
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
    private boolean initAACMediaCodec() {
        try {
            L.d("codec params", mKeyBitRate + " " + mChannelCount + " " + mSampleRate);
            MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                mSampleRate, mChannelCount);
            format.setInteger(MediaFormat.KEY_BIT_RATE, mKeyBitRate);
            format.setInteger(MediaFormat.KEY_AAC_PROFILE, CodecProfileLevel.AACObjectLC);
            format.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, mOneFrameSize);
            mMediaCodec = createCodec(format);
            mMediaCodec.start();
        } catch (Exception e) {
            L.e(e);
            return false;
        }
        return true;
    }

    protected abstract MediaCodec createCodec(MediaFormat format) throws Exception;

    /**
     * 编码PCM->aac
     */
    public void input(final byte[] data, final int offset, final int length) {
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
                            inputBuffer.limit(data.length);
                            inputBuffer.put(data, offset, length);
                            mMediaCodec.queueInputBuffer(inputIndex, 0, data.length, 0, 0);
                        } catch (CodecException e) {
                            if (!e.isTransient()) {
                                L.e(e);
                                stopAndRelease();
                            }
                        } catch (Exception e) {
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
        BufferInfo bufferInfo = new BufferInfo();

        while (mIsRunning) {
            try {
                final long start = System.currentTimeMillis();
                int outputIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, DEFAULT_WAIT_INTERVAL);
                if (outputIndex < 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        L.e(e);
                    }
                } else {
                    ByteBuffer outputBuffer = mMediaCodec.getOutputBuffer(outputIndex);//拿到输出Buffer
                    byte[] chunkAudio = getOutBytes(bufferInfo, outputBuffer);
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
                }
            } catch (CodecException e) {
                L.e(e);

                if (!e.isTransient()) {
                    stopAndRelease();
                }
            } catch (Exception e) {
                stopAndRelease();
                L.e(e);
            }
        }

        try {
            mMediaCodec.stop();
            mMediaCodec.release();
        } catch (Exception e) {
            L.e(e);
        }
    }

    protected abstract byte[] getOutBytes(BufferInfo bufferInfo, ByteBuffer outputBuffer);

    public void setOutputCallback(Callback aacCallback) {
        mAacCallback = aacCallback;
    }
}
