package com.cchien.sieveoferatosthenes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;

public class SplashActivity extends AppCompatActivity {
    // https://www.bignerdranch.com/blog/splash-screens-the-right-way/

    View.OnTouchListener myGestureDetector = null;
    boolean bOnPage = true;
    int splash_seconds = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
            Thread timer = new Thread() {
                public void run() {
                    try {
                        sleep(splash_seconds + 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        GoToMainActivity();
                    }
                }
            };
            timer.start();
    }

    protected void GoToMainActivity() {
        if (bOnPage) {
            bOnPage = false;

            final Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        }
    }

    // This example shows an Activity, but you would use the same approach if
    // you were subclassing a View.
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        int action = MotionEventCompat.getActionMasked(event);

        switch(action) {
            case (MotionEvent.ACTION_DOWN) :
                GoToMainActivity();
                return true;
            case (MotionEvent.ACTION_MOVE) :
                GoToMainActivity();
                return true;
            case (MotionEvent.ACTION_UP) :
                GoToMainActivity();
                return true;
            case (MotionEvent.ACTION_CANCEL) :
                GoToMainActivity();
                return true;
            case (MotionEvent.ACTION_OUTSIDE) :
                GoToMainActivity();
                return true;
            default :
                return super.onTouchEvent(event);
        }
    }
}
