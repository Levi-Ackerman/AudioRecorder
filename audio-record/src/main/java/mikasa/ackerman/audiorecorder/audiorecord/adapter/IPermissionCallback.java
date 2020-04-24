package mikasa.ackerman.audiorecorder.audiorecord.adapter;

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
 * 2020-04-24 16:02
 */
public interface IPermissionCallback {
    void onGranted();
    void onDenied();
}
