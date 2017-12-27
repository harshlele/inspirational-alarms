package com.example.h.alarmclock;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class AlarmPickerActivity extends AppCompatActivity {

    //TextViews that show the current set time and ringtone of the alarm
    private TextView currentAlarmTimeText,currentAlarmRingtoneText;

    //boolean vars indicating whether the spinners have loaded or not
    private boolean initialEventMot = false;
    private boolean initialEventRep = false;

    //Spinners to select options for alarm repeat time and motivation types
    private Spinner motivationOptionsSpinner, repeatOptionsSpinner;
    //Textview that shows the current set motivation text, image URI or youtube URL
    private TextView motivationSelectedText;
    //Used while choosing an image from the gallery.
    private static int PICK_IMAGE_REQUEST = 6999;
    //URI of the current set ringtone of alarm
    private Uri currentRingtoneUri;

    //Type of alarm repeat and motivation options.
    private int selectedMotivationType = Alarm.MOT_NONE;
    private int selectedRepeatType = Alarm.REPEAT_NONE;

    //Current set 24-hour hour and min of alarm
    private int currentAlarmHour, currentAlarmMin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_picker);

        //init views
        currentAlarmTimeText = findViewById(R.id.current_time_text);
        currentAlarmRingtoneText = findViewById(R.id.current_ringtone_text);
        motivationOptionsSpinner = findViewById(R.id.motivation_options_spinner);
        repeatOptionsSpinner = findViewById(R.id.repeat_options_spinner);
        motivationSelectedText = findViewById(R.id.selected_motivation_text);


        getSupportActionBar().setTitle("New Alarm");


        //listener for motivation types spinner. When an item is changed, display the motivationSelectedText,
        //and do other things to get the user to select text/image/video that has to be played on dismiss
        motivationOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(!initialEventMot){
                    initialEventMot = true;
                }
                else{
                    switch (String.valueOf(adapterView.getItemAtPosition(i))){
                        case "Show Text":
                            motivationSelectedText.setText("");
                            selectedMotivationType = Alarm.MOT_TEXT;
                            getQuoteText();
                            break;
                        case "Show a Photo":
                            motivationSelectedText.setText("");
                            selectedMotivationType = Alarm.MOT_IMG;
                            getImageURI();
                            break;
                        case "Play a Youtube Video":
                            motivationSelectedText.setText("");
                            selectedMotivationType = Alarm.MOT_VID;
                            getYTVideoURL();
                            break;
                        case "None":
                            selectedMotivationType = Alarm.MOT_NONE;
                            motivationSelectedText.setText("");
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });


        //listener for repeat options spinner
        repeatOptionsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(!initialEventRep){
                    initialEventRep = true;
                }

                else {

                    switch (String.valueOf(adapterView.getItemAtPosition(i))) {
                        case "None":
                            selectedRepeatType =Alarm.REPEAT_NONE;
                            break;
                        case "Daily":
                            selectedRepeatType = Alarm.REPEAT_DAILY;
                            break;
                        case "Weekly":
                            selectedRepeatType = Alarm.REPEAT_WEEKLY;
                            break;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        //Get the default alarm ringtone, and show it in the textview
        getDefaultRingtone();
        String defaultRingtoneTitle = RingtoneManager.getRingtone(getApplicationContext(),currentRingtoneUri).getTitle(getApplicationContext());
        currentAlarmRingtoneText.setText(defaultRingtoneTitle);
    }

    //Shows a dialog with text entry to get text that has to be shown after dismiss
    private void getQuoteText(){
        new LovelyTextInputDialog(this,R.style.TimePickerTheme)
                .setTitle("Show Text")
                .setMessage("Enter the quote or message that you want to see after dismissing the alarm")
                .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                    @Override
                    public void onTextInputConfirmed(String text) {
                        motivationSelectedText.setText(text);
                    }
                })
                .show();
    }

    //Shows a dialog with text entry to get URL of video that has to be played.
    private void getYTVideoURL(){
        new LovelyTextInputDialog(this, R.style.TimePickerTheme)
                .setTitle("Play a Youtube Video")
                .setMessage("Paste the URL of the Youtube Video that you want to see after dismissing the alarm")
                .setConfirmButton(android.R.string.ok, new LovelyTextInputDialog.OnTextInputConfirmListener() {
                    @Override
                    public void onTextInputConfirmed(String text) {
                        motivationSelectedText.setText(text);
                    }
                })
                .show();
    }


    //Starts an intent to go to the image picker to choose the image.
    private void getImageURI(){
        Intent intent = new Intent();
        // Show only images, no videos or anything else
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        // Always show the chooser (if there are multiple options available)
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            motivationSelectedText.setText(uri.toString());
        }

    }

    // get the URI of default alarm ringtone
    private void getDefaultRingtone(){
        currentRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(getApplicationContext(),RingtoneManager.TYPE_ALARM);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    //Shows a time picker for picking alarm time
    public void showTimePicker(View v){
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    //Shows a ringtone with all the alarm ringtones in the system.
    public void showRingtonePicker(View v){

        RingtoneManager manager = new RingtoneManager(this);
        manager.setType(RingtoneManager.TYPE_ALARM);
        Cursor cursor = manager.getCursor();

        //make a hashmap with all titles as keys,and URI as values
        final Map<String, String> list = new HashMap<>();
        while (cursor.moveToNext()) {
            String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
            String uriColumnIndex = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);
            String uriIdColumnIndex = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);

            String notificationUri = Uri.parse(uriColumnIndex + "/" + uriIdColumnIndex).toString();

            list.put(notificationTitle, notificationUri);
        }

        //make an array of all the keys(ie. titles) of hashmap.
        String[] items = list.keySet().toArray(new String[list.keySet().size()]);

        //Show a multi-choice dialog that allows you to choose your ringtone.
        new LovelyChoiceDialog(this,R.style.SwitchTheme)
                .setTitle("Set Ringtone")
                .setItems(items, new LovelyChoiceDialog.OnItemSelectedListener<String>() {
                    @Override
                    public void onItemSelected(int position, String item) {
                        currentAlarmRingtoneText.setText(item);
                        currentRingtoneUri = Uri.parse(list.get(item));
                    }
                }).show();

    }


    //Event generated when user sets the time for alarm
    public void setCurrentAlarmTimeText(String text){
        currentAlarmTimeText.setText(text);
    }

    private static class TimeSetEvent{
        String hour;
        String min;
        String ampm;
        int h,m;

        public TimeSetEvent(int h, int m){

            this.h = h;
            this.m = m;

            //convert 24h to AM/PM.
            if(h <= 12){
                hour = String.valueOf(h);
                ampm = "AM";
            }
            else{
                hour = String.valueOf(h-12);
                ampm = "PM";
            }

            if(m == 0) min = "00";
            else{
                if (m < 10){
                    min = "0" + String.valueOf(m);
                }
                else{
                    min = String.valueOf(m);
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(TimeSetEvent event) {
        currentAlarmHour = event.h;
        currentAlarmMin = event.m;
        setCurrentAlarmTimeText(event.hour + ":" + event.min + event.ampm);
    }

    //Time Picker dialog
    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            TimePickerDialog dialog = new TimePickerDialog(getActivity(),R.style.TimePickerTheme,this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));

            // Create a new instance of TimePickerDialog and return it
            return dialog;
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            EventBus.getDefault().post(new TimeSetEvent(hourOfDay,minute));
        }


    }

}
