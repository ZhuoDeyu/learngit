package com.startnet.android.musicplayer;

import android.database.Cursor;
import android.database.CursorWrapper;
/**
 * 从数据库读取信息并转化为歌曲的辅助类
 * */
public class SongCursorWrapper extends CursorWrapper {
    public SongCursorWrapper(Cursor cursor){
        super(cursor);
    }
    public Song getSong(){
        long id = getLong(getColumnIndex(SongDbSchema.SongTable.Cols.ID));
        String songName = getString(getColumnIndex(SongDbSchema.SongTable.Cols.SONGNAME));
        String artist = getString(getColumnIndex(SongDbSchema.SongTable.Cols.ARTIST));
        String album = getString(getColumnIndex(SongDbSchema.SongTable.Cols.ALBUM));
        int duration = getInt(getColumnIndex(SongDbSchema.SongTable.Cols.DURANTION));
        long size = getLong(getColumnIndex(SongDbSchema.SongTable.Cols.SIZE));
        String uri = getString(getColumnIndex(SongDbSchema.SongTable.Cols.URI));
        int recent = getInt(getColumnIndex(SongDbSchema.SongTable.Cols.RECENT));

        Song song = new Song();
        song.setId(id);
        song.setSongName(songName);
        song.setArtist(artist);
        song.setAlbum(album);
        song.setDuration(duration);
        song.setSize(size);
        song.setUri(uri);
        song.setRecent(recent);

        return song;
    }
}
