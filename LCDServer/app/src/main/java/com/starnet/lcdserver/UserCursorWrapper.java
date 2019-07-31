package com.starnet.lcdserver;

import android.database.Cursor;
import android.database.CursorWrapper;

/**
 * 从数据库读取信息并转化为用户列表
 * */
public class UserCursorWrapper extends CursorWrapper {
    public UserCursorWrapper(Cursor cursor){
        super(cursor);
    }
    public User getUser(){
        String userName = getString(getColumnIndex(UserDbSchema.UserTable.Cols.USERNAME));
        String password = getString(getColumnIndex(UserDbSchema.UserTable.Cols.PASSWORD));

        User user = new User(userName,password);

        return user;
    }
}
