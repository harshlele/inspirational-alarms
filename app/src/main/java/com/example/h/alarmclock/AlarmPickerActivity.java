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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.yarolegovich.lovelydialog.LovelyChoiceDialog;
import com.yarolegovich.lovelydialog.LovelyTextInputDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AlarmPickerActivity extends AppCompatActivity {

    //TextViews that show the current set time and ringtone of the alarm
    private TextView currentAlarmTimeText,currentAlarmRingtoneText;

    //boolean vars indicating whether the spinners have loaded or not
    //(onSelectedListener is also fired when the view is loaded)
    private boolean initialEventMot = false;

    //Spinners to select options for alarm repeat time and motivation types
    private Spinner motivationOptionsSpinner;

    //Textview that shows the current set motivation text, image URI or youtube URL
    private TextView motivationSelectedText;

    //Used while choosing an image from the gallery.
    private static int PICK_IMAGE_REQUEST = 6999;

    //URI of the current set ringtone of alarm
    private Uri currentRingtoneUri;

    //Type of motivation options.
    private int selectedMotivationType = Alarm.MOT_NONE;


    //Current set 24-hour hour and min of alarm
    private int currentAlarmHour = 6;
    private int currentAlarmMin  = 0;

    //switch to set snooze/repeat everyday on or off
    private Switch repeatSwitch;

    //indicates whether an alarm is being edited, and the actual alarm object(because alarm ids can't be changed)
    private boolean isEditing = false;
    private Alarm editedAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_picker);

        //init views
        currentAlarmTimeText = findViewById(R.id.current_time_text);
        currentAlarmRingtoneText = findViewById(R.id.current_ringtone_text);
        motivationOptionsSpinner = findViewById(R.id.motivation_options_spinner);
        motivationSelectedText = findViewById(R.id.selected_motivation_text);
        repeatSwitch = findViewById(R.id.repeat_switch);

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

        //Get the default alarm ringtone, and show it in the textview
        getDefaultRingtone();
        String defaultRingtoneTitle = RingtoneManager.getRingtone(getApplicationContext(),currentRingtoneUri).getTitle(getApplicationContext());
        currentAlarmRingtoneText.setText(defaultRingtoneTitle);

        setDefaultAlarmTimeText();

    }

    //close the activity when the back button is pressed so all the view values go back to default positions
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
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

    //set default alarm time to current time
    private void setDefaultAlarmTimeText(){
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        currentAlarmHour = calendar.get(Calendar.HOUR_OF_DAY);
        currentAlarmMin = calendar.get(Calendar.MINUTE);

        String hour = String.valueOf((currentAlarmHour > 12)?(currentAlarmHour - 12):currentAlarmHour);
        String min = (currentAlarmMin == 0)?"00":String.valueOf(currentAlarmMin);
        String ampm = (currentAlarmHour >= 12)?"PM":"AM";

        currentAlarmTimeText.setText(hour + ":" + min + ampm);
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

    //save the alarm and close the activity
    public void saveAlarm(View v){
        Alarm a;
        //if this is an alarm that is being edited, use that exact object(because ids can't be changed)
        if(isEditing) a = editedAlarm;
        else a = new Alarm();

        a.setHour(currentAlarmHour);
        a.setMin(currentAlarmMin);
        a.setRepeat(repeatSwitch.isChecked());
        a.setMot_type(selectedMotivationType);
        a.setMotivationData(motivationSelectedText.getText().toString());
        a.setRingtoneUri(currentRingtoneUri);

        AlarmStorage storage = new AlarmStorage(getApplicationContext());
        storage.saveAlarm(a);
        Toast.makeText(getApplicationContext(),
                    "Alarm set for " + a.getTimePretty(), Toast.LENGTH_SHORT).show();

        if(isEditing) {
             new SimpleAlarmManager(getApplicationContext()).cancel(a.getId());
        }

        new SimpleAlarmManager(getApplicationContext())
                .setup(-1,a.getHour(),a.getMin(),0)
                .register(a.getId())
                .start();

        EventBus.getDefault().postSticky(new Events.AlarmAddedEvent());

        //set isEditing back to false so it isn't considered twice
        isEditing = false;
        editedAlarm = null;

        this.finish();
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
            if(h < 12){
                hour = String.valueOf(h);
                ampm = "AM";
            }
            else if (h == 12){
                hour = String.valueOf(h);
                ampm = "PM";
            }
            else if(h > 12){
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
        Log.d("LOG!", "onMessageEvent: " + currentAlarmHour + ":" + currentAlarmMin);
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


    //Fired when an alarm is to be edited
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.AlarmEditEvent event) {
        //set it to true so it can be used in saveAlarm()
        isEditing = true;
        editedAlarm = event.clickedAlarm;

        getSupportActionBar().setTitle("Edit Alarm");

        //set the time
        String timeText = editedAlarm.getTimePretty();
        if(editedAlarm.getHour() > 12) timeText+="PM";
        else timeText+="AM";
        currentAlarmTimeText.setText(timeText);
        currentAlarmHour = editedAlarm.getHour();
        currentAlarmMin = editedAlarm.getMin();

        //set the repeat switch
        repeatSwitch.setChecked(editedAlarm.isRepeat());

        //set the motivation type
        selectedMotivationType = editedAlarm.getMot_type();
        if(selectedMotivationType == Alarm.MOT_NONE) motivationOptionsSpinner.setSelection(0);
        else if(selectedMotivationType == Alarm.MOT_TEXT) motivationOptionsSpinner.setSelection(1);
        else if(selectedMotivationType == Alarm.MOT_IMG) motivationOptionsSpinner.setSelection(2);
        else if(selectedMotivationType == Alarm.MOT_VID) motivationOptionsSpinner.setSelection(3);
        //set motivation data
        motivationSelectedText.setText(editedAlarm.getMotivationData());
        //remove this sticky event so it isn't fired twice
        EventBus.getDefault().removeStickyEvent(event);
    }


}
