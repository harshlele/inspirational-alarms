package com.example.h.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by h on 1/1/18.
 * Receiver that gets the alarm
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id",0);
        if(id != 0){
            Alarm a = new AlarmStorage(context).getAlarm(String.valueOf(id));
            Log.d("LOG!", "onReceive: " + a.getTimePretty());

            if(a.isRepeat()){
                new SimpleAlarmManager(context)
                        .setup(-1,a.getHour(),a.getMin(),0)
                        .register(id)
                        .start();
            }
            else{
                new AlarmStorage(context).deleteAlarm(id);
            }
        }
    }
}
