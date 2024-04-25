package com.example.translationapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UserOperate {

    //添加账号
    public static void insert(Context context, String account, String password) {
        UserHelper helper = new UserHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("account", account);
        values.put("password", password);

        db.insert("User", null, values);
        db.close();
    }

    //修改密码
    public static void update(Context context, String account, String password) {
        UserHelper helper = new UserHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("password", password);

        db.update("User", values, "account = ?", new String[]{account});
        db.close();
    }

    //验证账号密码
    public static boolean validate(Context context, String account, String password) {
        UserHelper helper = new UserHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query(false, "User", null, null, null, null, null, null, null);
        boolean f = false;
        if (cursor.moveToFirst()) {
            do {
                int index = cursor.getColumnIndex("account");
                if (cursor.getString(index).equals(account)) {
                    index = cursor.getColumnIndex("password");
                    if (cursor.getString(index).equals(password)) {
                        f = true;
                        break;
                    }
                }
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return f;
    }

    //查询账号是否已存在
    public static boolean isExist(Context context, String account) {
        UserHelper helper = new UserHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query(false, "User", null, null, null, null, null, null, null);
        boolean f = false;
        if (cursor.moveToFirst()) {
            do {
                int index = cursor.getColumnIndex("account");
                if (cursor.getString(index).equals(account)) {
                    f = true;
                    break;
                }
            } while (cursor.moveToNext());
        }
        db.close();
        cursor.close();
        return f;
    }
}
