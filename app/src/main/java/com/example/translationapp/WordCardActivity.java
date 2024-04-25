package com.example.translationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WordCardActivity extends AppCompatActivity {

    private static final String TAG = "WordCardActivity";

    private ImageView back;
    private TextView cardCount;
    private TextView copyCard;
    private TextView toTxCard;
    private TextView frTxCard;
    private View flip;
    private ImageView lastWord;
    private ImageView nextWord;

    private List<Word> words = new ArrayList<>();
    int wordPosition = 0;
    float copyZ;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_card);
        getWindow().setNavigationBarColor(getResources().getColor(R.color.dim_green));

        //返回按钮监听
        back = (ImageView) findViewById(R.id.from_word_card_back);
        back.setOnClickListener(v -> finish());

        //获取显示当前单词卡片数量的TextView
        cardCount = (TextView) findViewById(R.id.card_count);

        //翻牌监听
        toTxCard = (TextView) findViewById(R.id.word_card_toTx);
        frTxCard = (TextView) findViewById(R.id.word_card_frTx);
        flip = (View) findViewById(R.id.flip);
        flip.setOnClickListener(v -> {
            //模拟拿起效果
            ObjectAnimator toTxAnimator = ObjectAnimator.ofFloat(toTxCard, "scaleY", 1f, 1.2f, 1f);
            ObjectAnimator frTxAnimator = ObjectAnimator.ofFloat(frTxCard, "scaleY", 1f, 1.2f, 1f);
            toTxAnimator.setDuration(260);
            frTxAnimator.setDuration(260);
            toTxAnimator.start();
            frTxAnimator.start();
            //模拟翻面效果
            toTxAnimator = ObjectAnimator.ofFloat(toTxCard, "scaleX", 1f, 0f, 1f);
            frTxAnimator = ObjectAnimator.ofFloat(frTxCard, "scaleX", 1f, 0f, 1f);
            toTxAnimator.setDuration(260);
            frTxAnimator.setDuration(260);
            /*
            开始缩放运动，模拟翻转
            且切换两个面的可视情况，达到背面亦有文字的效果
             */
            if (toTxCard.getVisibility() == View.VISIBLE && frTxCard.getVisibility() == View.INVISIBLE) {
                toTxAnimator.start();
                frTxAnimator.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toTxCard.setVisibility(View.INVISIBLE);
                        frTxCard.setVisibility(View.VISIBLE);
                    }
                }, 130);
            } else if (toTxCard.getVisibility() == View.INVISIBLE && frTxCard.getVisibility() == View.VISIBLE) {
                toTxAnimator.start();
                frTxAnimator.start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toTxCard.setVisibility(View.VISIBLE);
                        frTxCard.setVisibility(View.INVISIBLE);
                    }
                }, 130);
            }
        });

        //单词切换按钮监听
        copyCard = (TextView) findViewById(R.id.copy_card);
        copyZ = copyCard.getTranslationZ();
        lastWord = (ImageView) findViewById(R.id.last_word);
        nextWord = (ImageView) findViewById(R.id.next_word);
        lastWord.setOnClickListener(v -> toLastWord());
        nextWord.setOnClickListener(v -> toNextWord());

        initList();
    }

    //将数据库内容取出，放入ArrayList管理
    @SuppressLint("SetTextI18n")
    private void initList() {
        words.clear();

        CollectHelper helper = new CollectHelper(WordCardActivity.this);
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

        if (words.size() == 0) return;
        cardCount.setText((wordPosition + 1) + " / " + words.size());
        toTxCard.setText(words.get(wordPosition).getToTx());
        frTxCard.setText(words.get(wordPosition).getFrTx());
    }

    //切换上一个单词
    @SuppressLint("SetTextI18n")
    private void toLastWord() {
        if (words.size() == 0) return;
        if (wordPosition == 0) {
            wordPosition = words.size() - 1;
            cardCount.setText((wordPosition + 1) + " / " + words.size());
        } else {
            wordPosition--;
            cardCount.setText((wordPosition + 1) + " / " + words.size());
        }

        //换牌前确保toTx朝上
        if (toTxCard.getVisibility() == View.INVISIBLE && frTxCard.getVisibility() == View.VISIBLE) {
            //模拟拿起效果
            ObjectAnimator toTxAnimator = ObjectAnimator.ofFloat(toTxCard, "scaleY", 1f, 1.2f, 1f);
            ObjectAnimator frTxAnimator = ObjectAnimator.ofFloat(frTxCard, "scaleY", 1f, 1.2f, 1f);
            toTxAnimator.setDuration(260);
            frTxAnimator.setDuration(260);
            toTxAnimator.start();
            frTxAnimator.start();
            //模拟翻面效果
            toTxAnimator = ObjectAnimator.ofFloat(toTxCard, "scaleX", 1f, 0f, 1f);
            frTxAnimator = ObjectAnimator.ofFloat(frTxCard, "scaleX", 1f, 0f, 1f);
            toTxAnimator.setDuration(260);
            frTxAnimator.setDuration(260);
            toTxAnimator.start();
            frTxAnimator.start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    toTxCard.setVisibility(View.VISIBLE);
                    frTxCard.setVisibility(View.INVISIBLE);
                }
            }, 130);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateToLast();
                }
            }, 260);
        } else animateToLast();
    }

    //切换下一个单词
    @SuppressLint("SetTextI18n")
    private void toNextWord() {
        if (words.size() == 0) return;
        if (wordPosition == words.size() - 1) {
            wordPosition = 0;
            cardCount.setText((wordPosition + 1) + " / " + words.size());
        } else {
            wordPosition++;
            cardCount.setText((wordPosition + 1) + " / " + words.size());
        }

        //换牌前确保toTx朝上
        if (toTxCard.getVisibility() == View.INVISIBLE && frTxCard.getVisibility() == View.VISIBLE) {
            //模拟拿起效果
            ObjectAnimator toTxAnimator = ObjectAnimator.ofFloat(toTxCard, "scaleY", 1f, 1.2f, 1f);
            ObjectAnimator frTxAnimator = ObjectAnimator.ofFloat(frTxCard, "scaleY", 1f, 1.2f, 1f);
            toTxAnimator.setDuration(260);
            frTxAnimator.setDuration(260);
            toTxAnimator.start();
            frTxAnimator.start();
            //模拟翻面效果
            toTxAnimator = ObjectAnimator.ofFloat(toTxCard, "scaleX", 1f, 0f, 1f);
            frTxAnimator = ObjectAnimator.ofFloat(frTxCard, "scaleX", 1f, 0f, 1f);
            toTxAnimator.setDuration(260);
            frTxAnimator.setDuration(260);
            toTxAnimator.start();
            frTxAnimator.start();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    toTxCard.setVisibility(View.VISIBLE);
                    frTxCard.setVisibility(View.INVISIBLE);
                }
            }, 130);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateToNext();
                }
            }, 260);
        } else animateToNext();
    }

    //Last换牌动画
    private void animateToLast() {
        /*
        将toTx复制给copyCard
        让copyCard在牌顶，然后放入底部
        toTxCard则切换内容
        达到看似换牌的效果
         */
        copyCard.setTranslationZ(0);
        copyCard.setText(words.get(wordPosition).getToTx());
        copyCard.setVisibility(View.VISIBLE);

        //模拟插入卡片堆的底部
        ObjectAnimator copyTxAnimator = ObjectAnimator.ofFloat(copyCard, "translationX", 0f, 2000f, 0f);
        copyTxAnimator.setDuration(500);
        copyTxAnimator.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                copyCard.setTranslationZ(copyZ);
            }
        }, 250);

        //把copyCard隐形，放回牌顶以便下次用
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                toTxCard.setText(copyCard.getText());
                frTxCard.setText(words.get(wordPosition).getFrTx());
                copyCard.setVisibility(View.INVISIBLE);
            }
        }, 500);
    }

    //next换牌动画
    private void animateToNext() {
        /*
        将toTx复制给copyCard
        让copyCard在牌顶，然后放入底部
        toTxCard则切换内容
        达到看似换牌的效果
         */
        copyCard.setText(toTxCard.getText());
        copyCard.setVisibility(View.VISIBLE);
        toTxCard.setText(words.get(wordPosition).getToTx());
        frTxCard.setText(words.get(wordPosition).getFrTx());

        //模拟插入卡片堆的底部
        ObjectAnimator copyTxAnimator = ObjectAnimator.ofFloat(copyCard, "translationX", 0f, -2000f, 0f);
        copyTxAnimator.setDuration(500);
        copyTxAnimator.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                copyCard.setTranslationZ(0);
            }
        }, 250);

        //把copyCard隐形，放回牌顶以便下次用
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                copyCard.setVisibility(View.INVISIBLE);
                copyCard.setTranslationZ(copyZ);
            }
        }, 500);
    }

}