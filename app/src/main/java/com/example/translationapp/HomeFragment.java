package com.example.translationapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    public static TextView translationCountView;
    public static TextView collectionCountView;

    View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home, container, false);

        //翻译跳转按钮监听
        TextView toTranslationActivityButton =
                (TextView) view.findViewById(R.id.to_translation_activity);
        toTranslationActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), TranslationActivity.class);
            startActivity(intent);
        });

        //翻译次数显示
        translationCountView = (TextView) view.findViewById(R.id.translation_count);
        try {
            SharedPreferences sp = view.getContext().getSharedPreferences("User", Context.MODE_PRIVATE);
            int translationCount = sp.getInt("translationCount", 0);
            translationCountView.setText(String.valueOf(translationCount));
            Log.d(TAG, "onCreateView: 翻译次数显示成功,count = " + translationCount);
        } catch (Exception e) {
            Log.d(TAG, "onCreateView: 翻译次数显示失败");
            e.printStackTrace();
        }

        //词句本按钮监听
        ConstraintLayout toCollectActivity = (ConstraintLayout) view.findViewById(R.id.to_collect_activity);
        toCollectActivity.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CollectActivity.class);
            startActivity(intent);
        });

        //词句本内单词数量显示
        collectionCountView = (TextView) view.findViewById(R.id.collect_count);
        try {
            SharedPreferences sp = view.getContext().getSharedPreferences("User", Context.MODE_PRIVATE);
            int collectionCount = sp.getInt("collectionCount", 0);
            collectionCountView.setText(String.valueOf(collectionCount));
            Log.d(TAG, "onCreateView: 收藏单词数显示成功,count = " + collectionCount);
        } catch (Exception e) {
            Log.d(TAG, "onCreateView: 收藏单词数次数显示失败");
            e.printStackTrace();
        }

        //广告按钮监听
        ConstraintLayout AD = (ConstraintLayout) view.findViewById(R.id.ad);
        AD.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://ak.hypergryph.com/"));
            startActivity(intent);
        });

        return view;
    }
}