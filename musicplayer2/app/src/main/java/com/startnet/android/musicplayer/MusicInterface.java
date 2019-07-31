package com.startnet.android.musicplayer;
/**
 *将Service中的函数通过接口传递给别的类使用
 **/
public interface MusicInterface {
    int PlayMusicByCommand(int command, int position);
    boolean PlayMusicByUri(String songUri);
}
