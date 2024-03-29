package com.harshallele.h.alarmclock;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.paperdb.Paper;

/**
 *Created by h on 12/30/17.
 * Class for handling storage of alarms
 */

public class AlarmStorage {

    //Key of the List that stores all ids
    private static final String IDLIST_TAG = "all-ids";


    public AlarmStorage(Context context){
        Paper.init(context);
    }

    //get all ids in a list
    public List<String> getAllIds() {
        List<String> idList;
        idList = Paper.book().read(IDLIST_TAG,new ArrayList<String>());
        return idList;
    }

    //get all alarms
    public List<Alarm> getAllAlarms(){
        List<String> ids = getAllIds();
        List<Alarm> alarmList = new ArrayList<>();
        for (String id : ids) {
            Alarm a = getAlarm(id);
            alarmList.add(a);
        }
        return alarmList;
    }

    //store an alarm
    public void saveAlarm(Alarm a){
        Paper.book().write(String.valueOf(a.getId()),a);

        List<String> idList = getAllIds();

        //check to see if this id already exists so duplicate ids aren't stored
        if(!idList.contains(String.valueOf(a.getId()))) {
            idList.add(String.valueOf(a.getId()));
            Paper.book().write(IDLIST_TAG,idList);
        }

    }

    public Alarm getAlarm(String id){
        Alarm a = Paper.book().read(id,null);
        return a;
    }

    public void deleteAlarm(int id){
        Paper.book().delete(String.valueOf(id));
        List<String> idList = getAllIds();
        idList.remove(String.valueOf(id));
        Paper.book().write(IDLIST_TAG,idList);
    }




}
