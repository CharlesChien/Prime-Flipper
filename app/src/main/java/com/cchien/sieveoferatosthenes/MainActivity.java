package com.cchien.sieveoferatosthenes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Button startButton;
    EditText maxNumberText;
    int max_number = 0;
    int MAX_MAX_NUMBER = 10000000; // 10 million

    TextView test_output_text_view;
    private static final String TAG = "SOE - MainActivity";

    final String OUTPUT_FORMATTER = "All prime numbers up to %1$d: %2$s";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.start_btn);
        maxNumberText = (EditText) findViewById(R.id.max_num_edit_text);

        test_output_text_view = (TextView) findViewById(R.id.test_output_testview);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                max_number = Integer.parseInt(maxNumberText.getText().toString());
                if (max_number < 2) {
                    PromptMessage(R.string.dialog_title_error, R.string.max_number_too_small_message);
                } else if (max_number > MAX_MAX_NUMBER) {
                    PromptMessage(R.string.dialog_title_error, R.string.max_number_too_large_message);
                } else {
                    try {
                        Log.d(TAG, "Initializing Array");
                        int[] arr = new int[max_number + 1];

                        Log.d(TAG, "Looping through sqrt()");
                        for (int i = 2; i <= Math.sqrt(max_number); i++) {
                            if (arr[i] == 0) {
                                for (int j = i * i; j <= max_number; j += i) {
                                    arr[j] = 1;
                                }
                            }
                        }

                        Log.d(TAG, "Collecting primes");
                        boolean bFirst = true;
                        StringBuilder sb = new StringBuilder();
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
                        Log.d(TAG, String.format(OUTPUT_FORMATTER, max_number, sb.to String()));
                        test_output_text_view.setText(String.format(OUTPUT_FORMATTER, max_number, sb.toString()));
                    } catch (Exception ex) {
                        Log.d(TAG, String.format("Error: %1", ex.getMessage()));
                    }

                }
            }
        });
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
