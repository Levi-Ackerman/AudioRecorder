package mikasa.ackerman.audiorecorder.aacencodec;

import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;

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
public class AACDecoder extends BaseAACCodec {
    public AACDecoder(int sampleRate, int channelCount, int oneFrameSize, int keyBitRate) {
        super(sampleRate, channelCount, oneFrameSize, keyBitRate);
    }

    @Override
    protected MediaCodec createCodec(MediaFormat format) throws Exception {
        MediaCodec codec = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
        codec.configure(format, null, null, 0);
        return codec;
    }

    @Override
    protected byte[] getOutBytes(BufferInfo bufferInfo, ByteBuffer outputBuffer) {
        int outBitSize = bufferInfo.size;
        outputBuffer.position(bufferInfo.offset);
        outputBuffer.limit(bufferInfo.offset + outBitSize);
        byte[] chunkAudio = new byte[outBitSize];
        outputBuffer.get(chunkAudio, 0, outBitSize);//将编码得到的AAC数据 取出到byte[]中
        return chunkAudio;
    }
}
