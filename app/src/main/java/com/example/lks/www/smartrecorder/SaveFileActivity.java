package com.example.lks.www.smartrecorder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Heesan on 2015-07-29.
 */
public class SaveFileActivity extends Activity implements View.OnClickListener {
    private Button mOK, mCancel;
    private EditText fileNameView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.save_file);

        //get recordTime value
        Bundle b = getIntent().getExtras();
        int recordTime = b.getInt("recordTime");

        //change file name based on date/time/length
        SimpleDateFormat dateTime = new SimpleDateFormat("yy-MM-dd HH:mm:ss", Locale.KOREA);
        fileNameView = (EditText) findViewById(R.id.edit_filename);
        fileNameView.setText(dateTime.format(Calendar.getInstance() .getTime())+"_"+timeFormat(recordTime));

        //set cursor location at the end of text
        int len = fileNameView.length();
        fileNameView.setSelection(len);

        setContent();
    }

    private void setContent() {
        mOK = (Button) findViewById(R.id.btn_ok);
        mCancel = (Button) findViewById(R.id.btn_cancel);

        mOK.setOnClickListener(this);
        mCancel.setOnClickListener(this);
    }

    public void onClick(View v){
        switch(v.getId()) {
            case R.id.btn_ok:
                this.finish();
                break;
            case R.id.btn_cancel:
                this.finish();
                break;
        }
    }

    private String timeFormat(int timeInSeconds)
    {
        int hours = (int) timeInSeconds/3600;
        int minutes = (int) (timeInSeconds - hours*3600) / 60;
        int seconds = (int) ((timeInSeconds - hours*3600) % 60) / 10 * 10;

        return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
    }

}
