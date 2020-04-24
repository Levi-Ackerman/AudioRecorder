package mikasa.ackerman.audiorecorder.audiorecord.statemachine;

/**
 * alisdk-review
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
 * <p>Copyright: Copyright (c) 2019</p>
 *
 * @author zhengxian.lzx@alibaba-inc.com
 * @version 1.0
 * 2019-11-06 15:28
 */
public interface IActionReceiver {
    void doAction(int action, Object data);

    void removeAction(int action);

    void doAction(int action, Object data, long delay);
}
