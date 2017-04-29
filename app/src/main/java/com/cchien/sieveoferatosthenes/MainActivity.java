package com.cchien.sieveoferatosthenes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    Button startButton;
    EditText maxNumberText;
    int max_number = 0;
    int MAX_MAX_NUMBER = 1000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.start_btn);
        maxNumberText = (EditText) findViewById(R.id.max_num_edit_text);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                max_number = Integer.parseInt(maxNumberText.getText().toString());
                if (max_number < 2) {
                    PromptMessage(R.string.dialog_title_error, R.string.max_number_too_small_message);
                } else if (max_number > MAX_MAX_NUMBER) {
                    PromptMessage(R.string.dialog_title_error, R.string.max_number_too_large_message);
                } else {
                    int[] arr = new int[max_number + 1];
                    for (int i = 2; i <= Math.sqrt(max_number); i++) {
                        if (arr[i] == 0) {
                            for (int j = i * i; j <= max_number; j += i) {
                                arr[j] = 1;
                            }
                        }
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
