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

import java.util.ArrayList;
import java.util.List;


/**
 * Created by h on 12/20/17.
 * Main Activity
 */

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_layout);

        //reduce action bar shadow
        getSupportActionBar().setElevation(2);
        getSupportActionBar().setTitle("Alarms");

        recyclerView = findViewById(R.id.recycler_view);

        // Setup swiping feature and RecyclerView
        RecyclerViewSwipeManager swipeMgr = new RecyclerViewSwipeManager();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(swipeMgr.createWrappedAdapter(new MyAdapter()));

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
    public void startNewAlarmActivity(View v){
        Intent i = new Intent(this,AlarmPickerActivity.class);
        startActivity(i);
    }

    // Event for when an alarm is swiped away.
    public static class DeleteAlarmEvent { /* Additional fields if needed */ }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(DeleteAlarmEvent event) {
        Snackbar.make(recyclerView,"Alarm Deleted", Snackbar.LENGTH_LONG).show();
    }

    //placeholder, will probably be removed
    static class MyItem {
        public final long id;
        public final String text;

        public MyItem(long id, String text) {
            this.id = id;
            this.text = text;
        }
    }

    //swipable recyclerview adapter
    static class MyViewHolder extends AbstractSwipeableItemViewHolder {
        FrameLayout containerView;

        public MyViewHolder(View itemView) {
            super(itemView);
            containerView = itemView.findViewById(R.id.container);
        }

        @Override
        public View getSwipeableContainerView() {
            return containerView;
        }
    }

    static class MyAdapter extends RecyclerView.Adapter<MyViewHolder> implements SwipeableItemAdapter<MyViewHolder> {
        interface Swipeable extends SwipeableItemConstants {
        }

        List<MyItem> mItems;

        public MyAdapter() {
            setHasStableIds(true); // this is required for swiping feature.

            mItems = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                mItems.add(new MyItem(i, "Item " + i));
            }
        }

        @Override
        public long getItemId(int position) {
            return mItems.get(position).id; // need to return stable (= not change even after position changed) value
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_item_view, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
        }

        @Override
        public int getItemCount() {
            return mItems.size();
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
                adapter.mItems.remove(position);
                adapter.notifyItemRemoved(position);
                //send a message using EventBus
                EventBus.getDefault().post(new DeleteAlarmEvent());
            }
        }
    }



}
