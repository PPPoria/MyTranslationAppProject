package com.example.translationapp;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

public class PersonalFragment extends Fragment implements View.OnClickListener {

    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_personal, container, false);

        LinearLayout clearHistory = (LinearLayout) view.findViewById(R.id.clear_history);
        LinearLayout resign = (LinearLayout) view.findViewById(R.id.resign);

        clearHistory.setOnClickListener(this);
        resign.setOnClickListener(this);

        return view;
    }

    //两个按钮的监听
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.clear_history) {
            HistoryOperate.clearHistory(getActivity());
            Toast.makeText(getActivity(), "已清空", Toast.LENGTH_SHORT).show();
        } else if (v.getId() == R.id.resign) {
            try {
                SharedPreferences.Editor editor = getActivity().getSharedPreferences("User", MODE_PRIVATE).edit();
                editor.putInt("loginState", 0);
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
        }
    }
}