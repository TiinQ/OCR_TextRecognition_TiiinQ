package com.tiiinq.zodiac.ocr;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.tiiinq.zodiac.ocr.api.MiddleDB;

import java.util.ArrayList;

public class OFdisplayedActivity extends AppCompatActivity {

    TextView textviewOfClicked;
    ListView listView;
    ArrayList arraylist;
    MiddleDB middle = new MiddleDB();
    String value;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ofdisplayed);
        textviewOfClicked = findViewById(R.id.textViewOFclicked);
        listView = findViewById((R.id.listOfClicked));
        Intent intent = getIntent();
        value = intent.getStringExtra("nextActivityData"); //if it's a string you stored.
        textviewOfClicked.setText(value);
        getOfClickedItems(value);
        handler = new Handler();
        handler.postDelayed(() -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    android.R.layout.activity_list_item, android.R.id.text1, arraylist);

            // Assign adapter to ListView
            listView.setAdapter(adapter);
        }, 1000);
    }

    public void getOfClickedItems(String ofClicked) {
        arraylist = new ArrayList();
        arraylist = middle.getItemsbyOF(ofClicked);
    }
}
