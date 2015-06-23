package com.example.lks.www.smartrecorder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class RecordFragment extends Fragment implements View.OnClickListener {


    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private static final String AUDIO_RECORDER_FOLDER = "SmartRecorder";

    private MediaRecorder recorder = null;
    private int currentFormat = 0;
    private int output_formats[] = { MediaRecorder.OutputFormat.MPEG_4,
            MediaRecorder.OutputFormat.THREE_GPP };
    private String file_exts[] = { AUDIO_RECORDER_FILE_EXT_MP4,
            AUDIO_RECORDER_FILE_EXT_3GP };

    private RotaryKnobView jogView;
    Intent recordServiceIntent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View v = inflater.inflate((R.layout.record), container, false);
        jogView = (RotaryKnobView)v.findViewById(R.id.jogview);
        jogView.setKnobListener(new RotaryKnobView.RotaryKnobListener()
        {
            public void onKnobChanged(int arg)
            {
                if (arg > 0)
                    ;     // rotate right
                else
                    ;     // rotate left
            }
        });

        /*
        Button service_start = (Button)findViewById(R.id.service_start);

        Button service_end = (Button)findViewById(R.id.service_end);
        service_start.setOnClickListener(this);
        service_end.setOnClickListener(this);
        */
        return v;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setButtonHandlers();
        enableButtons(false);
        super.onActivityCreated(savedInstanceState);
    }


    private void setButtonHandlers() {

        ((Button) getView().findViewById(R.id.btnStart)).setOnClickListener(btnClick);
        ((Button) getView().findViewById(R.id.btnStop)).setOnClickListener(btnClick);
    }

    private void enableButton(int id, boolean isEnable) {
        ((Button) getView().findViewById(id)).setEnabled(isEnable);
    }

    private void enableButtons(boolean isRecording) {
        enableButton(R.id.btnStart, !isRecording);
        enableButton(R.id.btnStop, isRecording);
    }

    private String getFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + file_exts[currentFormat]);
    }

    private void startRecording() {
        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //recorder.setOutputFormat(output_formats[currentFormat]);
        //recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(getFilename());
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        recorder.setOnErrorListener(errorListener);
        recorder.setOnInfoListener(infoListener);

        try {
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if (null != recorder) {
            recorder.stop();
            recorder.reset();
            recorder.release();

            recorder = null;
        }
    }

    private void displayFormatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String formats[] = { "MPEG 4", "3GPP" };

        builder.setTitle(getString(R.string.choose_format_title))
                .setSingleChoiceItems(formats, currentFormat,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                currentFormat = which;


                                dialog.dismiss();
                            }
                        }).show();
    }

    private MediaRecorder.OnErrorListener errorListener = new MediaRecorder.OnErrorListener() {
        @Override
        public void onError(MediaRecorder mr, int what, int extra) {
            Toast.makeText(getActivity(),
                    "Error: " + what + ", " + extra, Toast.LENGTH_SHORT).show();
        }
    };

    private MediaRecorder.OnInfoListener infoListener = new MediaRecorder.OnInfoListener() {
        @Override
        public void onInfo(MediaRecorder mr, int what, int extra) {
            Toast.makeText(getActivity(),
                    "Warning: " + what + ", " + extra, Toast.LENGTH_SHORT)
                    .show();
        }
    };

    private View.OnClickListener btnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnStart: {
                    Toast.makeText(getActivity(), "Start Recording",
                            Toast.LENGTH_SHORT).show();

                    enableButtons(true);
                    startRecording();

                    break;
                }
                case R.id.btnStop: {
                    Toast.makeText(getActivity(), "Stop Recording",
                            Toast.LENGTH_SHORT).show();
                    enableButtons(false);
                    stopRecording();

                    break;
                }
            }
        }
    };


    public void onClick(View v) {
        if(v.getId() == R.id.service_start){
            getActivity().startService(new Intent(getActivity(), RecordService.class));
        }else if(v.getId() == R.id.service_end){
            getActivity().stopService(new Intent(getActivity(), RecordService.class));
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}
