package com.giang.videoplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;

public class VideoFilesAdapter extends RecyclerView.Adapter<VideoFilesAdapter.ViewHolder> {
    private ArrayList<MediaFiles> videos;
    private Context context;
    private BottomSheetDialog bottomSheetDialog;

    public VideoFilesAdapter(ArrayList<MediaFiles> videos, Context context) {
        this.videos = videos;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.videoName.setText(videos.get(position).getDisplayName());
        String size = videos.get(position).getSize();
        holder.videoSize.setText(android.text.format.Formatter.formatFileSize(context, Long.parseLong(size)));
        double milliSeconds = Double.parseDouble(videos.get(position).getDuration());
        holder.videoDuration.setText(timeConvert((long) milliSeconds));
        Glide.with(context).load(new File(videos.get(position).getPath()))
                .into(holder.imageVideo);

        holder.menuMore.setOnClickListener(view -> {
            bottomSheetDialog = new BottomSheetDialog(context, R.style.BottomSheetTheme);
            View bsView = LayoutInflater.from(context).inflate(R.layout.video_bs_layout, view.findViewById(R.id.bottomSheet));
            bsView.findViewById(R.id.bs_play).setOnClickListener(view1 -> {
                holder.itemView.performClick();
                bottomSheetDialog.dismiss();
            });

            bsView.findViewById(R.id.bs_rename).setOnClickListener(new View.OnClickListener() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("Rename to");
                    EditText editText = new EditText(context);
                    String path = videos.get(position).getPath();
                    final File file = new File(path);
                    String videoName = file.getName();
                    videoName = videoName.substring(0, videoName.lastIndexOf("."));
                    editText.setText(videoName);
                    dialog.setView(editText);
                    editText.requestFocus();

                    dialog.setPositiveButton("OK", (dialogInterface, i) -> {
                        String onlyPath = file.getParentFile().getAbsolutePath();
                        String ext = file.getAbsolutePath();
                        ext = ext.substring(ext.lastIndexOf("."));
                        String newPath = onlyPath + "/" + editText.getText().toString() + ext;
                        File newFile = new File(newPath);
                        boolean rename = file.renameTo(newFile);
                        if (rename) {
                            ContentResolver resolver = context.getApplicationContext().getContentResolver();
                            resolver.delete(MediaStore.Files.getContentUri("external"),
                                    MediaStore.MediaColumns.DATA + "=?", new String[]
                                            {file.getAbsolutePath()});
                            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                            intent.setData(Uri.fromFile(newFile));
                            context.getApplicationContext().sendBroadcast(intent);

                            videos.get(position).setPath(newPath);
                            holder.videoName.setText(editText.getText().toString() + ext);
                            Toast.makeText(context, "Video Renamed", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Process Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());
                    dialog.create().show();
                    bottomSheetDialog.dismiss();
                }
            });

            bsView.findViewById(R.id.bs_share).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri uri = Uri.parse(videos.get(position).getPath());
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("video/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    context.startActivity(Intent.createChooser(shareIntent, "Share Video To"));
                    bottomSheetDialog.dismiss();
                }
            });

            bsView.findViewById(R.id.bs_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("Delete");
                    dialog.setMessage("Do you want to delete this video");
                    dialog.setPositiveButton("Delete", (dialogInterface, i) -> {
                        Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                                Long.parseLong(videos.get(position).getId()));
                        File file = new File(videos.get(position).getPath());
                        boolean delete = file.delete();
                        if (delete) {
                            context.getContentResolver().delete(contentUri, null, null);
                            videos.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, videos.size());
                            Toast.makeText(context, "Video Deleted", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Can't Deleted", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    dialog.show();
                    bottomSheetDialog.dismiss();
                }
            });

            bsView.findViewById(R.id.bs_properties).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                    dialog.setTitle("Properties");

                    String one = "File: " + videos.get(position).getDisplayName();
                    String path = videos.get(position).getPath();
                    int indexOfPath = path.lastIndexOf("/");
                    String two = "Path: " + path.substring(0, indexOfPath);
                    String three = "Size: " + android.text.format.Formatter.formatFileSize(context, Long.parseLong(videos.get(position).getSize()));
                    String four = "Length: " + timeConvert((long) milliSeconds);
                    String nameWithFormat = videos.get(position).getDisplayName();
                    int index = nameWithFormat.lastIndexOf(".");
                    String format = nameWithFormat.substring(index + 1);
                    String five = "Format: " + format;

                    MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
                    metadataRetriever.setDataSource(videos.get(position).getPath());
                    String height = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
                    String width = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
                    String six = "Resolution: " + width + "x" + height;

                    dialog.setMessage(one + "\n\n" + two + "\n\n" + three + "\n\n" + four + "\n\n" + five + "\n\n" + six);
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    dialog.show();
                    bottomSheetDialog.dismiss();
                }
            });

            bottomSheetDialog.setContentView(bsView);
            bottomSheetDialog.show();
        });

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, VideoPlayerActivity.class);
            intent.putExtra("position", position);
            intent.putExtra("video_title", videos.get(position).getDisplayName());
            Bundle bundle = new Bundle();
            bundle.putParcelableArrayList("videos", videos);
            intent.putExtras(bundle);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageVideo, menuMore;
        private TextView videoName, videoSize, videoDuration;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageVideo = itemView.findViewById(R.id.imageVideo);
            menuMore = itemView.findViewById(R.id.more);
            videoDuration = itemView.findViewById(R.id.duration);
            videoSize = itemView.findViewById(R.id.size);
            videoName = itemView.findViewById(R.id.videoName);
        }
    }

    @SuppressLint("DefaultLocale")
    private String timeConvert(long value) {
        String videoTime;
        int duration = (int) value;
        int hour = duration / 3600000;
        int minutes = (duration / 60000) % 60000;
        int seconds = duration % 60000 / 1000;
        if (hour > 0) {
            videoTime = String.format("%02d:%02d:%02d", hour, minutes, seconds);
        } else {
            videoTime = String.format("%02d:%02d", minutes, seconds);
        }
        return videoTime;
    }
}
