package com.example.gonggong.ui.story;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.gonggong.R;

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

    private ArrayList<ReviewData> mSearchData = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    private static String IP_ADDRESS = "211.211.158.42/yongrun/svm";
    private static String TAG = "phptest";
    private String mJsonString;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

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
                PostUpdate();
                swipeRefreshLayout.setRefreshing(false); //새로고침표시 없애기
            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();
        PostUpdate();
    }

    public void PostUpdate() {
        mSearchData.clear();
        adapter.notifyDataSetChanged();
        GetData task = new GetData();
        task.execute("http://" + IP_ADDRESS + "/POST.php", "");
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
                showResult();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getApplicationContext(), "Please Wait", null, true, true);
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
    private void showResult() {

        String TAG_JSON = "POST_DATA";
        String TAG_ANSWER_CONTENTS = "POST_ANSWER_CONTENTS";
        String TAG_ANSWER_DATE = "POST_ANSWER_DATE";
        String TAG_POST_ID = "POST_ID";


        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for (int i = jsonArray.length()-1; i>=0; i--) {

                JSONObject item = jsonArray.getJSONObject(i);

                String POST_NICKNAME = item.getString(TAG_POST_ID);
                String POST_DATE = item.getString(TAG_ANSWER_CONTENTS);
                String POST_CONTENTS = item.getString(TAG_ANSWER_DATE);

                ReviewData reviewData = new ReviewData();

                reviewData.setNickname(POST_NICKNAME); // 게시글 작성자
                reviewData.setDate(POST_DATE); // 게시글 작성 날짜
                reviewData.setContents(POST_CONTENTS); // 게시글 내용

                mSearchData.add(reviewData);
                adapter.notifyDataSetChanged();
            }

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }
}
