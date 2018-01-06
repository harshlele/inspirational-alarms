package com.example.h.alarmclock;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

public class AlarmActivity extends AppCompatActivity {

    private TextView alarmTimeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        final Window win= getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        alarmTimeText = findViewById(R.id.alarm_time);

        Events.AlarmFireEvent event = EventBus.getDefault().getStickyEvent(Events.AlarmFireEvent.class);
        if(event != null){

            Alarm a = event.alarmToFire;
            if(a != null){
                alarmTimeText.setText(a.getTimePretty());
            }

            EventBus.getDefault().removeStickyEvent(event);
        }

    }
}
