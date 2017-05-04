package com.cchien.sieveoferatosthenes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;

public class PrimeFlipActivity extends AppCompatActivity {
    private GestureDetector gestureDetector;

    Button startButton;
    EditText maxNumberText;
    int numbers_per_page = 500; // 300
    NumberCell[] primes = new NumberCell[numbers_per_page];

    int max_number = numbers_per_page;

    private static final String DEBUG_TAG = "SOE - PrimeFlip";

    final String OUTPUT_FORMATTER = "All prime numbers up to %1$d: %2$s";
    final String PAGE_OUTPUT_FORMATTER = "Prime numbers between %1$d and %2$d: %3$s";
    int heightPixels;
    int widthPixels;

    GridView gridView;
    GridAdapter gridAdapter;

    boolean bProcessing = false;

    Toast currentToast;
    TextView currentText;

    final String FIRST_RANGE_FORMATTER = "Showing numbers between %d and %d... \r\nFlip up or down to show different range of numbers.";
    final String RANGE_FORMATTER = "Showing numbers between %d and %d...";
    boolean bFirstRangeDisplay = true;

    enum TextViewColorBackground {
        PRIME, COMPOSITE, COMPOSITE_HIGHLITE
    }

    public enum SwipeDirection {
        LEFT, RIGHT, UP, DOWN
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prime_flip);

        GetScreenSize();

        CalculatePrimes();

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
                if (currentToast != null) {
                    currentToast.cancel();
                }

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

        gridAdapter = new GridAdapter(this);
        gridView = (GridView) findViewById(R.id.prim_gridview);
        gridView.setAdapter(gridAdapter);
        gridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // DEBUG Log.d(DEBUG_TAG, "onTouchEvent()");
                gestureDetector.onTouchEvent(event);
                return false;
            }
        });
        gridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        RefreshGridView(SwipeDirection.UP);
    }

    private void onSwipeLeft() {
    }

    private void onSwipeRight() {
        startActivity(new Intent(this, MainActivity.class));
    }

    private void adjustGridViewNumColumns() {
        if (max_number < 10000) {
            gridView.setNumColumns(10); // 4 * 10 + 9 = 49
        } else if (max_number < 100000) {
            gridView.setNumColumns(8);  // 5 * 8 + 7 = 47
        } else if (max_number < 1000000) {
            gridView.setNumColumns(6); // 6 * 6 + 5 = 41
        } else if (max_number < 10000000) {
            gridView.setNumColumns(5); // 7 * 5 + 4 = 39
        } else if (max_number < 100000000) {
            gridView.setNumColumns(4); // 8 * 4 + 3 = 35
        } else {
            gridView.setNumColumns(3); // 9 * 3 + 2 = 29
        }
    }

    private void onSwipeTop() {
        if (bProcessing) {
            return;
        }
        clearView();
        if (max_number == SegmentedSieveOfEratosthenes.max_num_limit) {
            currentToast = Toast.makeText(PrimeFlipActivity.this, String.format("Maximum supported number %d is reached.", SegmentedSieveOfEratosthenes.max_num_limit), Toast.LENGTH_LONG);
            currentToast.show();
        }

        bProcessing = true;

        // Head back on Max Number.
        // DEBUG Log.d(DEBUG_TAG, "onSwipeTop()");
        if (SegmentedSieveOfEratosthenes.max_num_limit - max_number >= numbers_per_page)
            max_number += numbers_per_page;
        else
            max_number = SegmentedSieveOfEratosthenes.max_num_limit;

        CalculatePrimes();
        RefreshGridView(SwipeDirection.UP);

        bProcessing = false;
    }

    private void onSwipeBottom() {
        if (bProcessing) {
            // DEBUG Log.d(DEBUG_TAG, String.format("onSwipeBottom() - bProcessing %s", bProcessing? "true":"false"));
            return;
        }
        clearView();

        bProcessing = true;
        // DEBUG Log.d(DEBUG_TAG, "onSwipeBottom()");

        // Head back on Max Number.
        // DEBUG Log.d(DEBUG_TAG, "onSwipeBottom()");
        if (max_number > numbers_per_page) {
            max_number -= numbers_per_page;
            CalculatePrimes();
            RefreshGridView(SwipeDirection.DOWN);
        }

        bProcessing = false;
    }

    private void RefreshGridView(SwipeDirection direction) {
        displayRange();
        adjustGridViewNumColumns();
        gridAdapter.notifyDataSetChanged();
        gridView.invalidateViews();
//        gridView.smoothScrollToPosition((direction == SwipeDirection.DOWN)? numbers_per_page : 1);
    }

    void GetScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        heightPixels = displayMetrics.heightPixels;
        widthPixels = displayMetrics.widthPixels;
        // DEBUG Log.d(DEBUG_TAG, String.format("WidthPixels: %d, heightPixels: %d", this.widthPixels, this.heightPixels));
    }

    private void CalculatePrimes() {
        // DEBUG Log.d(DEBUG_TAG, "CalculatePrimes()");
        try {
            SegmentedSieveOfEratosthenes sieve = new SegmentedSieveOfEratosthenes(max_number);
            int start_number = (max_number > numbers_per_page)? max_number - numbers_per_page + 1 : 2;
            int index_offset = (max_number > numbers_per_page)? max_number - numbers_per_page + 1: 1;
            if (start_number == 2) {
                primes[0] = new NumberCell(1, false);
            }
            for (int i = start_number; i <= max_number; i++) {
                primes[i - index_offset] = new NumberCell(i, sieve.isPrime(i));
            }
        } catch (Exception ex) {
            // DEBUG Log.d(DEBUG_TAG, String.format("Error: %1", ex.getMessage()));
        }
    }
    private void OldCalculatePrimes() {
        // DEBUG Log.d(DEBUG_TAG, "CalculatePrimes()");
        try {
            // DEBUG Log.d(DEBUG_TAG, "Initializing Array");
            int[] arr = new int[max_number + 1];

            // Set the first two to compisite numbers.
            arr[0] = 1;
            arr[1] = 1;

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
            int start_number = (max_number > numbers_per_page)? max_number - numbers_per_page + 1 : 2;
            int index_offset = (max_number > numbers_per_page)? max_number - numbers_per_page + 1: 1;
            if (start_number == 2) {
                primes[0] = new NumberCell(1, false);
            }
            // DEBUG Log.d(DEBUG_TAG, String.format("start_number = %d, index_offset = %d, max_number = %d", start_number, index_offset, max_number));
            for (int i = start_number; i <= max_number; i++) {
                // DEBUG Log.d(DEBUG_TAG, String.format("primes[%d] = (%d, %s)...", i - index_offset, i, arr[i] == 0? "true":"false"));
                primes[i - index_offset] = new NumberCell(i, arr[i] == 0);
            }
            // DEBUG Log.d(DEBUG_TAG, String.format(PAGE_OUTPUT_FORMATTER, start_number, max_number, sb.toString()));
        } catch (Exception ex) {
            // DEBUG Log.d(DEBUG_TAG, String.format("Error: %1", ex.getMessage()));
        }
    }

    private void clearView() {
        // Clear Toast if it's there.
        if (currentToast != null) {
            currentToast.cancel();
        }

        // Change TextView color if it was changed because of selection.
        if ( currentText != null) {
            showColor(currentText, TextViewColorBackground.COMPOSITE);
        }
    }

    private void displayRange() {
        currentToast = Toast.makeText(PrimeFlipActivity.this, String.format(bFirstRangeDisplay? FIRST_RANGE_FORMATTER:RANGE_FORMATTER, max_number - numbers_per_page + 1, max_number), Toast.LENGTH_LONG);
        currentToast.show();
        bFirstRangeDisplay = false;
    }

    private void showColor(TextView textView, TextViewColorBackground color_type) {
        switch (color_type) {
            case COMPOSITE:
                textView.setTextColor(Color.GRAY);
                textView.setBackgroundColor(Color.LTGRAY);
                break;
            case COMPOSITE_HIGHLITE:
                textView.setTextColor(Color.DKGRAY);
                textView.setBackgroundColor(Color.LTGRAY);
                break;
            case PRIME:
                textView.setTextColor(Color.RED);
                textView.setBackgroundColor(Color.WHITE);
                break;
            default:
                break;
        }
    }

    class NumberCell {
        int value;
        boolean isPrime;
        public NumberCell(int value, boolean is_prime) {
            this.value = value;
            this.isPrime = is_prime;
        }
    }
    class GridAdapter extends BaseAdapter {
        Context context;
        public GridAdapter(Context c) {
            context = c;
        }

        @Override
        public int getCount() {
            // DEBUG Log.d(DEBUG_TAG, "GridAdapter::getCount()");
            return numbers_per_page;
        }

        @Override
        public Object getItem(int position) {
            // DEBUG Log.d(DEBUG_TAG, String.format("GridAdapter::getItem(%d)", position));
            return primes[position];
        }

        @Override
        public long getItemId(int i) {
            // DEBUG Log.d(DEBUG_TAG, String.format("GridAdapter::getItemId(%d)", i));
            return i;
        }

        private void showPrimeDivisors(TextView textView, int number) {
            SegmentedSieveOfEratosthenes sieve = new SegmentedSieveOfEratosthenes(numbers_per_page);

            Iterator<Integer> prime_divisors = sieve.getPrimeDivisors(number);
            StringBuilder sb = new StringBuilder();
            sb.append(number);
            sb.append(" = ");
            sb.append(1);
            while (prime_divisors.hasNext()) {
                sb.append(" x ");
                sb.append(prime_divisors.next());
            }
            sb.append(" x ");
            sb.append(number);

            currentToast = Toast.makeText(PrimeFlipActivity.this, sb.toString(), Toast.LENGTH_SHORT);
            currentToast.show();

            currentText = textView;
            showColor(currentText, TextViewColorBackground.COMPOSITE_HIGHLITE);
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            // DEBUG Log.d(DEBUG_TAG, String.format("GridAdapter::getView(%d)", position));
            final TextView textView = new TextView(context);
            final NumberCell cell = primes[position];
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            textView.setText(String.valueOf(String.valueOf(cell.value)));
            if (cell.isPrime) {
                showColor(textView, TextViewColorBackground.PRIME);
            } else {
                showColor(textView, TextViewColorBackground.COMPOSITE);
                textView.setOnTouchListener(new View.OnTouchListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            clearView();
                            showPrimeDivisors(textView, cell.value);
                            return false;
                        }
                        return true;
                    }
                });
            }
            return textView;
        }
    }
}
