package com.example.h.alarmclock;

import android.net.Uri;

import java.util.Random;

/**
 * POJO for storing Alarm info to disk
 */

public class Alarm {

    private int id;

    private int hour,min;

    private int repeat;
    public static int REPEAT_NONE = 0;
    public static int REPEAT_DAILY = 1;
    public static int REPEAT_WEEKLY = 2;

    private boolean snooze = true;

    private int mot_type;
    public static int MOT_NONE = 3;
    public static int MOT_TEXT = 4;
    public static int MOT_IMG = 5;
    public static int MOT_VID = 6;

    private String motivationData;

    private Uri ringtoneUri;

    public String getMotivationData() {
        return motivationData;
    }

    public Uri getRingtoneUri() {
        return ringtoneUri;
    }

    public void setRingtoneUri(Uri ringtoneUri) {
        this.ringtoneUri = ringtoneUri;
    }

    public void setMotivationData(String motivationData) {
        this.motivationData = motivationData;
    }

    public Alarm() {
        id = new Random().nextInt(90000) + 10000;
    }

    public int getId(){
        return id;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public boolean isSnooze() {
        return snooze;
    }

    public void setSnooze(boolean snooze) {
        this.snooze = snooze;
    }

    public int getMot_type() {
        return mot_type;
    }

    public void setMot_type(int mot_type) {
        this.mot_type = mot_type;
    }
}
