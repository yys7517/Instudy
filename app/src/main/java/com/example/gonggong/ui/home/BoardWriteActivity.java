package com.example.gonggong.ui.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.gonggong.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BoardWriteActivity extends AppCompatActivity {

    private String POST_NICKNAME, POST_ID, POST_CONTENTS, POST_IMGPATH;       //게시글 작성 PHP 제목, 닉네임, ID, 내용 인자 값
    private String GET_POST_CODE;
    private String mJsonString; // JSON 파싱 값을 받아서 임시로 담는 공간.

    private String UPLOAD_ID, UPLOAD_CONTENTS, UPLOAD_CODE;
    private String GET_CODE;
    private String userid, user_nickname;

    private static String IP_ADDRESS = "211.211.158.42";
    private static String TAG = "instudy";


    EditText mEditTextContents;
    TextView mTextViewPostResult;
    Button mButtonSubmit;
    ImageView backspace, imgselect, selectimgview;

    private Uri filePath;

    //SharedPreferences
    private SharedPreferences appData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board_write);
        mEditTextContents = (EditText) findViewById(R.id.mEditTextContents);
        mTextViewPostResult = (TextView) findViewById(R.id.mTextViewPostResult);
        mButtonSubmit = (Button) findViewById(R.id.mButtonSubmit);
        backspace = (ImageView) findViewById(R.id.backspace);
        imgselect = (ImageView) findViewById(R.id.imgselect);
        selectimgview = (ImageView) findViewById(R.id.selectimgView);

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

        userid = appData.getString("ID", ""); // App 사용자 ID
        user_nickname = appData.getString("NICKNAME", "");

        //건의사항 작성 완료 버튼.
        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                POST_ID = userid;       // APP 사용자 ID 값 작성자 ID값으로 인자 값 넘겨주기
                POST_NICKNAME = user_nickname;  // APP 사용자 닉네임 값 작성자 닉네임 값으로 인자 값 넘겨주기
                POST_CONTENTS = mEditTextContents.getText().toString();
                InsertData task = new InsertData();
                task.execute("http://" + IP_ADDRESS + "/instudy/PostWriteAndroid.php", POST_ID, POST_NICKNAME, POST_CONTENTS);

                UPLOAD_ID = POST_ID;
                UPLOAD_CONTENTS = POST_CONTENTS;

                Log.d("업로드", "WHO:" +UPLOAD_ID);
                Log.d("업로드", "WHAT:" +UPLOAD_CONTENTS);


                GetPostCode getPostCode = new GetPostCode();
                getPostCode.execute("http://" + IP_ADDRESS + "/instudy/Post.php", "");

                Toast.makeText(getApplicationContext(), "게시글이 등록되었습니다.", Toast.LENGTH_SHORT).show();
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

    //결과 처리
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //request코드가 0이고 OK를 선택했고 data에 뭔가가 들어 있다면
        if(requestCode == 0 && resultCode == RESULT_OK){
            filePath = data.getData();
            Log.d(TAG, "uri:" + String.valueOf(filePath));
            try {
                //Uri 파일을 Bitmap으로 만들어서 ImageView에 집어 넣는다.
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(BoardWriteActivity.this.getContentResolver(), filePath);
                selectimgview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //upload the file
    private void uploadFile() {
        //업로드할 파일이 있으면 수행
        if (filePath != null) {
            //업로드 진행 Dialog 보이기
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("업로드중...");
            progressDialog.show();

            //storage
            FirebaseStorage storage = FirebaseStorage.getInstance();

            //Unique한 파일명을 만들자.
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMHH_mmss");
            Date now = new Date();
            String filename = formatter.format(now) + ".png";
            //storage 주소와 폴더 파일명을 지정해 준다.
            StorageReference storageRef = storage.getReferenceFromUrl("gs://gonggong-60888.appspot.com").child("images/" + filename);

            POST_IMGPATH = "images/" + filename;

            //올라가거라...
            storageRef.putFile(filePath)
                    //성공시
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss(); //업로드 진행 Dialog 상자 닫기
                            Toast.makeText(getApplicationContext(), "업로드 완료!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    //실패시
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    })
                    //진행중
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            @SuppressWarnings("VisibleForTests") //이걸 넣어 줘야 아랫줄에 에러가 사라진다. 넌 누구냐?
                            double progress = (100 * taskSnapshot.getBytesTransferred()) /  taskSnapshot.getTotalByteCount();
                            //dialog에 진행률을 퍼센트로 출력해 준다
                            progressDialog.setMessage("Uploaded " + ((int) progress) + "% ...");

                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
        }
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

            String serverURL = (String) params[0];
            String USER_ID = (String) params[1];
            String USER_NICKNAME = (String) params[2];
            String USER_CONTENTS = (String) params[3];

            String postParameters = "PostWID=" + USER_ID + "&PostNickName=" + USER_NICKNAME +
                    "&PostContent=" + USER_CONTENTS;

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
    // 값 가져오는 클래스
    private class GetPostCode extends AsyncTask<String, Void, String> {

        ProgressDialog progressDialog;
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(BoardWriteActivity.this, "Please Wait", null, true, true);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();

            Log.d(TAG, "코드 가져오자 - " + result);

            if (result == null) {

            } else {

                mJsonString = result;
                Log.d("업로드", "업로드 아이디 :" +UPLOAD_ID);
                Log.d("업로드", "업로드 내용 :" +UPLOAD_CONTENTS);

                UPLOAD_CODE = showBoard();   //  showBoard 메소드 실행
                Log.d("게시글 코드 가져오기", "GET_CODE:" +UPLOAD_CODE);

                uploadFile();           // 이미지 업로드 + 이미지 경로 가져오기 POST_IMGPATH에 넣기.

                Log.d("업로드", "이미지 경로:" +POST_IMGPATH);      // 여기까진 받아온다.

                InsertImage image = new InsertImage();
                image.execute("http://" + IP_ADDRESS + "/instudy/PostImageUploadAndroid.php", UPLOAD_CODE, POST_IMGPATH);
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

    private String showBoard() {

        String TAG_JSON = "Post";
        String TAG_CODE = "PostCode";
        String TAG_POST_WID = "PostWID";
        String TAG_NICKNAME = "PostNickName";
        String TAG_CONTENTS = "PostContent";



        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            for (int i = 0; i<jsonArray.length(); i++) {

                JSONObject item = jsonArray.getJSONObject(i);

                String POST_CONTENTS = item.getString(TAG_CONTENTS);
                String POST_WID = item.getString(TAG_POST_WID);
                String POST_NICKNAME = item.getString(TAG_NICKNAME);

                if( UPLOAD_CONTENTS.equals( POST_CONTENTS ) && POST_NICKNAME.equals(user_nickname) && POST_WID.equals( userid ) )   //업로드 아이디랑 업로드 내용이랑 같은 게시글의 코드 가져오기 ( 기준 ㅈ같다 ㅋㅋ )
                {
                    String POST_CODE = item.getString(TAG_CODE);
                    GET_CODE = POST_CODE;
                }
                else
                    continue;
            }
            UPLOAD_CODE = GET_CODE;

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }
        return UPLOAD_CODE;
    }


    class InsertImage extends AsyncTask<String, Void, String> {
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

            String serverURL = (String) params[0];
            String POST_CODE = (String) params[1];
            String IMG_PATH = (String) params[2];

            String postParameters = "&PostCode=" + POST_CODE + "&PostImgPath=" + IMG_PATH;

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