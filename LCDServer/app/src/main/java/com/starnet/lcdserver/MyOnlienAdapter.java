package com.starnet.lcdserver;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

/**
 * 在想用户列表对应的Adapter
 * */
public class MyOnlienAdapter extends BaseAdapter {
    private final ArrayList<String> mOnlineUser;

    public MyOnlienAdapter(Map<String, String> users) {
        mOnlineUser = new ArrayList<>();

        for (String key : users.keySet()){
            mOnlineUser.add("用户名: " + users.get(key)+"IP: "+ key );
        }
    }

    public void setOnlineUsers(Map<String, String> users){
        mOnlineUser.clear();
        for (String key : users.keySet()){
            mOnlineUser.add("用户名: " + users.get(key)+"IP: "+ key );
        }
    }

    @Override
    public int getCount() {
        return mOnlineUser.size();
    }

    @Override
    public String getItem(int position) {
        return mOnlineUser.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO implement you own logic with ID
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View result;
        if (convertView == null) {
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.myonlienadapter_item, parent, false);
        } else {
            result = convertView;
        }

        final String item = getItem(position);

        // TODO replace findViewById by ViewHolder
        ((TextView) result.findViewById(R.id.online_user)).setText(item);

        return result;
    }
}
