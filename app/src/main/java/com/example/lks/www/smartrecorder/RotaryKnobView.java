package com.example.lks.www.smartrecorder;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Heesan on 2015-04-27.
 */
public class RotaryKnobView extends ImageView {
    private float angle = 0f;
    private float theta_old = 0f;
    private RotaryKnobListener listener;
    private static final String TAG = " MyActivity";
    private TextView timerCnt;
    private Context mContext;

    public interface RotaryKnobListener {
        public void onKnobChanged(int arg);
    }

    public void setKnobListener(RotaryKnobListener l,  TextView timerCnt)
    {
        listener = l;
        this.timerCnt = timerCnt;
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
                        Log.d(TAG, "ACTION_POINTER_DOWN");
                        //theta_old = theta;
                        theta_old = getTheta(x, y);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        invalidate();
                        float delta_theta = theta - theta_old;
                        theta_old = theta;
                        int direction = (delta_theta > 0) ? 1 : -1;
                        angle = theta_old + 90;
                        //angle += 3*direction;
                        //notifyListener(direction);
                        timerCnt.setText(String.valueOf(angle));
                        break;
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "ACTION_UP");
                        angle = 0;
                        Log.d(TAG, "x is " + x + " y is " + y + " theta is " + theta);
                        break;
                }
                return true;
            }
        });
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
