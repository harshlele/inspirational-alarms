package com.example.h.alarmclock;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;
import android.widget.TimePicker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;

public class AlarmPickerActivity extends AppCompatActivity {

    private TextView currentAlarmTimetext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_picker);

        currentAlarmTimetext = findViewById(R.id.current_time_text);

        getSupportActionBar().setTitle("New Alarm");

    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    public void showTimePicker(View v){
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void setCurrentAlarmTimetext(String text){
        currentAlarmTimetext.setText(text);
    }

    private static class TimeSetEvent{
        String hour;
        String min;
        String ampm;

        public TimeSetEvent(int h, int m){
            if(h <= 12){
                hour = String.valueOf(h);
                ampm = "AM";
            }
            else{
                hour = String.valueOf(h-12);
                ampm = "PM";
            }

            if(m == 0) min = "00";
            else{
                if (m < 10){
                    min = "0" + String.valueOf(m);
                }
                else{
                    min = String.valueOf(m);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TimeSetEvent event) {
        setCurrentAlarmTimetext(event.hour + ":" + event.min + event.ampm);
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog dialog = new TimePickerDialog(getActivity(),R.style.TimePickerTheme,this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));

            // Create a new instance of TimePickerDialog and return it
            return dialog;
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            EventBus.getDefault().post(new TimeSetEvent(hourOfDay,minute));
        }


    }

}
