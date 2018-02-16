package com.harshallele.h.alarmclock;

import java.util.List;

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

    //For when an alarm is clicked
    public static class AlarmClickEvent{
        Alarm clickedAlarm;

        public AlarmClickEvent(Alarm clickedAlarm) {
            this.clickedAlarm = clickedAlarm;
        }
    }

    //For when an alarm is to be edited
    public static class AlarmEditEvent{
        Alarm clickedAlarm;

        public AlarmEditEvent(Alarm clickedAlarm) {
            this.clickedAlarm = clickedAlarm;
        }
    }

    //when alarm is to be fired
    public static class AlarmFireEvent{
        Alarm alarmToFire;

        public AlarmFireEvent(Alarm alarmToFire) {
            this.alarmToFire = alarmToFire;
        }
    }

    //for when a url has been retrieved from a post and is to be loaded into the list
    public static class URLLoadedEvent{
        List<String> urls;

        public URLLoadedEvent(List<String> urls) {
            this.urls = urls;
        }
    }


}
