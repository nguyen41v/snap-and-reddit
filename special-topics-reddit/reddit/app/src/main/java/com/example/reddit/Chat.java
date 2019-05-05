package com.example.reddit;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Chat extends Navigation {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        activity = "chat";
        makeMenu();
        setBotBarClickListeners();
    }
}
