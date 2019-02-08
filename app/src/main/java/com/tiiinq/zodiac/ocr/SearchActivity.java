package com.tiiinq.zodiac.ocr;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.tiiinq.zodiac.ocr.api.MiddleDB;
import com.tiiinq.zodiac.ocr.models.ListViewAdapter;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


public class SearchActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    // Declare Variables
    ListView list;
    ListViewAdapter adapter;
    SearchView editsearch;
    ArrayList<String> arraylist;
    ArrayList<String> OFTab;
    MiddleDB middle;
    TextView textView_Search;
    Spinner spinnerDate;
    String nextActivityData = "nextActivityData";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        OFTab = new ArrayList<>();
        setContentView(R.layout.activity_search);

        list = findViewById(R.id.listview);
        textView_Search = findViewById(R.id.textView_Search);
        spinnerDate = findViewById(R.id.spinner_Daate);
        editsearch = findViewById(R.id.search);
        editsearch.setOnQueryTextListener(this);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter_Date = ArrayAdapter.createFromResource(this,
                R.array.sortDate_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter_Date.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinnerDate.setAdapter(adapter_Date);
        list.setOnItemClickListener((parent, view, position, id) -> {
            String listItem = (String) list.getItemAtPosition(position);
            Intent i = new Intent(SearchActivity.this, OFdisplayedActivity.class);
            i.putExtra(nextActivityData, listItem);
            SearchActivity.this.startActivity(i);
        });
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        middle = new MiddleDB();
        list.setAdapter(null);
        Date date;
        Calendar cal;
        Handler handler = new Handler();
        switch (spinnerDate.getSelectedItemPosition()) {
            case (1):
                date = new Date(System.currentTimeMillis() - 3600 * 1000);
                OFTab = middle.getOFbyControlItemAndDate(query, date);
                break;
            case (2):
                date = new Date();
                cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                Date newDate = cal.getTime();
                OFTab = middle.getOFbyControlItemAndDate(query, newDate);
                break;
            case (3):
                cal = new GregorianCalendar();
                cal.add(Calendar.DAY_OF_MONTH, -7);
                date = cal.getTime();
                OFTab = middle.getOFbyControlItemAndDate(query, date);
                break;
            case (4):
                cal = new GregorianCalendar();
                cal.add(Calendar.DAY_OF_MONTH, -30);
                date = cal.getTime();
                OFTab = middle.getOFbyControlItemAndDate(query, date);
                break;
            default:
                date = new Date(0L);
                OFTab = middle.getOFbyControlItemAndDate(query, date);
                break;
        }

        handler.postDelayed(() -> {
            if (!OFTab.isEmpty()) {
                // Pass results to ListViewAdapter Class
                adapter = new ListViewAdapter(getApplicationContext(), OFTab);
                textView_Search.setText(query);
                // Binds the Adapter to the ListView
                list.setAdapter(adapter);
            } else {
                OFTab.add("Aucun OF correspondant à votre recherche");
                // Pass results to ListViewAdapter Class
                adapter = new ListViewAdapter(getApplicationContext(), OFTab);
                // Binds the Adapter to the ListView
                list.setAdapter(adapter);
            }
        }, 1500);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.equals("") || !OFTab.isEmpty()){
            list.setAdapter(null);
            OFTab.clear();
            textView_Search.setText("");
        }
        return false;
    }
}

