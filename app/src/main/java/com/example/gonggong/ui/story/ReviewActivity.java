package com.example.gonggong.ui.story;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.gonggong.R;
import com.example.gonggong.ui.profile.ProfileEdit;

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

public class ReviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewAdapter adapter;
    private RecyclerView.LayoutManager  mLayoutManager;
    private ImageView backspace;

    private String spostcode;

    private ArrayList<ReviewData> mSearchData = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    private static String IP_ADDRESS = "211.211.158.42";
    private static String TAG = "phptest";
    private String mJsonString;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        Intent intent = getIntent();

        spostcode = intent.getStringExtra("post_code");

        backspace = findViewById(R.id.backspace);

        backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.rvComment);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.scrollToPosition(0);
        adapter = new ReviewAdapter(mSearchData);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        swipeRefreshLayout = findViewById(R.id.reviewrefresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                CommentUpdate();
                swipeRefreshLayout.setRefreshing(false); //새로고침표시 없애기
            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();
        CommentUpdate();
    }

    public void CommentUpdate() {
        mSearchData.clear();
        adapter.notifyDataSetChanged();
        GetData task = new GetData();
        task.execute("http://" + IP_ADDRESS + "/instudy/PostComment.php", "");
    }

    //닉네임 변경 클래스
    class EditData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ReviewActivity.this, "Please Wait", null, true, true);
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
            String POST_CODE = (String) params[1];
            String USER_ID = (String) params[2];
            String USER_NICKNAME = (String) params[3];
            String USER_CONTENTS = (String) params[4];


            String postParameters = "PostCode=" + POST_CODE + "&PostCommentWID=" + USER_ID
                    + "&PostCommentNickName=" + USER_NICKNAME  + "&PostComment=" + USER_CONTENTS;

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


    private class GetData extends AsyncTask<String, Void, String> {

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
            progressDialog = ProgressDialog.show(ReviewActivity.this, "Please Wait", null, true, true);
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

            for (int i = jsonArray.length()-1; i>=0; i--) {

                JSONObject item = jsonArray.getJSONObject(i);

                String POST_CODE = item.getString(TAG_POST_CODE);

                if(POST_CODE.equals(spostcode)) {       // 게시글 코드가 일치하는 댓글들만 출력한다.

                    String COMMENT_WID = item.getString(TAG_COMMENT_WID);       // 댓글 작성자 ID << 이거로 프로필 사진 가져와야함.

                    String COMMENT_NICK = item.getString(TAG_COMMENT_NICK);
                    String COMMENT_CONTENTS = item.getString(TAG_COMMENT_CONTENTS);
                    String COMMENT_DATE = item.getString(TAG_COMMENT_DATE);

                    ReviewData reviewData = new ReviewData();

                    reviewData.setNickname(COMMENT_NICK); // 댓글 작성자 닉네임
                    reviewData.setDate(COMMENT_DATE); // 게시글 작성 날짜
                    reviewData.setContents(COMMENT_CONTENTS); // 게시글 내용

                    mSearchData.add(reviewData);
                    adapter.notifyDataSetChanged();
                }
                else {}

            }

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }
}
