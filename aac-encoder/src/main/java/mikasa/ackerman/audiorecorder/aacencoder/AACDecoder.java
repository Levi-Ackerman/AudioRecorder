package mikasa.ackerman.audiorecorder.aacencoder;

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
}
