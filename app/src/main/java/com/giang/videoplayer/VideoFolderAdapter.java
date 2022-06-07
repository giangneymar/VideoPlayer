package com.giang.videoplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VideoFolderAdapter extends RecyclerView.Adapter<VideoFolderAdapter.ViewHolder> {
    private ArrayList<MediaFiles> mediaFiles;
    private ArrayList<String> folderPaths;
    private Context context;

    public VideoFolderAdapter(ArrayList<MediaFiles> mediaFiles, ArrayList<String> folderPaths, Context context) {
        this.mediaFiles = mediaFiles;
        this.folderPaths = folderPaths;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.folder_item, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        int indexPath = folderPaths.get(position).lastIndexOf("/");
        String nameFolder = folderPaths.get(position).substring(indexPath + 1);
        holder.folderName.setText(nameFolder);
        holder.folderPath.setText(folderPaths.get(position));
        holder.noVideo.setText(noOfFolder(folderPaths.get(position))+ " Videos");
        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, VideoFilesActivity.class);
            intent.putExtra("folderName", nameFolder);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return folderPaths.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView folderName, folderPath, noVideo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            folderName = itemView.findViewById(R.id.folderName);
            folderPath = itemView.findViewById(R.id.folderPath);
            noVideo = itemView.findViewById(R.id.noVideo);
        }
    }

    int noOfFolder(String folderName) {
        int fileNo = 0;
        for (MediaFiles mediaFiles : mediaFiles) {
            if (mediaFiles.getPath().substring(0, mediaFiles.getPath().lastIndexOf("/")).endsWith(folderName)) {
                fileNo++;
            }
        }
        return fileNo;
    }
}
