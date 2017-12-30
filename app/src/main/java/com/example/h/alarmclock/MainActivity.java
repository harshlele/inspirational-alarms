package com.example.h.alarmclock;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionRemoveItem;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.annotation.SwipeableItemDrawableTypes;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.annotation.SwipeableItemResults;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractSwipeableItemViewHolder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;


/**
 * Created by h on 12/20/17.
 * Main Activity
 */

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AlarmStorage alarmStorage;
    private TextView emptyListText;

    private MyAdapter adapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_layout);

        //reduce action bar shadow
        getSupportActionBar().setElevation(2);
        getSupportActionBar().setTitle("Alarms");

        recyclerView = findViewById(R.id.recycler_view);
        emptyListText = findViewById(R.id.empty_list);
        alarmStorage = new AlarmStorage(getApplicationContext());

        // Setup swiping feature and RecyclerView
        RecyclerViewSwipeManager swipeMgr = new RecyclerViewSwipeManager();

        adapter = new MyAdapter(alarmStorage.getAllAlarms());
        //if there are no alarms, show the no alarms textview
        if(adapter.alarms.size() == 0){
            emptyListText.setVisibility(View.VISIBLE);
        }

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(swipeMgr.createWrappedAdapter(adapter));

        swipeMgr.attachRecyclerView(recyclerView);
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

    //fired when the fab is clicked
    public void startAlarmPickerActivity(View v){
        Intent i = new Intent(this,AlarmPickerActivity.class);
        startActivity(i);
    }


    //fired when an alarm is deleted
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.DeleteAlarmEvent event) {
        alarmStorage.deleteAlarm(event.deletedAlarm.getId());
        Snackbar.make(recyclerView,"Alarm Deleted", Snackbar.LENGTH_LONG).show();
        //if, after deleting this alarm, there are no alarms left, show the no alarms text
        if(adapter.alarms.size() == 0){
            emptyListText.setVisibility(View.VISIBLE);
        }
    }

    //sticky event fired by AlarmPickerActivity when an alarm is added(ie, when the "Save" button is clicked)
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.AlarmAddedEvent event) {
        adapter.setAlarmList(alarmStorage.getAllAlarms());
        adapter.notifyDataSetChanged();
        // make sure the no alarm text is invisible
        if(adapter.alarms.size() > 0){
            emptyListText.setVisibility(View.INVISIBLE);
        }

    }

    //fired when an alarm is clicked. This just fires another sticky event that AlarmPickerActivity listens for
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.AlarmClickEvent event){
        EventBus.getDefault().postSticky(new Events.AlarmEditEvent(event.clickedAlarm));
        Intent i = new Intent(this,AlarmPickerActivity.class);
        startActivity(i);
    }


    //swipable recyclerview adapter
    static class MyViewHolder extends AbstractSwipeableItemViewHolder {
        FrameLayout containerView;
        TextView alarmDigitsText, alarmAMPMText, alarmRepeatText;

        public MyViewHolder(View itemView) {
            super(itemView);
            containerView = itemView.findViewById(R.id.container);
            alarmDigitsText = itemView.findViewById(R.id.alarm_time_digits);
            alarmAMPMText = itemView.findViewById(R.id.alarm_time_ampm);
            alarmRepeatText = itemView.findViewById(R.id.alarm_repeat);
        }

        @Override
        public View getSwipeableContainerView() {
            return containerView;
        }
    }

    static class MyAdapter extends RecyclerView.Adapter<MyViewHolder> implements SwipeableItemAdapter<MyViewHolder> {
        interface Swipeable extends SwipeableItemConstants {
        }

        List<Alarm> alarms;

        public MyAdapter(List<Alarm> list) {
            setHasStableIds(true); // this is required for swiping feature.
            alarms = list;
        }

        //set alarm list after adapter has been initialised
        public void setAlarmList(List<Alarm> list){
            alarms = list;
        }

        @Override
        public long getItemId(int position) {
            return alarms.get(position).getId(); // need to return stable (= not change even after position changed) value
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_item_view, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            Alarm a = alarms.get(position);
            //the alarm position is sent inside an event
            final int p = position;
            //get repeat text
            String repeat = "";
            if(a.getRepeat() == Alarm.REPEAT_NONE){
                repeat = "One Time";
            }
            else if(a.getRepeat() == Alarm.REPEAT_DAILY){
                repeat = "Daily";
            }
            else if(a.getRepeat() == Alarm.REPEAT_WEEKLY){
                repeat = "Weekly";
            }
            //get AM/PM text
            String ampm;
            ampm = "AM";
            if(a.getHour() > 12){
                ampm = "PM";
            }

            holder.alarmDigitsText.setText(a.getTimePretty());
            holder.alarmAMPMText.setText(ampm);
            holder.alarmRepeatText.setText(repeat);

            //click listener for view, just fires an event
            holder.containerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().postSticky(new Events.AlarmClickEvent(alarms.get(p)));
                }
            });
        }

        @Override
        public int getItemCount() {
            return alarms.size();
        }

        @Override
        public void onSwipeItemStarted(MyViewHolder holder, int position) {
            notifyDataSetChanged();
        }

        @Override
        public SwipeResultAction onSwipeItem(MyViewHolder holder, int position, @SwipeableItemResults int result) {
            if (result == Swipeable.RESULT_CANCELED) {
                return new SwipeResultActionDefault();
            } else {
                return new MySwipeResultActionRemoveItem(this, position);
            }
        }

        @Override
        public int onGetSwipeReactionType(MyViewHolder holder, int position, int x, int y) {
            return Swipeable.REACTION_CAN_SWIPE_LEFT;
        }

        @Override
        public void onSetSwipeBackground(MyViewHolder holder, int position, @SwipeableItemDrawableTypes int type) {
        }

        static class MySwipeResultActionRemoveItem extends SwipeResultActionRemoveItem {
            private MyAdapter adapter;
            private int position;

            public MySwipeResultActionRemoveItem(MyAdapter adapter, int position) {
                this.adapter = adapter;
                this.position = position;
            }

            @Override
            protected void onPerformAction() {
                Alarm a = adapter.alarms.get(position);
                adapter.alarms.remove(position);
                adapter.notifyItemRemoved(position);
                //send a message using EventBus
                EventBus.getDefault().post(new Events.DeleteAlarmEvent(a));
            }
        }
    }



}
