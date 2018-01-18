package com.harshallele.h.alarmclock;

import android.net.Uri;

import java.io.Serializable;
import java.util.Random;

/**
 * POJO for storing Alarm info to disk
 */

public class Alarm implements Serializable {
    //id, generated on initialisation
    private int id;
    //hour and minute
    private int hour,min;

    //repeat type
    private boolean repeat = false;


    //motivation type
    private int mot_type;
    public static int MOT_NONE = 3;
    public static int MOT_TEXT = 4;
    public static int MOT_IMG = 5;
    public static int MOT_VID = 6;

    //the motivation data(text/video url/image URI)
    private String motivationData;

    //ringtone URI
    private Uri ringtoneUri;

    public String getMotivationData() {
        return motivationData;
    }

    public Uri getRingtoneUri() {return ringtoneUri;}

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

    public boolean isRepeat() {
        return repeat;
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public int getMot_type() {
        return mot_type;
    }

    public void setMot_type(int mot_type) {
        this.mot_type = mot_type;
    }


    //Gives the time in a 12 hour format, in a string
    public String getTimePretty(){
        String t = "";

        int h2 = hour;
        if(hour > 12){
            h2 = hour - 12;
        }
        if(h2 < 10) t+="0";
        t+=String.valueOf(h2);

        t+=":";

        if(min < 10) t+="0";
        t+=String.valueOf(min);

        return t;
    }

}
