package com.giang.videoplayer;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

public class VideoFilesActivity extends AppCompatActivity {

    private RecyclerView containerVideos;
    private ArrayList<MediaFiles> videos;
    private VideoFilesAdapter adapter;
    private String folderName;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_files);
        init();
        getSupportActionBar().setTitle(folderName);
        showVideos();
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showVideos();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showVideos() {
        videos = fetchMedia(folderName);
        adapter = new VideoFilesAdapter(videos, this);
        containerVideos.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("Range")
    private ArrayList<MediaFiles> fetchMedia(String folderName) {
        ArrayList<MediaFiles> videos = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Video.Media.DATA + " like?";
        String[] selectionArg = new String[]{"%" + folderName + "%"};
        Cursor cursor = getContentResolver().query(uri, null, selection, selectionArg, null);
        if (cursor != null && cursor.moveToNext()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                MediaFiles v = new MediaFiles(id, title, displayName, size, duration, path, dateAdded);
                videos.add(v);
            }
            while (cursor.moveToNext());
        }
        return videos;
    }

    private void init() {
        containerVideos = findViewById(R.id.containerVideos);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshVideos);
        folderName = getIntent().getStringExtra("folderName");
    }
}