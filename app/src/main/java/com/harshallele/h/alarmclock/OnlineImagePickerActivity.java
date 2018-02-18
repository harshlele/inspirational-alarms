package com.harshallele.h.alarmclock;

import android.Manifest;
import android.app.Activity;
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

/*
* Activity that lets the user choose from a list of pictures from reddit
* */

public class OnlineImagePickerActivity extends AppCompatActivity {

    //The discrete scroll view
    DiscreteScrollView scrollView;
    //adapter
    ImageAdapter adapter;
    //button to select images
    Button selectBtn;


    private boolean imgSelected = false;
    private String imgPath = "";

    //request code for the storage permissions needed to save the image
    private static final int PERM_REQ_CODE = 682;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_image_picker);

        adapter = new ImageAdapter(new ArrayList<String>());

        scrollView = findViewById(R.id.image_list);
        selectBtn = findViewById(R.id.btn_select);

        //enable sliding of list. Also, set transitions for current item
        scrollView.setAdapter(adapter);
        scrollView.setOffscreenItems(10);
        scrollView.setSlideOnFling(true);
        scrollView.setItemTransformer(new ScaleTransformer.Builder()
                .setMaxScale(1.05f)
                .setMinScale(0.8f)
                .setPivotX(Pivot.X.CENTER) // CENTER is a default one
                .setPivotY(Pivot.Y.BOTTOM) // CENTER is a default one
                .build());

        //Start AsyncTask to load images from reddit
        selectBtn.setText("Loading...");
        new URLLoaderTask().execute();


        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //If we have storage permissions, save the image. If we don't, ask for the permissions
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

    //if the user has granted permission, save the image
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

    //save the image using picasso
    private void saveImg(int pos){
        selectBtn.setText("Saving Image...");
        Picasso.with(getApplicationContext())
                .load(adapter.urls.get(pos))
                .into(getTarget());
    }


    //Custom target implementation to save the image to the Downloads directory.
    private Target getTarget(){
        Target target = new Target(){

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //generate a random image file name.
                        Random r = new Random();
                        int i1 = r.nextInt(9999 - 1001) + 1001;
                        //save the file in the downloads directory as a JPEG file
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
                        }
                        //finish this activity
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

    //if the image has been selected, put the path in the returning intent
    // (which will captured by onActivityResult of AlarmPickerActivity)
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



    //Asynctask to get image urls from reddit posts
    private static class URLLoaderTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            //total no of urls extracted
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

                //get urls from a page, and send them to be added to the scroller using EventBus
                //do that until at least 10 urls have been extracted
                while (urlCount <= 20) {
                    List<String> newUrls = new ArrayList<>();
                    //getMotivated.next() loads a new page
                    for (Submission s : getMotivated.next()) {
                        if (!s.isSelfPost() && (s.getUrl().contains("i.imgur.com")|s.getUrl().contains("i.redd.it")) ) {
                            newUrls.add(s.getUrl());
                            urlCount++;
                        }
                    }
                    //send the urls using EventBus
                    EventBus.getDefault().post(new Events.URLLoadedEvent(newUrls));
                }
            }
            catch (Exception e){
                Log.e("LOG!", "doInBackground: " + e.toString());
            }
            return null;
        }

    }

    //Fired when new urls are added
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Events.URLLoadedEvent event){
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


    //adapter for scroller
    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder>{
        //list of image urls
        private List<String> urls = new ArrayList<>();

        public ImageAdapter(List<String> urls) {
            this.urls = urls;
        }
        //completely wipe and reload list
        public void setUrls(List<String> urls) {
            this.urls = urls;
        }
        //add more urls to existing list
        public void addURLs(List<String> newUrls) {this.urls.addAll(newUrls); }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            //make an imageview that has a width = 0.7 x screen width
            ImageView view = new ImageView(getApplicationContext());
            int w = (int) (Resources.getSystem().getDisplayMetrics().widthPixels * 0.7);
            view.setLayoutParams(new ViewGroup.LayoutParams(w, ViewGroup.LayoutParams.WRAP_CONTENT));
            view.setPadding(16,0,16,0);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            //Load the image into the imageview
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
