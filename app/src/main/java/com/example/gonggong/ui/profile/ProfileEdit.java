package com.example.gonggong.ui.profile;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gonggong.R;

public class ProfileEdit extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        //아래에서 위로 올라오면서 켜진다.
        overridePendingTransition(R.anim.vertical_in, R.anim.none);
    }

    public void editfin(){
        finish();
        //아래로 내려가면서 사라진다.
        overridePendingTransition(R.anim.none, R.anim.vertical_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(isFinishing()){
            //back 버튼을 누르면 화면 종료가 야기되면 동작
            overridePendingTransition(R.anim.none, R.anim.vertical_out);
        }
    }
}
