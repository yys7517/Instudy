package com.example.gonggong;

import android.app.Application;
import com.kakao.sdk.common.KakaoSdk;

public class KakaoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        KakaoSdk.init(this,"cbc15b278157a56287ea87c5ae4b7b82");
    }
}
