package com.example.gonggong.Intro;

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
                return new FragmentIntroA();

            case 1:
                return new FragmentIntroB();

            case 2:
                return new FragmentIntroC();

            default:
                return new FragmentIntroD();
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
