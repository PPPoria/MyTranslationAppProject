package com.example.translationapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class HistoryHelper extends SQLiteOpenHelper {

    public static final String CREATE_HISTORY = "create table History("
            + "fromLanguage text, "
            + "toLanguage text, "
            + "collect text)";

    public HistoryHelper(Context context) {
        super(context, "History.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_HISTORY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
