package com.cchien.sieveoferatosthenes;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
    boolean bFirstShown = false;
    boolean bAllShown = false;

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
                if (bProcessing) {
                    return;
                } else {
                    if (firstVisibleItem == 0) {
                        bFirstShown = true;
                    }
                    // DEBUG Log.d(DEBUG_TAG, String.format("totalItemCount: %d, visibleItemCount: %d, firstVisibleItem: %d", totalItemCount, visibleItemCount, firstVisibleItem));
                    if (bFirstShown && firstVisibleItem + visibleItemCount >= totalItemCount) {
                        // DEBUG Log.d(DEBUG_TAG, "firstVisibleItem + visibleItemCount >= totalItemCount");
                        bAllShown = true;
                    }
                }
            }
        });
        RefreshGridView(SwipeDirection.UP);
    }

    private void onSwipeLeft() {
    }

    private void onSwipeRight() {
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
        if (bProcessing || !bAllShown) {
            return;
        }
        bProcessing = true;

        // Head back on Max Number.
        // DEBUG Log.d(DEBUG_TAG, "onSwipeTop()");
        max_number += numbers_per_page;
        CalculatePrimes();
        RefreshGridView(SwipeDirection.UP);

        bProcessing = false;
    }

    private void onSwipeBottom() {
        if (bProcessing || !bAllShown) {
            return;
        }
        bProcessing = true;

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
        adjustGridViewNumColumns();
        gridAdapter.notifyDataSetChanged();
        gridView.invalidateViews();
        gridView.smoothScrollToPosition((direction == SwipeDirection.DOWN)? numbers_per_page : 1);
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
        bAllShown = false;
        bFirstShown = false;
        try {
            SegmentedSieveOfEratosthenes sieve = new SegmentedSieveOfEratosthenes(max_number);
            int start_number = (max_number > numbers_per_page)? max_number - numbers_per_page + 1 : 2;
            int index_offset = (max_number > numbers_per_page)? max_number - numbers_per_page + 1: 1;
            if (start_number == 2) {
                primes[0] = new NumberCell(1, false);
            }
            // DEBUG Log.d(DEBUG_TAG, String.format("start_number = %d, index_offset = %d, max_number = %d", start_number, index_offset, max_number));
            for (int i = start_number; i <= max_number; i++) {
               // DEBUG Log.d(DEBUG_TAG, String.format("primes[%d] = (%d, %s)...", i - index_offset, i, arr[i] == 0? "true":"false"));
                primes[i - index_offset] = new NumberCell(i, sieve.isPrime(i));
            }
            // DEBUG Log.d(DEBUG_TAG, String.format(PAGE_OUTPUT_FORMATTER, start_number, max_number, sb.toString()));
        } catch (Exception ex) {
            // DEBUG Log.d(DEBUG_TAG, String.format("Error: %1", ex.getMessage()));
        }
    }
    private void OldCalculatePrimes() {
        // DEBUG Log.d(DEBUG_TAG, "CalculatePrimes()");
        bAllShown = false;
        bFirstShown = false;
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

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
//            // DEBUG Log.d(DEBUG_TAG, String.format("GridAdapter::getView(%d)", position));
            TextView textView = new TextView(context);
            NumberCell cell = primes[position];
            textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
            textView.setText(String.valueOf(String.valueOf(cell.value)));
            textView.setTextColor(cell.isPrime? Color.RED : Color.GRAY);
            textView.setBackgroundColor(cell.isPrime? Color.WHITE : Color.LTGRAY);
            return textView;
        }
    }
}
