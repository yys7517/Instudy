package com.example.gonggong;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class FragmentAdapter extends FragmentStateAdapter {

    public int mCount;

    public FragmentAdapter(FragmentActivity fa, int count) {
        super(fa);
        mCount = count;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        int index = getRealPosition(position);

        switch (index) {
            case 0:
                return new FragmentIntroFirst();

            case 1:
                return new FragmentIntroSecond();

            case 2:
                return new FragmentIntroThird();

            default:
                return new FragmentIntroForth();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    public int getRealPosition(int position) {
        return position % mCount;
    }
}
