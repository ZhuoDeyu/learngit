package com.startnet.android.musicplayer;
/**
 * 数据表的组成，表名及字段
 * */
public class SongDbSchema {
    public static final class SongTable {
        public static final String NAME = "songs";

        public static final class Cols {
            public static final String ID = "id";
            public static final String SONGNAME = "songname";
            public static final String ARTIST = "artist";
            public static final String ALBUM = "album";
            public static final String DURANTION = "durantion";
            public static final String SIZE = "size";
            public static final String URI = "songuri";
            public static final String ALBUMID = "albumid";
            public static final String RECENT = "recent";
        }
    }
}