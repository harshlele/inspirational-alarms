package com.example.h.alarmclock;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class AlarmPickerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_picker);

        getSupportActionBar().setTitle("New Alarm");


    }
}
