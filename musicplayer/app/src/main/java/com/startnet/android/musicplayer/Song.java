package com.startnet.android.musicplayer;

import java.util.Date;

/**
 * 歌曲类
 * 存放歌曲的各类信息
 * */
public class Song {
    private long mId;//歌曲id
    private String mSongName;//歌曲名
    private String mArtist;//歌手
    private String mAlbum;//专辑名
    private int mDuration;//长度
    private long mSize;//大小
    private String mUri;//歌曲uri
    private int mRecent;//最近播放时间

    public long getRecent() {
        return mRecent;
    }

    public void setRecent(int recent) {
        mRecent = recent;
    }

    public long getAlbumId() {
        return mAlbumId;
    }

    private long mAlbumId;//专辑号

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        mId = id;
    }

    public String getSongName() {
        return mSongName;
    }

    public void setSongName(String songName) {
        mSongName = songName;
    }


    public String getArtist() {
        return mArtist;
    }

    public void setArtist(String artist) {
        mArtist = artist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public void setAlbum(String album) {
        mAlbum = album;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public long getSize() {
        return mSize;
    }

    public void setSize(long size) {
        mSize = size;
    }

    public String getUri() {
        return mUri;
    }

    public void setUri(String uri) {
        mUri = uri;
    }

    public void setAlbumId(long albumId) {
        mAlbumId = albumId;
    }

}
