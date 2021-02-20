package com.example.gonggong;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.kakao.sdk.auth.LoginClient;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.User;
import kotlin.Unit;
import kotlin.jvm.functions.Function2;


public class MainActivity extends Activity implements View.OnClickListener {

    private Button mButtonKakaoLogin;
    private TextView kakaonickname;
    private ImageView kakaoprofileImage;
    private static final String TAG = "MainActivity";
    Function2<OAuthToken, Throwable, Unit> callback = new Function2<OAuthToken, Throwable, Unit>() { //function2 형태가 로그인
        @Override
        public Unit invoke(OAuthToken oAuthToken, Throwable throwable) { //Token이 null이면 실패
            if (oAuthToken != null) {
                //TBD 로그인이 되었으니 수행할일 여기에 작성
            }
            if (throwable != null) {
                //TBD 실패하면 오류 수행할일 여기에 작성
            }
            updateKakaoLoginUi();
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        init();
        updateKakaoLoginUi();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.mButtonKakaoLogin:
                Log.i("GongGong", "카카오 로그인");
                if (LoginClient.getInstance().isKakaoTalkLoginAvailable(MainActivity.this)) { //카카오 설치되어있는지 확인
                    LoginClient.getInstance().loginWithKakaoTalk(MainActivity.this, callback);
                } else { //카톡이 설치되어있지 않을시
                    LoginClient.getInstance().loginWithKakaoAccount(MainActivity.this, callback);
                }
                break;

            default:
                break;
        }
    }

    private void init() {

        //kakao
        mButtonKakaoLogin = (Button) findViewById(R.id.mButtonKakaoLogin);
        mButtonKakaoLogin.setOnClickListener(this);
        kakaonickname = findViewById(R.id.nickname);
        kakaoprofileImage = findViewById(R.id.profile);


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
                    kakaonickname.setText(user.getKakaoAccount().getProfile().getNickname());
                    Glide.with(kakaoprofileImage).load(user.getKakaoAccount().getProfile().getThumbnailImageUrl()).circleCrop().into(kakaoprofileImage);

                    mButtonKakaoLogin.setVisibility(View.VISIBLE);
                } else { //로그아웃 상태
                    mButtonKakaoLogin.setVisibility(View.VISIBLE);
                    kakaonickname.setText(null);
                    kakaoprofileImage.setImageBitmap(null);

                }
                return null;
            }
        });
    }
}