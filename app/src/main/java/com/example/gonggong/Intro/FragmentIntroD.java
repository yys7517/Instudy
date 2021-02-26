package com.example.gonggong.Intro;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.gonggong.LoginActivity;
import com.example.gonggong.R;

public class FragmentIntroD extends Fragment {
    private Button mButtonStart;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_introd, container, false);

        mButtonStart = (Button) rootView.findViewById(R.id.mButtonStart);
        mButtonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntentLogin = new Intent(getActivity(), LoginActivity.class);
                startActivity(mIntentLogin);
            }
        });

        return rootView;
    }
}