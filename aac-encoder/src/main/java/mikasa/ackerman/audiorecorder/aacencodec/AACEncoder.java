package mikasa.ackerman.audiorecorder.aacencodec;

import java.nio.ByteBuffer;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaFormat;

/**
 * AudioRecorder
 *
 * <p>Title: AAC编码器</p>
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
 * 2020-04-24 19:42
 */
public class AACEncoder extends BaseAACCodec {
    public AACEncoder(int sampleRate, int channelCount, int oneFrameSize, int keyBitRate) {
        super(sampleRate, channelCount, oneFrameSize, keyBitRate);
    }

    @Override
    protected MediaCodec createCodec(MediaFormat format) throws Exception {
        MediaCodec codec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
        codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        return codec;
    }

    @Override
    protected byte[] getOutBytes(BufferInfo bufferInfo, ByteBuffer outputBuffer) {
        int outBitSize = bufferInfo.size;
        int outPacketSize = outBitSize + 7;//7为ADTS头部的大小
        outputBuffer.position(bufferInfo.offset);
        outputBuffer.limit(bufferInfo.offset + outBitSize);
        byte[] chunkAudio = new byte[outPacketSize];
        addADTStoPacket(chunkAudio, outPacketSize);//添加ADTS
        outputBuffer.get(chunkAudio, 7, outBitSize);//将编码得到的AAC数据 取出到byte[]中
        return chunkAudio;
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
}
