package com.example.gonggong.ui.story;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.gonggong.R;
import com.example.gonggong.ui.home.HomeAdapter;
import com.example.gonggong.ui.home.HomeData;
import com.example.gonggong.ui.home.HomeFragment;
import com.example.gonggong.ui.profile.ProfileEdit;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class StoryActivity extends AppCompatActivity {

    private String suserid;     //게시글 작성 사용자 ID
    private String spostcode;   //게시글 코드

    private static String TAG = "게시글 보기";

    private String mJsonString;

    //SharedPreferences
    private SharedPreferences appData;

    private int CommentCount = 0;

    private static String IP_ADDRESS = "211.211.158.42";

    ImageView storybackimg, comment, imgPost;
    TextView commentcount, detailtitle, detailnick, detaildate, maintext;
    Button rebutton, deletebtn;
    private String userid;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);

        storybackimg = (ImageView) findViewById(R.id.storyBackImg); //뒤로가기

        storybackimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //삭제버튼
        deletebtn = (Button) findViewById(R.id.mButtonDelete);
        deletebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String USER_CODE = spostcode;
                Log.i(TAG, "게시물 CODE : " + USER_CODE);

                DeleteData task = new DeleteData();
                task.execute("http://" + IP_ADDRESS + "/instudy/PostDeleteAndroid.php", USER_CODE);
                Toast.makeText(getApplicationContext(), "게시글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


        comment = (ImageView) findViewById(R.id.imgCommentIcon); //댓글화면으로 인텐트
        commentcount = (TextView) findViewById(R.id.txtCommentCount); //댓글화면으로 인텐트2
        detailnick = (TextView) findViewById(R.id.txtDetailNick); //닉네임
        detaildate = (TextView) findViewById(R.id.txtDetailDate); //작성 날짜
        maintext = (TextView) findViewById(R.id.txtMain); //글 내용
        imgPost = (ImageView) findViewById(R.id.imgPost);  // 글 사진

        Intent intent = getIntent();

        suserid = intent.getStringExtra("userid");
        spostcode = intent.getStringExtra("post_code");


        commentcount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoryActivity.this, ReviewActivity.class);
                intent.putExtra("post_code", spostcode);
                intent.putExtra("user_id", suserid);


                Log.d("intent", "코드 값 : " + spostcode);
                Log.d("intent", "아이디 값: " + suserid);


                startActivity(intent);
            }
        });

        comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StoryActivity.this, ReviewActivity.class);
                intent.putExtra("post_code", spostcode);
                intent.putExtra("user_id", suserid);


                Log.d("intent", "코드 값" + spostcode);
                Log.d("intent", "아이디 값" + suserid);


                startActivity(intent);
            }
        });

        //SharedPreferences
        appData = getSharedPreferences("appData", MODE_PRIVATE);
        userid = appData.getString("ID", ""); // App 사용자 ID


        rebutton = (Button) findViewById(R.id.rebutton);
        rebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent2 = new Intent(StoryActivity.this, StoryModifyActivity.class);
                intent2.putExtra("code", spostcode);
                startActivity(intent2);
            }
        });

        if (suserid.equals(userid)) {
            rebutton.setVisibility(View.VISIBLE);
            deletebtn.setVisibility(View.VISIBLE);
        } else {
            rebutton.setVisibility(View.INVISIBLE);
            deletebtn.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        PostUpdate();
        CommentUpdate();
    }

    // 값 가져오는 클래스
    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(StoryActivity.this, "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            Log.d(TAG, "response - " + result);

            if (result == null) {

            } else {

                mJsonString = result;
                showBoard();   //  showBoard 메소드 실행
            }
        }


        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = params[1];

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }

    private void showBoard() {

        String TAG_JSON = "Post Table";
        String TAG_CODE = "PostCode";
        String TAG_POST_WID = "PostWID";
        String TAG_NICKNAME = "PostNickName";
        String TAG_DATE = "PostDate";
        String TAG_CONTENTS = "PostContent";
        String TAG_IMGPATH = "PostImgPath";


        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for (int i = jsonArray.length() - 1; i >= 0; i--) {

                JSONObject item = jsonArray.getJSONObject(i);

                String POST_CODE = item.getString(TAG_CODE);

                if (POST_CODE.equals(spostcode)) {
                    String POST_WID = item.getString(TAG_POST_WID);
                    String POST_NICKNAME = item.getString(TAG_NICKNAME);
                    String POST_CONTENTS = item.getString(TAG_CONTENTS);
                    String POST_DATE = item.getString(TAG_DATE);
                    String POST_IMGPATH = item.getString(TAG_IMGPATH);

                    detailnick.setText(POST_NICKNAME);
                    detaildate.setText(POST_DATE);
                    maintext.setText(POST_CONTENTS);

                    spostcode = POST_CODE;
                    suserid = POST_WID;


                    FirebaseStorage storage = FirebaseStorage.getInstance("gs://gonggong-60888.appspot.com");
                    StorageReference storageRef = storage.getReference();
                    storageRef.child(POST_IMGPATH).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //이미지 로드 성공시
                            Glide.with(getApplicationContext()).load(uri).into(imgPost);   //게시글 사진
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            //이미지 로드 실패시
                        }
                    });

                }


            }

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

    public void PostUpdate() {      //게시글 새로고침 메소드
        GetData task = new GetData();
        task.execute("http://" + IP_ADDRESS + "/instudy/GetImgExample.php", "");
    }

    public void CommentUpdate() {      //댓글 개수 가져오는 메소드
        CommentCount = 0;
        GetCommentCount task = new GetCommentCount();
        task.execute("http://" + IP_ADDRESS + "/instudy/PostComment.php", "");
    }


    private class GetCommentCount extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            Log.d(TAG, "response - " + result);

            if (result == null) {

            } else {

                mJsonString = result;
                showComment();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(StoryActivity.this, "Please Wait", null, true, true);
        }

        @Override
        protected String doInBackground(String... params) {

            String serverURL = params[0];
            String postParameters = params[1];


            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }

                bufferedReader.close();

                return sb.toString().trim();


            } catch (Exception e) {

                Log.d(TAG, "GetData : Error ", e);
                errorString = e.toString();

                return null;
            }

        }
    }

    private void showComment() {

        String TAG_JSON = "PostComment Table";
        String TAG_POST_CODE = "PostCode";
        String TAG_COMMENT_WID = "PostCommentWID";
        String TAG_COMMENT_NICK = "PostCommentNickName";
        String TAG_COMMENT_CONTENTS = "PostComment";
        String TAG_COMMENT_DATE = "PostCommentDate";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for (int i = jsonArray.length() - 1; i >= 0; i--) {

                JSONObject item = jsonArray.getJSONObject(i);

                String POST_CODE = item.getString(TAG_POST_CODE);

                if (POST_CODE.equals(spostcode)) {       // 게시글 코드가 일치하는 댓글들만 출력한다.

                    CommentCount++;

                    String COMMENT_WID = item.getString(TAG_COMMENT_WID);       // 댓글 작성자 ID << 이거로 프로필 사진 가져와야함.
                    String COMMENT_NICK = item.getString(TAG_COMMENT_NICK);
                    String COMMENT_CONTENTS = item.getString(TAG_COMMENT_CONTENTS);
                    String COMMENT_DATE = item.getString(TAG_COMMENT_DATE);

                    ReviewData reviewData = new ReviewData();

                    reviewData.setNickname(COMMENT_NICK); // 댓글 작성자 닉네임
                    reviewData.setDate(COMMENT_DATE); // 게시글 작성 날짜
                    reviewData.setContents(COMMENT_CONTENTS); // 게시글 내용


                } else {
                }

            }
            commentcount.setText(String.valueOf(CommentCount));

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

    //게시글 삭제 클래스
    class DeleteData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(StoryActivity.this, "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.d(TAG, "POST response  - " + result);
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = (String) params[0];
            String USER_CODE = (String) params[1];

            String postParameters = "PostCode=" + USER_CODE;

            try {

                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "POST response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                bufferedReader.close();

                return sb.toString();

            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);

                return new String("Error: " + e.getMessage());
            }

        }
    }


}
