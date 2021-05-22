package com.example.musicplayer.app;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;

import androidx.room.Room;

import com.example.musicplayer.color.AppColor;
import com.example.musicplayer.loading.LoadingDialog;
import com.example.musicplayer.notification.CreateNotification;
import com.example.musicplayer.player.Player;
import com.example.musicplayer.database.AppDatabase;

public class App extends Application {
    private static App uniqueInstance;
    private Player player;
    private Intent playerService;
    private AppDatabase db;
    private LoadingDialog loading;
    private Activity currentActivity;
    private AppColor appColor;
    private boolean isMusicPlayerInit;
    private Activity lastActivity;

    public void setPlayerService(Intent playerService) {
        this.playerService = playerService;
    }

    public Intent getPlayerService() {
        return playerService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        uniqueInstance = this;
        appColor = new AppColor();
        appColor.setRed();
        isMusicPlayerInit = false;
    }

    public static App getApp() {
        return uniqueInstance;
    }

    public synchronized AppDatabase getDb() {
        if (db == null){
            db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return db;
    }

    public synchronized Player getPlayer(){
        if (player == null){
            Log.d("testing", "player created");
            player = new Player();
        }
        return player;
    }

    public void recreateNotification() {
        if (player.isPlaying()) {
            if (player.getSource().equals("."))
                createTrackNotification(appColor.getPauseColor());
            else createRadioNotification(appColor.getPauseColor());
        }
    }

    public void createTrackNotification(int index) {
        CreateNotification.createNotification(getApplicationContext(),
                index,
                player.getCurrentQueueTrack(),
                player.getQueueSize()-1);
    }

    public void createRadioNotification(int index) {
        CreateNotification.createNotification(getApplicationContext(),
                index,
                player.getCurrentRadio(),
                player.getRadioListSize() - 1);
    }

    public AppColor getAppColor() {
        return appColor;
    }

    //Loading circle
    public void createLoadingDialog(Activity activity) {
        loading = new LoadingDialog(activity);
    }

    public void dismissLoading() {
        this.loading.dismissDialog();
    }

    public LoadingDialog getLoadingDialog() {
        return loading;
    }

    public void nullLoading() {
        loading = null;
    }
    //Loading circle


    public boolean isMusicPlayerInit() {
        return isMusicPlayerInit;
    }

    public void setMusicPlayerInit(boolean musicPlayerInit) {
        isMusicPlayerInit = musicPlayerInit;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }

    public Activity getLastActivity() {
        return lastActivity;
    }

    public void setLastActivity(Activity lastActivity) {
        this.lastActivity = lastActivity;
    }
}
