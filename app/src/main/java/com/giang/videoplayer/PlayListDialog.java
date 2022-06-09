package com.giang.videoplayer;

import static com.giang.videoplayer.VideoFilesActivity.MY_PREF;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class PlayListDialog extends BottomSheetDialogFragment {

    private ArrayList<MediaFiles> mediaFiles = new ArrayList<>();
    VideoFilesAdapter adapter;
    BottomSheetDialog dialog;
    RecyclerView recyclerView;
    TextView folder;

    public PlayListDialog(ArrayList<MediaFiles> mediaFiles, VideoFilesAdapter adapter) {
        this.mediaFiles = mediaFiles;
        this.adapter = adapter;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.playlist_bs_layout, null);
        dialog.setContentView(view);

        recyclerView = view.findViewById(R.id.playlistRY);
        folder = view.findViewById(R.id.playlistName);

        SharedPreferences preferences = this.getActivity().getSharedPreferences(MY_PREF, Context.MODE_PRIVATE);
        String folderName = preferences.getString("playlistFolderName", "abc");
        folder.setText(folderName);

        mediaFiles = fetchMedia(folderName);

        adapter = new VideoFilesAdapter(mediaFiles, getContext(),1);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        return dialog;
    }

    @SuppressLint("Range")
    private ArrayList<MediaFiles> fetchMedia(String folderName) {
        ArrayList<MediaFiles> videos = new ArrayList<>();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String selection = MediaStore.Video.Media.DATA + " like?";
        String[] selectionArg = new String[]{"%" + folderName + "%"};
        Cursor cursor = getContext().getContentResolver().query(uri, null, selection, selectionArg, null);
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
}
