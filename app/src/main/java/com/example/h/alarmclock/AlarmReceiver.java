package com.example.h.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by h on 1/1/18.
 * Receiver that gets the alarm
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int id = intent.getIntExtra("id",0);
        if(id != 0){
            AlarmStorage alarmStorage = new AlarmStorage(context);
            Alarm a = alarmStorage.getAlarm(String.valueOf(id));
            Log.d("LOG!", "onReceive: " + a.getTimePretty());

            if(a.isRepeat()){
                new SimpleAlarmManager(context)
                        .setup(-1,a.getHour(),a.getMin(),0)
                        .register(id)
                        .start();
            }
            else{
                alarmStorage.deleteAlarm(id);
            }

            PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ALARM_FIRE");
            wl.acquire(1000);
            EventBus.getDefault().postSticky(new Events.AlarmFireEvent(a));
            Intent i = new Intent(context,AlarmActivity.class);
            context.startActivity(i);
            wl.release();
        }


    }
}
