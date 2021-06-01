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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gonggong.R;
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

public class ProfileEdit extends AppCompatActivity {

    private String USER_ID, USER_NICKNAME, USER_CONTENTS, USER_PROFILEURL = null, USER_TESTID = "Tester01";

    private static String IP_ADDRESS = "211.211.158.42/instudy";
    private static String TAG1 = "프로필수정 게시물";

    ImageView backspace;
    ImageButton imgbtncamera;
    TextView mTextViewPostResult;
    EditText mEditNickname, mEditProfileContents;
    Button validation;

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

        mTextViewPostResult = findViewById(R.id.mTextViewPostResult); //실패 성공 알려줌 하지만 안씀

        Intent mIntent = getIntent(); //인텐트 frag4에서 해줘야함
        String Default_nickname = mIntent.getStringExtra("nickname");
        String Defalut_contents = mIntent.getStringExtra("contents");
        Log.i("TAG1", "NICK : " + Default_nickname);
        Log.i("TAG1", "CONTENTS : " + Defalut_contents);

        //프로필 이미지 선택&변경
        imgbtncamera = (ImageButton) findViewById(R.id.imgbtnCamera);
        imgbtncamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPermission) goToAlbum();
                else Toast.makeText(view.getContext(), getResources()
                        .getString(R.string.permission_2), Toast.LENGTH_LONG).show();
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
        mEditNickname.setText(Default_nickname);                //intent 닉네임값 받아옴
        mEditProfileContents.setText(Defalut_contents);         //intent contents값 받아옴

        //SharedPreferences
        appData = getSharedPreferences("appData", MODE_PRIVATE);
        String userid = appData.getString("ID", ""); // App 사용자 ID

        //확인버튼
        validation = (Button) findViewById(R.id.mButtonStart);
        validation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                USER_NICKNAME = mEditNickname.getText().toString();         //바뀐 닉네임 값 가져오기
                USER_CONTENTS = mEditProfileContents.getText().toString();  //바뀐 소개 값 가져오기
//              USER_PROFILEURL // 미구현
                USER_ID = userid;                       // App 사용자 ID값 가져와서 인자 값으로 넣어주기


                if (USER_NICKNAME.length() < 2) {
                    Toast.makeText(getApplicationContext(), "닉네임을 더 길게 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    EditData task = new EditData();
                    task.execute("http://" + IP_ADDRESS + "/UserModifyAndroid.php", USER_TESTID, USER_NICKNAME, USER_CONTENTS, USER_PROFILEURL);
                    Toast.makeText(getApplicationContext(), "프로필이 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    save(USER_NICKNAME);
                    finish();
                }
            }
        });
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();

            if (tempFile != null) {
                if (tempFile.exists()) {
                    if (tempFile.delete()) {
                        Log.e(TAG, tempFile.getAbsolutePath() + " 삭제 성공");
                        tempFile = null;
                    }
                }
            }

            return;
        }

        if (requestCode == PICK_FROM_ALBUM) {

            Uri photoUri = data.getData();
            Log.d(TAG, "PICK_FROM_ALBUM photoUri : " + photoUri);

            Cursor cursor = null;

            try {

                /*
                 *  Uri 스키마를
                 *  content:/// 에서 file:/// 로  변경한다.
                 */
                String[] proj = {MediaStore.Images.Media.DATA};

                assert photoUri != null;
                cursor = getContentResolver().query(photoUri, proj, null, null, null);

                assert cursor != null;
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                cursor.moveToFirst();

                tempFile = new File(cursor.getString(column_index));

                Log.d(TAG, "tempFile Uri : " + Uri.fromFile(tempFile));

            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

            setImage();

        } else if (requestCode == PICK_FROM_CAMERA) {

            setImage();

        }
    }

    /**
     * 앨범에서 이미지 가져오기
     */
    private void goToAlbum() {

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }


    /**
     * 카메라에서 이미지 가져오기 @@@@현재 오류있음!!@@@@
     */
    private void takePhoto() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        try {
            tempFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if (tempFile != null) {

            Uri photoUri = Uri.fromFile(tempFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    /**
     * 폴더 및 파일 만들기
     */
    private File createImageFile() throws IOException {

        // 이미지 파일 이름 ( blackJin_{시간}_ )
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "blackJin_" + timeStamp + "_";

        // 이미지가 저장될 폴더 이름 ( blackJin )
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/blackJin/");
        if (!storageDir.exists()) storageDir.mkdirs();

        // 파일 생성
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        Log.d(TAG, "createImageFile : " + image.getAbsolutePath());

        return image;
    }

    /**
     * tempFile 을 bitmap 으로 변환 후 ImageView 에 설정한다.
     */
    private void setImage() {

        ImageView imageView = (ImageView) findViewById(R.id.cardView);

        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap originalBm = BitmapFactory.decodeFile(tempFile.getAbsolutePath(), options);
        Log.d(TAG, "setImage : " + tempFile.getAbsolutePath());

        imageView.setImageBitmap(originalBm);

        /**
         *  tempFile 사용 후 null 처리를 해줘야 합니다.
         *  (resultCode != RESULT_OK) 일 때 tempFile 을 삭제하기 때문에
         *  기존에 데이터가 남아 있게 되면 원치 않은 삭제가 이뤄집니다.
         */
        tempFile = null;

    }

    /**
     * 권한 설정
     */
    private void tedPermission() {

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
                isPermission = true;

            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                // 권한 요청 실패
                isPermission = false;

            }
        };

        TedPermission.with(this)
                .setPermissionListener(permissionListener)
                .setRationaleMessage(getResources().getString(R.string.permission_2))
                .setDeniedMessage(getResources().getString(R.string.permission_1))
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .check();

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
}
