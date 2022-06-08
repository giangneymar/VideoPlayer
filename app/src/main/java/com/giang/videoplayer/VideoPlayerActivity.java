package com.giang.videoplayer;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.mediacodec.MediaCodecAdapter;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;

public class VideoPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    private PlayerView playerView;
    private SimpleExoPlayer player;
    private int position;
    private String videoTitle;
    private ArrayList<MediaFiles> videoFiles;
    private TextView title;
    private ImageView imgNext, imgPrev, videoBack, lock, unlock, scaling;
    private RelativeLayout root;
    private ConcatenatingMediaSource concatenatingMediaSource;
    private ControlsMode controlsMode;
    private ArrayList<IconModel> iconModelsList;
    private PlaybackIconsAdapter adapter;
    private RecyclerView recyclerViewIcons;
    private boolean expand = false;
    private View nightMode;
    boolean dark = false;
    boolean mute = false;

    public enum ControlsMode {
        LOCK, FULLSCREEN
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_video_player);
        init();
        title.setText(videoTitle);
        playVideo();
    }

    private void playVideo() {
        String path = videoFiles.get(position).getPath();
        Uri uri = Uri.parse(path);
        player = new SimpleExoPlayer.Builder(this).build();
        DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "app"));
        concatenatingMediaSource = new ConcatenatingMediaSource();
        for (int i = 0; i < videoFiles.size(); i++) {
            new File(String.valueOf(videoFiles.get(i)));
            MediaSource mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory)
                    .createMediaSource(MediaItem.fromUri(Uri.parse(String.valueOf(uri))));
            concatenatingMediaSource.addMediaSource(mediaSource);
        }
        playerView.setPlayer(player);
        playerView.setKeepScreenOn(true);
        player.prepare(concatenatingMediaSource);
        player.seekTo(position, C.TIME_UNSET);
        playError();
    }

    private void playError() {
        player.addListener(new Player.Listener() {
            @Override
            public void onPlayerError(PlaybackException error) {
                Toast.makeText(VideoPlayerActivity.this, "Video Playing Error", Toast.LENGTH_SHORT).show();
            }
        });
        player.setPlayWhenReady(true);
    }

    private void init() {
        playerView = findViewById(R.id.exoplayerView);
        videoFiles = new ArrayList<>();
        getSupportActionBar().hide();
        position = getIntent().getIntExtra("position", 1);
        videoTitle = getIntent().getStringExtra("video_title");
        videoFiles = getIntent().getExtras().getParcelableArrayList("videos");

        title = findViewById(R.id.videoTitle);
        imgNext = findViewById(R.id.exo_next);
        imgPrev = findViewById(R.id.exo_prev);
        root = findViewById(R.id.rootLayout);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);
        videoBack = findViewById(R.id.videoBack);
        scaling = findViewById(R.id.scaling);
        nightMode = findViewById(R.id.nightMode);

        imgPrev.setOnClickListener(this);
        imgNext.setOnClickListener(this);
        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        scaling.setOnClickListener(firstListener);

        iconModelsList = new ArrayList<>();
        iconModelsList.add(new IconModel(R.drawable.ic_right, ""));
        iconModelsList.add(new IconModel(R.drawable.ic_night, "Night"));
        iconModelsList.add(new IconModel(R.drawable.ic_volume_off, "Mute"));
        iconModelsList.add(new IconModel(R.drawable.ic_rotation, "Rotate"));

        recyclerViewIcons = findViewById(R.id.recyclerviewIcon);
        adapter = new PlaybackIconsAdapter(iconModelsList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, true);
        recyclerViewIcons.setLayoutManager(layoutManager);
        recyclerViewIcons.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        adapter.setOnItemClickListener(new PlaybackIconsAdapter.OnItemClickListener() {
            @SuppressLint("Range")
            @Override
            public void onItemClick(int position) {
                if (position == 0) {
                    if (expand) {
                        iconModelsList.clear();
                        iconModelsList.add(new IconModel(R.drawable.ic_right, ""));
                        iconModelsList.add(new IconModel(R.drawable.ic_night, "Night"));
                        iconModelsList.add(new IconModel(R.drawable.ic_volume_off, "Mute"));
                        iconModelsList.add(new IconModel(R.drawable.ic_rotation, "Rotate"));
                        adapter.notifyDataSetChanged();
                        expand = false;
                    } else {
                        if (iconModelsList.size() == 4) {
                            iconModelsList.add(new IconModel(R.drawable.ic_volume, "Volume"));
                            iconModelsList.add(new IconModel(R.drawable.ic_brightness, "Brightness"));
                            iconModelsList.add(new IconModel(R.drawable.ic_equalizer, "Equalizer"));
                            iconModelsList.add(new IconModel(R.drawable.ic_speed, "Speed"));
                            iconModelsList.add(new IconModel(R.drawable.ic_subtitles, "Subtitles"));
                        }
                        iconModelsList.set(position, new IconModel(R.drawable.ic_left, ""));
                        adapter.notifyDataSetChanged();
                        expand = true;
                    }
                }
                if (position == 1) {
                    if (dark) {
                        nightMode.setVisibility(View.GONE);
                        iconModelsList.set(position, new IconModel(R.drawable.ic_night, "Night"));
                        adapter.notifyDataSetChanged();
                        dark = false;
                    } else {
                        nightMode.setVisibility(View.VISIBLE);
                        iconModelsList.set(position, new IconModel(R.drawable.ic_night, "Day"));
                        adapter.notifyDataSetChanged();
                        dark = true;
                    }
                }
                if (position == 2) {
                    if (mute) {
                        player.setVolume(100);
                        iconModelsList.set(position, new IconModel(R.drawable.ic_volume_off, "Mute"));
                        adapter.notifyDataSetChanged();
                        mute = false;
                    } else {
                        player.setVolume(0);
                        iconModelsList.set(position, new IconModel(R.drawable.ic_volume, "Un Mute"));
                        adapter.notifyDataSetChanged();
                        mute = true;
                    }
                }
                if (position == 3) {
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        adapter.notifyDataSetChanged();
                    } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        adapter.notifyDataSetChanged();
                    }
                }
                if(position==4){
                    VolumeDialog dialog = new VolumeDialog();
                    dialog.show(getSupportFragmentManager(),"dialog");
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (player.isPlaying()) {
            player.stop();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        player.setPlayWhenReady(true);
        player.getPlaybackState();
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.videoBack:
                if (player != null) {
                    player.release();
                }
                finish();
                break;
            case R.id.lock:
                controlsMode = ControlsMode.FULLSCREEN;
                root.setVisibility(View.VISIBLE);
                lock.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Un Locked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.unlock:
                controlsMode = ControlsMode.LOCK;
                root.setVisibility(View.INVISIBLE);
                lock.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Locked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.exo_next:
                try {
                    player.stop();
                    position++;
                    playVideo();
                } catch (Exception e) {
                    Toast.makeText(this, "no Next Video", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case R.id.exo_prev:
                try {
                    player.stop();
                    position--;
                    playVideo();
                } catch (Exception e) {
                    Toast.makeText(this, "no Previous Video", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
    }

    View.OnClickListener firstListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.ic_full_screen);

            Toast.makeText(VideoPlayerActivity.this, "Full Screen", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(secondListener);
        }
    };

    View.OnClickListener secondListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_ZOOM);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.ic_zoom);

            Toast.makeText(VideoPlayerActivity.this, "Zoom", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(thirdListener);
        }
    };

    View.OnClickListener thirdListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            playerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            player.setVideoScalingMode(C.VIDEO_SCALING_MODE_DEFAULT);
            scaling.setImageResource(R.drawable.ic_fit);

            Toast.makeText(VideoPlayerActivity.this, "Fit", Toast.LENGTH_SHORT).show();
            scaling.setOnClickListener(firstListener);
        }
    };
}