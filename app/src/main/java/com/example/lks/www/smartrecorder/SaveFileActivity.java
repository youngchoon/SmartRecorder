package com.example.lks.www.smartrecorder;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Button;

/**
 * Created by Heesan on 2015-07-29.
 */
public class SaveFileActivity extends Activity implements View.OnClickListener {
    private Button mOK, mCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.save_file);

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
}
