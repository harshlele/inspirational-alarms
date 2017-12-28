package com.example.h.alarmclock;

import android.content.Context;

import com.orhanobut.hawk.Hawk;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by h on 12/28/17.
 * Class for handling storage of alarms
 */

public class AlarmStorage {


    //Key of the List that stores all ids
    private static final String IDLIST_TAG = "all-ids";

    private Context c;

    public AlarmStorage(Context context){
        c = context;
    }

    public List<Integer> getAllIds(){
        List<Integer> idList = new ArrayList<>();
        Hawk.get(IDLIST_TAG, null);
        return idList;
    }

    public List<Alarm> getAllAlarms(){
        List<Integer> ids = getAllIds();
        List<Alarm> alarmList = new ArrayList<>();
        for(int id:ids){
            Hawk.init(c).build();
            Alarm a = Hawk.get(String.valueOf(id),null);
            alarmList.add(a);
        }
        return alarmList;
    }

    public void saveAlarm(Alarm a){
        Hawk.init(c).build();
        Hawk.put(String.valueOf(a.getId()),a);
        List<Integer> idList = getAllIds();

        if(idList !=null){
            idList.add(a.getId());
        }
        else{
            idList = new ArrayList<>();
            idList.add(a.getId());
        }
        Hawk.put(IDLIST_TAG,idList);
    }

    public Alarm getAlarm(int id){
        Alarm a;
        Hawk.init(c).build();
        a = Hawk.get(String.valueOf(id),null);
        return a;
    }

    public void deleteAlarm(int id){

        Hawk.init(c).build();

        Hawk.delete(String.valueOf(id));

        List<Integer> idList = getAllIds();
        idList.remove(id);
        Hawk.put(IDLIST_TAG,idList);
    }


}
