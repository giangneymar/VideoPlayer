package com.giang.videoplayer;

import android.annotation.SuppressLint;
import android.app.PictureInPictureParams;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Rational;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.developer.filepicker.model.DialogProperties;
import com.developer.filepicker.view.FilePickerDialog;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
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
    private ImageView imgNext, imgPrev, videoBack, lock, unlock, scaling, videoList;
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
    private PlaybackParameters parameters;
    private float speed;
    private DialogProperties dialogProperties;
    private FilePickerDialog filePickerDialog;
    private VideoFilesAdapter videoFilesAdapter;
    private PictureInPictureParams.Builder pip;
    private boolean isCrossChecked;

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
        player.setPlaybackParameters(parameters);
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

    private void screenOrientation() {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            Bitmap bitmap;
            String path = videoFiles.get(position).getPath();
            Uri uri = Uri.parse(path);
            retriever.setDataSource(this, uri);
            bitmap = retriever.getFrameAtTime();

            int videoWidth = bitmap.getWidth();
            int videoHeight = bitmap.getHeight();
            if (videoWidth > videoHeight) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        playerView = findViewById(R.id.exoplayerView);
        videoFiles = new ArrayList<>();
        getSupportActionBar().hide();
        position = getIntent().getIntExtra("position", 1);
        videoTitle = getIntent().getStringExtra("video_title");
        videoFiles = getIntent().getExtras().getParcelableArrayList("videos");
        screenOrientation();

        title = findViewById(R.id.videoTitle);
        imgNext = findViewById(R.id.exo_next);
        imgPrev = findViewById(R.id.exo_prev);
        root = findViewById(R.id.rootLayout);
        lock = findViewById(R.id.lock);
        unlock = findViewById(R.id.unlock);
        videoBack = findViewById(R.id.videoBack);
        scaling = findViewById(R.id.scaling);
        nightMode = findViewById(R.id.nightMode);
        videoList = findViewById(R.id.videoList);

        imgPrev.setOnClickListener(this);
        imgNext.setOnClickListener(this);
        videoBack.setOnClickListener(this);
        lock.setOnClickListener(this);
        unlock.setOnClickListener(this);
        scaling.setOnClickListener(firstListener);
        videoList.setOnClickListener(this);

        dialogProperties = new DialogProperties();
        filePickerDialog = new FilePickerDialog(VideoPlayerActivity.this);
        filePickerDialog.setTitle("Select a Subtitle File");
        filePickerDialog.setPositiveBtnName("OK");
        filePickerDialog.setNegativeBtnName("Cancel");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            pip = new PictureInPictureParams.Builder();
        }

        iconModelsList = new ArrayList<>();
        iconModelsList.add(new IconModel(R.drawable.ic_right, ""));
        iconModelsList.add(new IconModel(R.drawable.ic_night, "Night"));
        iconModelsList.add(new IconModel(R.drawable.ic_pip, "Popup"));
        iconModelsList.add(new IconModel(R.drawable.ic_equalizer, "Equalizer"));
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
                        iconModelsList = new ArrayList<>();
                        iconModelsList.add(new IconModel(R.drawable.ic_right, ""));
                        iconModelsList.add(new IconModel(R.drawable.ic_night, "Night"));
                        iconModelsList.add(new IconModel(R.drawable.ic_pip, "Popup"));
                        iconModelsList.add(new IconModel(R.drawable.ic_equalizer, "Equalizer"));
                        iconModelsList.add(new IconModel(R.drawable.ic_rotation, "Rotate"));
                        adapter.notifyDataSetChanged();
                        expand = false;
                    } else {
                        if (iconModelsList.size() == 5) {
                            iconModelsList.add(new IconModel(R.drawable.ic_volume_off, "Mute"));
                            iconModelsList.add(new IconModel(R.drawable.ic_volume, "Volume"));
                            iconModelsList.add(new IconModel(R.drawable.ic_brightness, "Brightness"));
                            iconModelsList.add(new IconModel(R.drawable.ic_speed, "Speed"));
                        }
                        iconModelsList.set(position, new IconModel(R.drawable.ic_left, ""));
                        adapter.notifyDataSetChanged();
                        expand = true;
                    }
                }
                if (position == 1) {
                    if (dark) {
                        //night
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
                    //popup
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Rational aspectRatio = new Rational(16, 9);
                        pip.setAspectRatio(aspectRatio);
                        enterPictureInPictureMode(pip.build());
                    } else {
                        Log.e("aaa", "yes");
                    }
                }
                if (position == 3) {
                    //equalizer
                    Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                    if ((intent.resolveActivity(getPackageManager()) != null)) {
                        startActivityForResult(intent, 123);
                    } else {
                        Toast.makeText(VideoPlayerActivity.this, "No Equalizer Found", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    }
                }
                if (position == 4) {
                    //rotate
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                        adapter.notifyDataSetChanged();
                    } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                        adapter.notifyDataSetChanged();
                    }
                }
                if (position == 5) {
                    //mute
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
                if (position == 6) {
                    //volume
                    VolumeDialog dialog = new VolumeDialog();
                    dialog.show(getSupportFragmentManager(), "dialog");
                    adapter.notifyDataSetChanged();
                }
                if (position == 7) {
                    //brightness
                    BrightnessDialog dialog = new BrightnessDialog();
                    dialog.show(getSupportFragmentManager(), "dialog");
                    adapter.notifyDataSetChanged();
                }
                if (position == 8) {
                    //speed
                    AlertDialog.Builder dialog = new AlertDialog.Builder(VideoPlayerActivity.this);
                    dialog.setTitle("Select Playback Speed").setPositiveButton("OK", null);
                    String[] items = {"0.5x", "1x Normal Speed", "1.25x", "1.5x", "2x"};
                    int checkItem = -1;
                    dialog.setSingleChoiceItems(items, checkItem, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            switch (i) {
                                case 0:
                                    speed = 0.5f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 1:
                                    speed = 1f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 2:
                                    speed = 1.25f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 3:
                                    speed = 1.5f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                case 4:
                                    speed = 2f;
                                    parameters = new PlaybackParameters(speed);
                                    player.setPlaybackParameters(parameters);
                                    break;
                                default:
                                    break;
                            }
                        }
                    });
                    AlertDialog alert = dialog.create();
                    alert.show();
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onPause() {
        super.onPause();
        player.setPlayWhenReady(false);
        player.getPlaybackState();
        if (isInPictureInPictureMode()) {
            player.setPlayWhenReady(true);
        } else {
            player.setPlayWhenReady(false);
            player.getPlaybackState();
        }
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
            case R.id.videoList:
                PlayListDialog dialog = new PlayListDialog(videoFiles, videoFilesAdapter);
                dialog.show(getSupportFragmentManager(), dialog.getTag());
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

    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode, Configuration newConfig) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig);
        isCrossChecked = isInPictureInPictureMode;
        if (isInPictureInPictureMode) {
            playerView.hideController();
        } else {
            playerView.showController();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isCrossChecked) {
            player.release();
            finish();
        }
    }
}