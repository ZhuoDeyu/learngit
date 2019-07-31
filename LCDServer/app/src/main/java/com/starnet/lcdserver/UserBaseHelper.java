package com.starnet.lcdserver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库生成
 * */
public class UserBaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "UserDataBase.db";

    public UserBaseHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("create table " + UserDbSchema.UserTable.NAME  + "(" +
                UserDbSchema.UserTable.Cols.USERNAME + " UNIQUE," +
                UserDbSchema.UserTable.Cols.PASSWORD  + ")"
        );
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

    }

}
