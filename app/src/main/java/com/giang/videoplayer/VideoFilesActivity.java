package com.giang.videoplayer;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

public class VideoFilesActivity extends AppCompatActivity implements SearchView.OnQueryTextListener {

    private RecyclerView containerVideos;
    private ArrayList<MediaFiles> videos;
    private static VideoFilesAdapter adapter;
    private String folderName;
    private SwipeRefreshLayout swipeRefreshLayout;
    public static final String MY_PREF = "my pref";
    private String sortOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_files);
        init();
        getSupportActionBar().setTitle(folderName);

        SharedPreferences.Editor editor = getSharedPreferences(MY_PREF,MODE_PRIVATE).edit();
        editor.putString("playlistFolderName",folderName);
        editor.apply();

        showVideos();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            showVideos();
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showVideos() {
        videos = fetchMedia(folderName);
        adapter = new VideoFilesAdapter(videos, this,0);
        containerVideos.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @SuppressLint("Range")
    private ArrayList<MediaFiles> fetchMedia(String folderName) {
        SharedPreferences preferences = getSharedPreferences(MY_PREF, MODE_PRIVATE);
        String sortValue = preferences.getString("sort", "abcd");
        ArrayList<MediaFiles> videos = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        if (sortValue.equals("sortName")) {
            sortOrder = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
        } else if (sortValue.equals("sortSize")) {
            sortOrder = MediaStore.MediaColumns.SIZE + " DESC";
        } else if (sortValue.equals("sortDate")) {
            sortOrder = MediaStore.MediaColumns.DATE_ADDED + " DESC";
        } else {
            sortOrder = MediaStore.Video.Media.DURATION + " DESC";
        }

        String selection = MediaStore.Video.Media.DATA + " like?";
        String[] selectionArg = new String[]{"%" + folderName + "%"};
        Cursor cursor = getContentResolver().query(uri, null, selection, selectionArg, sortOrder);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.video_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_video);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        SharedPreferences preferences = getSharedPreferences(MY_PREF, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        int id = item.getItemId();
        switch (id) {
            case R.id.refresh_files:
                finish();
                startActivity(getIntent());
                break;
            case R.id.sort_by:
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle("Sort By");
                dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editor.apply();
                        finish();
                        startActivity(getIntent());
                        dialogInterface.dismiss();
                    }
                });
                String[] items = {"Name (A to Z)", "Size (Big to Small)", "Date (New to Old)", "Length (Long to Short)"};
                dialog.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                editor.putString("sort", "sortName");
                                break;
                            case 1:
                                editor.putString("sort", "sortSize");
                                break;
                            case 2:
                                editor.putString("sort", "sortDate");
                                break;
                            case 3:
                                editor.putString("sort", "sortLength");
                                break;
                        }
                    }
                });
                dialog.create().show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String inputs = newText.toLowerCase();
        ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
        for (MediaFiles media : videos) {
            if (media.getTitle().toLowerCase().contains(inputs)) {
                mediaFiles.add(media);
            }
        }
        VideoFilesActivity.adapter.updateVideoFiles(mediaFiles);
        return true;
    }
}