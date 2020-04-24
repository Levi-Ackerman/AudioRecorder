package mikasa.ackerman.audiorecorder.util;
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
 * 2020-04-22 21:59
 */
public class AssertUtil {
    public static void mustOk(boolean condition, String msg) {
        if (BuildConfig.DEBUG && !condition){
            throw new AssertionError(msg);
        }
    }
}
