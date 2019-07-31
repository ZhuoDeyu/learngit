package com.startnet.android.musicplayer;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.widget.ImageView;
/**
 * 该
 * */
public class MusicPlayerFragment extends Fragment {
    //对应于Service中的各类操作，用于确定操作类型
    private static final int NEXTSONG = 2;
    private static final int SEEKBAR = 4;
    private static final int SEEKCURRENT = 5;
    private static final int ISPLAYING = 6;
    private static final int SEEKLENGTH =7;
    private static final int SEEKSONGINDEX = 8;
    //标识需要的信息，当前歌曲uri和播放状态
    private static final String ARG_SONG_URI = "com.startnet.android.musicplayer.song_uri";
    private static final String ARG_MUSIC_PLAYING = "music_is_playing";
    private SeekBar mSeekBar;//进度条
    private TextView mSongLength;//歌曲时长
    private TextView mCurrentLength;//当前时长
    private Timer mTimer;//定时器
    private boolean isSeekBarChanging;//互斥变量，防止进度条与定时器冲突。
    private TextView mSongName;//歌曲名
    private String mSongUri;//当前歌曲uri
    private Song mSong;//当前歌曲
    private ImageView mSongImage;//专辑图片
    // 时间格式设定
    private SimpleDateFormat format = new SimpleDateFormat("mm:ss", Locale.CHINA);

    /**
     * 新建歌曲详情页的方法,需要调用者通过argument传递歌曲播放信息
     */
    public static MusicPlayerFragment newInstance(String songUri , boolean isPlaying){
        Bundle args = new Bundle();
        args.putSerializable(ARG_SONG_URI, songUri);
        args.putBoolean(ARG_MUSIC_PLAYING,isPlaying);
        MusicPlayerFragment musicPlayerFragment = new MusicPlayerFragment();
        musicPlayerFragment.setArguments(args);
        return musicPlayerFragment;
    }

    /**
     * 在退出当前页时，将定时器停止不在更新本页面
     * */
    @Override
    public void onDestroy(){
        if(mTimer != null){
            mTimer.cancel();
        }
        super.onDestroy();
    }
    /**
     * 绑定控件，返回视图供管理详情页显示的activity使用
     * */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        View view = inflater.inflate(R.layout.activity_song, container, false);
            if(getArguments() != null){
                mSongUri= (String)getArguments().getSerializable(ARG_SONG_URI);
                mSong = MusicBox.get(getContext()).getSong(mSongUri);
                mSongName = (TextView)view.findViewById(R.id.song_name);
                mSongLength = (TextView)view.findViewById(R.id.song_length);
                mCurrentLength = (TextView)view.findViewById(R.id.song_cur);
                mSongImage = (ImageView)view.findViewById(R.id.song_image);
            }

        mSong = MusicBox.get(getActivity())
                .getSongs()
                .get(MusicListActivity.getMi().PlayMusicByCommand(SEEKSONGINDEX, 0));
        String messageToShow = mSong.getSongName()
                + "\n歌手 " +mSong.getArtist()
                + "\n专辑 " + mSong.getAlbum();
        mSongName.setText(messageToShow);
        mSongLength.setText(format.format(mSong.getDuration()));
        mCurrentLength.setText(format.format(MusicListActivity.getMi().PlayMusicByCommand(SEEKCURRENT, 0)));
        getPicture(getContext(), mSong.getUri(), mSongImage);

        //进度条初始化
        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);
        mSeekBar.setOnSeekBarChangeListener(new SongSeekBar());

        mSeekBar.setMax(mSong.getDuration());
        if(mTimer != null){
            mTimer.cancel();
        }
            mTimer = new Timer();
        if(MusicListActivity.getMi().PlayMusicByCommand(ISPLAYING, 0) == 0 && getActivity() != null){
            mTimer.schedule(new TimerTask() {
                Runnable updateUI = new Runnable() {
                    @Override
                    public void run() {
                        if(Math.abs(MusicListActivity
                                .getMi()
                                .PlayMusicByCommand(SEEKCURRENT, 0) - mSong.getDuration()) <= 1000){
                            int songIndex = MusicListActivity.getMi().PlayMusicByCommand(NEXTSONG, 0);
                            mSong = MusicBox.get(getActivity()).getSongs().get(songIndex);
                            String messageToShow = mSong.getSongName()
                                    + "\n歌手 " +mSong.getArtist()
                                    + "\n专辑 " + mSong.getAlbum();
                            mSongName.setText(messageToShow);
                            mSongLength.setText(format.format(mSong.getDuration()));
                            getPicture(getContext(), mSong.getUri(), mSongImage);
                        }
                        mCurrentLength.setText(format.format(MusicListActivity.getMi().PlayMusicByCommand(SEEKCURRENT, 0)));
                    }
                };
                @Override
                public void run() {
                    if(!isSeekBarChanging){
                        mSeekBar.setProgress(MusicListActivity.getMi().PlayMusicByCommand(SEEKCURRENT, 0));
                            getActivity().runOnUiThread(updateUI);
                    }
                }
            },0,50);
        }
        return view;
    }
     /**
     * 进度条管理
     * */
    public class SongSeekBar implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            isSeekBarChanging = false;
            MusicListActivity.getMi().PlayMusicByCommand(SEEKBAR,seekBar.getProgress());
        }
    }

    /**
     * 获得专辑图片
     * @param context  场景
     * @param url 当前歌曲的url
     * @param ivPic 对应专辑显示位置的控件
     * */
    public void getPicture(Context context, String url, ImageView ivPic){
        Uri selectedAudio = Uri.parse(url);
        MediaMetadataRetriever myRetriever = new MediaMetadataRetriever();
        myRetriever.setDataSource(context, selectedAudio);
        byte[] artwork;

        artwork = myRetriever.getEmbeddedPicture();

        if (artwork != null) {
            Bitmap bMap = BitmapFactory.decodeByteArray(artwork, 0, artwork.length);
            ivPic.setImageBitmap(bMap);
        } else {
            ivPic.setImageResource(R.drawable.default_music);
        }
    }
}
