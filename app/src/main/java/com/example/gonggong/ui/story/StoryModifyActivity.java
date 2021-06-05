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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.gonggong.R;
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

public class StoryModifyActivity extends AppCompatActivity {

    private String spostcode;   //게시글 코드
    private ImageView boardimg, backspace;         //게시글 이미지
    private ImageButton imgselect;      //이미지 선택버튼 아직 미구현
    private EditText edittextcontents;  //게시글 수정
    private Button complete;            //완료버튼
    private static String TAG = "게시글 보기";
    private static String IP_ADDRESS = "211.211.158.42";
    //SharedPreferences
    private SharedPreferences appData;
    private String mJsonString;
    private String userid;

    //임시 변수
    String USER_CODE, USER_CONTENTS;


    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_modify);

        Intent intent = getIntent();
        spostcode = intent.getStringExtra("code");


        PostUpdate();
        boardimg = (ImageView) findViewById(R.id.selectimgView);
        edittextcontents = (EditText) findViewById(R.id.mEditTextContents);

        //완료버튼
        complete = (Button) findViewById(R.id.mButtonSubmit);
        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                USER_CODE = spostcode;
                USER_CONTENTS = edittextcontents.getText().toString();
                Log.i(TAG, "게시물 CODE : "+ USER_CODE);
                Log.i(TAG, "CONTENTS 내용 : "+ USER_CONTENTS);


                EditData task = new EditData();
                task.execute("http://" + IP_ADDRESS + "/instudy/PostModifyAndroid.php", USER_CODE, USER_CONTENTS);
                Toast.makeText(getApplicationContext(), "게시글이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }
        });



        backspace = (ImageView) findViewById(R.id.backspace); //뒤로가기
        backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //프로필 이미지 선택&변경
        imgselect = (ImageButton) findViewById(R.id.imgselect);
        imgselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //이미지를 선택
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), 0);
            }
        });

        //SharedPreferences
        appData = getSharedPreferences("appData", MODE_PRIVATE);
        String userid = appData.getString("ID", ""); // App 사용자 ID
    }

    // 값 가져오는 클래스
    private class GetData extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(StoryModifyActivity.this, "Please Wait", null, true, true);
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

                    edittextcontents.setText(POST_CONTENTS);
                    spostcode = POST_CODE;

                    FirebaseStorage storage = FirebaseStorage.getInstance("gs://gonggong-60888.appspot.com");
                    StorageReference storageRef = storage.getReference();
                    storageRef.child(POST_IMGPATH).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            //이미지 로드 성공시
                            Glide.with(getApplication()).load(uri).into(boardimg);   //게시글 사진
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

    //게시글 변경 클래스
    class EditData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(StoryModifyActivity.this, "Please Wait", null, true, true);
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
            String USER_CONTENTS = (String) params[2];


            String postParameters = "PostCode=" + USER_CODE + "&PostContent=" + USER_CONTENTS;

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
