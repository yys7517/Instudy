package com.example.gonggong.ui.story;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.gonggong.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    final private String TAG1 = "테스트중이에요옹";

    private Uri Image;

    private ArrayList<ReviewData> reviewdata;
    //리스터 객체 참조를 저장하는 변수
    private OnItemClickListener mListener = null;

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


    //position에 해당하는 데이터를 뷰홀더의 아이템뷰에 표시
    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {

        getImg();
        Glide.with(holder.itemView.getContext()).load(Image).into(holder.profile);

        holder.nickname.setText(reviewdata.get(position).getNickname());
        holder.contents.setText(reviewdata.get(position).getContents());
        holder.date.setText(reviewdata.get(position).getDate());

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

    //생성자에서 데이터 리스트 객체를 전달받음.
    public ReviewAdapter(ArrayList<ReviewData> mSearchData) {
        this.reviewdata = mSearchData;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public CircleImageView profile; // 프사
        public TextView nickname;
        public TextView contents;
        public TextView date;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.imgProfile);
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
}
