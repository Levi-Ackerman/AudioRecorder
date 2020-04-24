package mikasa.ackerman.audiorecorder.demo;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import mikasa.ackerman.audiorecorder.aacencoder.AACEncoder;
import mikasa.ackerman.audiorecorder.aacencoder.BaseAACCodec;
import mikasa.ackerman.audiorecorder.audiorecord.AuRecordManager;
import mikasa.ackerman.audiorecorder.audiorecord.AudioRecorder;
import mikasa.ackerman.audiorecorder.audiorecord.IPCMDataCallback;
import mikasa.ackerman.audiorecorder.util.L;

public class MainActivity extends AppCompatActivity implements IPCMDataCallback {

    /**
     * 录音机，负责产出PCM数据
     */
    private AuRecordManager mAuRecordManager;

    /**
     * 转码器，将PCM数据硬编码成AAC数据
     */
    private BaseAACCodec mAACEncoder;

    /**
     * 文件输出，将AAC数据写出到文件保存
     */
    private AacFileWriter mFileWriter;

    private TextView mTvStatus, mTvTime;
    private long mStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTvStatus = findViewById(R.id.tv_status);
        mTvTime = findViewById(R.id.time);

        mFileWriter = new AacFileWriter(getApplicationContext());

        mAACEncoder = new AACEncoder(AudioRecorder.SAMPLE_RATE_IN_HZ, AudioRecorder.getChannelCount(),
            AudioRecorder.BUFFER_SIZE_IN_BYTES, 128000);
        mAACEncoder.setAacCallback(aac -> {
            L.i("onAacCallback:", "aac callback length: " + aac.length);
            mFileWriter.write(aac);
        });

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
        mAACEncoder.start();
    }

    public void onRecordStop(View view) {
        mAACEncoder.stopAndRelease();
        mAuRecordManager.unBindRecord(this);
        mFileWriter.finish();
    }

    @Override
    public void onRecordReady() {
        L.i("PCMDataCallback：", "onRecordReady");
    }

    @Override
    public void onStartFail(int reason, String msg) {
        L.i("PCMDataCallback：", "onStartFail:" + reason + ", " + msg);
        runOnUiThread(() -> mTvStatus.setText("录音未开始，原因：" + msg));
    }

    @Override
    public void onStartSuccess() {
        L.i("PCMDataCallback：", "onStartSuccess");
        runOnUiThread(() -> {
            mTvStatus.setText("录音开始");
            mStartTime = System.currentTimeMillis();
        });
    }

    @Override
    public void onPCMDataCallback(byte[] pcm) {
        L.i("PCMDataCallback：", "onPcmDataCallback length: " + pcm.length);
        mAACEncoder.input(pcm);
        runOnUiThread(()->{
            long seconds = (System.currentTimeMillis() - mStartTime) /1000;
            mTvTime.setText(String.valueOf(seconds));
        });
    }

    @Override
    public void onRecordStop() {
        L.i("PCMDataCallback：", "onRecordStop");
        runOnUiThread(()->{
            mTvStatus.setText("录音已结束，文件保存在：" + mFileWriter.getFileName());
        });
    }
}
