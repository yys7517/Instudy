package com.example.gonggong.ui.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.gonggong.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private HomeAdapter adapter,mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<HomeData> mSearchData = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;


    private Uri Post, Profile;
    private static String IP_ADDRESS = "211.211.158.42";
    private static String TAG = "phptest";

    private String mJsonString; // JSON 파싱 값을 받아서 임시로 담는 공간.

    private FloatingActionButton addbtn; //게시물 작성 버튼

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootview = (ViewGroup) inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = (RecyclerView) rootview.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.scrollToPosition(0);
        adapter = new HomeAdapter(mSearchData);

        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        swipeRefreshLayout = rootview.findViewById(R.id.refresh_layout);        // 스와이프 리프레시

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                PostUpdate();       // 게시글 목록 새로고침
                swipeRefreshLayout.setRefreshing(false); //새로고침표시 없애기
            }
        });

        //게시글 작성 플로팅 버튼
        addbtn = (FloatingActionButton) rootview.findViewById(R.id.addBtn);
        addbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("버튼 작동확인", "게시물 추가 버튼 눌렀다.");
                Intent intent = new Intent(getActivity(), BoardWriteActivity.class ); // 게시글 작성 Activity로 이동.
                startActivity(intent);
            }
        });

        return rootview;
    }

    @Override
    public void onResume() {
        super.onResume();
        PostUpdate();
    }


    public void PostUpdate() {      //게시글 새로고침 메소드
        mSearchData.clear();        //게시글 목록 clear
        adapter.notifyDataSetChanged();  // 게시글 목록 갱신
        GetData task = new GetData();
        task.execute("http://" + IP_ADDRESS + "/instudy/GetImgExample.php", "");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    // 값 가져오는 클래스
    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(getActivity(), "Please Wait", null, true, true);
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

            for (int i = jsonArray.length()-1; i>=0; i--) {

                JSONObject item = jsonArray.getJSONObject(i);

                String POST_CODE = item.getString(TAG_CODE);
                String POST_WID = item.getString(TAG_POST_WID);
                String POST_NICKNAME = item.getString(TAG_NICKNAME);
                String POST_CONTENTS = item.getString(TAG_CONTENTS);
                String POST_DATE = item.getString(TAG_DATE);
                String POST_IMGPATH = item.getString(TAG_IMGPATH);


                HomeData boardData = new HomeData();

                boardData.setCode(POST_CODE); // 게시글 코드
                boardData.setImgPost(POST_IMGPATH); // 게시글 사진 경로

                boardData.setNickname(POST_NICKNAME); // 게시글 작성자
                boardData.setDate(POST_DATE); // 게시글 작성 날짜
                boardData.setContents(POST_CONTENTS); // 게시글 내용

                boardData.setUserid(POST_WID);   // 게시글 작성자 ID


                mSearchData.add(boardData);
                adapter.notifyDataSetChanged();

            }

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }




//    private void getProfileImg(String userID) {
//        FirebaseStorage storage = FirebaseStorage.getInstance("gs://gonggong-60888.appspot.com");
//        StorageReference storageRef = storage.getReference();
//
//        ProfileImg task = new ProfileImg();
//        // UserID로 user 테이블에서 프사 경로 가져와서 child()안에 넣기
//        storageRef.child("images/20210523_4740.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                //이미지 로드 성공시
//                HomeData boardData = new HomeData();
//                boardData.setProfileuri(uri);
////
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                //이미지 로드 실패시
////                Toast.makeText(context, "실패", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//
//
//
//    // 프로필 이미지 값 가져오는 클래스
//    private class ProfileImg extends AsyncTask<String, Void, String> {
//
//        ProgressDialog progressDialog;
//        String errorString = null;
//
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//
//            progressDialog = ProgressDialog.show(getActivity(), "Please Wait", null, true, true);
//        }
//
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//
//            progressDialog.dismiss();
//
//            Log.d(TAG, "response - " + result);
//
//            if (result == null) {
//
//            } else {
//
//                mJsonString = result;
//                showProfile();   //  showProfile 메소드 실행
//            }
//        }
//
//
//        @Override
//        protected String doInBackground(String... params) {
//
//            String serverURL = params[0];
//            String postParameters = params[1];
//
//            try {
//
//                URL url = new URL(serverURL);
//                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//
//
//                httpURLConnection.setReadTimeout(5000);
//                httpURLConnection.setConnectTimeout(5000);
//                httpURLConnection.setRequestMethod("POST");
//                httpURLConnection.setDoInput(true);
//                httpURLConnection.connect();
//
//
//                OutputStream outputStream = httpURLConnection.getOutputStream();
//                outputStream.write(postParameters.getBytes("UTF-8"));
//                outputStream.flush();
//                outputStream.close();
//
//
//                int responseStatusCode = httpURLConnection.getResponseCode();
//                Log.d(TAG, "response code - " + responseStatusCode);
//
//                InputStream inputStream;
//                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
//                    inputStream = httpURLConnection.getInputStream();
//                } else {
//                    inputStream = httpURLConnection.getErrorStream();
//                }
//
//
//                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
//                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//
//                StringBuilder sb = new StringBuilder();
//                String line;
//
//                while ((line = bufferedReader.readLine()) != null) {
//                    sb.append(line);
//                }
//
//                bufferedReader.close();
//
//                return sb.toString().trim();
//
//
//            } catch (Exception e) {
//
//                Log.d(TAG, "GetData : Error ", e);
//                errorString = e.toString();
//
//                return null;
//            }
//
//        }
//    }
//    private void showProfile(String PostWID) {
//
//        String TAG_JSON = "Post Table";
//        String TAG_CODE = "PostCode";
//        String TAG_CONTENTS = "PostContent";
//
//
//        try {
//            JSONObject jsonObject = new JSONObject(mJsonString);
//            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
//
//            for (int i = jsonArray.length()-1; i>=0; i--) {
//
//                JSONObject item = jsonArray.getJSONObject(i);
//
//                String POST_WID = item.getString(TAG_POST_WID);
//
//                String POST_CODE = item.getString(TAG_CODE);
//                String POST_CONTENTS = item.getString(TAG_CONTENTS);
//
//
//                HomeData boardData = new HomeData(posturi);
//
//                boardData.setCode(POST_CODE); // 게시글 코드
//                getPostImg(POST_CODE);          // 게시글 코드로 게시글 사진 가져오기
//
//                boardData.setNickname(POST_NICKNAME); // 게시글 작성자
//                boardData.setDate(POST_DATE); // 게시글 작성 날짜
//                boardData.setContents(POST_CONTENTS); // 게시글 내용
//
//                boardData.setUserid(POST_WID);   // 게시글 작성자 ID
//                getProfileImg(POST_WID);        // 유저 아이디로 프사 가져오기
//
//
//                mSearchData.add(boardData);
//                adapter.notifyDataSetChanged();
//
//            }
//
//        } catch (JSONException e) {
//
//            Log.d(TAG, "showResult : ", e);
//        }
//
//    }


}