package com.example.translationapp;

import android.animation.ObjectAnimator;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.sparkchain.core.LLM;
import com.iflytek.sparkchain.core.LLMCallbacks;
import com.iflytek.sparkchain.core.LLMConfig;
import com.iflytek.sparkchain.core.LLMError;
import com.iflytek.sparkchain.core.LLMEvent;
import com.iflytek.sparkchain.core.LLMResult;
import com.iflytek.sparkchain.core.Memory;
import com.iflytek.sparkchain.core.SparkChain;
import com.iflytek.sparkchain.core.SparkChainConfig;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";

    private LLM llm;
    // 设定flag，在输出未完成时无法进行发送
    private boolean sessionFinished = true;

    private View view;
    private ImageView startChatButton;
    private EditText input;
    private TextView output;
    private ScrollView scrollView;
    private View background;
    private FrameLayout chatContainer;

    private List<String> contents = new ArrayList<>();
    float containerZ;

    LLMCallbacks llmCallbacks = new LLMCallbacks() {
        @Override
        public void onLLMResult(LLMResult llmResult, Object usrContext) {
            Log.d(TAG, "onLLMResult\n");
            String content = llmResult.getContent();
            int contentLength = content.length();
            Log.e(TAG, "onLLMResult:" + content);
            int status = llmResult.getStatus();
            if (content != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < contentLength; i++) {
                            contents.add(content.substring(i, i + 1));
                        }
                        output.append(content);
                        toEnd();
                    }
                });
            }

            if (usrContext != null) {
                String context = (String) usrContext;
                Log.d(TAG, "context:" + context);
            }
            if (status == 2) {
                int completionTokens = llmResult.getCompletionTokens();
                int promptTokens = llmResult.getPromptTokens();//
                int totalTokens = llmResult.getTotalTokens();
                Log.e(TAG, "completionTokens:" + completionTokens + "promptTokens:" + promptTokens + "totalTokens:" + totalTokens);

                //输出完毕即可进行下一次输入
                sessionFinished = true;
                toEnd();
            }
        }

        @Override
        public void onLLMEvent(LLMEvent event, Object usrContext) {
            Log.w(TAG, "onLLMEvent:" + " " + event.getEventID() + " " + event.getEventMsg());

        }

        @Override
        public void onLLMError(LLMError error, Object usrContext) {
            Log.d(TAG, "onLLMError\n");
            Log.e(TAG, "errCode:" + error.getErrCode() + "errDesc:" + error.getErrMsg());

            //提示用户发生了错误
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    output.append("错误:" + " err:" + error.getErrCode() + " errDesc:" + error.getErrMsg() + "\n");
                }
            });
            if (usrContext != null) {
                String context = (String) usrContext;
                Log.d(TAG, "context:" + context);
            }

            sessionFinished = true;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        initView();
        initButtonClickListener();
        initSDK();
        return view;
    }

    //初始化控件
    private void initView() {
        startChatButton = (ImageView) view.findViewById(R.id.start_chat_button);
        input = (EditText) view.findViewById(R.id.chat_input);
        output = (TextView) view.findViewById(R.id.chat_output);
        scrollView = (ScrollView) view.findViewById(R.id.output_scrollView);
        chatContainer = LayoutInflater.from(getActivity()).inflate(R.layout.activity_desktop, null).findViewById(R.id.chat_container);
    }

    //为按钮设置监听以及处理
    private void initButtonClickListener() {
        startChatButton.setOnClickListener(v -> {
            if (sessionFinished) {
                contents.clear();
                startChat();
                toEnd();
            } else {
                Toast.makeText(getActivity(), "请稍等！", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //开始回答
    private void startChat() {
        if (llm == null) {
            Log.e(TAG, "tartChat failed!");
            return;
        }

        //获取输入内容
        String usrInputText = input.getText().toString();
        if (usrInputText.length() == 0) {
            Toast.makeText(getActivity(), "请输入文字", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "用户未输入但执行了点击按钮");
            return;
        }
        Log.d(TAG, "用户输入：" + usrInputText);
        output.setText("");

        String myContext = "myContext";

        int ret = llm.arun(usrInputText, myContext);
        if (ret != 0) {
            Log.e(TAG, "SparkChain failed:\n" + ret);
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                input.setText("");
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                outputPerWord();
            }
        }, 5000);


        sessionFinished = false;
    }

    //让内容逐字输出，增强用户体验
    private void outputPerWord() {

    }

    //将视角跟随输出
    public void toEnd() {
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }

    //初始化SDK
    private void initSDK() {
        SparkChainConfig sparkChainConfig = SparkChainConfig.builder();
        sparkChainConfig.appID("1c257cdc")
                .apiKey("8467f8bbd889011ff3aaccab6ecfd4c3")
                .apiSecret("NzgzMGI0ZTJhOTBmYTVjM2EyYmY1OGU1");
        int ret = SparkChain.getInst().init(getActivity(), sparkChainConfig);
        if (ret == 0) {
            Log.d(TAG, "SDK初始化成功：" + ret);
            setLLMConfig();
        } else
            Log.d(TAG, "SDK初始化失败：其他错误:" + ret);

    }

    //配置大模型参数
    private void setLLMConfig() {
        Log.d(TAG, "setLLMConfig");
        LLMConfig llmConfig = LLMConfig.builder();
        llmConfig.domain("generalv3.5");
        llmConfig.url("ws(s)://spark-api.xf-yun.com/v3.5/chat");

        Memory tokens_memory = Memory.tokenMemory(8192);
        llm = new LLM(llmConfig, tokens_memory);
        llm.registerLLMCallbacks(llmCallbacks);
    }
}