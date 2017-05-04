package com.cchien.sieveoferatosthenes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.StringRes;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private GestureDetector gestureDetector;

    Button startButton;
    Button resetButton;
    EditText maxNumberText;
    final int numbers_per_page = 1000;

    int max_number = numbers_per_page;
    int MAX_MAX_NUMBER = 20000000; // 20 million
    // On a Samsung S4 it gets OutOfMemoryError at 33,541,489
    // On LG 5X, it's at                           52,649,801

    TextView test_output_text_view;
    private static final String DEBUG_TAG = "SOE - MainActivity";

    final String OUTPUT_FORMATTER = "All prime numbers up to %1$d: %2$s";
    final String PAGE_OUTPUT_FORMATTER = "Prime numbers between %1$d and %2$d: %3$s";
    int heightPixels;
    int widthPixels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Disable button before any text is entered.
        startButton = (Button) findViewById(R.id.start_btn);
        startButton.setEnabled(false);

        resetButton = (Button) findViewById(R.id.reset_btn);
        resetButton.setEnabled(true);

        maxNumberText = (EditText) findViewById(R.id.max_num_edit_text);

        test_output_text_view = (TextView) findViewById(R.id.test_output_testview);

        GetScreenSize();

        // Add a GestureDetector using anonymous class derived from GestureDetector.SimpleOnGestureListener
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            // Override s all the callback methods of GestureDetector.SimpleOnGestureListener
            @Override
            public boolean onSingleTapUp(MotionEvent ev) {
                // DEBUG Log.d(DEBUG_TAG, String.format("Action was onSingleTapUp(%s)", ev.toString()));
                return true;
            }
            @Override
            public void onShowPress(MotionEvent ev) {
                // DEBUG Log.d(DEBUG_TAG, String.format("Action was onShowPress(%s)", ev.toString()));
            }
            @Override
            public void onLongPress(MotionEvent ev) {
                // DEBUG Log.d(DEBUG_TAG, String.format("Action was onLongPress(%s)", ev.toString()));
            }
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                // DEBUG Log.d(DEBUG_TAG, String.format("onScroll(%s, %s, %f, %f)", e1.toString(), e2.toString(), distanceX, distanceY));
                return true;
            }
            @Override
            public boolean onDown(MotionEvent ev) {
                // DEBUG Log.d(DEBUG_TAG, String.format("Action was onDown(%s)", ev.toString()));
                return true;
            }
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // DEBUG Log.d(DEBUG_TAG, String.format("onFling(%s, %s, %f, %f)", e1.toString(), e2.toString(), velocityX, velocityY));
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                    }
                } else {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffY > 0) {
                            onSwipeBottom();
                        } else {
                            onSwipeTop();
                        }
                    }
                }
                return true;
            }
        });

        // Add a listener to maxNumberText EditText to enable or disable startButton
        maxNumberText.addTextChangedListener(new TextWatcher() {

            private String prev_value = "";

            public void afterTextChanged(Editable s) {
                // Deal with maxNumberText here.
                String number_text = maxNumberText.getText().toString();
                if (number_text.isEmpty()) {
                    startButton.setEnabled(false);
                    return;
                }
                max_number = Integer.parseInt(maxNumberText.getText().toString());
                max_number = Integer.parseInt(maxNumberText.getText().toString());
                if (max_number < 2) {
                } else {
                    startButton.setEnabled(true);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                prev_value = s.toString();
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                // DEBUG Log.d(DEBUG_TAG, String.format("onTextChanged() - s: %s, start: %d, count: %d", s, start, count));
                String number_text = s.toString();
                if (!number_text.isEmpty()) {
                    try {
                        int value = Integer.parseInt(number_text);
                    }
                    catch (NumberFormatException e) {
                        PromptMessage(R.string.dialog_title_error, R.string.retry_message);
                        maxNumberText.setText(prev_value);
                    }
                }
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (max_number < 2) {
                    PromptMessage(R.string.dialog_title_error, R.string.retry_message);
                    return;
                }

                startButton.setEnabled(false);
                // CalculatePrimes(false);
                CheckPrimeDivisors();
                startButton.setEnabled(true);
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maxNumberText.setText("");
            }
        });

    }

    private void onSwipeLeft() {
        startActivity(new Intent(this, PrimeFlipActivity.class));
    }

    private void onSwipeRight() {
    }

    private void onSwipeTop() {
        // Head back on Max Number.
        // DEBUG Log.d(DEBUG_TAG, "onSwipeTop()");
        max_number += numbers_per_page;
        CalculatePrimes(true);
    }

    private void onSwipeBottom() {
        // Head back on Max Number.
        // DEBUG Log.d(DEBUG_TAG, "onSwipeBottom()");
        if (max_number >= numbers_per_page) {
            max_number -= numbers_per_page;
            CalculatePrimes(true);
        }
    }

    private void CheckPrimeDivisors() {
        // DEBUG Log.d(DEBUG_TAG, "CheckPrimeDivisors()");
        try {
            SegmentedSieveOfEratosthenes sieve = new SegmentedSieveOfEratosthenes(numbers_per_page);

            Iterator<Integer> prime_divisors = sieve.getPrimeDivisors(max_number);
            StringBuilder sb = new StringBuilder();
            boolean bIsPrime = true;
            sb.append(1);
            while (prime_divisors.hasNext()) {
                int prime_number = prime_divisors.next().intValue();
                sb.append(" x ");
                sb.append(prime_number);
                bIsPrime = prime_number == max_number;
            }
            if (bIsPrime) {
                test_output_text_view.setText(String.format("%d is a prime number.", max_number));
            } else {
                sb.append(" x ");
                sb.append(max_number);
                test_output_text_view.setText(String.format("%d is a composit number: %s", max_number, sb.toString()));
            }
        } catch (Exception ex) {
            // DEBUG Log.d(DEBUG_TAG, String.format("Error: %1", ex.getMessage()));
        }
    }

    private void CalculatePrimes(boolean bUsePage) {
        // DEBUG Log.d(DEBUG_TAG, "CalculatePrimes()");
        try {
            SegmentedSieveOfEratosthenes sieve = new SegmentedSieveOfEratosthenes(numbers_per_page);

            test_output_text_view.setText(String.format("%d is a %s number", max_number, sieve.isPrime(max_number)? "prime":"composite"));
        } catch (Exception ex) {
            // DEBUG Log.d(DEBUG_TAG, String.format("Error: %1", ex.getMessage()));
        }
    }
    private void CalculatePrimesOld(boolean bUsePage) {
        // DEBUG Log.d(DEBUG_TAG, "CalculatePrimes()");
        try {
            // DEBUG Log.d(DEBUG_TAG, "Initializing Array");
            int[] arr = new int[max_number + 1];

            // DEBUG Log.d(DEBUG_TAG, "Looping through sqrt()");
            for (int i = 2; i <= Math.sqrt(max_number); i++) {
                if (arr[i] == 0) {
                    for (int j = i * i; j <= max_number; j += i) {
                        arr[j] = 1;
                    }
                }
            }

            // DEBUG Log.d(DEBUG_TAG, "Collecting primes");
            boolean bFirst = true;
            StringBuilder sb = new StringBuilder();
            if (bUsePage) {
                int start_number = (max_number > numbers_per_page)? max_number - numbers_per_page : 2;
                for (int i = start_number; i < max_number; i++) {
                    if (arr[i] == 0) {
                        if (bFirst) {
                            bFirst = false;
                        } else {
                            sb.append(", ");
                        }
                        sb.append(new Integer(i).toString());
                    }
                }
                // DEBUG Log.d(DEBUG_TAG, String.format(PAGE_OUTPUT_FORMATTER, start_number, max_number, sb.toString()));
                test_output_text_view.setText(String.format(PAGE_OUTPUT_FORMATTER, start_number, max_number, sb.toString()));
            } else {
                for (int i = 2; i < max_number; i++) {
                    if (arr[i] == 0) {
                        if (bFirst) {
                            bFirst = false;
                        } else {
                            sb.append(", ");
                        }
                        sb.append(new Integer(i).toString());
                    }
                }
                // DEBUG Log.d(DEBUG_TAG, String.format(OUTPUT_FORMATTER, max_number, sb.toString()));
                test_output_text_view.setText(String.format(OUTPUT_FORMATTER, max_number, sb.toString()));
            }
        } catch (Exception ex) {
            // DEBUG Log.d(DEBUG_TAG, String.format("Error: %1", ex.getMessage()));
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        boolean eventConsumed = gestureDetector.onTouchEvent(event);
        if (eventConsumed) {
            return true;
        }
        else {
            int action = MotionEventCompat.getActionMasked(event);
            switch (action) {
                case (MotionEvent.ACTION_DOWN):
                    // DEBUG Log.d(DEBUG_TAG, "Action was DOWN");
                    return true;
                case (MotionEvent.ACTION_MOVE):
                    // DEBUG Log.d(DEBUG_TAG, "Action was MOVE");
                    return true;
                case (MotionEvent.ACTION_UP):
                    // DEBUG Log.d(DEBUG_TAG, "Action was UP");
                    return true;
                case (MotionEvent.ACTION_SCROLL):
                    // DEBUG Log.d(DEBUG_TAG, "Action was ACTION_SCROLL");
                    return true;
                case (MotionEvent.ACTION_CANCEL):
                    // DEBUG Log.d(DEBUG_TAG, "Action was CANCEL");
                    return true;
                case (MotionEvent.ACTION_OUTSIDE):
                    // DEBUG Log.d(DEBUG_TAG, "Movement occurred outside bounds of current screen element");
                    return true;
                default:
                    return super.onTouchEvent(event);
            }
        }
    }

    void GetScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        heightPixels = displayMetrics.heightPixels;
        widthPixels = displayMetrics.widthPixels;
        // DEBUG Log.d(DEBUG_TAG, String.format("WidthPixels: %d, heightPixels: %d", this.widthPixels, this.heightPixels));
    }

    void PromptMessage(@StringRes int titleId, @StringRes int messageId) {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(messageId)
                .setTitle(titleId).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // You don't have to do anything here if you just want it dismissed when clicked
            }
        });;

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        // 4. Show the dialog
        dialog.show();
    }
}
