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
 * 2020-03-10 13:31
 */
public interface IPCMDataCallback {
    /**
     * 第1步：ready
     */
    void onRecordReady();

    /**
     * 第2.1步：录音启动失败（权限等原因）
     * @param reason
     * @param msg
     */
    void onStartFail(int reason, String msg);

    /**
     * 第2.2步：录音启动成功
     */
    void onStartSuccess();

    /**
     * 第3步：录音PCM数据回调
     * @param pcm
     */
    void onPCMDataCallback(byte[] pcm);

    /**
     * 第4步：录音停止，不再回传PCM数据
     */
    void onRecordStop();
}
