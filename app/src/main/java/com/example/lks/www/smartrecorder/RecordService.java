package com.example.lks.www.smartrecorder;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;

public class RecordService extends Service {
    public RecordService() {
    }

    public final static String TAG = "RecordService";

    MediaPlayer mMediaPlayer;
    String mRecordPath = "";

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer arg0) {
                stopSelf();	//stop this service
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Record service has started successfully");
        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            mRecordPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/SmartRecorder.mp3";
            File mp3file = new File(mRecordPath);
            if(mp3file.exists()){
                new Thread(mRun).start();
            }
        }
        return START_STICKY;
    }

    Runnable mRun = new Runnable() {
        public void run() {
            try{
                mMediaPlayer.setDataSource(mRecordPath);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    /*
    private Runnable recordRunnable = new Runnable() {

        @Override
        public void run() {

            byte[] audiodata = new byte[mBufferSizeInBytes];
            int readsize = 0;

            Log.d(TAG, "start to record");
            // start the audio recording
            try {
                mAudioRecord.startRecording();
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }

            // in the loop to read data from audio and save it to file.
            while (mInRecording == true) {
                readsize = mAudioRecord.read(audiodata, 0, mBufferSizeInBytes);
                if (AudioRecord.ERROR_INVALID_OPERATION != readsize
                        && mFos != null) {
                    try {
                        mFos.write(audiodata);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // stop recording
            try {
                mAudioRecord.stop();
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }

            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mRecordLogTextView.append("\n Audio finishes recording");
                }
            });

            // close the file
            try {
                if (mFos != null)
                    mFos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };
    */

    @Override
    public void onDestroy() {
        Log.i(TAG, "Record service has destroyed");
        if(mMediaPlayer!= null && mMediaPlayer.isPlaying()){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer=null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }
}
