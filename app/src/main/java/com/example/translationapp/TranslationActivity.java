package com.example.translationapp;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TranslationActivity extends AppCompatActivity {

    private static final String TAG = "TranslationActivity";

    private final String appId = "20240413002022831";//软件id
    private final String key = "yXSufy5Bs461X_p5F_T7";//我的密钥

    private final String[] mode = {"英文 -> 中文", "中文 -> 英文", "中文 -> 日文", "中文 -> 俄文"};

    private ImageView backButton;
    private Spinner spLanguage;
    private TextView translationView;
    private RecyclerView historyWords;

    private String fromLanguageType;
    private String toLanguageType;
    private final List<Word> words = new ArrayList<>();
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translation);
        sp = getSharedPreferences("User", Context.MODE_PRIVATE);
        editor = sp.edit();

        //返回按钮监听
        backButton = (ImageView) findViewById(R.id.from_translation_back);
        backButton.setOnClickListener(v -> {
            SharedPreferences sp = getSharedPreferences("User", Context.MODE_PRIVATE);
            int translationCount = sp.getInt("translationCount", 0);
            HomeFragment.translationCountView.setText(String.valueOf(translationCount));
            finish();
        });

        //为spinner设置适配器
        ArrayAdapter<String> spinnerAdapter =
                new ArrayAdapter<>(this, R.layout.spinner_mode, mode);
        spLanguage = (Spinner) findViewById(R.id.translation_mode);
        spLanguage.setAdapter(spinnerAdapter);

        //翻译类型下拉监听
        spListener();

        //获取输入文本
        EditText input = (EditText) findViewById(R.id.input);
        //String inputText = input.getText().toString();

        //获取译文View
        translationView = (TextView) findViewById(R.id.output);

        //翻译按键监听
        View translateButton = (View) findViewById(R.id.translate);
        translateButton.setOnClickListener(v -> translate(input.getText().toString()));

        //为历史记录RecyclerView添加适配器
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        HistoryAdapter historyAdapter = new HistoryAdapter();
        historyWords = (RecyclerView) findViewById(R.id.history);
        historyWords.setLayoutManager(layoutManager);
        historyWords.setAdapter(historyAdapter);

        //重写回退方法
        OnBackPressedDispatcher dispatcher = getOnBackPressedDispatcher();
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                int translationCount = sp.getInt("translationCount", 0);
                int collectionCount = sp.getInt("collectionCount", 0);
                HomeFragment.translationCountView.setText(String.valueOf(translationCount));
                HomeFragment.collectionCountView.setText(String.valueOf(collectionCount));
                finish();
            }
        };
        dispatcher.addCallback(callback);
    }

    //spinner监听，选择翻译模式
    private void spListener() {
        spLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        fromLanguageType = "en";
                        toLanguageType = "zh";
                        break;
                    case 1:
                        fromLanguageType = "zh";
                        toLanguageType = "en";
                        break;
                    case 2:
                        fromLanguageType = "zh";
                        toLanguageType = "jp";
                        break;
                    case 3:
                        fromLanguageType = "zh";
                        toLanguageType = "ru";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                fromLanguageType = "en";
                toLanguageType = "zh";
            }
        });
    }

    private void translate(String inputText) {
        //判断正确输入
        if (inputText.equals("") || inputText.isEmpty()) {
            Toast.makeText(this, "请输入文本！", Toast.LENGTH_SHORT).show();
            return;
        }

        translationView.setText("翻译中……");

        String salt = RandomNum(4377);//随机数
        String spliceStr = appId + inputText + salt + key;//拼接字符串
        String sign = stringToMD5(spliceStr);//MD5加密
        asyncGET(inputText, salt, sign);//异步请求
    }

    //异步请求
    private void asyncGET(String inputText, String salt, String sign) {
        //通用翻译API HTTPS地址：
        String httpsStr = "https://fanyi-api.baidu.com/api/trans/vip/translate";
        String url = httpsStr +
                "?appid=" + appId + "&q=" + inputText + "&from=" + fromLanguageType + "&to=" +
                toLanguageType + "&salt=" + salt + "&sign=" + sign;

        final Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        Call call = (new OkHttpClient()).newCall(request);
        //子线程
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                goToUIThread(e.toString(), 0);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                SharedPreferences sp = getSharedPreferences("User", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                try {
                    int translationCount = sp.getInt("translationCount", 0);
                    translationCount++;
                    editor.putInt("translationCount", translationCount);
                    editor.commit();
                } catch (Exception e) {
                    Log.d(TAG, "onResponse: 增加翻译次数失败");
                    e.printStackTrace();
                }
                Log.d(TAG, "onResponse: 增加翻译次数成功");
                goToUIThread(response.body().string(), 1);
            }
        });
    }

    //切换到主线程
    private void goToUIThread(final String respone, final int f) {
        TranslationActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (f == 0) {
                    Toast.makeText(TranslationActivity.this, "ERROR!", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, respone);
                } else {
                    //将翻译显示
                    HistoryHelper helper = new HistoryHelper(TranslationActivity.this);
                    SQLiteDatabase db = helper.getWritableDatabase();
                    final TranslateResult result = new Gson().fromJson(respone.toString(), TranslateResult.class);
                    String frTx = result.getTrans_result().get(0).getSrc();
                    String toTx = result.getTrans_result().get(0).getDst();
                    translationView.setText(toTx);
                    //添加到数据库中
                    HistoryOperate.insert(TranslationActivity.this, frTx, toTx);
                    //刷新RecyclerView
                    initList();
                    historyWords.getAdapter().notifyDataSetChanged();

                }
            }
        });
    }

    private void initList() {
        words.clear();

        //把单词数据库里面的单词全部取出到数组中
        HistoryHelper helper = new HistoryHelper(TranslationActivity.this);
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor cursor = db.query(false, "History", null, null, null, null, null, null, null);
        if (cursor.moveToFirst())
            do {
                Word word = new Word();
                int index;
                index = cursor.getColumnIndex("fromLanguage");
                word.setFrTx(cursor.getString(index));
                index = cursor.getColumnIndex("toLanguage");
                word.setToTx(cursor.getString(index));
                index = cursor.getColumnIndex("collect");
                word.setCollect(cursor.getString(index));
                words.add(word);
            } while (cursor.moveToNext());

        cursor.close();
        db.close();
    }

    //MD5加密
    private String stringToMD5(String spliceStr) {
        byte[] hash;

        try {
            hash = MessageDigest.getInstance("MD5")
                    .digest(spliceStr.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    //随机数生成
    private String RandomNum(int seek) {
        Random r = new Random(seek);
        int ran = 0;
        for (int i = 0; i < 5; i++) {
            ran = r.nextInt(100);
            Log.i(TAG, "ran = " + ran);
        }
        return String.valueOf(ran);
    }

    //历史单词RecyclerView适配器
    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryHolder> {

        public HistoryAdapter() {
            initList();
        }


        class HistoryHolder extends RecyclerView.ViewHolder {
            public LinearLayout fromAndTo;
            public TextView historyFromLanguage;
            public TextView historyToLanguage;
            public View collectButton;

            public HistoryHolder(@NonNull View itemView) {
                super(itemView);
                fromAndTo = (LinearLayout) itemView.findViewById(R.id.from_and_to);
                historyFromLanguage = (TextView) itemView.findViewById(R.id.history_fromLanguage);
                historyToLanguage = (TextView) itemView.findViewById(R.id.history_toLanguage);
                collectButton = (View) itemView.findViewById(R.id.collect_button);
            }
        }

        //设置监听
        @NonNull
        @Override
        public HistoryAdapter.HistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(TranslationActivity.this)
                    .inflate(R.layout.history_word, parent, false);
            HistoryHolder holder = new HistoryHolder(itemView);

            //译文点击显示监听
            holder.fromAndTo.setOnClickListener(v -> {
                TextView toTx = holder.historyToLanguage;
                if (toTx.getVisibility() == View.INVISIBLE)
                    toTx.setVisibility(View.VISIBLE);
                else if (toTx.getVisibility() == View.VISIBLE)
                    toTx.setVisibility(View.INVISIBLE);
            });

            //收藏按钮监听
            holder.collectButton.setOnClickListener(v -> {
                int preCollectState = HistoryOperate.changeCollectState(TranslationActivity.this, holder.historyFromLanguage.getText().toString(), holder.historyToLanguage.getText().toString());
                if (preCollectState == HistoryOperate.IS_NOT_COLLECT) {
                    int collectionCount = sp.getInt("collectionCount", 0);
                    collectionCount++;
                    editor.putInt("collectionCount", collectionCount);
                    editor.commit();
                    holder.collectButton.setBackground(getDrawable(R.drawable.collect));
                    CollectOperate.insert(TranslationActivity.this, holder.historyFromLanguage.getText().toString(), holder.historyToLanguage.getText().toString());
                } else if (preCollectState == HistoryOperate.IS_COLLECT) {
                    int collectionCount = sp.getInt("collectionCount", 0);
                    collectionCount--;
                    editor.putInt("collectionCount", collectionCount);
                    editor.commit();
                    CollectOperate.delWord(TranslationActivity.this, holder.historyFromLanguage.getText().toString());
                    holder.collectButton.setBackground(getDrawable(R.drawable.not_collect));
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryHolder holder, int position) {
            Word word = words.get(words.size() - position - 1);
            holder.historyFromLanguage.setText(word.getFrTx());
            holder.historyToLanguage.setText(word.getToTx());
            if (word.getCollect().equals("yes"))
                holder.collectButton.setBackground(getDrawable(R.drawable.collect));
            else holder.collectButton.setBackground(getDrawable(R.drawable.not_collect));
        }

        //返回数据库的数据数量
        @Override
        public int getItemCount() {
            return words.size();
        }
    }
}