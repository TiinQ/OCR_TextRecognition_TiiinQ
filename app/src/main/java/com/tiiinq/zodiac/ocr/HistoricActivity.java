package com.tiiinq.zodiac.ocr;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.tiiinq.zodiac.ocr.api.MiddleDB;
import com.tiiinq.zodiac.ocr.models.ListViewAdapter;
import com.tiiinq.zodiac.ocr.models.MatchedItems;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HistoricActivity extends AppCompatActivity {


    MiddleDB middleWare = new MiddleDB();
    ListView list;
    ListViewAdapter adapter;
    ArrayList<MatchedItems> tab_Data = new ArrayList<>();
    Spinner spinner_Date;
    Button button_Go;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic);
        spinner_Date = findViewById(R.id.spinner_Date);
        button_Go = findViewById(R.id.button_Go);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter_Date = ArrayAdapter.createFromResource(this,
                R.array.sortDate_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter_Date.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner_Date.setAdapter(adapter_Date);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    public void OnClickGo(View view) {
        Calendar cal = Calendar.getInstance();
        Date result;
        int i = spinner_Date.getSelectedItemPosition();
        switch (i) {
            case 1:
                cal.add(Calendar.MINUTE, -1);
                result = cal.getTime();
                middleWare.getOFbyDate(result);
                break;
            case 2:
                cal.add(Calendar.WEEK_OF_MONTH, -1);
                result = cal.getTime();
                middleWare.getOFbyDate(result);
                break;
        }
    }
}

