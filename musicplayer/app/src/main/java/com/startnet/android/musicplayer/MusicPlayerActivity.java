package com.startnet.android.musicplayer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
/**
 * 该类管理歌曲详情页的显示与操作
 * */
public class MusicPlayerActivity extends AppCompatActivity {
    //对应于Service中的各类操作，用于确定操作类型
    private static final int SONGPLAY = 0;
    private static final int SONGPAUSE = 1;
    private static final int NEXTSONG = 2;
    private static final int PRESONG = 3;
    //标识要求的信息
    private static final String EXTRA_SONG_URI = "com.startnet.android.musicplayer.song_uri";
    private static final String EXTRA_MUSIC_PLAYING = "music_is_playing";
    //上一首按钮
    private ImageButton mPreviousButton;
    //暂停播放按钮
    private ImageButton mPlayAndPauseButton;
    //下一首按钮
    private ImageButton mNextButton;
    //当前播放歌曲的uri
    private String mSongUri;
    //播放状态
    private boolean mIsPlaying;
    //当前歌曲详情页
    private Fragment fragment;
    private PlayerActivityReceiver mPlayerActivityReceiver;

    /**
     * 新建歌曲详情页的方法,需要调用者通过Intent传递歌曲播放信息
     */
    public static Intent newIntent(Context packageContext, String songUri, boolean isPlaying){
        Intent intent = new Intent(packageContext, MusicPlayerActivity.class);
        intent.putExtra(EXTRA_SONG_URI, songUri);
        intent.putExtra(EXTRA_MUSIC_PLAYING, isPlaying);
        return intent;
    }

    /**
     * 初始化并绑定控件，将详情页部分递交对应的fragment
     * */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_2);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container_2);

        if(fragment == null){
            fragment = CreateFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container_2, fragment)
                    .commit();
        }

        mIsPlaying = true;
        if(getIntent() != null)
        {
            mSongUri = getIntent().getStringExtra(EXTRA_SONG_URI);
        }

        mPreviousButton = (ImageButton)findViewById(R.id.previous_song_button_2);
        mPreviousButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (!Check.isFastClick()) {
                    return;
                }
                MusicListActivity.getMi().PlayMusicByCommand(PRESONG,0);
            }
        });

        mPlayAndPauseButton = (ImageButton)findViewById(R.id.play_and_pause_song_button_2);
        mPlayAndPauseButton.setBackground(getResources().getDrawable(R.drawable.pause_song));
        mPlayAndPauseButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (!Check.isFastClick()) {
                    return;
                }
                if(mIsPlaying){
                    MusicListActivity.getMi().PlayMusicByCommand(SONGPAUSE,0);
                }else {
                    MusicListActivity.getMi().PlayMusicByCommand(SONGPLAY, 0);
                }
            }
        });

        mNextButton = (ImageButton)findViewById(R.id.next_song_button_2);
        mNextButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if (!Check.isFastClick()) {
                    return;
                }
                MusicListActivity.getMi().PlayMusicByCommand(NEXTSONG,0);
            }
        });

        mPlayerActivityReceiver = new PlayerActivityReceiver();
        register();
    }

    private void register(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("change.the.song");
        intentFilter.addAction("pause.the.song");
        registerReceiver(mPlayerActivityReceiver,intentFilter);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mPlayerActivityReceiver);
    }

    /**
     * 通过当前歌曲信息生成响应的详情页显示，供activity显示
     * */
    private Fragment CreateFragment(){
        mSongUri = getIntent().getStringExtra(EXTRA_SONG_URI);
        return MusicPlayerFragment.newInstance(mSongUri, mIsPlaying);
    }
    private void UpdateSong(){
        FragmentManager fm = getSupportFragmentManager();
        fragment= fm.findFragmentById(R.id.fragment_container_2);
        fragment = CreateFragment();
        fm.beginTransaction()
                .add(R.id.fragment_container_2, fragment)
                .commit();

        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container_2, fragment);
        transaction.commit();
    }
    public class  PlayerActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action != null){
                if(action.equals("change.the.song")){
                    UpdateSong();
                    mIsPlaying = true;
                    mPlayAndPauseButton.setBackground(getResources().getDrawable(R.drawable.pause_song));
                }else if(action.equals("pause.the.song")){
                    mIsPlaying = false;
                    mPlayAndPauseButton.setBackground(getResources().getDrawable(R.drawable.play_song));
                }
            }
        }
    }
}