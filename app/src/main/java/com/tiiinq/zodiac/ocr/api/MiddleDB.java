package com.tiiinq.zodiac.ocr.api;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.tiiinq.zodiac.ocr.models.MatchedItems;

import java.util.ArrayList;
import java.util.Date;

public class MiddleDB {

    private String TAG = "REGARDE ICI !!!";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ArrayList<String> mArrayList_controls;
    private ArrayList<String> mArrayList_2;
    private ArrayList<String> mArrayList_OF;

    public ArrayList<String> GetCalibrerItems() {
        mArrayList_controls = new ArrayList<>();
        DocumentReference docRef = db.collection("REFS").document("Calibrer");
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    for (int i = 1; i < 5; i++) {
                        String controller;
                        controller = (document.getString("Calibrer-" + i));
                        mArrayList_controls.add(controller);
                        Log.d("TAAAAAG", "Query completed"); //Print the name
                    }
                } else {
                    Log.d("TAAAAAG", "No such document");
                }
            } else {
                Log.d("TAAAAAG", "get failed with ", task.getException());
            }
        });
        return mArrayList_controls;
    }

    public ArrayList<String> GetCalibratedItems() {
        mArrayList_2 = new ArrayList<>();
        DocumentReference docRef = db.collection("REFS").document("Calibrated");
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    for (int i = 1; i < 5; i++) {
                        mArrayList_2.add(document.getString("Calibrated-" + i));
                        Log.d("TAAAAAG", "Query completed"); //Print the name
                    }
                } else {
                    Log.d("TAAAAAG", "No such document");
                }
            } else {
                Log.d("TAAAAAG", "get failed with ", task.getException());
            }
        });
        return mArrayList_2;
    }

    public ArrayList getItemsbyOF(String OF) {
        mArrayList_OF = new ArrayList<>();
        CollectionReference matchedItemsRef = db.collection("MatchedItems").document("CoupleItems").collection("Calib");
        matchedItemsRef
                .whereEqualTo("of_Id", OF)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "HOLLA");
                        for (DocumentSnapshot document : task.getResult()) {
                            mArrayList_OF.addAll(document.toObject(MatchedItems.class).getTab_Controls());
                            Log.d(TAG, "Query completed");
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
        return mArrayList_OF;
    }

    public ArrayList<String> getOFbyControlItemAndDate(String controlItem, Date date) {
        mArrayList_OF = new ArrayList<>();
        CollectionReference matchedItemsRef = db.collection("MatchedItems").document("CoupleItems").collection("Calib");
        matchedItemsRef
                .whereArrayContains("tab_Controls", controlItem.trim())
                .whereGreaterThan("date", date)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            mArrayList_OF.add(document.toObject(MatchedItems.class).getOF_Id());
                            Log.d(TAG, "Query completed");
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
        return mArrayList_OF;
    }

    public ArrayList<String> getOFbyDate(Date date) {
        mArrayList_OF = new ArrayList<>();
        Timestamp ts = new Timestamp(date);
        CollectionReference matchedItemsRef = db.collection("MatchedItems").document("CoupleItems").collection("Calib");
        matchedItemsRef
                .whereGreaterThan("date", ts)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            mArrayList_OF.add(document.toObject(MatchedItems.class).getOF_Id());
                            Log.d(TAG, "Query completed");
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
        return mArrayList_OF;
    }

}
