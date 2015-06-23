package com.example.lks.www.smartrecorder;

import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class RecordService extends Service {
    public RecordService() {
    }

    public final static String TAG = "RecordService";

    private int currentFormat = 0;
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";

    public static int mBufferSizeInBytes = 2097152; //default 2MB
    public static final String AUDIO_RECORDER_FOLDER = "SmartRecorder";
    private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4,
            AUDIO_RECORDER_FILE_EXT_3GP };

    private AudioRecord mAudioRecord;
    private int mRecordFrequency = 22050;
    private int mRecordChannel = AudioFormat.CHANNEL_IN_MONO;
    private int mRecordBits = AudioFormat.ENCODING_PCM_16BIT;

    private boolean mInRecording = false;
    private FileOutputStream mFos = null;
    private Thread mRecordThread;

    MediaPlayer mMediaPlayer;
    String mRecordPath = "";


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "Record service has started successfully");

        // set up the audio source : get the buffer size for audio
        // record.

        int minBufferSizeInBytes = AudioRecord.getMinBufferSize(
                mRecordFrequency, mRecordChannel, mRecordBits);


        if(AudioRecord.ERROR_BAD_VALUE == minBufferSizeInBytes){

            Log.e(TAG, "Configuration Error");
            return 0;
        }

        // calculate the buffer size used in the file operation.
        mBufferSizeInBytes = minBufferSizeInBytes * 4;


        // create AudioRecord object
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                mRecordFrequency, mRecordChannel, mRecordBits, minBufferSizeInBytes);

        // reset the save file setup
        //String rawFilePath = WaveFileWrapper.getRawFilePath(RAW_PCM_FILE_NAME);

        try {
            File file = new File(getFilename());
            if (file.exists()) {
                file.delete();
            }

            mFos = new FileOutputStream(file);

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (mInRecording == false) {

            mRecordThread = new Thread(recordRunnable);
            mRecordThread.setName("Demo.AudioRecord");
            mRecordThread.start();

            Log.i(TAG, "Audio starts recording");

            mInRecording = true;

        }

        // show the log info
        String audioInfo = " Audio Information : \n"
                + " sample rate = " + mRecordFrequency + "\n"
                + " channel = " + mRecordChannel + "\n"
                + " sample byte = " + mRecordBits;
        Log.i(TAG, audioInfo);

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


    private Runnable recordRunnable = new Runnable() {

        @Override
        public void run() {

            byte[] audioData = new byte[mBufferSizeInBytes];
            int readSize = 0;

            Log.d(TAG, "start to record");
            // start the audio recording
            try {
                mAudioRecord.startRecording();
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }

            // in the loop to read data from audio and save it to file.
            while (mInRecording == true) {
                readSize = mAudioRecord.read(audioData, 0, mBufferSizeInBytes);
                if (AudioRecord.ERROR_INVALID_OPERATION != readSize
                        && mFos != null) {
                    try {
                        mFos.write(audioData);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            // stop recording
            try {
                mAudioRecord.stop();
                mInRecording = false;
            } catch (IllegalStateException ex) {
                ex.printStackTrace();
            }

            // close the file
            try {
                if (mFos != null)
                    mFos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    };

    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
    }


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
