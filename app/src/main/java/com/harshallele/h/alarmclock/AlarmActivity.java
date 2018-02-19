package com.harshallele.h.alarmclock;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/*
*Actual alarm activity
*/

public class AlarmActivity extends AppCompatActivity {

    //alarm time text
    private TextView alarmTimeText;
    //custom class extended from TextView that has a typewriter animation
    private TypeWriterText motivationText;
    //alarm to fire
    private Alarm alarm;
    //button to dismiss
    private Button dismissBtn;
    //root layout
    private RelativeLayout rootLayout;
    //ringtone to play
    private Ringtone r;
    //indicates whether ringtone is to be kept on
    private boolean keepPlayingRing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        //unlock and keep the screen on and bright
        final Window win= getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        alarmTimeText = findViewById(R.id.alarmview_time);
        motivationText = findViewById(R.id.alarmview_motivation_text);
        dismissBtn = findViewById(R.id.btn_dismiss);
        rootLayout = findViewById(R.id.alarmview_root_layout);

        //start background animation
        AnimationDrawable animationDrawable = (AnimationDrawable) rootLayout.getBackground();
        animationDrawable.setEnterFadeDuration(500);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();


        Events.AlarmFireEvent event = EventBus.getDefault().getStickyEvent(Events.AlarmFireEvent.class);
        if(event != null){
            //get alarm object from event
            alarm = event.alarmToFire;
            if(alarm != null){

                //set alarm time
                String ampm = (alarm.getHour() >= 12)?"PM":"AM";
                alarmTimeText.setText(alarm.getTimePretty() + ampm);
                //set the motivating quote
                if(alarm.getMot_type() == Alarm.MOT_TEXT){
                    motivationText.setCharacterDelay(100);
                    motivationText.animateText(alarm.getMotivationData());
                }

                //start alarm ringtone
                if(alarm.getRingtoneUri() != null){

                    //get alarm ringtone
                    r = RingtoneManager.getRingtone(getApplicationContext(),alarm.getRingtoneUri());

                    //set it so that the sound is played at alarm volume
                    AudioAttributes aa = new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build();
                    r.setAudioAttributes(aa);


                    //start the ringtone
                    r.play();

                    //on some phones, ringtone is played only once.
                    //So make a timer that plays it again if it has stopped.
                    Timer mTimer = new Timer();
                    mTimer.scheduleAtFixedRate(new TimerTask() {
                        public void run() {
                            if(keepPlayingRing){
                                if(!r.isPlaying()){
                                    r.play();
                                }
                            }
                            else{
                                r.stop();
                                cancel();
                            }
                        }
                    }, 2000, 1000);


                }

            }
            //remove event
            EventBus.getDefault().removeStickyEvent(event);
        }


        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //stop ringtone
                keepPlayingRing = false;


                //show image
                if(alarm.getMot_type() == Alarm.MOT_IMG){

                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(new File(alarm.getMotivationData())), "image/*");

                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    startActivity(intent);


                }
                //open yt video
                else if(alarm.getMot_type() == Alarm.MOT_VID){
                    Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(alarm.getMotivationData()));
                    try {
                        startActivity(appIntent);
                    } catch (ActivityNotFoundException ex) {
                        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(alarm.getMotivationData()));
                        startActivity(webIntent);
                    }
                }

                //close activity
                finish();
            }
        });

    }
}
