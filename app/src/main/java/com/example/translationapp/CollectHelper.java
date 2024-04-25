package com.example.translationapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class CollectHelper extends SQLiteOpenHelper {

    public static final String CREATE_COLLECT = "create table Collect("
            + "fromLanguage text, "
            + "toLanguage text)";

    public CollectHelper(Context context) {
        super(context, "Collect.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_COLLECT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
