package com.tamu.jcabelloc.picturememorygame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public class MainActivity extends AppCompatActivity {

    static final int NUM_PAIR_PICTURES = 15;
    ImageView[] imageViews;
    Bitmap[] imgs;
    boolean isMatching = false;
    int firstSelection;
    int score = 0;
    ImageView firstView;
    boolean tapDisable = false;
    int attempts;
    TextView scoreTextView;
    TextView timerTextView;
    Runnable run;
    Handler handler;
    RelativeLayout relativeLayout;
    GridLayout gridLayout;
    ProgressBar progressBar;

    public void startGame(View view) {
        handler.removeCallbacks(run);
        beginGame();
    }
    public void flipPicture(View view) {
        if (!tapDisable) {
            int selection = Integer.valueOf(view.getTag().toString());
            final ImageView imgView = (ImageView) view;
            imgView.setImageBitmap(imgs[selection]);
            if (!isMatching) {
                firstSelection = selection;
                firstView = (ImageView) view;
                isMatching = true;
            } else {
                if (selection == firstSelection) {
                    score++;
                    if (score == NUM_PAIR_PICTURES) {
                        finalizeGame();
                    }
                } else {
                    tapDisable = true;
                    new CountDownTimer(1500, 1000) {
                        public void onTick(long millisUntilFinished) {
                        }
                        public void onFinish() {
                            firstView.setImageResource(R.drawable.picture);
                            imgView.setImageResource(R.drawable.picture);
                            tapDisable = false;
                        }
                    }.start();
                }
                isMatching = false;
                attempts++;
                scoreTextView.setText("Score " + String.valueOf(score) + " / " + String.valueOf(attempts));
            }
            Log.i("Score", String.valueOf(score));
        }
    }
    public void finalizeGame(){
        handler.removeCallbacks(run);
        tapDisable = true;
        Toast.makeText(this, "Congratulations, Your Final Score is :" + score + " / " + attempts, Toast.LENGTH_LONG).show();
    }

    public void beginGame() {
        gridLayout.setVisibility(GridLayout.INVISIBLE);
        relativeLayout.setVisibility(RelativeLayout.VISIBLE);
        isMatching = false;
        score = 0;
        attempts = 0;
        tapDisable = false;
        scoreTextView.setText("Score " + String.valueOf(score) + " / " + String.valueOf(attempts));
        timerTextView.setText("00:00");
        String strUrl = "https://picsum.photos/200/300/?random";
        imgs = new Bitmap[NUM_PAIR_PICTURES];
        imageViews = new ImageView[NUM_PAIR_PICTURES * 2];

        DownloadImageTask task = new DownloadImageTask();
        try {
            imgs = task.execute(strUrl).get();
        }catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        int[] idImages = new int[NUM_PAIR_PICTURES * 2];
        for (int i = 0; i < NUM_PAIR_PICTURES; i++) {
            idImages[i] = i;
            idImages[i + NUM_PAIR_PICTURES] = i;
        }
        Random rand = new Random();
        for (int i = 0; i < NUM_PAIR_PICTURES * 2 ; i++) {
            int random = rand.nextInt(NUM_PAIR_PICTURES * 2);
            int temp = idImages[i];
            idImages[i] = idImages[random];
            idImages[random] = temp;
        }
        for (int i= 0; i < NUM_PAIR_PICTURES * 2; i++) {
            imageViews[i] = (ImageView) gridLayout.getChildAt(i);
            imageViews[i].setImageResource(R.drawable.picture);
            imageViews[i].setTag(idImages[i]);
        }
        handler = new Handler();
        long startTime = System.currentTimeMillis();
        run = new Runnable() {
            @Override
            public void run() {
                // Insert code to be run every second
                //Log.i("Runnable has run!", "A second must have happened");
                long millis = System.currentTimeMillis() - startTime;
                int seconds = (int) (millis / 1000);
                int minutes = seconds / 60;
                seconds     = seconds % 60;
                timerTextView.setText(String.format("%d:%02d", minutes, seconds));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(run);

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timerTextView = (TextView)findViewById(R.id.timerTextView);
        scoreTextView = (TextView)findViewById(R.id.scoreTextView);
        relativeLayout = (RelativeLayout) findViewById(R.id.loadingRelativeLayout);
        gridLayout = (GridLayout) findViewById(R.id.gridLayout);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setMax(NUM_PAIR_PICTURES);
        beginGame();
    }
    private class DownloadImageTask extends AsyncTask<String, Integer, Bitmap[]> {

        protected Bitmap[] doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                Bitmap[] bitmaps = new Bitmap[NUM_PAIR_PICTURES];
                for (int i = 0; i < NUM_PAIR_PICTURES ; i++) {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmaps[i] = bitmap;
                    /*try {
                        Thread.sleep(500);
                        publishProgress(i);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/
                    publishProgress(i);
                }
                return bitmaps;
            }catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values){
            progressBar.setProgress(values[0]);
            Log.i("Progress", String.valueOf(values[0]));
        }
        @Override
        protected void onPostExecute(Bitmap[] result) {
            Log.i("Post Execute: ", String.valueOf(result));
            gridLayout.setVisibility(GridLayout.VISIBLE);
            relativeLayout.setVisibility(RelativeLayout.INVISIBLE);
        }
    }
}
