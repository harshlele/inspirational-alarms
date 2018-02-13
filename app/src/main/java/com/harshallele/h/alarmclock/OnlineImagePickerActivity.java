package com.harshallele.h.alarmclock;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.yarolegovich.discretescrollview.DiscreteScrollView;
import com.yarolegovich.discretescrollview.transform.Pivot;
import com.yarolegovich.discretescrollview.transform.ScaleTransformer;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Submission;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.DefaultPaginator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OnlineImagePickerActivity extends AppCompatActivity {

    DiscreteScrollView scrollView;
    List<String> urlList = new ArrayList<>();
    ImageAdapter adapter;
    Button selectBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_image_picker);

        adapter = new ImageAdapter(new ArrayList<String>());

        scrollView = findViewById(R.id.image_list);
        selectBtn = findViewById(R.id.btn_select);


        scrollView.setAdapter(adapter);
        scrollView.setOffscreenItems(10);
        scrollView.setSlideOnFling(true);

        scrollView.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER) // CENTER is a default one
                .setPivotY(Pivot.Y.BOTTOM) // CENTER is a default one
                .build());

        selectBtn.setText("Loading...");
        new URLLoaderTask().execute();

    }

    private static class URLLoaderTask extends AsyncTask<Void,Void,Void>{

        List<String> images = new ArrayList<>();;

        @Override
        protected Void doInBackground(Void... voids) {

            try {

                RedditClient redditClient = OAuthHelper.automatic(
                        new OkHttpNetworkAdapter(new UserAgent("com.harshallele.h.alarmclock")),
                        Credentials.userlessApp("f-xvVGw6DBvnxg", UUID.randomUUID())
                );

                DefaultPaginator<Submission> getMotivated = redditClient.subreddit("GetMotivated")
                        .posts()
                        .limit(50)
                        .build();

                while (images.size() <= 20) {
                    for (Submission s : getMotivated.next()) {
                        if (!s.isSelfPost() && s.getUrl().contains("i.imgur.com")) {
                            images.add(s.getUrl());
                        }
                    }
                }
            }
            catch (Exception e){
                Log.e("LOG!", "doInBackground: " + e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            EventBus.getDefault().post(new Events.URLLoadedEvent(images));
        }
    }

    @Subscribe
    public void onMessageEvent(Events.URLLoadedEvent event){
        urlList.addAll(event.urls);
        adapter.setUrls(urlList);
        adapter.notifyDataSetChanged();
        selectBtn.setText("Select");
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


    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder>{

        private List<String> urls = new ArrayList<>();

        public ImageAdapter(List<String> urls) {
            this.urls = urls;
        }

        public void setUrls(List<String> urls) {
            this.urls = urls;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            ImageView view = new ImageView(getApplicationContext());
            int w = (int) (Resources.getSystem().getDisplayMetrics().widthPixels * 0.7);
            view.setLayoutParams(new ViewGroup.LayoutParams(w, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.setPadding(16,0,16,0);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Picasso.with(getApplicationContext())
                    .load(urls.get(position))
                    .placeholder(R.drawable.placeholder)
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            private ImageView imageView;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;
            }
        }
    }
}
