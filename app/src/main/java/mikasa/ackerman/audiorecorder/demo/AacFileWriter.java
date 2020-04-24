package mikasa.ackerman.audiorecorder.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.Context;

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
 * 2020-04-24 17:26
 */
public class AacFileWriter {
    private OutputStream mFileOutputStream;
    private final File mAacFile;

    public AacFileWriter(Context context){
        File aacDir = context.getExternalFilesDir("aac");
        mAacFile = new File(aacDir, System.currentTimeMillis()+".aac");
        try {
            mFileOutputStream = new FileOutputStream(mAacFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getFileName(){
        return mAacFile.getAbsolutePath();
    }

    public void write(byte[] aac){
        if (mFileOutputStream != null) {
            try {
                mFileOutputStream.write(aac);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void finish(){
        if (mFileOutputStream != null) {
            try {
                mFileOutputStream.flush();
                mFileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
