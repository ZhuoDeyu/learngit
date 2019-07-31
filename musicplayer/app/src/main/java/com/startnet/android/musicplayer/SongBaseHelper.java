package com.startnet.android.musicplayer;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * 数据库生成
 * */
public class SongBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "ListenHistoryBase.db";

    public SongBaseHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table " + SongDbSchema.SongTable.NAME  + "(" +
                SongDbSchema.SongTable.Cols.ID + "," +
                SongDbSchema.SongTable.Cols.SONGNAME + "," +
                SongDbSchema.SongTable.Cols.ARTIST + "," +
                SongDbSchema.SongTable.Cols.ALBUM + "," +
                SongDbSchema.SongTable.Cols.DURANTION + "," +
                SongDbSchema.SongTable.Cols.SIZE + "," +
                SongDbSchema.SongTable.Cols.URI + " UNIQUE," +
                SongDbSchema.SongTable.Cols.ALBUMID + ","+
                SongDbSchema.SongTable.Cols.RECENT +
         ")"
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }

}
