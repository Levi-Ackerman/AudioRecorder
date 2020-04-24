package mikasa.ackerman.audiorecorder.aacencoder;

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
}
