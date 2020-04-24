package mikasa.ackerman.audiorecorder.audiorecord.statemachine;

/**
 * alisdk-review
 *
 * <p>Copyright: Copyright (c) 2019</p>
 *
 * @author zhengxian.lzx@alibaba-inc.com
 * @version 1.0
 * 2019-11-05 11:42
 */
public abstract class LiteState {
    public abstract String name();
    public void onEnter(){
    }

    public boolean doAction(int action, Object data){
        return false;
    }
}
