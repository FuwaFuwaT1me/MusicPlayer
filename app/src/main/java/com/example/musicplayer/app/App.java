package com.example.musicplayer.app;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import androidx.room.Room;

import com.example.musicplayer.color.AppColor;
import com.example.musicplayer.loading.LoadingDialog;
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
            player = new Player();
        }
        return player;
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


    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(Activity currentActivity) {
        this.currentActivity = currentActivity;
    }
}
