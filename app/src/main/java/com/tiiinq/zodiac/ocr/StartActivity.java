package com.tiiinq.zodiac.ocr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


public class StartActivity extends AppCompatActivity  {

    Button buttonScan;
    Button buttonHistoric;
    Button buttonSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        buttonScan = findViewById(R.id.button_Go);
        buttonHistoric = findViewById(R.id.button_Historic);
        buttonSearch = findViewById(R.id.button_Search);
    }

    public void onClickBtnScan(View v) {
        Intent intent = new Intent(this, InterActivity.class);
        this.startActivity ( intent );
    }
    public void onClickBtnHistoric(View v) {
        Intent intent = new Intent(this, HistoricActivity.class);
        this.startActivity ( intent );
    }

    public void onClickBtnHandle(View view) {
        Intent intent = new Intent(this, ManagementActivity.class);
        this.startActivity ( intent );
    }

    public void onClickBtnSearch(View view) {
        Intent intent = new Intent(this, SearchActivity.class);
        this.startActivity ( intent );
    }
}
