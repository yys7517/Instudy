package com.example.gonggong.ui.home;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gonggong.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.ViewHolder> {


    final private String TAG1 = "테스트중이에요옹";

    private Uri Post = null;



    private ArrayList<HomeData> iData;
    //리스너 객체 참조를 저장하는 변수
    private OnItemClickListener mListener = null;



    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    //OnItemClickListener 리스너 객체 참조를 어댑터에 전달하는 메소드
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgProfile;
        public ImageView imgPost;
        public TextView nickname;
        public TextView contents;
        public TextView date;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            //뷰 객체에 대한 참조.(Hold Strong reference)
            imgPost = itemView.findViewById(R.id.imgPost);
            imgProfile = itemView.findViewById(R.id.imgProfile);
            nickname = itemView.findViewById(R.id.txtNick);
            contents = itemView.findViewById(R.id.txtContents);
            date = itemView.findViewById(R.id.txtDate);



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

    //생성자에서 데이터 리스트 객체를 전달받음.
    public HomeAdapter(ArrayList<HomeData> mSearchData) {
        this.iData = mSearchData;
    }

    @NonNull
    //아이템 뷰를 위한 뷰홀더 객체 생성하여 리턴
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_home_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    //position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        FirebaseStorage storage = FirebaseStorage.getInstance("gs://gonggong-60888.appspot.com");
        StorageReference storageRef = storage.getReference();
        storageRef.child( iData.get(position).getImgPost() ).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //이미지 로드 성공시
                Glide.with(holder.itemView.getContext()).load( uri ).into( holder.imgPost );   //게시글 사진
            }

        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                //이미지 로드 실패시
            }
        });

        holder.nickname.setText( iData.get(position).getNickname() );   //닉네임
        holder.contents.setText(iData.get(position).getContents());     //내용
        holder.date.setText(iData.get(position).getDate());             //날짜

    }

    //전체 데이터 갯수 리턴
    @Override
    public int getItemCount() {
        return iData.size();
    }
}
