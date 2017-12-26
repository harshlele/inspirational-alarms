package com.example.h.alarmclock;

import java.util.Random;

/**
 * POJO for storing Alarm info to disk
 */

public class Alarm {

    private int id;

    private int hour,min;
    public static int REPEAT_NONE = 0;
    public static int REPEAT_DAILY = 1;
    public static int REPEAT_WEEKLY = 2;

    private int repeat;
    private boolean snooze = true;

    private boolean motivation = true;
    private int mot_type;
    public static int MOT_NONE = 3;
    public static int MOT_TEXT = 4;
    public static int MOT_IMG = 5;
    public static int MOT_VID = 6;



    public Alarm() {
        id = new Random().nextInt(9000) + 1000;
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

    public boolean isMotivation() {
        return motivation;
    }

    public void setMotivation(boolean motivation) {
        this.motivation = motivation;
    }

    public int getMot_type() {
        return mot_type;
    }

    public void setMot_type(int mot_type) {
        this.mot_type = mot_type;
    }
}
