package com.startnet.android.musicplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;


import java.io.IOException;
/**
 * 该类继承Service，在其中管理MediaPlayer的播放
 * 并提供对外的接口
 * */
public class MusicService extends Service {
    //对应于Service中的各类操作，用于确定操作类型
    private static final int SONGPLAY = 0;
    private static final int SONGPAUSE = 1;
    private static final int NEXTSONG = 2;
    private static final int PRESONG = 3;
    private static final int SEEKBAR = 4;
    private static final int SEEKCURRENT = 5;
    private static final int ISPLAYING = 6;
    private static final int SEEKLENGTH = 7;
    private static final int SEEKSONGINDEX = 8;
    private String mSongUri = "";//待播放歌曲uri
    private String mCurrentUri = "";//当前歌曲uri
    private MediaPlayer mPlayer;//MediaPlayer对象

    @Override
    public void onCreate(){
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        mPlayer.stop();
        mPlayer.release();

        super.onDestroy();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return new MusicController();
    }

    /**
     * 提供操作播放的接口
     * */
    public class MusicController extends Binder implements MusicInterface {
        public boolean PlayMusicByUri(String songUri) {
            mSongUri = songUri;
            return MusicService.this.PlayMusicByUri(mSongUri);
        }

        public int PlayMusicByCommand(int command, int position) {
            return MusicService.this.PlayMusicByCommand(command, position);
        }

    }


    /**
     * 通过歌曲uri播放歌曲
     * 供歌曲项点击时调用
     * 比较待播放歌曲与当前播放歌曲是否同一首，返回决定控制歌曲详情页的显示参数
     * */
    public boolean PlayMusicByUri(String songUri){

        //第一次播放，初始化播放对象
        if (null == mPlayer){
            mPlayer = new MediaPlayer();
        }
        //判断是否同一首歌
        if(mCurrentUri == null){
            mCurrentUri = "";
        }
        if(songUri == null){
            songUri = "";
        }
        if (mCurrentUri.equals(songUri) && !songUri.equals("") && mPlayer.isPlaying()){
            return true;// 继续播放且显示详情页
        }else if(songUri.equals("")){
            songUri = MusicBox.getNextSongUri(songUri);
        }

        if(!mCurrentUri.equals(songUri)) {
            mPlayer.reset();
            mCurrentUri = songUri;
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mPlayer.setDataSource(this, Uri.parse(mCurrentUri));
                mPlayer.prepare();
            } catch (IOException e) {
                Toast.makeText(this,"找不到歌曲，更新下曲库吧",Toast.LENGTH_SHORT)
                        .show();
                e.printStackTrace();
            }
        }
        //播放时记录时间
        MusicBox.get(getApplicationContext()).UpdateListenTime(mCurrentUri);
        mPlayer.start();
        Intent intent = new Intent();
        intent.setAction("change.the.song");
        sendBroadcast(intent);
        return false;
    }

    /**
     * 通过命令类型执行响应操作
     * 包括播放、暂停、切歌、进度条响应、当前播放歌曲的时长与序号
     * */
    public int PlayMusicByCommand(int command, int mCurrentPosition){
        switch(command){
            case SONGPLAY:
                PlayMusicByUri(mSongUri);
                int songIndex = MusicBox.getSongIndex(mSongUri);
                return songIndex;

            case SONGPAUSE:
                mPlayer.pause();
                Intent intent = new Intent();
                intent.setAction("pause.the.song");
                sendBroadcast(intent);
                break;

            case NEXTSONG:
                mSongUri = MusicBox.getNextSongUri(mCurrentUri);
                PlayMusicByUri(mSongUri);
                songIndex = MusicBox.getSongIndex(mSongUri);
                return songIndex;

            case PRESONG:
                mSongUri = MusicBox.getPreSongUri(mCurrentUri);
                PlayMusicByUri(mSongUri);
                songIndex = MusicBox.getSongIndex(mSongUri);
                return songIndex;

            case SEEKBAR:
                mPlayer.seekTo(mCurrentPosition);
                break;

            case SEEKCURRENT:
                return mPlayer.getCurrentPosition();

            case ISPLAYING:
                if(mPlayer == null){
                    mPlayer = new MediaPlayer();
                }
                return (mPlayer.isPlaying())? 0 : -1;

            case SEEKLENGTH:
                return mPlayer.getDuration();

            case SEEKSONGINDEX:
                return MusicBox.getSongIndex(mCurrentUri);
            default:
                break;
        }
        return 0;
    }
}