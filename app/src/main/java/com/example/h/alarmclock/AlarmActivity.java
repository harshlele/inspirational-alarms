package com.example.h.alarmclock;

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

public class AlarmActivity extends AppCompatActivity {

    private TextView alarmTimeText;
    private TypeWriterText motivationText;

    private Alarm alarm;

    private Button dismissBtn;

    private MediaPlayer player;

    private RelativeLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        final Window win= getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON |
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        alarmTimeText = findViewById(R.id.alarmview_time);
        motivationText = findViewById(R.id.alarmview_motivation_text);
        dismissBtn = findViewById(R.id.btn_dismiss);
        rootLayout = findViewById(R.id.alarmview_root_layout);

        AnimationDrawable animationDrawable = (AnimationDrawable) rootLayout.getBackground();
        animationDrawable.setEnterFadeDuration(2000);
        animationDrawable.setExitFadeDuration(4000);
        animationDrawable.start();


        Events.AlarmFireEvent event = EventBus.getDefault().getStickyEvent(Events.AlarmFireEvent.class);
        if(event != null){

            alarm = event.alarmToFire;
            if(alarm != null){
                String ampm = (alarm.getHour() >= 12)?"PM":"AM";
                alarmTimeText.setText(alarm.getTimePretty() + ampm);

                if(alarm.getMot_type() == Alarm.MOT_TEXT){
                    motivationText.setCharacterDelay(100);
                    motivationText.animateText(alarm.getMotivationData());
                }

                if(alarm.getRingtoneUri() != null){
                    player = MediaPlayer.create(getApplicationContext(),alarm.getRingtoneUri());
                    player.setLooping(true);
                    player.start();
                }

            }

            EventBus.getDefault().removeStickyEvent(event);
        }

        dismissBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                player.stop();
                player.release();
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
                else if(alarm.getMot_type() == Alarm.MOT_VID){
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(alarm.getMotivationData()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setPackage("com.google.android.youtube");
                    startActivity(intent);
                }


                finish();
            }
        });

    }
}
