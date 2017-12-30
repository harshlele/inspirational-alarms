package com.example.h.alarmclock;

/**
 * Created by h on 12/30/17.
 */

public class Events {

    // Event for when an alarm is swiped away.
    public static class DeleteAlarmEvent {
        Alarm deletedAlarm;

        public DeleteAlarmEvent(Alarm deletedAlarm) {
            this.deletedAlarm = deletedAlarm;
        }
    }

    //For when an alarm is added
    public static class AlarmAddedEvent{}


}
