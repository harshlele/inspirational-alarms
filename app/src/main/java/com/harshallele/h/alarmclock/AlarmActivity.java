package com.harshallele.h.alarmclock;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
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
    //mediaplayer for playing sound
    private MediaPlayer player;
    //root layout
    private RelativeLayout rootLayout;

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
        animationDrawable.setEnterFadeDuration(2000);
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

                    player = MediaPlayer.create(getApplicationContext(),alarm.getRingtoneUri());
                    player.setLooping(true);
                    player.start();

                }

            }
            //remove event
            EventBus.getDefault().removeStickyEvent(event);
        }


        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //stop ringtone
                player.stop();
                player.release();

                //show image
                if(alarm.getMot_type() == Alarm.MOT_IMG){
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(alarm.getMotivationData())), "image/*");
                        startActivity(intent);
                    } else {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setDataAndType(Uri.fromFile(new File(alarm.getMotivationData())),"image/*");
                        startActivity(intent);
                    }

                }
                //open yt video
                else if(alarm.getMot_type() == Alarm.MOT_VID){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(alarm.getMotivationData()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage("com.google.android.youtube");
                    startActivity(intent);
                }

                //close activity
                finish();
            }
        });

    }
}