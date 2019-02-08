package com.tiiinq.zodiac.ocr.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

@SuppressWarnings("serial") //With this annotation we are going to hide compiler warnings
public class MatchedItems implements Serializable {

    private ArrayList<String> tab_Controls;
    private String of_Id;
    private Date date;

    public MatchedItems(ArrayList<String> tab_Controls, String OF_Id, Date date) {
        this.tab_Controls = tab_Controls;
        this.of_Id = OF_Id;
        this.date = date;
    }

    public MatchedItems() {
    }

    public void reset() {
        // Setup the instance
        this.of_Id = "";
        this.tab_Controls.clear();
        this.date = null;
    }

    // --- GETTERS ---

    public ArrayList<String> getTab_Controls() {
        return tab_Controls;
    }

    public String getOF_Id() {
        return of_Id;
    }

    public Date getDate() {
        return date;
    }


// --- SETTERS ---

    public void setArrayListControlItem(ArrayList<String> tab_Controls) {
        this.tab_Controls = tab_Controls;
    }

    public void setOF_Id(String OF_Id) {
        this.of_Id = OF_Id;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
