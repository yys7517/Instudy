package com.example.gonggong.ui.story;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.gonggong.R;

import java.util.ArrayList;

public class ReviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewAdapter adapter;
    private RecyclerView.LayoutManager  mLayoutManager;

    private ArrayList<ReviewData> mSearchData = new ArrayList<>();

    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        recyclerView = (RecyclerView) findViewById(R.id.rvComment);
        recyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.scrollToPosition(0);
        adapter = new ReviewAdapter(mSearchData);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        swipeRefreshLayout = findViewById(R.id.reviewrefresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Post
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        PostUpdate();
    }

    public void PostUpdate() {
        mSearchData.clear();
        adapter.notifyDataSetChanged();
        GetData task = new GetData();
        task.execute("http://" + IP_ADDRESS + "/POST.php", "");
    }
}
