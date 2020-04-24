package mikasa.ackerman.audiorecorder.audiorecord;

import mikasa.ackerman.audiorecorder.audiorecord.statemachine.LiteState;
import mikasa.ackerman.audiorecorder.audiorecord.statemachine.LiteStateMachine;

/**
 * cloud-dragon-game-app-android的副本
 *
 * <p>Title: 录音机状态机</p>
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
 * 2020-03-10 15:39
 */
public class RecordStateMachine extends LiteStateMachine implements IRecordAction, IErrorCode{
    private static final int BUFFER_LOOPER_DELAY = 0;
    private final InitState mInitState;
    private final WaitPermissionState mPreRecordState;
    private final RecordingState mRecordingState;

    private final IActionCallback mCallback;

    protected RecordStateMachine(IActionCallback callback) {
        super("Audio-Record-Machine");
        mInitState = new InitState();
        mPreRecordState = new WaitPermissionState();
        mRecordingState = new RecordingState();
        this.mCallback = callback;

        setInitState(mInitState);
    }

    private class InitState extends LiteState{

        @Override
        public String name() {
            return "init";
        }

        @Override
        public boolean doAction(int action, Object data) {
            switch (action){
                case ACTION_BIND_RECORDER:
                    mCallback.onAddCallback((IPCMDataCallback) data);
                    mCallback.onRequestRecordPermission();
                    transferTo(mPreRecordState);
                    break;
                default:
                    break;
            }
            return super.doAction(action, data);
        }
    }

    private class WaitPermissionState extends LiteState{
        @Override
        public String name() {
            return "Prerecord";
        }

        @Override
        public boolean doAction(int action, Object data) {
            switch (action){
                case ACTION_PERMISSION_GRANTED:
                    mCallback.onRecordStart();
                    transferTo(mRecordingState);
                    break;
                case ACTION_PERMISSION_DENIED:
                    mCallback.onRecordFail(ERROR_PERMISSION_DENY);
                    transferTo(mInitState);
                    break;
                default:
                    break;
            }
            return super.doAction(action, data);
        }
    }

    private class RecordingState extends LiteState{
        @Override
        public String name() {
            return "Recording";
        }

        @Override
        public void onEnter() {
            super.onEnter();
            RecordStateMachine.this.doAction(ACTION_READ_BUFFER_LOOP, null);
        }

        @Override
        public boolean doAction(int action, Object data) {
            switch (action){
                case ACTION_READ_BUFFER_LOOP:
                    mCallback.onBufferLoopOnce();
                    RecordStateMachine.this.doAction(ACTION_READ_BUFFER_LOOP, null, BUFFER_LOOPER_DELAY);
                    break;
                case ACTION_UNBIND_RECORDER:
                    int count = mCallback.onRemoveCallback((IPCMDataCallback)data);
                    if (count == 0) {
                        //没有监听者了，停止录音
                        removeAction(ACTION_READ_BUFFER_LOOP);
                        mCallback.onRecordStop();
                        transferTo(mInitState);
                    }
                    break;
                default:
                    break;
            }
            return super.doAction(action, data);
        }
    }

    public interface IActionCallback {
        void onRequestRecordPermission();

        void onRecordFail(int errorCode);

        void onRecordStart();

        int onAddCallback(IPCMDataCallback callback);

        int onRemoveCallback(IPCMDataCallback callback);

        /**
         * 读一次PCM缓存，往外广播一次
         */
        void onBufferLoopOnce();

        void onRecordStop();
    }
}
