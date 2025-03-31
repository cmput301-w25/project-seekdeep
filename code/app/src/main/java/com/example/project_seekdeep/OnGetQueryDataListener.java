package com.example.project_seekdeep;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;

public interface OnGetQueryDataListener {
    public ArrayList<String> onStartListener(ArrayList<String> daysInMonthArray);

    public String onSuccess(ArrayList<DocumentSnapshot> queryReturn);
    public void onFailed(DatabaseError databaseError);
}
