package com.startnet.android.musicplayer;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.SyncStateContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 该类控制播放、暂停、歌曲切换，管理歌曲列表的显示
 * */
public class MusicListActivity extends AppCompatActivity{
    //接口及对应于Service中的各类操作，用于确定操作类型
    public static MusicInterface mi;
    private static final int SONGPLAY = 0;
    private static final int SONGPAUSE = 1;
    private static final int NEXTSONG = 2;
    private static final int PRESONG = 3;
    private static final int ISPLAYING = 6;
    private static final int SEEKSONGINDEX = 8;
    private static final int REQUEST_CODE = 10;
    //需要请求的系统权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //显示当前歌曲名
    private TextView mCurrentSong;
    //上一首按钮
    private ImageButton mPreviousButton;
    //暂停、播放按钮
    private ImageButton mPlayAndPauseButton;
    //下一单按钮
    private ImageButton mNextButton;
    //服务连接
    private MusicServiceConn mMusicServiceConn;
    //歌曲显示列表的对象，用于更新列表视图
    private Fragment fragment;
    //是否正在播放音乐
    private boolean mIsPlaying = false;
    private ListActivityReceiver mListActivityReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState){
        CheckPermission();
        //新建并开启服务
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
        mMusicServiceConn = new MusicServiceConn();
        //绑定服务
        bindService(intent, mMusicServiceConn, BIND_AUTO_CREATE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        //为歌曲列表的fragment，绑定视图
        FragmentManager fm = getSupportFragmentManager();
         fragment= fm.findFragmentById(R.id.fragment_container);
        if(fragment == null){
            fragment = CreateFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
        mListActivityReceiver = new ListActivityReceiver();
        register();
    }

    private void register(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("change.the.song");
        intentFilter.addAction("pause.the.song");
        registerReceiver(mListActivityReceiver,intentFilter);
    }
    /**
     * 该类提供Service连接及接口初始化
     * */
public class MusicServiceConn implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //将接口相关操作放在该回调函数后，保证接口不为空
            // TODO Auto-generated method stub

            //初始化接口
            mi = (MusicInterface) service;

            mCurrentSong = (TextView)findViewById(R.id.current_song);

            mPreviousButton = (ImageButton)findViewById(R.id.previous_song_button);
            mPreviousButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){
                    if(MusicBox.get(getApplicationContext()).getSongs().size() == 0) {
                        Toast.makeText(getApplicationContext(),
                                "当前无歌曲，点击左上角菜单导入吧。", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    if (!Check.isFastClick()) {
                        return;
                    }
                    mi.PlayMusicByCommand(PRESONG, 0);
                }
            });

            mPlayAndPauseButton = (ImageButton)findViewById(R.id.play_and_pause_song_button);
            mPlayAndPauseButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){

                    if (!Check.isFastClick()) {
                        return;
                    }
                    if(MusicBox.get(getApplicationContext()).getSongs().size() == 0) {
                        Toast.makeText(getApplicationContext(), "当前无歌曲，点击左上角菜单从本地导入吧。", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    if(mIsPlaying){
                        mi.PlayMusicByCommand(SONGPAUSE, 0);
                    }else{
                        mi.PlayMusicByCommand(SONGPLAY, 0);
                    }
                }
            });

            mNextButton = (ImageButton)findViewById(R.id.next_song_button);
            mNextButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){

                    //列表为空，提醒导入
                    if(MusicBox.get(getApplicationContext()).getSongs().size() == 0) {
                        Toast.makeText(getApplicationContext(), "当前无歌曲，点击左上角菜单从本地导入吧。", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    if (!Check.isFastClick()) {
                        return;
                    }
                    mi.PlayMusicByCommand(NEXTSONG, 0);
                }
            });
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {

            // TODO Auto-generated method stub
        }
    }

    /**
     * 更新主界面中当前按钮与歌曲名显示状态
     * */
    public void UpdateCurrentSong(){
        //判断是否在播放歌曲，播放为0，未播放为-1
        if(mi != null) {

            if (mi.PlayMusicByCommand(ISPLAYING, 0) == 0) {
                mIsPlaying = true;
            } else {
                mIsPlaying = false;
            }
            if (mIsPlaying) {
                mCurrentSong.setText(MusicBox.get(getApplicationContext())
                        .getSongs()
                        .get(mi.PlayMusicByCommand(SEEKSONGINDEX, 0))
                        .getSongName());
                mPlayAndPauseButton.setBackground(getResources().getDrawable(R.drawable.pause_song));
            } else if (mi.PlayMusicByCommand(SEEKSONGINDEX, 0) != -1) {
                mCurrentSong.setText(MusicBox.get(getApplicationContext())
                        .getSongs()
                        .get(mi.PlayMusicByCommand(SEEKSONGINDEX, 0))
                        .getSongName());
                mPlayAndPauseButton.setBackground(getResources().getDrawable(R.drawable.play_song));
            }
        }
    }

    /**
     * 调用歌曲列表fragment的新建方法，获得相应对象
     * */
    private Fragment CreateFragment(){
        return MusicListFragment.newInstanceState(mIsPlaying);
    }

    /**
     * 判断安卓手机版本，动态请求权限
     * */
    private void CheckPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_CODE);
            }
        }else{
            return;
        }
    }

    /**
     * 结束时解绑服务
     * */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mMusicServiceConn);
        unregisterReceiver(mListActivityReceiver);
    }

    /**
     * 将对应的服务的接口对外公开，可以对同一个服务进行操作
     * */
    public static MusicInterface getMi() {
        return mi;
    }

    /**
     * 在从详情界面退回主界面时更新主界面视图
     * */
    @Override
    public void onRestart(){
        super.onRestart();
        UpdateCurrentSong();
    }

    /**
     * 增加菜单项
     * */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.title, menu);
        getMenuInflater().inflate(R.menu.recent,menu);
        return true;
    }
    /**
     * 通过菜单项ID进行响应"搜索"操作，读取SD卡中MP3文件
     * 并更新歌曲列表，以新布局替换原布局
     * */
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.scan_sd:
                GetSongsContent();
                return true;

            case R.id.scan_recent:
                if(item.getTitle().equals("最近播放")){
                    item.setTitle("总列表");
                    MusicBox.get(getApplicationContext()).getSongs(1);
                }else{
                    item.setTitle("最近播放");
                    MusicBox.get(getApplicationContext()).getSongs(0);
                }
                FragmentManager fm = getSupportFragmentManager();
                fragment= fm.findFragmentById(R.id.fragment_container);
                fragment = CreateFragment();
                fm.beginTransaction()
                        .add(R.id.fragment_container, fragment)
                        .commit();

                FragmentTransaction transaction = fm.beginTransaction();
                transaction.replace(R.id.fragment_container, fragment);
                transaction.replace(R.id.fragment_container, fragment);
                transaction.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * 读取SD卡上的MP3文件，并保存信息在歌曲列表中
     * 返回歌曲数目
     **/
    private void GetSongsContent(){
        //读取数据，将结果放在Cursor中，再比对文件信息，将MP3文件数据保留
        List<Song> songsToRemove = MusicBox.get(this).getSongs();
        List<Song> songsToAdd = new ArrayList<>();
         ContentResolver contentResolver = this.getContentResolver();
        Cursor cursor = contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,null,null,null,MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
        if(cursor != null && cursor.moveToFirst()){
            for(int i = 0; i <cursor.getCount();i++) {
                Song m = new Song();
                long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                String songName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                int duration = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
                long size = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                long album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                int ismusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));

                if (ismusic != 0 && duration / (500 * 60) >= 1) {
                    m.setId(id);
                    m.setSongName(songName);
                    m.setArtist(artist);
                    m.setDuration(duration);
                    m.setSize(size);
                    m.setUri(url);
                    m.setAlbum(album);
                    m.setAlbumId(album_id);
                    m.setRecent(0);
                }
                for(Song song: songsToRemove){
                    if(song.getUri().equals(m.getUri())){
                        songsToRemove.remove(song);
                        break;
                    }
                }
                boolean exited = false;
                for(Song song : MusicBox.get(this).getSongs()){
                    if(song.getUri().equals(m.getUri())){
                        exited = true;
                        break;
                    }
                }
                if(!exited){
                    songsToAdd.add(m);
                }
                cursor.moveToNext();
            }
        }
        for(Song song : songsToRemove){
            MusicBox.get(this).DeleteSong(song.getUri());
        }
        for(Song song : songsToAdd){
            MusicBox.get(this).addSong(song);
        }

        FragmentManager fm = getSupportFragmentManager();
        fragment= fm.findFragmentById(R.id.fragment_container);
        fragment = CreateFragment();
        fm.beginTransaction()
                .add(R.id.fragment_container, fragment)
                .commit();

        FragmentTransaction transaction = fm.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
    private class ListActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
                if( action != null) {
                    if (action.equals("change.the.song")) {
                        UpdateCurrentSong();
                    }else if(action.equals("pause.the.song")){
                        UpdateCurrentSong();
                    }
                }
        }
    }
}
