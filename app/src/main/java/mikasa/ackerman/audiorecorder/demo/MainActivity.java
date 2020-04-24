package mikasa.ackerman.audiorecorder.demo;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import mikasa.ackerman.audiorecorder.audiorecord.AuRecordManager;
import mikasa.ackerman.audiorecorder.audiorecord.IPCMDataCallback;
import mikasa.ackerman.audiorecorder.util.L;

public class MainActivity extends AppCompatActivity implements IPCMDataCallback{

    private AuRecordManager mAuRecordManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuRecordManager = AuRecordManager.instance();
        mAuRecordManager.init(this, callback ->
            AndPermission.with(MainActivity.this)
                .runtime()
                .permission(Permission.RECORD_AUDIO)
                .onDenied(data -> callback.onDenied())
                .onGranted(data -> callback.onGranted())
                .start());
    }

    public void onRecordStart(View view) {
        mAuRecordManager.bindRecord(this);
    }

    public void onRecordStop(View view) {
        mAuRecordManager.unBindRecord(this);
    }

    @Override
    public void onRecordReady() {
        L.i("PCMDataCallback：", "onRecordReady");
    }

    @Override
    public void onStartFail(int reason, String msg) {
        L.i("PCMDataCallback：", "onStartFail:"+reason + ", "+msg);
    }

    @Override
    public void onStartSuccess() {
        L.i("PCMDataCallback：", "onStartSuccess");
    }

    @Override
    public void onPCMDataCallback(byte[] pcm) {
        L.i("PCMDataCallback：", "onPcmDataCallback length: "+pcm.length);
    }

    @Override
    public void onRecordStop() {
        L.i("PCMDataCallback：", "onRecordStop");
    }
}
