package com.example.gonggong;

import android.app.Activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button mButtonKakaoLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mButtonKakaoLogin:
                Log.i("GongGong","카카오 로그인");
                break;

            default:
                break;
        }
    }

    private void init() {

        mButtonKakaoLogin = (Button) findViewById(R.id.mButtonKakaoLogin);
        mButtonKakaoLogin.setOnClickListener(this);

    }
}