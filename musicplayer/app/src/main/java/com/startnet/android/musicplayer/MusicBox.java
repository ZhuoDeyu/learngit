package com.startnet.android.musicplayer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 该类为一个单例类，用以保存所有的歌曲信息
 **/

public class MusicBox {
    //单例对象
    private static MusicBox sMusicBox;
    //保留上下文供调用搜索函数时使用
    private static Context sContext;
    private static int sMode = 0;
    //数据库对象
    private  SQLiteDatabase mDatabase;

    /**
     * 通过上下文返回MusicBox 单例对象
     * **/
    public static MusicBox get(Context context){
        if(sMusicBox == null){
            sMusicBox = new MusicBox(context);
        }
        return sMusicBox;
    }

    /**
     * 定义歌曲列表
     **/
    private MusicBox(Context context){
        sContext = context.getApplicationContext();
        mDatabase = new SongBaseHelper(sContext)
                .getWritableDatabase();
    }

    /**
     * 通过对应模式即所有歌曲还是最近3天的播放列表，获得当前歌曲列表
     **/
    public List<Song> getSongs(int mode) {
        sMode = mode;
        List<Song> songs =  new ArrayList<>();
        long threeDays = 3*24*60*60*1000;
        SongCursorWrapper cursor;
        if(mode == 0){
            cursor = querySongs(null, null);
        } else{
            Date date = new Date();
            int time = 0;
            int year = date.getYear()/100;
            int month = date.getMonth();
            time = year*100 + month;
            int day = date.getDay();
            time = time*100 +day;
            cursor = querySongs(
                     "? - recent <= 3",
                    new String[]{String.valueOf(time)});
        }
        try {
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                songs.add(cursor.getSong());
                cursor.moveToNext();
            }
        }finally {
            cursor.close();
        }
        return songs;
    }

    public List<Song> getSongs() {
        return getSongs(sMode);
    }

    /**
     * 通过歌曲的uri得到列表中对应的歌曲对象
     *
     * @param songUir 歌曲的uri
     * @return song   对应歌曲
     * **/
    public Song getSong(String songUir){
       SongCursorWrapper cursor = querySongs(
               SongDbSchema.SongTable.Cols.URI + "=?",
               new String[]{songUir});
       try{
           if(cursor.getCount() == 0){
               return null;
           }
           cursor.moveToFirst();
           return cursor.getSong();
       }finally {
           cursor.close();
       }
    }

    /**
     * 向数据库添加歌曲
     * */
    public void addSong(Song song){
        ContentValues values = getContentValues(song);
        mDatabase.insert(SongDbSchema.SongTable.NAME, null, values);
    }

    /**
     * 清空数据库
     * */
    public void Clear(){
        mDatabase.execSQL("delete from " + SongDbSchema.SongTable.NAME );
    }

    /**
     * 提取歌曲对象信息的辅助函数
     * */
    public static ContentValues getContentValues(Song song){
        ContentValues values = new ContentValues();
        values.put(SongDbSchema.SongTable.Cols.ID, song.getId());
        values.put(SongDbSchema.SongTable.Cols.SONGNAME, song.getSongName());
        values.put(SongDbSchema.SongTable.Cols.ARTIST, song.getArtist());
        values.put(SongDbSchema.SongTable.Cols.ALBUM, song.getAlbum());
        values.put(SongDbSchema.SongTable.Cols.DURANTION, song.getDuration());
        values.put(SongDbSchema.SongTable.Cols.SIZE, song.getSize());
        values.put(SongDbSchema.SongTable.Cols.URI, song.getUri());
        values.put(SongDbSchema.SongTable.Cols.ALBUMID, song.getAlbumId());
        values.put(SongDbSchema.SongTable.Cols.RECENT, song.getRecent());

        return values;
    }

    /**
     * 添加历史记录时将用到的函数，用于保存听的时刻
     */
    public void UpdateListenTime(String songUri){
        Song song = getSong(songUri);
        ContentValues values = getContentValues(song);
        Date date = new Date();
        //保存播放的年月日，如2019年5月27号为 190527
        int time = 0;
        int year = date.getYear()/100;
        int month = date.getMonth();
        time = year*100 + month;
        int day = date.getDay();
        time = time*100 +day;

        values.put(SongDbSchema.SongTable.Cols.RECENT, time);
        mDatabase.update(SongDbSchema.SongTable.NAME, values,
                SongDbSchema.SongTable.Cols.URI + "=?",
                new String[]{songUri});
    }

    public void DeleteSong(String songUri){
        mDatabase.delete(SongDbSchema.SongTable.NAME,
                SongDbSchema.SongTable.Cols.URI + "= ?",
                new String[]{songUri});
    }
    /**
     * 查询数据库中的歌曲
     * */
    private SongCursorWrapper querySongs(String whereClause, String[] whereArgs){
        Cursor cursor = mDatabase.query(
                SongDbSchema.SongTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new SongCursorWrapper(cursor);
    }

    /**
     * 通过歌曲的uri得到列表中对应的序号
     * 如果没有对应歌曲则返回 -1
     * @param songUir 歌曲的uri
     * @return i      对应序号
     **/
    public static int getSongIndex(String songUir){
        List<Song> songs = MusicBox.get(sContext).getSongs(sMode);
        Song  song ;
        for(int i = 0; i<songs.size() ; i++){
            song = songs.get(i);
            if(song.getUri().equals(songUir)){
                return i;
            }
        }
        return -1;
    }

    /**
     * 通过当前歌曲的uri得到列表中前一首歌曲
     * 如果列表为空，则返回 null
     * @param songUri 当前歌曲的uri
     * @return song.getUri()  前一首的uri
     **/
    public  static String getPreSongUri(String songUri){
        List<Song> songs = MusicBox.get(sContext).getSongs(sMode);
        //当前列表不为空时进行查找
        if(songs.size() > 0){
            Song  song = songs.get(0);
            //第一次播放时，返回第一首
            if(songUri.equals("")){

                return song.getUri();
            }
            for(int i = 0; i<songs.size();i++) {
                //不是当前歌曲则保存在song中
                if (!song.getUri().equals(songUri)) {
                    song = songs.get(i);
                }else if(i == 0){
                    //当前为第一首，返回最后一首
                    song = songs.get(songs.size()-1);
                    return song.getUri();
                }
                //匹配到当前歌曲，返回前一首
                if(song.getUri() == songUri){
                    song = songs.get(i-1);
                    return song.getUri();
                }
            }
        }
        return null;
    }

    /**
     * 通过当前歌曲的uri得到列表中下一首歌曲
     * 如果列表为空，则返回 null
     * @param songUri 当前歌曲的uri
     * @return song.getUri()  下一首的uri
     **/
    public static String getNextSongUri(String songUri){
        List<Song> songs = MusicBox.get(sContext).getSongs(sMode);
        //当前列表不为空时进行查找
        if(songs.size() > 0){
            Song  song = songs.get(0);
            //第一次播放时，返回第一首
            if(songUri.equals("")){
                return song.getUri();
            }
            int index;
            for(index = 0; index<songs.size();index++) {
                if(songUri.equals(songs.get(index).getUri())){
                    break;
                }
            }
            //如果是最后一首则返回第一首，否则返回下一首
            if(index == songs.size()-1){
                song = songs.get(0);
            }else{
                song = songs.get(index+1);
            }
            return song.getUri();
        }
        return null;
    }
}
