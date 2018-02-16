package com.harshallele.h.alarmclock;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
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
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class OnlineImagePickerActivity extends AppCompatActivity {

    DiscreteScrollView scrollView;
    List<String> urlList = new ArrayList<>();
    ImageAdapter adapter;
    Button selectBtn;

    private boolean imgSelected = false;
    private String imgPath = "";

    private static final int PERM_REQ_CODE = 682;

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


        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    saveImg(scrollView.getCurrentItem());

                }
                else{
                    ActivityCompat.requestPermissions(OnlineImagePickerActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERM_REQ_CODE);

                }

            }
        });

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PERM_REQ_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    saveImg(scrollView.getCurrentItem());
                }
        }
    }

    private void saveImg(int pos){
        selectBtn.setText("Saving Image...");
        Picasso.with(getApplicationContext())
                .load(urlList.get(pos))
                .into(getTarget());
    }


    //target to save
    private Target getTarget(){
        Target target = new Target(){

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                final List<String> list = new ArrayList<>();
                final Context c = getApplicationContext();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Random r = new Random();
                        int i1 = r.nextInt(9999 - 1001) + 1001;
                        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + String.valueOf(i1) + ".jpg");
                        try {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                            ostream.flush();
                            ostream.close();
                            imgSelected = true;
                            imgPath = file.getAbsolutePath();
                        } catch (IOException e) {
                            Log.e("IOException ", e.toString());
                            Log.d("LOG!", "run: " + file.getAbsolutePath());
                        }

                        finishActivity();
                    }
                }).start();

            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {}

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {}
        };
        return target;
    }

    private void finishActivity(){

        if(imgSelected){
            Intent returnIntent = new Intent();
            returnIntent.putExtra("result",imgPath);
            setResult(Activity.RESULT_OK,returnIntent);
            finish();
        }
        else{
            Intent returnIntent = new Intent();
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        }
    }


    private static class URLLoaderTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            int urlCount = 0;

            try {

                RedditClient redditClient = OAuthHelper.automatic(
                        new OkHttpNetworkAdapter(new UserAgent("com.harshallele.h.alarmclock")),
                        Credentials.userlessApp("f-xvVGw6DBvnxg", UUID.randomUUID())
                );

                DefaultPaginator<Submission> getMotivated = redditClient.subreddit("GetMotivated")
                        .posts()
                        .limit(50)
                        .build();

                while (urlCount <= 10) {
                    List<String> newUrls = new ArrayList<>();
                    for (Submission s : getMotivated.next()) {
                        if (!s.isSelfPost() && s.getUrl().contains("i.imgur.com")) {
                            newUrls.add(s.getUrl());
                            urlCount++;
                        }
                    }
                    EventBus.getDefault().post(new Events.URLLoadedEvent(newUrls));
                }
            }
            catch (Exception e){
                Log.e("LOG!", "doInBackground: " + e.toString());
            }
            return null;
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.URLLoadedEvent event){
        urlList.addAll(event.urls);
        adapter.addURLs(event.urls);
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

        public void addURLs(List<String> newUrls) {this.urls.addAll(newUrls); }

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

            ImageView imageView;

            public ViewHolder(View itemView) {
                super(itemView);
                imageView = (ImageView) itemView;
            }
        }
    }
}
