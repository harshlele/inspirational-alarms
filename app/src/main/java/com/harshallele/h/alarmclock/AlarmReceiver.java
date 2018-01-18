package com.harshallele.h.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;

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
            //get the alarm from storage
            AlarmStorage alarmStorage = new AlarmStorage(context);
            Alarm a = alarmStorage.getAlarm(String.valueOf(id));

            //if it's a repeating alarm, schedule one for tomorrow
            if(a.isRepeat()){
                new SimpleAlarmManager(context)
                        .setup(-1,a.getHour(),a.getMin(),0)
                        .register(id)
                        .start();
            }
            //if it's one time, remove it from storage
            else{
                alarmStorage.deleteAlarm(id);
            }

            //wake up the phone(if it's sleeping)
            PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "ALARM_FIRE");
            wl.acquire(1000);
            //post a sticky event with the alarm for AlarmActivity
            EventBus.getDefault().postSticky(new Events.AlarmFireEvent(a));
            //start activity
            Intent i = new Intent(context,AlarmActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
            wl.release();
        }


    }
}
