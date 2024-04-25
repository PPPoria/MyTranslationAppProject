package com.example.translationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.dim_green));

        CollectOperate.initCount(this);
        HistoryOperate.initCount(this);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //判断登录状态
                        int state = 0;
                        try {
                            SharedPreferences sp = getSharedPreferences("User", MODE_PRIVATE);
                            state = sp.getInt("loginState", 0);
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        if (state == 1) {
                            Intent intent = new Intent(MainActivity.this, DesktopActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
            }
        },1000);




    }
}