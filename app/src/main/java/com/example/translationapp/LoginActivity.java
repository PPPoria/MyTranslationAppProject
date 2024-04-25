package com.example.translationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";

    UserHelper helper = new UserHelper(this);

    private EditText accountView;
    private EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        accountView = (EditText) findViewById(R.id.login_account);
        passwordView = (EditText) findViewById(R.id.login_password);

        Button loginButton = (Button) findViewById(R.id.login_button);
        TextView newUserButton = (TextView) findViewById(R.id.newuser_button);

        //填入上一次登录的账号和密码
        try{
            SharedPreferences sp = getSharedPreferences("User", MODE_PRIVATE);
            accountView.setText(sp.getString("account",null));
            passwordView.setText(sp.getString("password",null));
        }catch (Exception e){
            e.printStackTrace();
        }

        //为注册和登录按钮设置监听
        loginButton.setOnClickListener(this);
        newUserButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.login_button) {
            String account = accountView.getText().toString();
            String password = passwordView.getText().toString();

            //判断账号密码是否填写
            if (account.equals("") || password.equals("")) {
                Toast.makeText(this, "账号或密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            //验证登录
            if (UserOperate.validate(this, account, password)) {
                //记住登录状态和账号密码
                try {
                    SharedPreferences.Editor editor = getSharedPreferences("User", MODE_PRIVATE).edit();
                    editor.putString("account", account);
                    editor.putString("password", password);
                    editor.putInt("loginState", 1);
                    editor.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent intent = new Intent(this, DesktopActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "账号或密码错误", Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.newuser_button) {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        }
    }
}