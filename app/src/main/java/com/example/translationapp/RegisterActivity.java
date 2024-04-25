package com.example.translationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        EditText registerAccountView = (EditText) findViewById(R.id.register_account);
        EditText registerPasswordView = (EditText) findViewById(R.id.register_password);
        EditText registerPasswordAgainView = (EditText) findViewById(R.id.register_account_again);

        Button registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(v -> {
            String account = registerAccountView.getText().toString();
            String password = registerPasswordView.getText().toString();
            String passwordAgain = registerPasswordAgainView.getText().toString();

            //判断是否输入
            if (account.equals("") || password.equals("")) {
                Toast.makeText(this, "账号或密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            //判断两次输入密码是否一致
            if (!password.equals(passwordAgain)) {
                Toast.makeText(this, "密码不相同", Toast.LENGTH_SHORT).show();
                return;
            }

            //判断账号是否已注册
            if (UserOperate.isExist(this, account)) {
                Toast.makeText(this, "该账号已注册", Toast.LENGTH_SHORT).show();
                return;
            }

            UserOperate.insert(this, account, password);
            Toast.makeText(this, "成功注册", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}