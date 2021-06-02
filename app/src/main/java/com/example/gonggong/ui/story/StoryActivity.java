package com.example.gonggong.ui.story;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.gonggong.R;

public class StoryActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView storybackimg, comment;
    TextView commentcount, detailtitle, detailnick, detaildata, maintext;


    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        storybackimg = (ImageView) findViewById(R.id.storyBackImg); //뒤로가기
        comment = (ImageView) findViewById(R.id.imgCommentIcon); //댓글화면으로 인텐트
        commentcount = (TextView) findViewById(R.id.txtCommentCount); //댓글화면으로 인텐트2
        detailtitle = (TextView) findViewById(R.id.txtDetailTitle); //제목
        detailnick = (TextView) findViewById(R.id.txtDetailNick); //닉네임
        detaildata = (TextView) findViewById(R.id.txtDetailDate); //작성 날짜
        maintext = (TextView) findViewById(R.id.txtMain); //글 내용

    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.storyBackImg: //뒤로가기 버튼
                finish();
                break;

            case R.id.comment: //댓글이미지 버튼
                Intent intent = new Intent(getApplicationContext(),ReviewActivity.class);
                startActivityForResult(intent,100);//액티비티 띄우기
                break;
        }
    }
}
