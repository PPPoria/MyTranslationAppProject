package com.example.translationapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class HistoryOperate {

    public static final int IS_COLLECT = 2;

    public static final int IS_NOT_COLLECT = 1;

    public static final int NO_THIS_WORD = 0;

    private static final String TAG = "HistoryOperate";

    public static void initCount(Context context){

    }

    /*
     * 添加历史单词
     * 如果历史记录中未存在，则直接添加
     * 如果存在，则将其放在数据库末尾
     */
    public static void insert(Context context, String fromLanguage, String toLanguage) {
        HistoryHelper helper = new HistoryHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("fromLanguage", fromLanguage);
        values.put("toLanguage", toLanguage);


        if (CollectOperate.isExist(context, fromLanguage, toLanguage)) {
            values.put("collect", "yes");
            Log.d(TAG, "insert: 该单词 存在 于“我的词句本”");
        } else {
            values.put("collect", "no");
            Log.d(TAG, "insert: 该单词 不存在 于“我的词句本”");
        }

        //单词置顶
        try {
            db.delete("History", "fromLanguage = ?", new String[]{fromLanguage});
        } catch (Exception e) {
            Log.d(TAG, "insert: 默认置顶");
        }

        db.insert("History", null, values);
        db.close();
    }

    //删除单词
    public static void delWord(Context context, String fromLanguage, String toLanguage) {
        HistoryHelper helper = new HistoryHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        db.delete("History", "fromLanguage = ?", new String[]{fromLanguage});
        db.close();
    }

    //清空历史记录
    public static void clearHistory(Context context) {
        HistoryHelper helper = new HistoryHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();

        db.delete("History", null, null);
        db.close();
    }

    //切换收藏状态,返回切换前的收藏状态
    public static int changeCollectState(Context context, String fromLanguage, String toLanguage) {
        HistoryHelper helper = new HistoryHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query(false, "History", null, null, null, null, null, null, null);

        boolean wordIsExist = false;
        if (cursor.moveToFirst())
            do {
                int index = cursor.getColumnIndex("fromLanguage");
                if (cursor.getString(index).equals(fromLanguage)) {
                    Log.d(TAG, "isExist:" + cursor.getString(index));
                    wordIsExist = true;
                    break;
                }
            } while (cursor.moveToNext());

        if (!wordIsExist) {
            cursor.close();
            db.close();
            return NO_THIS_WORD;
        }

        ContentValues values = new ContentValues();
        int index = cursor.getColumnIndex("collect");
        if (cursor.getString(index).equals("no")) {
            values.put("collect", "yes");
            db.update("History", values, "fromLanguage = ?", new String[]{fromLanguage});
            cursor.close();
            db.close();
            return IS_NOT_COLLECT;
        } else if (cursor.getString(index).equals("yes")) {
            values.put("collect", "no");
            db.update("History", values, "fromLanguage = ?", new String[]{fromLanguage});
            cursor.close();
            db.close();
            return IS_COLLECT;
        }
        cursor.close();
        db.close();
        return NO_THIS_WORD;
    }

    public static void removeHistoryCollect(Context context, String fromLanguage) {
        HistoryHelper helper = new HistoryHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query(false, "History", null, null, null, null, null, null, null);

        boolean wordIsExist = false;
        if (cursor.moveToFirst())
            do {
                int index = cursor.getColumnIndex("fromLanguage");
                if (cursor.getString(index).equals(fromLanguage)) {
                    wordIsExist = true;
                    break;
                }
            } while (cursor.moveToNext());

        if (!wordIsExist) {
            cursor.close();
            db.close();
            return;
        }

        ContentValues values = new ContentValues();
        values.put("collect", "no");
        db.update("History", values, "fromLanguage = ?", new String[]{fromLanguage});

        cursor.close();
        db.close();

        SharedPreferences sp = context.getSharedPreferences("User", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        int collectionCount = sp.getInt("collectionCount", 0);
        collectionCount--;
        editor.putInt("collectionCount", collectionCount);
        editor.commit();
    }
}
