package com.starnet.lcdserver;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;
/**
 * 用户列表对应的Adapter
 * */
public class MyAdapter extends BaseAdapter {
    private final ArrayList<String> mUserNames;
    private managerOnlineClient mManagerOnlineClient;

    public MyAdapter(Map<String, String> users, managerOnlineClient managerOnlineClient) {
        mUserNames = new ArrayList<>();
        mUserNames.addAll(users.keySet());
        mManagerOnlineClient = managerOnlineClient;
    }

    public void setUserNames(Map<String, String> users){
        mUserNames.clear();
        mUserNames.addAll(users.keySet());
    }
    @Override
    public int getCount() {
        return mUserNames.size();
    }

    @Override
    public String getItem(int position) {
        return mUserNames.get(position);
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
            result = LayoutInflater.from(parent.getContext()).inflate(R.layout.myadapter_item, parent, false);
        } else {
            result = convertView;
        }

        final String item = getItem(position);

        // TODO replace findViewById by ViewHolder
        ((TextView) result.findViewById(R.id.text_1)).setText(item);
        ((TextView) result.findViewById(R.id.delete_user)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mManagerOnlineClient.deleteSavedUser(item);
                mUserNames.remove(item);
                notifyDataSetChanged();
                Log.i("result","删除" + item);
            }
        });

        ((TextView) result.findViewById(R.id.modify_user)).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mManagerOnlineClient.changePasswordInList(item);
            }
        });

        return result;
    }
}
