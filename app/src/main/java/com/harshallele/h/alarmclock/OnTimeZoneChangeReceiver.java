package com.harshallele.h.alarmclock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by h on 1/9/18.
 */

public class OnTimeZoneChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // To get set of all registered ids on boot.
        Set<String> set = SimpleAlarmManager.getAllRegistrationIds(context);
        if(set != null) {
            for (Iterator<String> it = set.iterator(); it.hasNext(); ) {
                int id = Integer.parseInt(it.next());
                //to initialize with registreation id
                SimpleAlarmManager.initWithId(context, id).start();
            }
        }
    }
}
