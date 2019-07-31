package com.startnet.android.musicplayer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import java.util.List;
/**
 * 该类管理歌曲项的数据绑定、视图生成及响应点击事件
 * */
public class MusicListFragment extends Fragment{
    private static final String ARG_MUSIC_PLAYING = "music_is_playing";
    private RecyclerView mMusicRecyclerView;
    private MusicAdapter mMusicAdapter;
    /**
     * 新建歌曲项的方法,需要调用者通过argument传递歌曲播放信息
     */
    public static Fragment newInstanceState(boolean isPlaying){
        MusicListFragment musicListFragment = new MusicListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_MUSIC_PLAYING, isPlaying);
        musicListFragment.setArguments(args);
        return  musicListFragment;
    }

    /**
     * 绑定列表视图
     * */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_music_list, container, false);
        mMusicRecyclerView = (RecyclerView) view.findViewById(R.id.music_recycler_view);
        mMusicRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        updateUI();
        return view;
    }

    /**
     * 为RecyclerView绑定Adapter,以更新视图
     * */
    public void updateUI() {
        MusicBox musicBox = MusicBox.get(getActivity());
        List<Song> songs = musicBox.getSongs();
        if(mMusicAdapter == null){
            mMusicAdapter = new MusicAdapter(songs);
            mMusicRecyclerView.setAdapter(mMusicAdapter);
        }else{
            mMusicAdapter.setSongs(songs);
            mMusicAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 管理歌曲项的点击响应与属性设置
     * */
    private class MusicHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener{

        public TextView mTextView;
        private Song mSong;

        public MusicHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView;
            itemView.setOnClickListener(this);
        }
        //绑定数据对象，即该项对应的歌曲
        public void bindSong(Song song){
            mSong = song;
            mTextView.setText(mSong.getSongName());
        }
        @Override
        public void onClick(View view){
            //要播放的歌曲是否正在播放
            boolean theSameSong = MusicListActivity.getMi().PlayMusicByUri(mSong.getUri());

            //跟新主界面的显示
            //Activity activity = (Activity)getActivity();
            //((MusicListActivity)activity).UpdateCurrentSong();

            //是同一首则进入详情页
            if(theSameSong){
                Intent intent = MusicPlayerActivity.newIntent(getActivity(), mSong.getUri(), true);
                startActivity(intent);
            }
        }
    }

    /**
     * 产生并管理歌曲项
     * */
    private class MusicAdapter extends RecyclerView.Adapter<MusicHolder> {
        private List<Song> mSongs;

        public MusicAdapter(List<Song> songs) {
            mSongs = songs;
        }

        @NonNull
        @Override
        public MusicHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater
                    .inflate(android.R.layout.simple_list_item_1, viewGroup, false);
            return new MusicHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MusicHolder musicHolder, int i) {
            Song song = mSongs.get(i);
            musicHolder.bindSong(song);
        }

        @Override
        public int getItemCount() {
            return mSongs.size();
        }

        public void setSongs(List<Song> songs){
            mSongs = songs;
        }
    }

}
