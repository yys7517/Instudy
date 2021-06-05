package com.example.gonggong;

import android.content.Intent;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.widget.Toast;

import com.example.gonggong.ui.home.HomeFragment;
import com.example.gonggong.ui.story.StoryFragment;
import com.example.gonggong.ui.calender.CalenderFragment;
import com.example.gonggong.ui.profile.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment;
    StoryFragment storyFragment;
    CalenderFragment calenderFragment;
    ProfileFragment profileFragment;

    private final long FINISH_INTERVAL_TIME = 2000;
    private long   backPressedTime = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.nav_view);

        //프래그먼트 생성


        //처음에 띄울화면 이걸로 기릿~
        homeFragment = new HomeFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, homeFragment,"home").commitAllowingStateLoss();

        // 바텀 내비게이션 뷰 초기 선택 값 (홈)
        bottomNavigationView.setSelectedItemId(R.id.frag_navigation_home);

        //bottomnavigationview의 아이콘을 선택 했을때 원하는 프래그먼트가 띄워질 수 있도록 리스터 추가
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction transaction = fm.beginTransaction();
                switch (menuItem.getItemId()) {
                    case R.id.frag_navigation_home: {
                        fm.popBackStack("home",FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        homeFragment = new HomeFragment();
                        transaction.replace(R.id.nav_host_fragment, homeFragment,"home");
                        transaction.addToBackStack("home");
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        transaction.commit();
                        transaction.isAddToBackStackAllowed();
                        return true;
                    }
                    case R.id.frag_navigation_story: {
                        fm.popBackStack("story",FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        storyFragment = new StoryFragment();
                        transaction.replace(R.id.nav_host_fragment, storyFragment,"story");
                        transaction.addToBackStack("story");
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        transaction.commit();
                        transaction.isAddToBackStackAllowed();
                        return true;
                    }
                    case R.id.frag_navigation_calender: {
                        fm.popBackStack("calender",FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        calenderFragment = new CalenderFragment();
                        transaction.replace(R.id.nav_host_fragment, calenderFragment,"calender");
                        transaction.addToBackStack("calender");
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        transaction.commit();
                        transaction.isAddToBackStackAllowed();
                        return true;
                    }
                    case R.id.frag_navigation_profile: {
                        fm.popBackStack("profile",FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        profileFragment = new ProfileFragment();
                        transaction.replace(R.id.nav_host_fragment, profileFragment,"profile");
                        transaction.addToBackStack("profile");
                        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        transaction.commit();
                        transaction.isAddToBackStackAllowed();
                        return true;
                    }

                    default:return false;
                }
            }
        });
    }

    // 화면 전환 시 바텀 내비게이션 바 메뉴 선택 정보 갱신
    public void updateBottomMenu (BottomNavigationView navigation) {
        if (getSupportFragmentManager().findFragmentByTag("home") != null && getSupportFragmentManager().findFragmentByTag("home").isVisible()) {
            bottomNavigationView.getMenu().findItem(R.id.frag_navigation_home).setChecked(true);
        } else if (getSupportFragmentManager().findFragmentByTag("story") != null && getSupportFragmentManager().findFragmentByTag("story").isVisible()) {
            bottomNavigationView.getMenu().findItem(R.id.frag_navigation_story).setChecked(true);
        } else if (getSupportFragmentManager().findFragmentByTag("calender") != null && getSupportFragmentManager().findFragmentByTag("calender").isVisible()) {
            bottomNavigationView.getMenu().findItem(R.id.frag_navigation_calender).setChecked(true);
        } else if (getSupportFragmentManager().findFragmentByTag("profile") != null && getSupportFragmentManager().findFragmentByTag("profile").isVisible()) {
            bottomNavigationView.getMenu().findItem(R.id.frag_navigation_profile).setChecked(true);
        }
    }

    //뒤로가기 버튼 눌렀을 때
    @Override
    public void onBackPressed() {



        long tempTime = System.currentTimeMillis();
        long intervalTime = tempTime - backPressedTime;

        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            if (!(0 > intervalTime || FINISH_INTERVAL_TIME < intervalTime)) {
                finishAffinity();
                System.runFinalization();
                System.exit(0);
            } else {
                backPressedTime = tempTime;
                Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        super.onBackPressed();
        BottomNavigationView bnv = findViewById(R.id.nav_view);
        updateBottomMenu(bnv);
    }

    @Override
    public void supportFinishAfterTransition() {
        ActivityCompat.finishAfterTransition(this);
    }
}