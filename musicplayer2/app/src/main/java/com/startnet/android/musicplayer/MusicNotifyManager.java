package com.startnet.android.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.widget.RemoteViews;

public class MusicNotifyManager {
    private static final int NEXT_SONG = 1;
    private static final int PRE_SONG = 2;
    private static final int PLAY_PAUSE_SONG = 3;

    private static final int ISPLAYING = 6;
    private static final int SEEKSONGINDEX = 8;
    private NotificationManager mNotificationManager;
    private Context mContext;
    private RemoteViews mNotificationView;
    private RemoteViews mBigNotificationView;
    private Notification mNotification;
    private NotifyReceiver mNotifyReceiver;

    public MusicNotifyManager(Context context){
        mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        mContext = context;
        prepare();
        mNotifyReceiver = new NotifyReceiver();
        register();
    }

    private void register(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("change.the.song");
        intentFilter.addAction("pause.the.song");
        mContext.registerReceiver(mNotifyReceiver,intentFilter);
    }

    private void prepare(){

        Notification.Builder builder = new Notification.Builder(mContext);
        mNotificationView = new RemoteViews(mContext.getPackageName(),
                R.layout.notify_song);
        mBigNotificationView = new RemoteViews(mContext.getPackageName(),
                R.layout.big_notify_song);


        mNotificationView.setOnClickPendingIntent(R.id.notify_song_pre,getPendingIntent(PRE_SONG,"pre"));
        mNotificationView.setOnClickPendingIntent(R.id.notify_song_play_pause,getPendingIntent(PLAY_PAUSE_SONG,"play_pause"));
        mNotificationView.setOnClickPendingIntent(R.id.notify_song_next,getPendingIntent(NEXT_SONG,"next"));
        mBigNotificationView.setOnClickPendingIntent(R.id.notify_song_pre,getPendingIntent(PRE_SONG,"pre"));
        mBigNotificationView.setOnClickPendingIntent(R.id.notify_song_play_pause,getPendingIntent(PLAY_PAUSE_SONG,"play_pause"));
        mBigNotificationView.setOnClickPendingIntent(R.id.notify_song_next,getPendingIntent(NEXT_SONG,"next"));

        mNotification = builder.build();
        mNotification.contentView = mNotificationView;
        mNotification.bigContentView = mBigNotificationView;
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 99,new Intent(mContext,MusicListActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mNotification.contentIntent = pendingIntent;
        mNotification.icon = R.drawable.default_music;

        // 发送通知，并设置id
        mNotificationManager.notify(9, mNotification);
    }

    private PendingIntent getPendingIntent(int requestId, String action){
        Intent intent = new Intent();
        intent.setPackage(mContext.getPackageName());
        intent.setAction(action);
        return PendingIntent.getBroadcast(mContext,requestId,intent,PendingIntent.FLAG_CANCEL_CURRENT);
    }

    private class NotifyReceiver extends BroadcastReceiver {
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


    private void UpdateCurrentSong(){

        Notification.Builder builder = new Notification.Builder(mContext);
        Song song = MusicBox.get(mContext)
                .getSongs()
                .get(MusicListActivity.getMi().PlayMusicByCommand(SEEKSONGINDEX,0));

        mNotificationView = new RemoteViews(mContext.getPackageName(),
                R.layout.notify_song);
        mBigNotificationView = new RemoteViews(mContext.getPackageName(),
                R.layout.big_notify_song);

        mNotificationView.setTextViewText(R.id.notify_song_name,song.getSongName());
        mNotificationView.setTextViewText(R.id.notify_song_artist,song.getArtist());
        Uri selectedAudio = Uri.parse(song.getUri());
        MediaMetadataRetriever myRetriever = new MediaMetadataRetriever();
        myRetriever.setDataSource(mContext, selectedAudio);
        byte[] artwork;
        artwork = myRetriever.getEmbeddedPicture();
        if(artwork != null){
            Bitmap bMap = BitmapFactory.decodeByteArray(artwork, 0, artwork.length);
            mNotificationView.setImageViewBitmap(R.id.notify_song_image, bMap);
        }else{
            mNotificationView.setImageViewResource(R.id.notify_song_image,R.drawable.default_music);
        }

        mBigNotificationView.setTextViewText(R.id.notify_song_name,song.getSongName());
        mBigNotificationView.setTextViewText(R.id.notify_song_artist,song.getArtist());
        selectedAudio = Uri.parse(song.getUri());
        myRetriever = new MediaMetadataRetriever();
        myRetriever.setDataSource(mContext, selectedAudio);
        artwork = null;
        artwork = myRetriever.getEmbeddedPicture();
        if(artwork != null){
            Bitmap bMap = BitmapFactory.decodeByteArray(artwork, 0, artwork.length);
            mBigNotificationView.setImageViewBitmap(R.id.notify_song_image, bMap);
        }else{
            mBigNotificationView.setImageViewResource(R.id.notify_song_image,R.drawable.default_music);
        }

        if(MusicListActivity.getMi().PlayMusicByCommand(ISPLAYING,0) == -1){
            mNotificationView.setImageViewResource(R.id.notify_song_play_pause,R.drawable.play_song);
            mBigNotificationView.setImageViewResource(R.id.notify_song_play_pause,R.drawable.play_song);
        }else{
            mNotificationView.setImageViewResource(R.id.notify_song_play_pause,R.drawable.pause_song);
            mBigNotificationView.setImageViewResource(R.id.notify_song_play_pause,R.drawable.pause_song);
        }
        mNotification = builder.build();
        mNotification.contentView = mNotificationView;
        mNotification.bigContentView = mBigNotificationView;
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 99, new Intent(mContext,MusicListActivity.class),PendingIntent.FLAG_UPDATE_CURRENT);
        mNotification.flags = Notification.FLAG_ONGOING_EVENT;
        mNotification.contentIntent = pendingIntent;
        mNotification.icon = R.drawable.default_music;
// 发送通知，并设置id
        mNotificationManager.notify(9, mNotification);
    }

    public void cancelAll(){
        mNotificationManager.cancelAll();
    }
}
