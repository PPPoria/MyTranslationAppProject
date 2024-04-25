package com.example.translationapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserHelper extends SQLiteOpenHelper {

    public static final String CREATE_USER = "create table User ("
            + "account integer primary key autoincrement, "
            + "password integer)";

    public UserHelper(Context context) {
        super(context, "User.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
