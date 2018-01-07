package com.example.h.alarmclock;

/**
 * Created by h on 1/7/18.
 */

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

public class TypeWriterText extends android.support.v7.widget.AppCompatTextView {

    private CharSequence mText;
    private int mIndex;
    private long mDelay = 150; //Default 150ms delay


    public TypeWriterText(Context context) {
        super(context);
    }

    public TypeWriterText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private Handler mHandler = new Handler();
    private Runnable characterAdder = new Runnable() {
        @Override
        public void run() {
            setText(mText.subSequence(0, mIndex++));
            if(mIndex < mText.length()) {
                mHandler.postDelayed(characterAdder, mDelay);
            }

        }
    };

    public void animateText(CharSequence text) {
        mText = text;
        mIndex = 0;

        setText("");
        mHandler.removeCallbacks(characterAdder);
        mHandler.postDelayed(characterAdder, mDelay);
    }

    public void setCharacterDelay(long millis) {
        mDelay = millis;
    }
}
