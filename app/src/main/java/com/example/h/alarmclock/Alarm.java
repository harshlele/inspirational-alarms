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

}
