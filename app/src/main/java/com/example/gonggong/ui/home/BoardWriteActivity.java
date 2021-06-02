package com.example.gonggong.ui.home;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gonggong.R;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BoardWriteActivity extends AppCompatActivity {

    private String POST_TITLE, POST_NICKNAME, POST_ID ,POST_CONTENTS;       //게시글 작성 PHP 제목, 닉네임, ID, 내용 인자 값

    private static String IP_ADDRESS = "211.211.158.42";
    private static String TAG = "SmartVendingMachine";


    EditText mEditTextTitle, mEditTextContents;
    TextView mTextViewPostResult;
    Button mButtonSubmit;
    ImageView backspace;

    //SharedPreferences
    private SharedPreferences appData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.board_write);
//
//
//        mEditTextTitle = (EditText) findViewById(R.id.mEditTextTitle);
//        mEditTextContents = (EditText) findViewById(R.id.mEditTextContents);
//
//        mTextViewPostResult = (TextView) findViewById(R.id.mTextViewPostResult);
//
//        mButtonSubmit = (Button) findViewById(R.id.mButtonSubmit);

        backspace = findViewById(R.id.backspace);



        //SharedPreferences
        appData = getSharedPreferences("appData", MODE_PRIVATE);

        String userid = appData.getString("ID", ""); // App 사용자 ID
        String user_nickname = appData.getString("NICKNAME", "");

        //건의사항 작성 완료 버튼.
        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                POST_ID = userid;       // APP 사용자 ID 값 작성자 ID값으로 인자 값 넘겨주기
                POST_TITLE = mEditTextTitle.getText().toString();
                POST_NICKNAME = user_nickname;  // APP 사용자 닉네임 값 작성자 닉네임 값으로 인자 값 넘겨주기
                POST_CONTENTS = mEditTextContents.getText().toString();
                InsertData task = new InsertData();
                task.execute("http://" + IP_ADDRESS + "/yongrun/svm/POST_WRITE_ANDROID.php", POST_TITLE, POST_ID ,POST_NICKNAME, POST_CONTENTS);
                Toast.makeText(getApplicationContext(), "건의사항이 등록되었습니다.", Toast.LENGTH_SHORT).show();

                finish();
            }
        });

        //뒤로가기 버튼
        backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(BoardWriteActivity.this, "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            mTextViewPostResult.setText(result);
            Log.d(TAG, "POST response  - " + result);
        }

        @Override
        protected String doInBackground(String... params) {

            String POST_TITLE = (String) params[1];
            String POST_ID = (String) params[2];
            String POST_NICKNAME = (String) params[3];
            String POST_CONTENTS = (String) params[4];
            String serverURL = (String) params[0];

            String postParameters = "POST_TITLE=" + POST_TITLE + "&POST_ID=" + POST_ID +"&POST_NICKNAME=" + POST_NICKNAME + "&POST_CONTENTS=" + POST_CONTENTS;

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