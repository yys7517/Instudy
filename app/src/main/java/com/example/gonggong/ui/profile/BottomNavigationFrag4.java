package com.example.gonggong.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import com.example.gonggong.LoginActivity;
import com.example.gonggong.R;

public class BottomNavigationFrag4 extends Fragment {
    private ImageView edit_profile;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_bottomnavigationfrag4, container, false);


        edit_profile = (ImageView) rootView.findViewById(R.id.account_cog);
        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntentLogin = new Intent(getActivity(), ProfileEdit.class);
                startActivity(mIntentLogin);
            }
        });
        return rootView;
    }
}
