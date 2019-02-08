package com.tiiinq.zodiac.ocr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tiiinq.zodiac.ocr.api.MiddleDB;
import com.tiiinq.zodiac.ocr.models.MatchedItems;

import java.util.ArrayList;

public class ListActivity extends AppCompatActivity {

    ListView listView;
    TextView OF;
    MiddleDB middle;
    MatchedItems hello;
    ArrayList<String> mArrayList_4 = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_);

        hello = new MatchedItems();
        middle = new MiddleDB();

        // Get ListView object from xml
        listView = findViewById(R.id.list);
        OF = findViewById(R.id.OF);

        //Get Data from MainActivity
        Intent i = getIntent();
        MatchedItems fullItems = (MatchedItems) i.getSerializableExtra("key");

        //Defined OF to show in textView
        OF.setText(fullItems.getOF_Id());

        // Defined Array values to show in ListView
        if (!fullItems.getTab_Controls().isEmpty()) {
            mArrayList_4.addAll(fullItems.getTab_Controls());
        }

        // Define a new Adapter
        // First parameter - Context
        // Second parameter - Layout for the row
        // Third parameter - ID of the TextView to which the data is written
        // Forth - the Array of data
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.activity_list_item, android.R.id.text1, mArrayList_4);


        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // ListView Item Click Listener
        listView.setOnItemClickListener((parent, view, position, id) -> {

            // ListView Clicked item index
            int itemPosition = position;

            // ListView Clicked item value
            String itemValue = (String) listView.getItemAtPosition(position);

            // Show Alert
            Toast.makeText(getApplicationContext(),
                    "Position :" + itemPosition + "  ListItem : " + itemValue, Toast.LENGTH_LONG)
                    .show();
        });
    }
}
