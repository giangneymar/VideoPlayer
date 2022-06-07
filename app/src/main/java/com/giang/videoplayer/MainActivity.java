package com.giang.videoplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private RecyclerView container;
    private SwipeRefreshLayout swipeRefreshLayout;
    private VideoFolderAdapter adapter;
    private ArrayList<MediaFiles> mediaFiles;
    private ArrayList<String> allFolderList;

    private static final int STORAGE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        checkPermission();
        showFolders();
        handlerRefresh();
    }

    private void init() {
        mediaFiles = new ArrayList<>();
        allFolderList = new ArrayList<>();
        container = findViewById(R.id.containerFolder);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshFolders);
    }

    private void handlerRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            showFolders();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.folder_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.rateUs:
                Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                break;
//            case R.id.refresh:
//                finish();
//                startActivity(getIntent());
//                break;
            case R.id.shareApp:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check thia app via\n" + "https://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName());
                shareIntent.setType("text/plain");
                startActivity(Intent.createChooser(shareIntent, "Share App To"));
                break;
        }
        return true;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showFolders() {
        mediaFiles = fetchMedia();
        adapter = new VideoFolderAdapter(mediaFiles, allFolderList, this);
        container.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION);
        } else {
            fetchMedia();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchMedia();
                Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @SuppressLint({"NotifyDataSetChanged", "Range"})
    private ArrayList<MediaFiles> fetchMedia() {
        ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = getContentResolver().query(uri, null, null, null, null, null);

        if (cursor != null && cursor.moveToNext()) {
            do {
                String id = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE));
                String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME));
                String size = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE));
                String duration = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION));
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA));
                String dateAdded = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED));
                MediaFiles m = new MediaFiles(id, title, displayName, size, duration, path, dateAdded);

                int index = path.lastIndexOf("/");
                String sub = path.substring(0, index);
                if (!allFolderList.contains(sub)) {
                    allFolderList.add(sub);
                }
                mediaFiles.add(m);
            }
            while (cursor.moveToNext());
        }
        return mediaFiles;
    }
}