package mikasa.ackerman.audiorecorder.audiorecord;

import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import mikasa.ackerman.audiorecorder.audiorecord.RecordStateMachine.IActionCallback;
import mikasa.ackerman.audiorecorder.audiorecord.adapter.IPermissionCallback;
import mikasa.ackerman.audiorecorder.audiorecord.adapter.IPermissioner;
import mikasa.ackerman.audiorecorder.audiorecord.util.AssertUtil;

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
 * 2020-03-10 13:29
 */
public class AuRecordManager implements IActionCallback, IRecordAction {
    private final HandlerThread mHandlerThread ;
    private final Handler mHandler;

    private final RecordStateMachine mStateMachine;

    private Set<IPCMDataCallback> mCallbacks = new HashSet<>();
    private Context mContext;
    private IPermissioner mPermissioner;

    private AudioRecorder mAudioRecorder;

    private final byte[] BUFFER = new byte[AudioRecorder.getSuggestBufferSize()];

    private boolean mIsRecording;

    public static AuRecordManager instance() {
        return Holder.INSTANCE;
    }

    private AuRecordManager() {
        mStateMachine = new RecordStateMachine(this);
        mHandlerThread = new HandlerThread("Audio-Record");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public void init(Context context, IPermissioner permissioner){
        this.mContext = context.getApplicationContext();
        mPermissioner = permissioner;
        AssertUtil.mustOk(permissioner != null, "初始化参数不能为空");
    }

    @Override
    public void onRequestRecordPermission() {
        mPermissioner.requestRecordPermission(new IPermissionCallback() {
            @Override
            public void onGranted() {
                mStateMachine.doAction(ACTION_PERMISSION_GRANTED, null);
            }

            @Override
            public void onDenied() {
                mStateMachine.doAction(ACTION_PERMISSION_DENIED, null);
            }
        });
    }

    @Override
    public void onRecordFail(int errorCode) {
        for (IPCMDataCallback ipcmDataCallback : mCallbacks) {
            ipcmDataCallback.onStartFail(errorCode, "");
        }
    }

    @Override
    public void onRecordStart() {
        mAudioRecorder = new AudioRecorder();
        mAudioRecorder.startRecord();
        for (IPCMDataCallback callback : mCallbacks) {
            callback.onStartSuccess();
        }
    }

    @Override
    public void onRecordStop() {
        mAudioRecorder.stopAndRelease();
        mAudioRecorder = null;
    }

    @Override
    public int onAddCallback(IPCMDataCallback callback) {
        mCallbacks.add(callback);
        callback.onRecordReady();
        return mCallbacks.size();
    }

    @Override
    public int onRemoveCallback(IPCMDataCallback callback) {
        mCallbacks.remove(callback);
        callback.onRecordStop();
        return mCallbacks.size();
    }

    private static class Holder {
        private static final AuRecordManager INSTANCE = new AuRecordManager();
    }

    /**
     * 绑定录音机，录音机会从第一个绑定开始运转，后续的绑定者绑定后直接监听PCM数据
     * @param callback
     */
    public void bindRecord(IPCMDataCallback callback){
        if (callback == null){
            return ;
        }
        this.mStateMachine.doAction(ACTION_BIND_RECORDER, callback);
    }

    /**
     * 解绑录音机，将callback移除，不再回调PCM数据，在最后一个callback移除后，录音停止运转
     * @param callback
     */
    public void unBindRecord(IPCMDataCallback callback){
        if (callback == null){
            return ;
        }
        this.mStateMachine.doAction(ACTION_UNBIND_RECORDER, callback);
    }

    @Override
    public void onBufferLoopOnce() {
        int length = mAudioRecorder.readBuffer(BUFFER);
        if (length > 0) {
            for (IPCMDataCallback callback : mCallbacks) {
                final byte[] data = new byte[length];
                System.arraycopy(BUFFER, 0, data, 0, length);
                callback.onPCMDataCallback(data);
            }
        }
    }
}
