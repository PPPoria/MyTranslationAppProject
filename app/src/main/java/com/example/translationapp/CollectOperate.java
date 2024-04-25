package com.example.translationapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.List;

public class CollectOperate {

    private static final String TAG = "CollectOperate";

    public static void initCount(Context context){
        CollectHelper helper = new CollectHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("Collect", new String[]{"fromLanguage"}, null, null, null, null, null);

        int count = 0;
        if(cursor.moveToFirst())
            do{
                count++;
            }while(cursor.moveToNext());

        cursor.close();
        db.close();

        SharedPreferences sp = context.getSharedPreferences("User", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("collectionCount", count);
        editor.commit();
    }

    //添加“收藏”单词
    public static void insert(Context context, String fromLanguage, String toLanguage) {
        CollectHelper helper = new CollectHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("fromLanguage", fromLanguage);
        values.put("toLanguage", toLanguage);

        db.insert("Collect", null, values);
        db.close();
    }

    public static boolean isExist(Context context, String fromLanguage, String toLanguage) {
        CollectHelper helper = new CollectHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("Collect", new String[]{"fromLanguage"}, null, null, null, null, null);

        if (cursor.moveToFirst())
            do {
                int index = cursor.getColumnIndex("fromLanguage");
                if (cursor.getString(index).equals(fromLanguage)) return true;
            } while (cursor.moveToNext());

        return false;
    }

    //删除单词
    public static void delWord(Context context, String fromLanguage) {
        CollectHelper helper = new CollectHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        db.delete("Collect", "fromLanguage = ?", new String[]{fromLanguage});
        db.close();
    }

    public static void delWord(Context context, List<String> delWords) {
        CollectHelper helper = new CollectHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        for (String word : delWords) {
            db.delete("Collect", "fromLanguage = ?", new String[]{word});
            HistoryOperate.removeHistoryCollect(context, word);
        }

        db.close();
    }
}
