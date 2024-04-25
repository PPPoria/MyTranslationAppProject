package com.example.translationapp;

import static android.view.View.NO_ID;
import static android.view.View.inflate;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

public class DesktopActivity extends AppCompatActivity {

    public static Context mContext;

    private static final String TAG = "DesktopActivity";

    private ViewPager2 vp;
    private TextView homePageButton;
    private TextView personalPageButton;
    private FrameLayout chatContainer;
    private View chatFragmentBackground;
    private LinearLayout callChatWindow;

    private int pagePosition;
    private Fragment chatWindow;
    private FragmentManager fragmentManager;
    private FragmentTransaction transaction;
    private List<Fragment> list = new ArrayList<>();
    private float containerZ;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_desktop);
        mContext = this;

        //将Fragment放入List
        list.add(new HomeFragment());
        list.add(new PersonalFragment());

        //为ViewPager2设置适配器
        vp = (ViewPager2) findViewById(R.id.view_pager);
        vp.setAdapter(new DesktopAdapter(this));

        //为底部导航栏设置监听，用于点击切换页面
        homePageButton = (TextView) findViewById(R.id.home_page_button);
        personalPageButton = (TextView) findViewById(R.id.personal_page_button);
        homePageButton.setOnClickListener(v -> vp.setCurrentItem(0));
        personalPageButton.setOnClickListener(v -> vp.setCurrentItem(1));

        //为ViewPager2注册页面监听器，用于切换页面时同步底部导航栏
        vp.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                pagePosition = position;
                if (position == 0) {
                    homePageButton.setTextSize(25);
                    homePageButton.setTextColor(getResources().getColor(R.color.green));
                    personalPageButton.setTextSize(20);
                    personalPageButton.setTextColor(getResources().getColor(R.color.black));
                } else if (position == 1) {
                    homePageButton.setTextSize(20);
                    homePageButton.setTextColor(getResources().getColor(R.color.black));
                    personalPageButton.setTextSize(25);
                    personalPageButton.setTextColor(getResources().getColor(R.color.green));
                }
            }
        });

        //为ai对话窗口提供容器
        chatContainer = (FrameLayout) findViewById(R.id.chat_container);
        chatWindow = new ChatFragment();
        fragmentManager = getSupportFragmentManager();
        transaction = fragmentManager.beginTransaction();

        initChat();

        //设置对话窗口呼出按钮
        chatFragmentBackground = (View) findViewById(R.id.chat_fragment_background);
        callChatWindow = (LinearLayout) findViewById(R.id.call_chat_fragment);
        containerZ = chatContainer.getTranslationZ();
        callChatWindow.setOnClickListener(v->{
            Log.d(TAG, "call Chat window");
            ObjectAnimator chatAnimator = ObjectAnimator.ofFloat(R.id.chat_fragment, "translationY", -2000f, 0f);
            chatAnimator.setDuration(400);
            chatAnimator.start();
            chatFragmentBackground.setVisibility(View.VISIBLE);
            chatContainer.setVisibility(View.VISIBLE);
        });

        //设置对话窗口退出
        chatFragmentBackground.setOnClickListener(v->{
            chatFragmentBackground.setVisibility(View.INVISIBLE);
            chatContainer.setVisibility(View.INVISIBLE);
        });

        //重写回退方法
        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (chatFragmentBackground.getVisibility() == View.VISIBLE
                || chatContainer.getVisibility() == View.VISIBLE){
                    chatFragmentBackground.setVisibility(View.INVISIBLE);
                    chatContainer.setVisibility(View.INVISIBLE);
                }
                else if (pagePosition == 1){
                        vp.setCurrentItem(0);
                } else finish();
            }
        };
        dispatcher.addCallback(callback);
    }

    private void initChat() {
        transaction.add(R.id.chat_container,chatWindow);
        transaction.commit();
        chatContainer.setVisibility(View.INVISIBLE);
    }

    //刷新
    public static void refresh(){

    }

    private class DesktopAdapter extends FragmentStateAdapter {

        public DesktopAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return list.get(position);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}