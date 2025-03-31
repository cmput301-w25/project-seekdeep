package com.example.project_seekdeep;

import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;

public interface OnGetQueryDataListener {
    public void onStart();
    public void onSuccess(ArrayList<String> moodsInMonth);
    public void onFailed(DatabaseError databaseError);
}
