package com.example.gonggong;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.kakao.sdk.auth.LoginClient;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;


public class LoginActivity extends Activity implements View.OnClickListener {

    private FloatingActionButton mButtonKakao, mButtonNaver, mButtonFacebook;
    private ViewPager2 mViewPager2;


    private static final String TAG = "MainActivity";
    Function2<OAuthToken, Throwable, Unit> callback = new Function2<OAuthToken, Throwable, Unit>() { //function2 형태가 로그인
        @Override
        public Unit invoke(OAuthToken oAuthToken, Throwable throwable) { //Token이 null이면 실패
            if (oAuthToken != null) {
                //TBD 로그인이 되었으니 수행할일 여기에 작성
                Log.i("로그인","로그인 되었습니다");
            }
            if (throwable != null) {
                //TBD 실패하면 오류 수행할일 여기에 작성
                Log.i("로그인","로그인에 실패하였습니다");
            }
            updateKakaoLoginUi();
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        init();
        updateKakaoLoginUi();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mButtonKakao:
                Log.i("GongGong", "카카오 로그인");
                if (LoginClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this)) { //카카오 설치되어있는지 확인
                    LoginClient.getInstance().loginWithKakaoTalk(LoginActivity.this, callback);
                } else { //카톡이 설치되어있지 않을시
                    LoginClient.getInstance().loginWithKakaoAccount(LoginActivity.this, callback);
                }
                break;

            case R.id.mButtonNaver:
                Log.i("GongGong", "네이버 로그인");
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;

            case R.id.mButtonFacebook:
                Log.i("GongGong", "페이스북 로그인");
                break;

            default:
                break;
        }
    }

    private void init() {

        //kakao
        mButtonKakao = (FloatingActionButton) findViewById(R.id.mButtonKakao);
        mButtonKakao.setOnClickListener(this);

        //naver
        mButtonNaver = (FloatingActionButton) findViewById(R.id.mButtonNaver);
        mButtonNaver.setOnClickListener(this);

        //facebook
        mButtonFacebook = (FloatingActionButton) findViewById(R.id.mButtonFacebook);
        mButtonFacebook.setOnClickListener(this);
    }

    private void updateKakaoLoginUi() {
        UserApiClient.getInstance().me(new Function2<User, Throwable, Unit>() {
            @Override
            public Unit invoke(User user, Throwable throwable) {
                if (user != null) { //로그인이 되었을시

                    Log.i(TAG, "invoke: id=" + user.getId());
                    Log.i(TAG, "invoke: email=" + user.getKakaoAccount().getEmail());
                    Log.i(TAG, "invoke: nickname=" + user.getKakaoAccount().getProfile().getNickname());
                    Log.i(TAG, "invoke: gender=" + user.getKakaoAccount().getGender());
                    Log.i(TAG, "invoke: age=" + user.getKakaoAccount().getAgeRange());

                } else { //로그아웃 상태

                }
                return null;
            }
        });
    }
}