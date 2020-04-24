package mikasa.ackerman.audiorecorder.audiorecord.statemachine;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import mikasa.ackerman.audiorecorder.audiorecord.util.AssertUtil;
import mikasa.ackerman.audiorecorder.audiorecord.util.L;

/**
 * alisdk-review
 * 轻量状态机，不支持子状态，解决状态迁移的时候不执行enter的问题
 * <p>Copyright: Copyright (c) 2019</p>
 *
 * @author zhengxian.lzx@alibaba-inc.com
 * @version 1.0
 * 2019-11-05 11:41
 */
public class LiteStateMachine implements IActionReceiver{
    private static final String TAG = "LiteState-%s";
    private final Handler mHandler ;

    private final String mName;
    private LiteState mCurState;

    protected LiteStateMachine(String name){
        this.mName = name;
        HandlerThread handlerThread = new HandlerThread("Record-Machine");
        handlerThread.start();
        this.mHandler = new Handler(handlerThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                L.i(TAG, "doAction , State:" + mCurState.name() + ", action:" + msg.what);
                mCurState.doAction(msg.what, msg.obj);
            }
        };
    }

    protected void setInitState(final LiteState state){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                transferTo(state);
            }
        });
    }

    @Override
    public void doAction(final int action, final Object data){
        doAction(action, data, 0);
    }

    @Override
    public void removeAction(int action){
        mHandler.removeMessages(action);
    }

    @Override
    public void doAction(final int action, final Object data, long delay){
        Message message = Message.obtain(mHandler, action, data);
        mHandler.sendMessageDelayed(message, delay);
    }

    protected Handler getHandler(){
        return mHandler;
    }

    protected void transferTo(final LiteState state){
        AssertUtil.mustOk(Looper.myLooper() == mHandler.getLooper(), "必须在规定的线程调用");
        mCurState = state;
        L.i(TAG, "enter state: "+mCurState.name());
        mCurState.onEnter();
    }
}
