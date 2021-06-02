package com.example.gonggong;

import android.content.Intent;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.example.gonggong.ui.home.HomeFragment;
import com.example.gonggong.ui.story.StoryFragment;
import com.example.gonggong.ui.calender.CalenderFragment;
import com.example.gonggong.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    HomeFragment fragment1;
    StoryFragment fragment2;
    CalenderFragment fragment3;
    ProfileFragment fragment4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.nav_view);

        Intent intent = getIntent();
        String USER_NICKNAME = intent.getStringExtra("nickname");
        Toast.makeText(getApplicationContext(), USER_NICKNAME + " 님 어서오세요.", Toast.LENGTH_SHORT).show();

        //프래그먼트 생성
        fragment1 = new HomeFragment();
        fragment2 = new StoryFragment();
        fragment3 = new CalenderFragment();
        fragment4 = new ProfileFragment();

        //처음에 띄울화면 이걸로 기릿~
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment1).commitAllowingStateLoss();

        //bottomnavigationview의 아이콘을 선택 했을때 원하는 프래그먼트가 띄워질 수 있도록 리스터 추가
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.frag_navigation_home: {
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment1).commitAllowingStateLoss();
                        return true;
                    }
                    case R.id.frag_navigation_story: {
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment2).commitAllowingStateLoss();
                        return true;
                    }
                    case R.id.frag_navigation_calender: {
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment3).commitAllowingStateLoss();
                        return true;
                    }
                    case R.id.frag_navigation_profile: {
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, fragment4).commitAllowingStateLoss();
                        return true;
                    }

                    default:return false;
                }
            }
        });
    }
}