package com.example.gonggong.ui.story;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gonggong.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    final private String TAG1 = "테스트중이에요옹";

    private Uri Image;

    private ArrayList<ReviewData> reviewdata ;
    //리스터 객체 참조를 저장하는 변수
    private OnItemClickListener mListener = null;

    private SharedPreferences appData;
    private String userid;

    @NonNull
    @NotNull
    //아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_review_item,parent,false);
        ViewHolder viewHolder = new ViewHolder(view);




        return viewHolder;
    }
    //생성자에서 데이터 리스트 객체를 전달받음.
    public ReviewAdapter(ArrayList<ReviewData> mSearchData) {
        this.reviewdata = mSearchData;
    }


    //position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시
    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {

        //SharedPreferences
        appData = holder.itemView.getContext().getSharedPreferences("appData", Context.MODE_PRIVATE);
        userid = appData.getString("ID", ""); // App 사용자 ID

        if ( userid.equals( reviewdata.get(position).getUserid() ) )
        {
            holder.mButtonDelete.setVisibility(View.VISIBLE);
            holder.mButtonEdit.setVisibility(View.VISIBLE);
        }
        else{
            holder.mButtonDelete.setVisibility(View.GONE);
            holder.mButtonEdit.setVisibility(View.GONE);
        }

        //게시글 삭제 클래스
        class DeleteData extends AsyncTask<String, Void, String> {
            ProgressDialog progressDialog;


            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                progressDialog = ProgressDialog.show(holder.itemView.getContext(), "Please Wait", null, true, true);
            }

            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);

                progressDialog.dismiss();
                Log.d("php", "POST response  - " + result);
            }

            @Override
            protected String doInBackground(String... params) {
                String serverURL = (String) params[0];
                String COMMENT_CODE = (String) params[1];

                String postParameters = "PostCommentCode=" + COMMENT_CODE;

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
                    Log.d("php", "POST response code - " + responseStatusCode);

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

                    Log.d("php", "InsertData: Error ", e);

                    return new String("Error: " + e.getMessage());
                }

            }
        }

        holder.nickname.setText(reviewdata.get(position).getNickname());
        holder.contents.setText(reviewdata.get(position).getContents());
        holder.date.setText(reviewdata.get(position).getDate());



        holder.mButtonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( userid.equals(reviewdata.get(position).getUserid()) )
                {
                    // App 사용자 ID와 글 작성자 ID가 일치
                    AlertDialog.Builder dlg = new AlertDialog.Builder(holder.itemView.getContext(), R.style.MyDialogTheme);
                    dlg.setTitle("댓글 삭제");
                    dlg.setMessage("댓글을 삭제하시겠습니까 ? ");
                    dlg.setIcon(R.drawable.delete);

                    dlg.setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    dlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            //댓글 삭제 코드
                            DeleteData deleteData = new DeleteData();
                            deleteData.execute("http://211.211.158.42/instudy/PostCommentDelete.php", reviewdata.get(position).getCode() );

                            reviewdata.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, reviewdata.size());

                            Toast.makeText(holder.itemView.getContext(), "댓글이 삭제되었습니다.", Toast.LENGTH_SHORT).show();

                        }

                    });

                    AlertDialog alertDialog = dlg.create();
                    dlg.show();
                }
            }
        });





    }

    private void getImg() {
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://gonggong-60888.appspot.com");
        StorageReference storageRef = storage.getReference();
        storageRef.child("images/20210523_4740.png").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //이미지 로드 성공시
                Image = uri;
//

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //이미지 로드 실패시
//                Toast.makeText(context, "실패", Toast.LENGTH_SHORT).show();
            }
        });
    }



    //전체 데이터 갯수 리턴
    @Override
    public int getItemCount() {
        return reviewdata.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    //OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메소드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }



    public class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView profile; // 프사
        public TextView nickname;
        public TextView contents;
        public TextView date;
        public Button mButtonDelete;
        public Button mButtonEdit;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.imgProfile);
            nickname = itemView.findViewById(R.id.txtNick);
            contents = itemView.findViewById(R.id.txtContents);
            date = itemView.findViewById(R.id.txtDate);
            mButtonDelete = itemView.findViewById(R.id.mButtonDelete);
            mButtonEdit = itemView.findViewById(R.id.mButtonEdit);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        // 데이터 리스트로부터 아이템 참조.
                        if (mListener != null) {
                            mListener.onItemClick(view, pos);
                            Log.d(TAG1, "onClick: "+pos+"번째");
                        }
                    }
                }
            });
        }
    }
}
