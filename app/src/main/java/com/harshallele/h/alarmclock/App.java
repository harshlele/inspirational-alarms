package com.harshallele.h.alarmclock;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import java.lang.reflect.Method;

/**
 * Created by h on 1/8/18.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //remove the check for content:// URIs in N and above
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                e.printStackTrace();
            }
        }

    }
}
