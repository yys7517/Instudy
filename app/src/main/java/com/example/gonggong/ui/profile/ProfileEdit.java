package com.example.gonggong.ui.profile;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.gonggong.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileEdit extends AppCompatActivity {

    private String USER_ID, USER_NICKNAME, USER_INTRO, USER_PROFILEURL;

    private String Default_nick, Default_intro;

    private static String IP_ADDRESS = "211.211.158.42/instudy";
    private static String TAG1 = "프로필수정 게시물";
    String userid;
    ImageView backspace;
    ImageButton imgbtncamera, imgsave;
    TextView mTextViewPostResult;
    EditText mEditNickname, mEditProfileContents;
    private CircleImageView profileimgview; // 유저 원형 이미지
    Button validation;

    private Uri filePath;

    private Boolean isPermission = true;
    private File tempFile;
    private static final String TAG = "사진 URI 확인";
    private static final int PICK_FROM_ALBUM = 1;
    private static final int PICK_FROM_CAMERA = 2;

    //SharedPreferences
    private SharedPreferences appData;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mTextViewPostResult = findViewById(R.id.mTextViewPostResult); //실패 성공 알려줌 하지만 안씀뷰



        //SharedPreferences
        appData = getSharedPreferences("appData", MODE_PRIVATE);
        userid = appData.getString("ID", ""); // App 사용자 ID

        profileimgview = (CircleImageView) findViewById(R.id.cardView);

        //이미지 save
        imgsave = (ImageButton) findViewById(R.id.imgbtnSave);
        imgsave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });

        Intent mIntent = getIntent(); //인텐트 frag4에서 해줘야함

        Default_nick = mIntent.getStringExtra("nickname");
        USER_PROFILEURL = mIntent.getStringExtra("profileurl");
        Default_intro = mIntent.getStringExtra("introduce");

        Log.i("TAG1", "NICK : " + Default_nick);
        Log.i("TAG1", "CONTENTS : " + Default_intro);

        //프로필 이미지 선택&변경
        imgbtncamera = (ImageButton) findViewById(R.id.imgbtnCamera);
        imgbtncamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //이미지를 선택
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), 0);
            }
        });
        //아래에서 위로 올라오면서 켜진다.
        overridePendingTransition(R.anim.vertical_in, R.anim.none);
        backspace = (ImageView) findViewById(R.id.backspace);
        backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editfin();
            }
        });

        //EditText
        mEditNickname = (EditText) findViewById(R.id.nicknameViewedit);
        mEditProfileContents = (EditText) findViewById(R.id.edtSetProfileContents);

        mEditNickname.setText(Default_nick);                //기존 닉네임값 받아옴

        if ( Default_intro.equals("간단한 자기소개글을 적어보세요") )
        {
            mEditProfileContents.setText("");
        }
        else {
            Log.d("intent","기존 내용 : "+Default_intro);
            mEditProfileContents.setText( Default_intro ); //기존 intro값 받아옴
        }




        //확인버튼
        validation = (Button) findViewById(R.id.mButtonStart);
        validation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                USER_NICKNAME = mEditNickname.getText().toString();         //바뀐 닉네임 값 가져오기
                USER_INTRO = mEditProfileContents.getText().toString();  //바뀐 소개 값 가져오기
                USER_PROFILEURL = uploadFile();
                USER_ID = userid;                       // App 사용자 ID값 가져와서 인자 값으로 넣어주기


                if (USER_NICKNAME.length() < 2) {
                    Toast.makeText(getApplicationContext(), "닉네임을 더 길게 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    EditData task = new EditData();
                    task.execute("http://" + IP_ADDRESS + "/UserModifyAndroid.php", USER_ID, USER_NICKNAME, USER_INTRO, USER_PROFILEURL);
                    Toast.makeText(getApplicationContext(), "프로필이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    save(USER_NICKNAME);
                }
            }
        });
        getImg();
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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap( ProfileEdit.this.getContentResolver(), filePath );
                profileimgview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void editfin() {
        finish();
        //아래로 내려가면서 사라진다.
        overridePendingTransition(R.anim.none, R.anim.vertical_out);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (isFinishing()) {
            //back 버튼을 누르면 화면 종료가 야기되면 동작
            overridePendingTransition(R.anim.none, R.anim.vertical_out);
        }
    }
    //upload the file
    private String uploadFile() {
        //업로드할 파일이 있으면 수행
        if (filePath != null) {
            //업로드 진행 Dialog 보이기
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("업로드중...");
            progressDialog.show();

            //storage
            FirebaseStorage storage = FirebaseStorage.getInstance();

            //Unique한 파일명을 만들자.
            String filename = userid + ".png";
            //storage 주소와 폴더 파일명을 지정해 준다.
            StorageReference storageRef = storage.getReferenceFromUrl("gs://gonggong-60888.appspot.com").child(userid+"/" + filename);

            USER_PROFILEURL = userid+"/" + filename;
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
                            finish();
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(), "파일을 먼저 선택하세요.", Toast.LENGTH_SHORT).show();
        }
        return USER_PROFILEURL;

    }


    //사용자 닉네임 값 App에 Save
    private void save(String nickname) {
        // SharedPreferences 객체만으론 저장 불가능 Editor 사용
        SharedPreferences.Editor editor = appData.edit();

        editor.putString("NICKNAME", nickname);

        // apply, commit 을 안하면 변경된 내용이 저장되지 않음
        editor.apply();
    }

    //닉네임 변경 클래스
    class EditData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ProfileEdit.this, "Please Wait", null, true, true);
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
            String USER_PROFILE = (String) params[4];


            String postParameters = "UserID=" + USER_ID + "&UserNickName=" + USER_NICKNAME
                    + "&UserIntroduce=" + USER_CONTENTS + "&UserProfile=" + USER_PROFILE;

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

    //프로필 이미지 가져오ㄱㅣ
    private void getImg() {
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://gonggong-60888.appspot.com");
        StorageReference storageRef = storage.getReference();
        storageRef.child(userid+"/"+userid+".png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //이미지 로드 성공시
                Glide.with(getApplicationContext()).load(uri).into(profileimgview);
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //이미지 로드 실패시
                Glide.with(getApplicationContext()).load(R.drawable.default_user).into(profileimgview);
            }
        });
    }
}