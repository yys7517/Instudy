package com.example.gonggong.ui.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.service.autofill.UserData;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gonggong.LoginActivity;
import com.example.gonggong.MainActivity;
import com.example.gonggong.R;
import com.example.gonggong.ui.home.HomeAdapter;
import com.example.gonggong.ui.home.HomeData;
import com.example.gonggong.ui.home.HomeFragment;
import com.example.gonggong.ui.story.StoryActivity;

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

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView recyclerView;
    private ImageView edit_profile;
    private SharedPreferences appData;
    private String sharedNickname;      //App 사용자 닉네임
    private String user_id;             //App 사용자 ID
    private String user_introduce; //소개

    private static String IP_ADDRESS = "211.211.158.42/instudy";
    private static String TAG = "프로필 게시물";

    private HomeAdapter adapter;
    private ArrayList<HomeData> mSearchData = new ArrayList<>();

    private String mJsonString; // JSON 파싱 값을 받아서 임시로 담는 공간.

    //View
    private CircleImageView userimg;
    private String profileurl;
    TextView usernick, userintroduce, following, follower;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_profile, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.scrollToPosition(0);
        adapter = new HomeAdapter(mSearchData);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        appData = getActivity().getSharedPreferences("appData", MODE_PRIVATE);

        user_id = appData.getString("ID","");

        usernick = (TextView) rootView.findViewById(R.id.txt_profile_name); //닉네임
        userintroduce = (TextView) rootView.findViewById(R.id.txt_profile_intoduce); //소개
        following = (TextView) rootView.findViewById(R.id.txt_profile_following_number);
        follower = (TextView) rootView.findViewById(R.id.txt_profile_follower_number);


        edit_profile = (ImageView) rootView.findViewById(R.id.account_cog);

        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(getActivity(), ProfileEdit.class);
                mIntent.putExtra("nickname",usernick.getText().toString());
                mIntent.putExtra("introduce",userintroduce.getText().toString());

                Log.d("intent","유저 닉 : "+usernick.getText().toString());
                Log.d("intent","유저 소개 : "+userintroduce.getText().toString());

                startActivity(mIntent);
            }
        });

        //커스텀 리스터 객체 생성 및 전달.
        adapter.setOnItemClickListener(new HomeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                // TODO : 아이템 클릭 이벤트를 플레그먼트에서 처뤼

                Intent intent = new Intent(getActivity(), StoryActivity.class);

                intent.putExtra("userid",mSearchData.get(position).getUserid());
                intent.putExtra("post_code",mSearchData.get(position).getCode());

                intent.putExtra("nickname", mSearchData.get(position).getNickname());
                intent.putExtra("contents", mSearchData.get(position).getContents());
                intent.putExtra("date", mSearchData.get(position).getDate());

                startActivity(intent);


            }
        });
        return rootView;
    }

    private void getSharedLoad() {
        user_id = appData.getString("ID", "");
        sharedNickname = appData.getString("NICKNAME", "");
        Log.i("Id 가져오나 확인", "onCreateView : "+ user_id);
        Log.i("Nickname 가져오나 확인", "onCreateView : "+ sharedNickname);
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
                profileurl = showResult();   //  showResult 메소드 실행

                Intent Intent = new Intent(getActivity(), ProfileEdit.class);
                Log.d("intent","프로필 url : "+profileurl);
                Intent.putExtra("profileurl",profileurl);

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

    private String showResult() {

        String TAG_JSON = "UserImg Table";
        String TAG_UserID = "UserID";
        String TAG_UserNickName = "UserNickName";
        String TAG_Introduce = "UserIntroduce";
        String TAG_UserProfile = "UserProfile";

        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for (int i = 0; i<jsonArray.length(); i++) {

                JSONObject item = jsonArray.getJSONObject(i);

                String POST_ID = item.getString(TAG_UserID);

                if ( POST_ID.equals(user_id) ) { //ID가 같은거만 담음
                    String POST_UserNickName = item.getString(TAG_UserNickName);
                    String POST_Introduce = item.getString(TAG_Introduce);
                    String POST_UserProfile = item.getString(TAG_UserProfile);

                    Log.i("유저 프로필 경로", "onCreateView : "+ POST_UserProfile);
                    Log.i("유저 NICK", "onCreateView : "+ POST_UserNickName);
                    Log.i("유저 소개", "onCreateView : "+ POST_Introduce);


                    usernick.setText( POST_UserNickName );
                    userintroduce.setText( POST_Introduce );

                    profileurl = POST_UserProfile;
                }

            }

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }
        return profileurl;
    }
    // 값 가져오는 클래스
    private class MyBoard extends AsyncTask<String, Void, String> {

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


                String POST_WID = item.getString(TAG_POST_WID);

                if( POST_WID.equals( user_id ) ) {
                    String POST_CODE = item.getString(TAG_CODE);
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


            }

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

    public void PostUpdate() {      //게시글 새로고침 메소드
        mSearchData.clear();        //게시글 목록 clear
        adapter.notifyDataSetChanged();
        MyBoard task = new MyBoard();
        task.execute("http://" + IP_ADDRESS + "/GetImgExample.php", "");

    }

    public void UserUpdate() {
        GetData task = new GetData();
        task.execute("http://" + IP_ADDRESS + "/User.php", "");
    }

    @Override
    public void onResume() {
        super.onResume();
        getSharedLoad();        // Shared값 변경되었을 수 있으니 최신화하기
        PostUpdate();
        UserUpdate();           // 내 게시글 정보 최신화
    }
}