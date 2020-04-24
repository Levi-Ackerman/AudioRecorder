package mikasa.ackerman.audiorecorder.audiorecord;

/**
 * cloud-dragon-game-app-android的副本
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
 * 2020-03-10 15:47
 */
public interface IRecordAction {
    int ACTION_BIND_RECORDER = 1;
    int ACTION_PERMISSION_GRANTED = 2;
    int ACTION_PERMISSION_DENIED = 3;
    int ACTION_UNBIND_RECORDER = 4;

    int ACTION_READ_BUFFER_LOOP = 5;
}
