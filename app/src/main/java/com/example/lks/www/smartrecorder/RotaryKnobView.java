package com.example.lks.www.smartrecorder;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

/**
 * Created by Heesan on 2015-04-27.
 */
public class RotaryKnobView extends ImageView {
    private float angle = 0f;
    private float theta_old = 0f;
    private RotaryKnobListener listener;
    private static final String TAG = " MyActivity";
    private TextView recordTimeView;
    private TextView maxTimeView;
    private Context mContext;
    private byte cycleCnt = 0;
    private float previousAngle = 0f;
    private int maxTime = 3600;
    private Vibrator vibrator;
    private byte vibratorSkip = 0;

    public interface RotaryKnobListener {
        public void onKnobChanged(int arg);
    }

    public void setKnobListener(RotaryKnobListener l,  TextView recordTimeView, TextView maxtimeView)
    {
        listener = l;
        this.recordTimeView = recordTimeView;
        this.maxTimeView = maxtimeView;
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    public RotaryKnobView(Context context) {
        super(context);
        initialize();
    }

    public RotaryKnobView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        mContext = context;
        initialize();
    }

    public RotaryKnobView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
        initialize();
    }

    private float getTheta(float x, float y)
    {
        float sx = x - (getWidth() / 2.0f);
        float sy = y - (getHeight() / 2.0f);

        float length = (float)Math.sqrt( sx*sx + sy*sy);
        float nx = sx / length;
        float ny = sy / length;
        float theta = (float)Math.atan2( ny, nx );

        final float rad2deg = (float)(180.0/Math.PI);
        float thetaDeg = theta*rad2deg;

        return (thetaDeg < 0) ? thetaDeg + 360.0f : thetaDeg;
    }

    public void initialize()
    {
        this.setImageResource(R.drawable.jogview);
        ViewGroup parentView = (ViewGroup)this.getParent();
        setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX(0);
                float y = event.getY(0);
                float theta = getTheta(x,y);

                switch(event.getAction() & MotionEvent.ACTION_MASK)
                {
                    case MotionEvent.ACTION_POINTER_DOWN:
                        //theta_old = theta;
                        theta_old = getTheta(x, y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        invalidate();
                        float delta_theta = theta - theta_old;
                        theta_old = theta;
                        int direction = (delta_theta > 0) ? 1 : -1;
                        angle = (theta_old + 90)%360;

                        if(previousAngle > 340 && previousAngle < 360 && angle > 0 && angle < 20 ) {
                            cycleCnt++;
                            if(cycleCnt > 1)
                                cycleCnt =1;
                        }
                        if(previousAngle >= 0 && previousAngle < 20 && angle <360 && angle > 340 ) {
                            cycleCnt--;
                            if(cycleCnt < - 1)
                                cycleCnt = -1;
                        }
                        //Log.v(TAG, "cycleCnt=" + cycleCnt + " previousAngle=" + previousAngle + " Angle=" + angle);
                        //if(previousAngle - angle >= -6 | previousAngle - angle >= 6 )

                        previousAngle = angle;

                        if (cycleCnt ==1)
                            angle = 360;
                        else if (cycleCnt == -1)
                            angle = 0;
                        else
                            if(vibratorSkip++ % 3 == 0) //need to be optimized
                                vibrator.vibrate(20);

                        float SecondPerAngle = (float) maxTime / 360;
                        float RecordTimeInSecond = SecondPerAngle * angle;
                        Log.v(TAG, "maxTimeInSecond =" + maxTime + " RecordTimeInSecond =" + RecordTimeInSecond + " angle = " + angle);
                        //Log.v(TAG, "timePerAngle =" + SecondPerAngle +" RecordTimeInSecond =" + RecordTimeInSecond);

                        recordTimeView.setText(timeFormat(RecordTimeInSecond));

                        break;

                    case MotionEvent.ACTION_UP:
                        angle = 0;
                        cycleCnt = 0;
                        previousAngle = 0f;
                        recordTimeView.setText("00:00:00");
                        maxTime = (int) getTotalBufferTime(); //Temp_code
                        maxTimeView.setText(timeFormat(maxTime));//temp_location
                        //TODO function : send RecordTimeInSecond
                        getContext().startActivity(new Intent(getContext(), SaveFileActivity.class));
                        invalidate();
                        break;

                    case MotionEvent.ACTION_DOWN:
                        invalidate();
                        break;
                }
                return true;
            }
        });
    }

    //Temp_function
    public int getTotalBufferTime()
    {
        int maxTime = new Random().nextInt(18000);
        //Log.v(TAG, "randomSeed =" + randomSeed + " totalBuffertime[randomSeed] = " + totalBuffertime[randomSeed]);
        return maxTime;
    }

    private CharSequence timeFormat(float timeInSeconds)
    {
        int hours = (int) timeInSeconds/3600;
        int minutes = (int) (timeInSeconds - hours*3600) / 60;
        int seconds = (int) ((timeInSeconds - hours*3600) % 60) / 10 * 10;

        return String.format("%02d", hours) + ":" + String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
    }

    private void notifyListener(int arg)
    {
        if (null!=listener)
            listener.onKnobChanged(arg);
    }

    protected void onDraw(Canvas c)
    {
        c.rotate(angle,getWidth()/2,getHeight()/2);
        super.onDraw(c);
    }
}
