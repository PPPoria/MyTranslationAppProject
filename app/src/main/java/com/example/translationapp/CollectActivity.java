package com.example.translationapp;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class CollectActivity extends AppCompatActivity {

    private static final String TAG = "CollectActivity";

    private ImageView back;
    private ImageView del;
    private RecyclerView collectWords;
    private TextView display;
    private TextView toWordCardActivity;

    private List<Word> words = new ArrayList<>();
    private List<String> delWords = new ArrayList<>();
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private final boolean READY = true;
    private final boolean NOT_READY = false;
    private boolean undoState;
    private boolean displayState;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect);
        sp = getSharedPreferences("User", MODE_PRIVATE);
        editor = sp.edit();
        undoState = NOT_READY;
        displayState = NOT_READY;

        //返回按钮监听
        back = (ImageView) findViewById(R.id.from_collect_back);
        back.setOnClickListener(v -> {
            int collectionCount = sp.getInt("collectionCount", 0);
            HomeFragment.collectionCountView.setText(String.valueOf(collectionCount));
            finish();
        });

        //重写回退方法
        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int collectionCount = sp.getInt("collectionCount", 0);
                HomeFragment.collectionCountView.setText(String.valueOf(collectionCount));
                finish();
            }
        };
        dispatcher.addCallback(callback);

        //为收藏RecyclerView配置适配器
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        CollectAdapter collectAdapter = new CollectAdapter();
        collectWords = (RecyclerView) findViewById(R.id.collect);
        collectWords.setLayoutManager(layoutManager);
        collectWords.setAdapter(collectAdapter);

        //取消收藏操作
        del = (ImageView) findViewById(R.id.del);
        del.setOnClickListener(v -> {
            Log.d(TAG, "click del\nbefore that the undoState is " + undoState + "\nnow the undoState is " + !undoState);
            if (undoState == NOT_READY) {
                undoState = READY;
            } else if (undoState == READY) {
                undoState = NOT_READY;
                CollectOperate.delWord(this, delWords);
            }
            initList();
            collectWords.getAdapter().notifyDataSetChanged();
        });

        //显示释义按钮监听
        display = (TextView) findViewById(R.id.display_translation);
        display.setOnClickListener(v -> {
            if (displayState == NOT_READY) {
                displayState = READY;
                display.setText("隐藏释义");
            } else if (displayState == READY) {
                displayState = NOT_READY;
                display.setText("显示释义");
            }
            collectWords.getAdapter().notifyDataSetChanged();
        });

        //为单词卡片按钮设置监听
        toWordCardActivity = (TextView) findViewById(R.id.to_word_card_activity);
        toWordCardActivity.setOnClickListener(v -> {
            Intent intent = new Intent(CollectActivity.this, WordCardActivity.class);
            startActivity(intent);
        });
    }

    private void initList() {
        words.clear();

        CollectHelper helper = new CollectHelper(CollectActivity.this);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query("Collect", null, null, null, null, null, null);
        if (cursor.moveToFirst())
            do {
                Word word = new Word();
                int index;
                index = cursor.getColumnIndex("fromLanguage");
                word.setFrTx(cursor.getString(index));
                index = cursor.getColumnIndex("toLanguage");
                word.setToTx(cursor.getString(index));
                words.add(word);
            } while (cursor.moveToNext());

        cursor.close();
        db.close();
        Log.d(TAG, "initList: size = " + words.size());
    }

    private class CollectAdapter extends RecyclerView.Adapter<CollectAdapter.CollectHolder> {

        public CollectAdapter() {
            initList();
        }

        class CollectHolder extends RecyclerView.ViewHolder {
            public LinearLayout toAndFrom;
            public TextView collectToTx;
            public TextView collectFromTx;
            public View undoCollectButton;

            public CollectHolder(@NonNull View itemView) {
                super(itemView);
                toAndFrom = (LinearLayout) itemView.findViewById(R.id.to_and_from);
                collectToTx = (TextView) itemView.findViewById(R.id.collect_toTx);
                collectFromTx = (TextView) itemView.findViewById(R.id.collect_fromTx);
                undoCollectButton = (View) itemView.findViewById(R.id.undo_collect_button);
            }
        }

        @NonNull
        @Override
        public CollectHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(CollectActivity.this)
                    .inflate(R.layout.collect_word, parent, false);
            CollectHolder holder = new CollectHolder(itemView);

            //译文点击显示监听
            holder.toAndFrom.setOnClickListener(v -> {
                TextView frTx = holder.collectFromTx;
                if (frTx.getVisibility() == View.INVISIBLE)
                    frTx.setVisibility(View.VISIBLE);
                else if (frTx.getVisibility() == View.VISIBLE)
                    frTx.setVisibility(View.INVISIBLE);
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull CollectHolder holder, int position) {
            Word word = words.get(words.size() - position - 1);
            holder.collectToTx.setText(word.getToTx());
            holder.collectFromTx.setText(word.getFrTx());

            //判断“显示释义”是否被点击
            if (displayState == NOT_READY) {
                holder.collectFromTx.setVisibility(View.INVISIBLE);
            } else if (displayState == READY) {
                holder.collectFromTx.setVisibility(View.VISIBLE);
            }

            //判断undo按钮是否显示
            if (undoState == NOT_READY) {
                Log.d(TAG, "now the visibility of button is invisible");
                holder.undoCollectButton.setVisibility(View.INVISIBLE);
            } else if (undoState == READY) {
                Log.d(TAG, "now the visibility of button is visible");
                holder.undoCollectButton.setVisibility(View.VISIBLE);
            }

            //为undo按钮设置监听
            holder.undoCollectButton.setOnClickListener(v -> {
                Log.d(TAG, "click undoButton");
                if (holder.undoCollectButton.getBackground().getConstantState().equals(getResources().getDrawable(R.drawable.collect).getConstantState())) {
                    holder.undoCollectButton.setBackground(getResources().getDrawable(R.drawable.not_collect));
                    delWords.add(holder.collectFromTx.getText().toString());
                } else {
                    holder.undoCollectButton.setBackground(getResources().getDrawable(R.drawable.collect));
                    delWords.remove(holder.collectFromTx.getText().toString());
                }
            });
        }

        @Override
        public int getItemCount() {
            return words.size();
        }
    }

}