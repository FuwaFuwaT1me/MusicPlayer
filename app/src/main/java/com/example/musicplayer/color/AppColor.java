package com.example.musicplayer.color;

import android.content.Context;
import android.graphics.Color;

import com.example.musicplayer.R;

public class AppColor {
    private String color;
    private int colorCode;
    private int playColor;
    private int pauseColor;
    private int backColor;
    private int addColor;
    private int deleteColor;
    private int likedColor;
    private int unlikedColor;
    private int bgColor;

    public AppColor() {
        setRed();
    }

    public void setRed() {
        color = "#FF362E";
        colorCode = R.color.primeColor;
        playColor = R.drawable.ic_play_red;
        pauseColor = R.drawable.ic_pause_red;
        backColor = R.drawable.ic_back_red;
        addColor = R.drawable.ic_add_red;
        deleteColor = R.drawable.ic_delete_red;
        likedColor = R.drawable.ic_liked_red;
        unlikedColor = R.drawable.ic_unliked_red;
        bgColor = R.drawable.list_bg_red;
    }

    public void setGreen() {
        color = "#33cc33";
        colorCode = R.color.primeColorGreen;
        playColor = R.drawable.ic_play_green;
        pauseColor = R.drawable.ic_pause_green;
        backColor = R.drawable.ic_back_green;
        addColor = R.drawable.ic_add_green;
        deleteColor = R.drawable.ic_delete_green;
        likedColor = R.drawable.ic_liked_green;
        unlikedColor = R.drawable.ic_unliked_green;
        bgColor = R.drawable.list_bg_green;
    }

    public void setBlue() {
        color = "#00ffff";
        colorCode = R.color.primeColorBlue;
        playColor = R.drawable.ic_play_blue;
        pauseColor = R.drawable.ic_pause_blue;
        backColor = R.drawable.ic_back_blue;
        addColor = R.drawable.ic_add_blue;
        deleteColor = R.drawable.ic_delete_blue;
        likedColor = R.drawable.ic_liked_blue;
        unlikedColor = R.drawable.ic_unliked_blue;
        bgColor = R.drawable.list_bg_blue;
    }

    public String getColor() {
        return color;
    }

    public int getColorCode() {
        return colorCode;
    }

    public int getPlayColor() {
        return playColor;
    }

    public int getPauseColor() {
        return pauseColor;
    }

    public int getBackColor() {
        return backColor;
    }

    public int getAddColor() {
        return addColor;
    }

    public int getDeleteColor() {
        return deleteColor;
    }

    public int getLikedColor() {
        return likedColor;
    }

    public int getUnlikedColor() {
        return unlikedColor;
    }

    public int getBgColor() {
        return bgColor;
    }
}
